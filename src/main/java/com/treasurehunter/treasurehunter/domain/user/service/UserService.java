package com.treasurehunter.treasurehunter.domain.user.service;

import com.treasurehunter.treasurehunter.domain.user.domain.User;
import com.treasurehunter.treasurehunter.domain.user.dto.UserRequestDto;
import com.treasurehunter.treasurehunter.domain.user.dto.UserResponseDto;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 유저정보를 등록하는 서비스
     * @param userRequestDto 회원가입 요청 DTO
     * @return 등록된 유저 정보 DTO
     */
    public UserResponseDto createUser(final UserRequestDto userRequestDto){

        //uid 입력값 null 검사
        if(userRequestDto.getUid() == null || userRequestDto.getUid().isEmpty()){
            throw new IllegalArgumentException("UID must not be blank.");
        }

        //oauth 입력값 null 검사
        if(userRequestDto.getOauth() == null || userRequestDto.getOauth().isEmpty()){
            throw new IllegalArgumentException("OAuth must not be blank.");
        }

        //nickname 입력값 null 검사
        if(userRequestDto.getNickname() == null || userRequestDto.getNickname().isEmpty()){
            throw new IllegalArgumentException("Nickname must not be blank.");
        }

        //name 입력값 null 검사
        if(userRequestDto.getName() == null || userRequestDto.getName().isEmpty()){
            throw new IllegalArgumentException("Name must not be blank.");
        }

        //닉네임 중복 검사
        final boolean isNicknameExist = userRepository.existsByNickname(userRequestDto.getNickname());
        if(isNicknameExist){
            throw new IllegalArgumentException("Nickname already exists.");
        }

        //유저 엔티티 생성 및 저장
        final User user = new User(userRequestDto);
        final User savedUserProfile = userRepository.save(user);

        return new UserResponseDto(savedUserProfile);
    }
}
