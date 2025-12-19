package com.treasurehunter.treasurehunter.domain.user.service;

import com.treasurehunter.treasurehunter.domain.chat.entity.room.participant.ChatRoomParticipant;
import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.review.entity.Review;
import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.dto.UserRequestDto;
import com.treasurehunter.treasurehunter.domain.user.dto.UserResponseDto;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 유저정보를 등록하는 서비스
     * @param userRequestDto 회원가입 요청 DTO
     * @param userId oauth에서 등록된 유저 아이디
     * @return 등록된 유저 정보 DTO
     */
    @Transactional
    public UserResponseDto createUser(
            final UserRequestDto userRequestDto,
            final Long userId
    ){

        //nickname 입력값 null 검사
        if(userRequestDto.getNickname() == null || userRequestDto.getNickname().isEmpty()){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        //name 입력값 null 검사
        if(userRequestDto.getName() == null || userRequestDto.getName().isEmpty()){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        //닉네임 중복 검사
        final boolean isNicknameExist = userRepository.existsByNickname(userRequestDto.getNickname());
        if(isNicknameExist){
            throw new CustomException(ExceptionCode.NICKNAME_DUPLICATE);
        }

        final String latStr = userRequestDto.getLat();
        final String lonStr = userRequestDto.getLon();

        final BigDecimal lat;
        final BigDecimal lon;
        try {
            if (latStr != null && lonStr != null) {
                lat = new BigDecimal(latStr);
                lon = new BigDecimal(lonStr);
            }
            else {
                lat = null;
                lon = null;
            }
        } catch (NumberFormatException ex) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        if(user.getRole() != Role.NOT_REGISTERED){
            throw new CustomException(ExceptionCode.USER_ALREADY_EXIST);
        }

        user.updateNickname(userRequestDto.getNickname());
        user.updateName(userRequestDto.getName());
        user.updateRoleToNotVerified();
        if(lat != null && lon != null){
            user.updateLat(lat);
            user.updateLon(lon);
        }

        if(userRequestDto.getProfileImage() != null && !userRequestDto.getProfileImage().isEmpty()){
            user.updateProfileImage(userRequestDto.getProfileImage());
        }

        return new UserResponseDto(user);
    }

    /**
     * 유저의 정보를 조회하는 메서드
     * 다른 회원의 정보도 조회 가능
     * 다른 회원 정보시 dto에서 민감한 정보는 빠짐
     * @param targetUserId 조회할 유저 아이디
     * @param requestUserId 요청한 유저 아이디
     * @return 등록된 유저 DTO
     */
    @Transactional(readOnly = true) //LazyInitializationException 방어 //N+1 해결을 통해 문제 해결되면 지우기
    public UserResponseDto getUser(
            final Long targetUserId,
            final Long requestUserId
    ){

        // 1) 유저 조회
        final User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) 기본 DTO생성
        final UserResponseDto userResponseDto = new UserResponseDto(user);

        // 3) 다른 유저 조회시 민감 정보 삭제
        final UserResponseDto publicUserResponseDto;

        //다른 유저 조회 할 때
        if(!targetUserId.equals(requestUserId)){
            publicUserResponseDto = userResponseDto.removeSensitiveData();
        }
        //자기 자신 조회 할 때
        else {
            publicUserResponseDto = userResponseDto;
        }

        return publicUserResponseDto;
    }

    /**
     * 유저의 정보를 수정하는 메서드
     * 본인의 정보만 수정 가능
     * 원하는 정보만 수정 가능
     * @param userId 자신의 유저 아이디
     * @param userRequestDto 수정할 정보들
     * @return 수정된 유저 DTO
     */
    @Transactional
    public UserResponseDto updateUser(
            final Long userId,
            final UserRequestDto userRequestDto
    ){
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        //변경할 닉네임이 존재하면 변경
        if(userRequestDto.getNickname() != null && !userRequestDto.getNickname().isEmpty()){

            //닉네임 중복 검사
            final boolean isNicknameExist = userRepository.existsByNickname(userRequestDto.getNickname());
            if(isNicknameExist){
                throw new CustomException(ExceptionCode.NICKNAME_DUPLICATE);
            }

            user.updateNickname(userRequestDto.getNickname());
        }

        //변경할 프로필 사진이 존재하면 변경
        if(userRequestDto.getProfileImage() != null && !userRequestDto.getProfileImage().isEmpty()){
            user.updateProfileImage(userRequestDto.getProfileImage());
        }

        //변경할 이름이 존재하면 변경
        if(userRequestDto.getName() != null && !userRequestDto.getName().isEmpty()){
            user.updateName(userRequestDto.getName());
        }

        //변경할 위도 경도 존재하면 변경
        if(userRequestDto.getLat() != null && userRequestDto.getLon() != null){
            final BigDecimal lat;
            final BigDecimal lon;

            try {
                lat = new BigDecimal(userRequestDto.getLat());
                lon = new BigDecimal(userRequestDto.getLon());
            } catch (NumberFormatException ex) {
                throw new CustomException(ExceptionCode.INVALID_REQUEST);
            }

            user.updateLat(lat);
            user.updateLon(lon);
        }

        return new UserResponseDto(user);
    }

    /**
     * 유저 삭제하는 메서드
     * 본인 계정만 삭제 가능
     * @param userId 자신의 유저 아이디
     */
    @Transactional
    public void deleteUser(final Long userId){

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        //자식 정리 (게시글)
        if(user.getPosts() != null){
            user.getPosts()
                    .forEach(Post::detachAuthor);
        }

        // 자식 정리 (후기)
        if(user.getReviews() != null){
            user.getReviews()
                    .forEach(Review::detachAuthor);
        }

        // 자식 정리 (받은 후기)
        if(user.getReceivedReviews() != null){
            user.getReceivedReviews()
                    .forEach(Review::detachAuthor);
        }

        // 자식 정리 (채팅방)
        if(user.getChatRoomParticipants() != null){
            user.getChatRoomParticipants()
                    .forEach(ChatRoomParticipant::detachUser);
        }

        userRepository.delete(user);
    }
}
