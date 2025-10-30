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

    /**
     * 이미지 조회하는 API
     * 바로 이미지를 띄워줌
     * @param objectKey 이미지 objectKey
     * @return 이미지
     */
    @GetMapping("/file/image")
    public ResponseEntity<Resource> getImage(
            @RequestParam("objectKey") final String objectKey
    ){
        return imageService.getImage(objectKey);
    }

    /**
     * 이미지 삭제하는 API
     * @param objectKey 이미지 objectKey
     * @param token JWT
     * @return 없음
     */
    @DeleteMapping("/file/image")
    public ResponseEntity<Void> deleteImage(
            @RequestParam("objectKey") final String objectKey,
            @RequestHeader(value = "Authorization") final String token
    ){
        final Long userId = Long.parseLong(jwtProvider.validateToken(token.substring(7)));

        imageService.deleteImage(objectKey, userId);

        return ResponseEntity.noContent().build();
    }
}
