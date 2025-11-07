package com.treasurehunter.treasurehunter.domain.chat.dto.room;

import com.treasurehunter.treasurehunter.domain.chat.entity.room.ChatRoom;
import com.treasurehunter.treasurehunter.domain.post.dto.PostSimpleResponseDto;
import com.treasurehunter.treasurehunter.domain.user.dto.UserSimpleResponseDto;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import lombok.Getter;

import java.util.List;

@Getter
public class ChatRoomResponseDto {
    private final String roomId;
    private final String name;
    private final PostSimpleResponseDto post;
    private final List<UserSimpleResponseDto> participants;

    public ChatRoomResponseDto(final ChatRoom chatRoom) {
        this.roomId = chatRoom.getRoomId();
        this.name = chatRoom.getName();
        this.post = new PostSimpleResponseDto(chatRoom.getPost());
        this.participants = chatRoom.getChatRoomParticipants().stream()
                .filter(p -> p.getParticipant() != null) // 참가자가 탈퇴한 경우
                .map(p -> {
                    final User u = p.getParticipant();

                    //익명인 경우 처리
                    if(p.isAnonymous()){
                        return new UserSimpleResponseDto();
                    }

                    return new UserSimpleResponseDto(u);
                })
                .toList();
    }
}
