package com.treasurehunter.treasurehunter.domain.post.service.like;

import com.treasurehunter.treasurehunter.domain.post.domain.Post;
import com.treasurehunter.treasurehunter.domain.post.domain.like.PostLike;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import com.treasurehunter.treasurehunter.domain.post.repository.like.PostLikeRepository;
import com.treasurehunter.treasurehunter.domain.user.domain.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;

    /**
     * 게시글에 좋아요 표시하는 메서드
     * 좋아요 표시될 게시글과 좋아요 표시한 유저는 N:N 관계이기 때문에 PostLike라는 조인 엔티티 사용
     * @param postId 좋아요 표시할 게시글 ID
     * @param userId 좋아요를 표시하고 싶은 유저 ID
     */
    @Transactional
    public void likePost(
            final Long postId,
            final Long userId
    ){

        // 1) 이미 좋아요 눌렀는지 확인
        final Boolean exists = postLikeRepository.existsByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.POST_LIKE_NOT_EXIST));
        //이미 눌렀다면
        if(exists){
            throw new CustomException(ExceptionCode.POST_LIKE_ALREADY_EXIST);
        }

        // 2) 좋아요를 표시하고 싶어하는 유저 조회
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 3) 좋아요 표시할 게시글 조회
        final Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionCode.POST_NOT_EXIST));

        // 4) 좋아요 연관관계 설정하는 조인 엔티티 설정
        final PostLike postLike = PostLike.builder()
                .post(post)
                .user(user)
                .build();

        // 5) 연관관계 설정
        postLikeRepository.save(postLike);
    }

    /**
     * 게시글에 표시된 좋아요를 해제하는 메서드
     * 게시글과 유저는 N:N이기 때문에 PostLike라는 조인 엔티티 사용함
     * @param postId 좋아요 해제할 게시글 ID
     * @param userId 좋아요 해제 하고 싶은 유저 ID
     */
    @Transactional
    public void unlikePost(
            final Long postId,
            final Long userId
    ){
        // 1) 좋아요 존재 유무 확인
        final PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.POST_LIKE_NOT_EXIST));

        // 2) 있다면 삭제
        postLikeRepository.delete(postLike);
    }
}
