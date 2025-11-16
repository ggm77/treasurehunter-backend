package com.treasurehunter.treasurehunter.domain.file.image.controller;

import com.treasurehunter.treasurehunter.domain.file.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /*
    이미지 업로드하는 API
    이미지는 multipart/form-data 형식으로 올려야함
     */
    @PostMapping( value = "/file/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createImage(
            @AuthenticationPrincipal final String userIdStr,
            @RequestPart(value = "file") final MultipartFile multipartFile
    ){
        final Long userId = Long.parseLong(userIdStr);

        return ResponseEntity.ok(imageService.createImage(multipartFile, userId));
    }

    /*
    이미지 조회하는 API
    바로 이미지를 띄워줌
     */
    @GetMapping("/file/image")
    public ResponseEntity<Resource> getImage(
            @RequestParam("objectKey") final String objectKey
    ){
        return imageService.getImage(objectKey);
    }

    /*
    이미지 삭제하는 API
     */
    @DeleteMapping("/file/image")
    public ResponseEntity<Void> deleteImage(
            @RequestParam("objectKey") final String objectKey,
            @AuthenticationPrincipal final String userIdStr
    ){
        final Long userId = Long.parseLong(userIdStr);

        imageService.deleteImage(objectKey, userId);

        return ResponseEntity.noContent().build();
    }
}
