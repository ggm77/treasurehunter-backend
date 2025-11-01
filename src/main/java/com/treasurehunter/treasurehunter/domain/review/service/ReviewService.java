package com.treasurehunter.treasurehunter.domain.review.service;

import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import com.treasurehunter.treasurehunter.domain.review.entity.Review;
import com.treasurehunter.treasurehunter.domain.review.entity.image.ReviewImage;
import com.treasurehunter.treasurehunter.domain.review.dto.ReviewRequestDto;
import com.treasurehunter.treasurehunter.domain.review.dto.ReviewResponseDto;
import com.treasurehunter.treasurehunter.domain.review.repository.ReviewRepository;
import com.treasurehunter.treasurehunter.domain.review.repository.image.ReviewImageRepository;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.event.domain.EventPublisher;
import com.treasurehunter.treasurehunter.global.event.model.ReviewCreateEvent;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final EventPublisher eventPublisher;

    //entityManager.flush()를 사용하기 위함
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 후기 등록하는 메서드
     * 게시글 등록하는 메서드와 거의 동일하게 흘러감
     * @param reviewRequestDto 후기 등록 요청 DTO
     * @param userId 후기를 작성한 유저 ID
     * @return 등록된 후기 DTO
     */
    @Transactional
    public ReviewResponseDto createReview(
            final ReviewRequestDto reviewRequestDto,
            final Long userId
    ){
        // 0) 요청값 검증은 DTO에서 어노테이션으로함

        // 1) 게시글 존재 확인
        final Post post = postRepository.findById(reviewRequestDto.getPostId())
                .orElseThrow(() -> new CustomException(ExceptionCode.POST_NOT_EXIST));

        // 2) 게시글이 완료 상태가 아닌경우 처리
        if(!post.isCompleted()){
            throw new CustomException(ExceptionCode.POST_NOT_COMPLETED);
        }

        // 3) 게시글에 리뷰가 이미 존재하면 실패 처리
        if(post.getReview() != null){
            throw new CustomException(ExceptionCode.REVIEW_ALREADY_EXIST);
        }

        // 4) 유저 존재 확인
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 5) 후기 작성에 따른 유저 정보 갱신
        //리뷰 당할 유저 조회
        final User targetUser = post.getAuthor();
        //불필요한 갱신 제외
        if(reviewRequestDto.getScore() != 0){
            // 총 점수 증가
            targetUser.increaseTotalScore(reviewRequestDto.getScore());
        }
        // 총 리뷰 수 1 증가
        targetUser.incrementTotalReviews();

        // 6) review 엔티티 생성
        final Review review = Review.builder()
                .title(reviewRequestDto.getTitle())
                .content(reviewRequestDto.getContent())
                .score(reviewRequestDto.getScore())
                .author(user)
                .post(post)
                .targetUser(targetUser)
                .build();

        // 7) review DB에 저장
        final Review savedReview = reviewRepository.save(review);

        // 8) ReviewImage 연관 관계 설정 및 DB에 저장
        //리스트가 null인 경우, 요소가 null인경우, 빈경우 예외처리
        final List<String> validUrls = Optional.ofNullable(reviewRequestDto.getImages())
                .orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        //null제거후 빈 리스트 대응
        if(!validUrls.isEmpty()) {
            //ReviewImage 리스트 생성
            final List<ReviewImage> reviewImages =  new ArrayList<>(validUrls.size());
            for(int i = 0; i < validUrls.size(); i++) {
                final ReviewImage img = ReviewImage.builder()
                        .url(validUrls.get(i))
                        .imageIndex(i)
                        .build();
                img.updateReview(savedReview);
                reviewImages.add(img);
            }

            reviewImageRepository.saveAll(reviewImages);
        }

        // 8) 이미지 정보가 최신 정보로 표시 되지 않는 문제 해결
        final Long savedReviewId = savedReview.getId();

        // 10) 배지 수여용 이벤트 발생 시키기
        eventPublisher.publish(
                ReviewCreateEvent.builder()
                        .userId(userId)
                        .reviewId(savedReviewId)
                        .build()
        );

        // 11) 변경사항 적용
        entityManager.flush();
        entityManager.clear();

        // 12) 최신 정보로 다시 가져오기
        final Review updatedReview = reviewRepository.findById(savedReviewId)
                .orElseThrow(() -> new CustomException(ExceptionCode.REVIEW_NOT_EXIST));

        return new ReviewResponseDto(updatedReview);
    }

    /**
     * 특정 후기를 조회하는 메서드
     * @param reviewId 조회할 후기 ID
     * @return 후기 DTO
     */
    public ReviewResponseDto getReview(final Long reviewId){

        final Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ExceptionCode.REVIEW_NOT_EXIST));

        return new ReviewResponseDto(review);
    }

    /**
     * 후기 수정하는 메서드
     * 게시글 수정과 흐름이 같다
     * @param reviewRequestDto 후기 수정 요청 DTO
     * @param reviewId 후기 ID
     * @param userId 후기 작성자 ID
     * @return 수정된 후기 DTO
     */
    @Transactional
    public ReviewResponseDto updateReview(
            final ReviewRequestDto reviewRequestDto,
            final Long reviewId,
            final Long userId
    ){

        // 1) 후기 조회
        final Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ExceptionCode.REVIEW_NOT_EXIST));

        // 2) 자신의 후기인지 확인
        if(!userId.equals(review.getAuthor().getId())){
            throw new CustomException(ExceptionCode.PERMISSION_DENIED);
        }

        // 3) 비어있지 않으면 변경
        //이미지 수정
        //null이면 무시, []이면 사진 삭제, 요소 존재하면 변경
        if(reviewRequestDto.getImages() != null){
            //원래 있던 이미지 삭제로 연관 관계 초기화
            reviewImageRepository.deleteByReviewId(reviewId);

            // 이미지 새로 저장
            //리스트가 null인 경우, 요소가 null인경우, 빈경우 예외처리
            final List<String> validUrls = Optional.ofNullable(reviewRequestDto.getImages())
                    .orElseGet(List::of).stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();

            //null제거후 빈 리스트 대응
            if(!validUrls.isEmpty()) {
                //ReviewImage 리스트 생성
                final List<ReviewImage> reviewImages =  new ArrayList<>(validUrls.size());
                for(int i = 0; i < validUrls.size(); i++) {
                    final ReviewImage img = ReviewImage.builder()
                            .url(validUrls.get(i))
                            .imageIndex(i)
                            .build();
                    img.updateReview(review);
                    reviewImages.add(img);
                }

                reviewImageRepository.saveAll(reviewImages);
            }
        }

        Optional.ofNullable(reviewRequestDto.getTitle()).filter(s -> !s.isBlank()).ifPresent(review::updateTitle);
        Optional.ofNullable(reviewRequestDto.getContent()).filter(s -> !s.isBlank()).ifPresent(review::updateContent);
        Optional.ofNullable(reviewRequestDto.getScore()).ifPresent(review::updateScore);

        // 4) deleteByReviewId가 트랜잭션을 우회해서 생긴 문제 해결
        //변경사항 적용
        entityManager.flush();
        entityManager.clear();

        //최신 정보로 다시 가져오기
        final Review patchedReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ExceptionCode.REVIEW_NOT_EXIST));

        return new ReviewResponseDto(patchedReview);
    }

    /**
     * 후기 삭제하는 메서드
     * @param reviewId 후기 ID
     * @param userId 후기 작성한 유저 ID
     */
    @Transactional
    public void deleteReview(
            final Long reviewId,
            final Long userId
    ){

        // 1) 후기 조회
        final Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ExceptionCode.REVIEW_NOT_EXIST));

        // 2) 자신의 후기인지 확인
        if(!userId.equals(review.getAuthor().getId())){
            throw new CustomException(ExceptionCode.PERMISSION_DENIED);
        }

        // 3) 자식 정리
        reviewImageRepository.deleteByReviewId(reviewId);

        // 4) 후기 삭제
        reviewRepository.delete(review);
    }
}
