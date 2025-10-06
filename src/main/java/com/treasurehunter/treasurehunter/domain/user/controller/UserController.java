package com.treasurehunter.treasurehunter.domain.user.controller;

import com.treasurehunter.treasurehunter.domain.user.dto.UserRequestDto;
import com.treasurehunter.treasurehunter.domain.user.dto.UserResponseDto;
import com.treasurehunter.treasurehunter.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //회원가입 API, 프론트에서 OAuth로 등록후 이 API호출
    @PostMapping("/user")
    @ApiResponse(
            responseCode = "200",
            description = "유저 등록 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
    )
    public ResponseEntity<?> createUser(
            @RequestBody final UserRequestDto userRequestDto
    ){

        final UserResponseDto userResponseDto = userService.createUser(userRequestDto);

        return ResponseEntity.ok(userResponseDto);
    }
}
