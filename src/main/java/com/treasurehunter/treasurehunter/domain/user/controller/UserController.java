package com.treasurehunter.treasurehunter.domain.user.controller;

import com.treasurehunter.treasurehunter.domain.user.dto.UserRequestDto;
import com.treasurehunter.treasurehunter.domain.user.dto.UserResponseDto;
import com.treasurehunter.treasurehunter.domain.user.service.UserService;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //회원가입 API, 프론트에서 OAuth로 등록후 이 API호출
    @PostMapping("/user/{id}")
    public ResponseEntity<UserResponseDto> createUser(
            @PathVariable final Long id,
            @AuthenticationPrincipal final String userIdStr,
            @RequestBody final UserRequestDto userRequestDto
    ){

        final Long userId = Long.parseLong(userIdStr);

        //다른 유저의 정보에 접근 방지
        if(!Objects.equals(userId, id)){
            throw new CustomException(ExceptionCode.FORBIDDEN_USER_RESOURCE_ACCESS);
        }

        return ResponseEntity.ok(userService.createUser(userRequestDto, userId));
    }

    //유저 조회 API
    @GetMapping("/user/{id}")
    public ResponseEntity<UserResponseDto> getUser(
            @PathVariable("id") final Long targetUserId,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok().body(userService.getUser(targetUserId, userId));
    }

    //유저 정보 수정 API
    @PatchMapping("/user/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable final Long id,
            @AuthenticationPrincipal final String userIdStr,
            @RequestBody final UserRequestDto userRequestDto
    ){

        final Long userId = Long.parseLong(userIdStr);

        //다른 유저의 정보에 접근 방지
        if(!Objects.equals(userId, id)){
            throw new CustomException(ExceptionCode.FORBIDDEN_USER_RESOURCE_ACCESS);
        }

        return ResponseEntity.ok().body(userService.updateUser(userId, userRequestDto));
    }

    //유저 삭제 API
    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable final Long id,
            @AuthenticationPrincipal final String userIdStr
    ){

        final Long userId = Long.parseLong(userIdStr);

        //다른 유저의 정보에 접근 방지
        if(!Objects.equals(userId, id)){
            throw new CustomException(ExceptionCode.FORBIDDEN_USER_RESOURCE_ACCESS);
        }

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }
}
