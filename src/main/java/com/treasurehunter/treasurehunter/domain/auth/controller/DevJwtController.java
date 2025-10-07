package com.treasurehunter.treasurehunter.domain.auth.controller;

import com.treasurehunter.treasurehunter.domain.auth.dto.DevJwtRequestDto;
import com.treasurehunter.treasurehunter.domain.auth.dto.DevJwtResponseDto;
import com.treasurehunter.treasurehunter.domain.auth.service.DevJwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 개발하는 도중에만 사용하는 API
 * 실제 운영 환경에는 포함되면 절대 안됨
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DevJwtController {

    private final DevJwtService devJwtService;

    @PostMapping("/auth/dev/jwt")
    public ResponseEntity<?> createJwt(
            @RequestBody final DevJwtRequestDto devJwtRequestDto
    ){
        final DevJwtResponseDto devJwtResponseDto = devJwtService.createJwt(devJwtRequestDto);

        return ResponseEntity.ok().body(devJwtResponseDto);
    }
}
