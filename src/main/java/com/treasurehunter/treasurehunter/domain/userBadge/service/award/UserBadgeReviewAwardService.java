package com.treasurehunter.treasurehunter.domain.userBadge.service.award;

import com.treasurehunter.treasurehunter.domain.admin.badge.entity.BadgeName;
import com.treasurehunter.treasurehunter.domain.review.repository.ReviewRepository;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.event.model.ReviewCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 후기 관련 배지 수여하는 메서드
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserBadgeReviewAwardService {

    private final UserBadgeGrantService userBadgeGrantService;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 배지 수여 가능한지 확인하고 수여하는 메서드
     * 문제 발생하면 에러 로그만 찍음
     * @param reviewCreateEvent 배지 수여할 때 필요한 정보 담은 메서드
     */
    // 리스너가 트랜잭션 끝난 후에 이 메서드를 호출 하기 때문에 새 트랜잭션 시작을 강제해야함
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void evaluateAndAwardForReviewCreateEvent(
            final ReviewCreateEvent reviewCreateEvent
    ){
        // 1) 변수 지정
        final LocalDateTime earnedDate = reviewCreateEvent.occurredAt();
        final Long reviewId = reviewCreateEvent.getReviewId();
        final Long userId = reviewCreateEvent.getUserId();

        // 2) 유저 조회
        final User user = userRepository.findById(userId).orElse(null);

        // 3) 유저 없으면 에러 로그 찍고 종료
        if(user == null){
            log.error("[EVENT ERROR] USER NOT FOUND: user id: {}",  reviewCreateEvent.getUserId());
            return;
        }

        // 4) 유저가 작성한 후기 개수 불러오기
        final Long reviewCount = reviewRepository.countByAuthorId(userId);

        // 5) 뱃지 수여 가능한지 확인 및 수여
        //첫 후기 작성 배지
        if(reviewCount >= 1){
            userBadgeGrantService.awardIfNotExists(user, BadgeName.FIRST_REVIEW, earnedDate);
        }

        //후기 10개 작성 배지
        if(reviewCount >= 10){
            userBadgeGrantService.awardIfNotExists(user, BadgeName.TEN_REVIEW, earnedDate);
        }

    }
}
