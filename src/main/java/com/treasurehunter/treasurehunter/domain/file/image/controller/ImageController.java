package com.treasurehunter.treasurehunter.domain.file.image.controller;

import com.treasurehunter.treasurehunter.domain.file.image.service.ImageService;
import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImageController {

    private final JwtProvider jwtProvider;
    private final ImageService imageService;

    /**
     * 이미지 업로드하는 API
     * 이미지는 multipart/form-data 형식으로 올려야함
     * @param token JWT
     * @param multipartFile 이미지 파일 하나
     * @return DTO에 담긴 파일 URL
     */
    @PostMapping( value = "/file/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createImage(
            @RequestHeader(value = "Authorization") final String token,
            @RequestPart(value = "file") final MultipartFile multipartFile
    ){
        final Long userId = Long.parseLong(jwtProvider.validateToken(token.substring(7)));

        return ResponseEntity.ok(imageService.createImage(multipartFile, userId));
    }

    @GetMapping("/file/image")
    public ResponseEntity<?> getImage(
            @RequestParam("objectKey") final String objectKey,
            @RequestHeader(value = "Authorization") final String token
    ){
        jwtProvider.validateToken(token.substring(7));


        return imageService.getImage(objectKey);
    }
}
