package com.treasurehunter.treasurehunter.domain.post.service.complete;

import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.post.entity.PostType;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCompleteService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public void completePost(
            final Long requestUserId,
            final Long postId
    ){
        // 1) 유저 조회
        final User requestUser = userRepository.findById(requestUserId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) 게시글 조회
        final Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionCode.POST_NOT_EXIST));
        final PostType postType = post.getType();

        // 3) 게시글 이미 완료 처리 되었는지 확인
        if(post.isCompleted()){
            throw new CustomException(ExceptionCode.POST_IS_COMPLETED);
        }

        // 4) [추후에 채팅이 다 만들어지면] 찾아준 사람과 같은지 확인

        // 5) 완료 처리
        post.updateIsCompleted(true);

        // 6) 포인트 지급
        final int point = post.getSetPoint();
        requestUser.addPoint(point);

        // 7) 물건 찾아준거면 물건 찾아준 횟수 증가
        if(postType.equals(PostType.LOST)){
            requestUser.incrementReturnedItemsCount();
        }
    }
}
