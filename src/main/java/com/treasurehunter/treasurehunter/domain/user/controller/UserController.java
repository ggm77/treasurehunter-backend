package com.treasurehunter.treasurehunter.domain.user.controller;

import com.treasurehunter.treasurehunter.domain.user.dto.UserRequestDto;
import com.treasurehunter.treasurehunter.domain.user.dto.UserResponseDto;
import com.treasurehunter.treasurehunter.domain.user.service.UserService;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    //회원가입 API, 프론트에서 OAuth로 등록후 이 API호출
    @PostMapping("/auth/user")
    @ApiResponse(
            responseCode = "200",
            description = "유저 등록 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
    )
    public ResponseEntity<UserResponseDto> createUser(
            @RequestBody final UserRequestDto userRequestDto
    ){

        final UserResponseDto userResponseDto = userService.createUser(userRequestDto);

        return ResponseEntity.ok(userResponseDto);
    }

    //유저 조회 API
    @GetMapping("/user/{id}")
    @ApiResponse(
            responseCode = "200",
            description = "유저 조회 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
    )
    public ResponseEntity<UserResponseDto> getUser(
            @PathVariable final String id,
            @RequestHeader(value = "Authorization") final String accessToken
    ){
        final Long targetUserId = Long.parseLong(id);

        return ResponseEntity.ok().body(userService.getUser(targetUserId));
    }

    //유저 정보 수정 API
    @PatchMapping("/user/{id}")
    @ApiResponse(
            responseCode = "200",
            description = "유저 정보 수정 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
    )
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable final String id,
            @RequestHeader(value = "Authorization") final String accessToken,
            @RequestBody final UserRequestDto userRequestDto
    ){
        final String tokenSub = jwtProvider.validateToken(accessToken.substring(7));

        //다른 유저의 정보에 접근 방지
        if(!Objects.equals(tokenSub, id)){
            throw new CustomException(ExceptionCode.FORBIDDEN_USER_RESOURCE_ACCESS);
        }

        //다른 유저의 정보에 접근 방지
        final Long userId = Long.parseLong(tokenSub);

        return ResponseEntity.ok().body(userService.updateUser(userId, userRequestDto));
    }

    //유저 삭제 API
    @DeleteMapping("/user/{id}")
    @ApiResponse(
            responseCode = "204",
            description = "유저 삭제 성공"
    )
    public ResponseEntity<UserResponseDto> deleteUser(
            @PathVariable final String id,
            @RequestHeader(value = "Authorization") final String accessToken
    ){
        final String tokenSub = jwtProvider.validateToken(accessToken.substring(7));

        //다른 유저의 정보에 접근 방지
        if(!Objects.equals(tokenSub, id)){
            throw new CustomException(ExceptionCode.FORBIDDEN_USER_RESOURCE_ACCESS);
        }

        //다른 유저의 정보에 접근 방지
        final Long userId = Long.parseLong(tokenSub);

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }
}
