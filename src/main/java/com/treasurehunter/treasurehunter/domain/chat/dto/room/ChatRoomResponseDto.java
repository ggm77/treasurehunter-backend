package com.treasurehunter.treasurehunter.domain.chat.dto.room;

import com.treasurehunter.treasurehunter.domain.chat.entity.room.ChatRoom;
import com.treasurehunter.treasurehunter.domain.post.dto.PostSimpleResponseDto;
import com.treasurehunter.treasurehunter.domain.user.dto.UserChatRoomResponseDto;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import lombok.Getter;

import java.util.List;

@Getter
public class ChatRoomResponseDto {
    private final String roomId;
    private final String name;
    private final PostSimpleResponseDto post;
    private final List<UserChatRoomResponseDto> participants;

    public ChatRoomResponseDto(final ChatRoom chatRoom) {
        this.roomId = chatRoom.getRoomId();
        this.name = chatRoom.getName();
        //게시글이 삭제 된 경우 예외처리
        if(chatRoom.getPost() != null) {
            this.post = new PostSimpleResponseDto(chatRoom.getPost());
        } else {
            this.post = null;
        }
        this.participants = chatRoom.getChatRoomParticipants().stream()
                .filter(p -> p.getParticipant() != null) // 참가자가 탈퇴한 경우
                .map(p -> {
                    final User u = p.getParticipant();

                    //익명인 경우 처리
                    if(p.isAnonymous()){
                        return new UserChatRoomResponseDto(p.isCaller());
                    }

                    return new UserChatRoomResponseDto(u, p.isCaller());
                })
                .toList();
    }
}
