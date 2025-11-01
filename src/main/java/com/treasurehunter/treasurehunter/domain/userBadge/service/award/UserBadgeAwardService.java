package com.treasurehunter.treasurehunter.domain.userBadge.service.award;

import com.treasurehunter.treasurehunter.domain.admin.badge.domain.Badge;
import com.treasurehunter.treasurehunter.domain.admin.badge.repository.BadgeRepository;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import com.treasurehunter.treasurehunter.domain.user.domain.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.domain.userBadge.domain.UserBadge;
import com.treasurehunter.treasurehunter.domain.userBadge.repository.UserBadgeRepository;
import com.treasurehunter.treasurehunter.global.event.model.PostCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 배지를 수여하는 메서드
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserBadgeAwardService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    /**
     * 게시글 등록시에 수여 받을 배지가 있는지 검사하는 메서드
     * 예외가 발생한다면 에러 로그만 찍힌다.
     * @param postCreateEvent 이벤트에 대한 정보가 담긴 메서드
     */
    // 리스너가 트랜잭션 끝난 후에 이 메서드를 호출 하기 때문에 새 트랜잭션 시작을 강제해야함
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void evaluateAndAwardForPostCreateEvent(
            final PostCreateEvent postCreateEvent
    ){
        // 1) 변수로 지정
        final LocalDateTime earnedDate = postCreateEvent.getOccurredAt();
        final Long postId = postCreateEvent.getPostId();
        final Long userId = postCreateEvent.getUserId();

        // 2) 유저 존재 확인 및 유저 정보 불러오기
        final User user = userRepository.findById(postCreateEvent.getUserId()).orElse(null);

        // 3) 유저 없다면 에러 로그 찍고 종료
        if(user == null){
            log.error("[EVENT ERROR] USER NOT FOUND: user id: {}",  postCreateEvent.getUserId());
            return;
        }

        // 4) 유저가 작성한 개시글 개수 불러오기
        final Long postCount = postRepository.countByAuthorId(userId);

        // 5) 배지 수여 가능한지 확인 및 수여
        //첫 게시글
        if(postCount > 0) {
            awardIfNotExists(user, "FIRST_POST", earnedDate);
        }
        //게시글 10개 작성
        if(postCount > 9){
            awardIfNotExists(user, "TEN_POST", earnedDate);
        }

    }

    /**
     * 배지를 수여할 수 있는지 검사하고 자격이 충분하면
     * 배지를 수여하는 메서드
     * 자격이 충분하지 않거나 배지가 없다면 에러 대신 로그만 찍힌다.
     * @param user 배지 수여 받을 유저 엔티티
     * @param badgeName 수여할 배지 이름
     * @param earnedDate 배지 얻은 LocalDateTime
     */
    private void awardIfNotExists(final User user, final String badgeName, final LocalDateTime earnedDate){
        // 1) 배지 조회
        final Badge badge = badgeRepository.findByName(badgeName);

        // 2) 배지가 존재하지 않으면 로그 찍고 종료
        if(badge == null){
            log.error("[EVENT ERROR] BADGE NOT FOUND: badge name: {}", badgeName);
            return;
        }

        // 3) 배지 이미 가지고 있는지 검사 -> 이미 가졌다면 종료
        if(userBadgeRepository.existsByBadgeIdAndUserId(badge.getId(), user.getId())){
            return;
        }

        // 4) 배지 저장
        userBadgeRepository.save(
                UserBadge.builder()
                        .earnedDate(earnedDate)
                        .user(user)
                        .badge(badge)
                        .build()
        );
    }
}
