package com.treasurehunter.treasurehunter.domain.userBadge.service.award;

import com.treasurehunter.treasurehunter.domain.admin.badge.entity.BadgeName;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.event.model.PostCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 게시글 관련 배지를 수여하는 메서드
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserBadgePostAwardService {

    private final UserBadgeGrantService userBadgeGrantService;
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
            userBadgeGrantService.awardIfNotExists(user, BadgeName.FIRST_POST, earnedDate);
        }
        //게시글 10개 작성
        if(postCount > 9){
            userBadgeGrantService.awardIfNotExists(user, BadgeName.TEN_POST, earnedDate);
        }

    }
}
