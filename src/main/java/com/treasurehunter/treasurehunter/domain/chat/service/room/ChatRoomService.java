package com.treasurehunter.treasurehunter.domain.chat.service.room;

import com.treasurehunter.treasurehunter.domain.chat.dto.room.ChatRoomRequestDto;
import com.treasurehunter.treasurehunter.domain.chat.dto.room.ChatRoomResponseDto;
import com.treasurehunter.treasurehunter.domain.chat.dto.room.list.ChatRoomListResponseDto;
import com.treasurehunter.treasurehunter.domain.chat.entity.room.ChatRoom;
import com.treasurehunter.treasurehunter.domain.chat.entity.room.participant.ChatRoomParticipant;
import com.treasurehunter.treasurehunter.domain.chat.repository.room.ChatRoomRepository;
import com.treasurehunter.treasurehunter.domain.chat.repository.room.participant.ChatRoomParticipantRepository;
import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;


    /**
     * 자신이 참가 중인 채팅방의 리스트를 가져오는 메서드
     * @param userId 검색할 유저 ID
     * @return 참가 중인 채팅방의 정보가 담긴 DTO의 리스트를 가진 DTO
     */
    public ChatRoomListResponseDto getChatRoomList(final Long userId){

        // 1) 유저 조회
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) ChatRoomParticipant 엔티티를 이용해서 참가중인 채팅방 정보 추출
        final List<ChatRoomResponseDto> chatRoomResponseDtoList = Optional.ofNullable(user.getChatRoomParticipants())
                .orElse(Collections.emptyList())
                .stream()
                .map(ChatRoomParticipant::getChatRoom)
                .filter(Objects::nonNull)
                .map(ChatRoomResponseDto::new)
                .toList();

        return new ChatRoomListResponseDto(chatRoomResponseDtoList);
    }

    /**
     * 새로운 채팅방을 생성하는 메서드
     * 게시물을 쓴 사람과 채팅을 시작하는 것이기 때문에
     * 게시물의 정보를 바탕으로 채팅방이 만들어짐
     * @param chatRoomRequestDto 만들 채팅방 정보
     * @param requestUserId 채팅을 거는 유저 ID
     * @return 만들어진 채팅방의 정보가 담긴 DTO
     */
    @Transactional
    public ChatRoomResponseDto createChatRoom(
            final ChatRoomRequestDto chatRoomRequestDto,
            final Long requestUserId
    ){

        // 1) 유저 조회
        final User requestUser = userRepository.findById(requestUserId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) 게시글 조회
        final Post post = postRepository.findById(chatRoomRequestDto.getPostId())
                .orElseThrow(() -> new CustomException(ExceptionCode.POST_NOT_EXIST));
        final Long postAuthorId = post.getAuthor().getId();

        // 3) 자신과의 채팅 방지
        if(requestUserId.equals(postAuthorId)) {
            throw new CustomException(ExceptionCode.CHAT_WITH_SELF_NOT_ALLOWED);
        }

        // 4) 완료된 게시글에 채팅 금지
        if(post.isCompleted()){
            throw new CustomException(ExceptionCode.POST_IS_COMPLETED);
        }

        // 5) 같은 게시글에 중복 채팅 방지

        // DB에서 조회할 때 (3,5) (5,3)을 다르다고 처리 하기 때문에 일관성 있게 처리
        final Long u1 = Math.min(postAuthorId, requestUserId);
        final Long u2 = Math.max(postAuthorId, requestUserId);

        final Optional<ChatRoom> existing = chatRoomRepository.findExistingRoomForPostAndPair(post.getId(), u1, u2);
        if(existing.isPresent()){
            throw new CustomException(ExceptionCode.CHAT_ROOM_ALREADY_EXIST);
        }

        // 6) 채팅방 엔티티 생성
        final ChatRoom chatRoom = ChatRoom.builder()
                .name(chatRoomRequestDto.getName())
                .post(post)
                .chatRoomParticipants(new ArrayList<>())
                .build();

        // 7) 채팅방에 참가한 유저들 관계 설정
        // 채팅을 건 유저
        chatRoom.getChatRoomParticipants().add(
                ChatRoomParticipant.builder()
                        .chatRoom(chatRoom)
                        .participant(requestUser)
                        .isAnonymous(chatRoomRequestDto.getIsAnonymous())
                        .build()
        );
        // 게시글 작성자 (채팅을 걸린 사람)
        chatRoom.getChatRoomParticipants().add(
                ChatRoomParticipant.builder()
                        .chatRoom(chatRoom)
                        .participant(post.getAuthor())
                        .isAnonymous(post.isAnonymous())
                        .build()
        );

        // 8) 채팅방 저장
        final ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        return new ChatRoomResponseDto(savedChatRoom);
    }

    /**
     * 채팅방의 정보를 조회하는 메서드
     * 자신이 참가중인 채팅방의 정보만 가져올 수 있다.
     * @param chatRoomId 조회할 채팅방의 roomId (UUID)
     * @param requestUserId 요청한 유저의 ID
     * @return 채팅방의 정보가 담긴 DTO
     */
    public ChatRoomResponseDto getChatRoom(
            final String chatRoomId,
            final Long requestUserId
    ){

        // 1) 채팅방 조회
        final ChatRoom chatRoom = chatRoomRepository.findByRoomId(chatRoomId)
                .orElseThrow(() -> new CustomException(ExceptionCode.CHAT_ROOM_NOT_EXIST));

        // 2) 채팅방에 자신이 포함 되어 있는지 확인
        final boolean exists = chatRoomParticipantRepository.existsParticipantInChatRoom(chatRoomId, requestUserId);
        if(!exists){
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_JOINED);
        }

        return new ChatRoomResponseDto(chatRoom);
    }

    /**
     * 채팅방의 정보를 수정하는 메서드
     * @param chatRoomId 수정할 채팅방 roomId (UUID)
     * @param chatRoomRequestDto 수정할 정보가 담긴 DTO
     * @param requestUserId 요청한 유저 ID
     * @return
     */
    @Transactional
    public ChatRoomResponseDto updateChatRoom(
            final String chatRoomId,
            final ChatRoomRequestDto chatRoomRequestDto,
            final Long requestUserId
    ){
        // 1) 유저 조회
        final boolean userExist = userRepository.existsById(requestUserId);
        if(!userExist){
            throw new CustomException(ExceptionCode.USER_NOT_EXIST);
        }

        // 2) 채팅방 조회
        final ChatRoom chatRoom = chatRoomRepository.findByRoomId(chatRoomId)
                .orElseThrow(() -> new CustomException(ExceptionCode.CHAT_ROOM_NOT_EXIST));

        // 3) 채팅방에 자신이 포함 되어있는지 확인
        final boolean exists = chatRoomParticipantRepository.existsParticipantInChatRoom(chatRoomId, requestUserId);
        if(!exists){
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_JOINED);
        }

        // 4) 수정할 정보 존재하면 정보 수정
        Optional.ofNullable(chatRoomRequestDto.getName()).filter(s -> !s.isBlank()).ifPresent(chatRoom::updateName);

        return new ChatRoomResponseDto(chatRoom);
    }

    /**
     * 채팅방 나가는 메서드
     * 만약 마지막 참가자가 나가면 자동으로 채팅방 삭제
     * @param chatRoomId 채팅방 roomId (UUID)
     * @param requestUserId 요청한 유저 ID
     */
    @Transactional
    public void leaveChatRoom(
            final String chatRoomId,
            final Long requestUserId
    ){

        // 1) 참가자 삭제 및 참가자가 아닐 경우 예외 처리
        final int deleted = chatRoomParticipantRepository.deleteByChatRoom_RoomIdAndParticipant_Id(chatRoomId, requestUserId);
        //삭제 실패 == 채팅방에 참가 하지 않음
        if(deleted == 0){
            //애초에 유저가 존재하지 않는 경우 예외 처리
            if(!userRepository.existsById(requestUserId)){
                throw new CustomException(ExceptionCode.USER_NOT_EXIST);
            }

            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_JOINED);
        }

        // 2) 채팅방에 아무도 없으면 채팅방 삭제
        final ChatRoom chatRoom = chatRoomRepository.findByRoomId(chatRoomId)
                        .orElseThrow(() -> new CustomException(ExceptionCode.CHAT_ROOM_NOT_EXIST));
        //활성 사용자(탈퇴하지 않은 사용자)가 남아있는지 확인
        final boolean hasActive = chatRoom.getChatRoomParticipants().stream()
                        .anyMatch(p -> p.getParticipant() != null);
        if(!hasActive){
            chatRoomRepository.delete(chatRoom);
        }
    }
}