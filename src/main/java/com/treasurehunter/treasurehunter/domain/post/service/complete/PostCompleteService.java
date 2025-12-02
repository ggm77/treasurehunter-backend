package com.treasurehunter.treasurehunter.domain.post.service.complete;

import com.treasurehunter.treasurehunter.domain.chat.repository.room.participant.ChatRoomParticipantRepository;
import com.treasurehunter.treasurehunter.domain.post.dto.complete.PostCompleteRequestDto;
import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.post.entity.PostType;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCompleteService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

    /**
     * 게시글을 완료처리 하는 메서드
     * 게시글 작성자가 요청 할 수 있다.
     * 채팅방 아이디를 함께 기재해야 그 상대에게 포인트를 지급 할 수 있다.
     * @param requestUserId
     * @param postId
     * @param postCompleteRequestDto
     */
    @Transactional
    public void completePost(
            final Long requestUserId,
            final Long postId,
            final PostCompleteRequestDto postCompleteRequestDto
    ){
        // 1) 유저 존재 확인
        final Boolean userExists = userRepository.existsById(requestUserId);
        if(!Boolean.TRUE.equals(userExists)){
            throw new CustomException(ExceptionCode.USER_NOT_EXIST);
        }

        // 2) 게시글 조회
        final Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionCode.POST_NOT_EXIST));
        final PostType postType = post.getType();

        // 3) 게시글 이미 완료 처리 되었는지 확인
        if(post.isCompleted()){
            throw new CustomException(ExceptionCode.POST_IS_COMPLETED);
        }

        // 4) 게시글 작성자가 요청한건지 확인
        if(!post.getAuthor().getId().equals(requestUserId)){
            throw new CustomException(ExceptionCode.FORBIDDEN_USER_RESOURCE_ACCESS);
        }

        // 5) 채팅방 정보 조회
        final String roomId = postCompleteRequestDto.getRoomId();
        final List<Tuple> participants =
                chatRoomParticipantRepository.findParticipantsAndIsCallerByRoomId(roomId);

        // 6) 참가자 정보 맵으로 추출
        final Map<Long, Boolean> participantsMap = participants.stream()
                .collect(Collectors.toMap(
                        t -> t.get("participantId", Long.class),
                        t -> t.get("isCaller", Boolean.class)
                ));

        // 7) 채팅방 멤버가 아닌 경우 처리
        if(!participantsMap.containsKey(requestUserId)){
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_JOINED);
        }

        // 8) 상대 유저 아이디 찾기
        final Long targetUserId = participantsMap.keySet().stream()
                .filter(id -> !id.equals(requestUserId))
                .findFirst()
                .orElse(null);

        // 9) 상대 유저 정보 조회
        final User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));


        // 5) 완료 처리
        post.updateIsCompleted(true);

        // 6) 포인트 지급
        final int point = post.getSetPoint();
        targetUser.addPoint(point);

        // 7) 물건 찾아준거면 물건 찾아준 횟수 증가
        if(postType.equals(PostType.LOST)){
            targetUser.incrementReturnedItemsCount();
        }
    }
}
