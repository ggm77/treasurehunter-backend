package com.treasurehunter.treasurehunter.domain.user.service;

import com.treasurehunter.treasurehunter.domain.post.domain.Post;
import com.treasurehunter.treasurehunter.domain.review.domain.Review;
import com.treasurehunter.treasurehunter.domain.user.domain.Role;
import com.treasurehunter.treasurehunter.domain.user.domain.User;
import com.treasurehunter.treasurehunter.domain.user.dto.UserRequestDto;
import com.treasurehunter.treasurehunter.domain.user.dto.UserResponseDto;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

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

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        if(user.getRole() != Role.NOT_REGISTERED){
            throw new CustomException(ExceptionCode.USER_ALREADY_EXIST);
        }

        user.updateNickname(userRequestDto.getNickname());
        user.updateName(userRequestDto.getName());
        user.updateRoleToNotVerified();

        if(userRequestDto.getProfileImage() != null && !userRequestDto.getProfileImage().isEmpty()){
            user.updateProfileImage(userRequestDto.getProfileImage());
        }

        return new UserResponseDto(user);
    }

    /**
     * 유저의 정보를 조회하는 메서드
     * 다른 회원의 정보도 조회 가능
     * @param userId 조회할 유저 아이디
     * @return 등록된 유저 DTO
     */
    @Transactional(readOnly = true) //LazyInitializationException 방어 //N+1 해결을 통해 문제 해결되면 지우기
    public UserResponseDto getUser(final Long userId){

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        return new UserResponseDto(user);
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

        userRepository.delete(user);
    }
}
