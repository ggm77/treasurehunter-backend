package com.treasurehunter.treasurehunter.domain.file.image.service;

import com.treasurehunter.treasurehunter.domain.file.image.domain.Image;
import com.treasurehunter.treasurehunter.domain.file.image.dto.ImageResponseDto;
import com.treasurehunter.treasurehunter.domain.file.image.repository.ImageRepository;
import com.treasurehunter.treasurehunter.domain.user.domain.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.infra.storage.local.LocalFileStorage;
import com.treasurehunter.treasurehunter.global.util.FileNameUtil;
import com.treasurehunter.treasurehunter.global.util.ImageUtil;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final FileNameUtil fileNameUtil;
    private final LocalFileStorage localFileStorage;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final MultipartProperties multipartProperties;
    private final ImageUtil imageUtil;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    /**
     * 이미지 파일 등록하고 저장하는 메서드
     * jpg, jpeg, png 파일만 저장할 수 있음
     * DB에 기본 정보 저장 후 로컬에 파일 저장
     * @param multipartFile 저장할 파일
     * @param userId 유저 아이디
     * @return
     */
    @Transactional
    public ImageResponseDto createImage(
            final MultipartFile multipartFile,
            final Long userId
    ){
        // 1) 파일 유무 검사
        if(multipartFile == null || multipartFile.isEmpty()){
            throw new CustomException(ExceptionCode.FILE_NOT_UPLOADED);
        }

        // 2) 파일 크기 검사
        if(multipartFile.getSize() > multipartProperties.getMaxFileSize().toBytes()){
            throw new CustomException(ExceptionCode.TOO_BIG_FILE);
        }

        // 3) 유저 존재 유무 검사
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 4) 파일 이름 유무 검사
        if(multipartFile.getOriginalFilename() == null || multipartFile.getOriginalFilename().isEmpty()) {
            throw new CustomException(ExceptionCode.INVALID_FILE_NAME);
        }

        // 5) 파일 이름 유효성 검사
        // \ / : * ? " < > | 9가지 기호와 제어문자, 보이지 않는 문자 방지
        if(!fileNameUtil.isValidFileName(multipartFile.getOriginalFilename())){
            throw new CustomException(ExceptionCode.INVALID_FILE_NAME);
        }

        // 6) 확장자 검사
        final String originalExtension = StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
        if(originalExtension == null || originalExtension.isEmpty()) {
            throw new CustomException(ExceptionCode.INVALID_FILE_EXTENSION);
        }

        if(!ALLOWED_EXTENSIONS.contains(originalExtension.toLowerCase(Locale.ROOT))){
            throw new CustomException(ExceptionCode.INVALID_FILE_EXTENSION);
        }

        // 7) 파일 시그니처 검사
        final boolean isValidImage;
        try {
            final BufferedInputStream bis = new BufferedInputStream(multipartFile.getInputStream());
            bis.mark(64); // 여유있게 설정
            final byte[] header = bis.readNBytes(16);
            isValidImage = imageUtil.isValidSignature(header, originalExtension.toLowerCase(Locale.ROOT));
            bis.reset(); // 저장 로직에서 다시 읽을 수 있도록 복귀
        } catch (IOException e) {
            throw new CustomException(ExceptionCode.INVALID_FILE);
        }
        if(!isValidImage){
            throw new CustomException(ExceptionCode.INVALID_FILE);
        }

        // 8) 엔티티에 저장될 값들 지정
        final String originalFileName = multipartFile.getOriginalFilename();
        final String extension = originalExtension.toLowerCase(Locale.ROOT);
        final Long size = multipartFile.getSize();

        // 8-1) objectKey는 원본 이름 바탕으로 해싱, 샤딩 진행
        final String objectKey;
        try {
            objectKey = fileNameUtil.generateShardedPath(originalFileName, extension);
        } catch (IllegalArgumentException | IllegalStateException ex){
            throw new CustomException(ExceptionCode.FILE_NOT_UPLOADED);
        }

        // 9) 엔티티 생성
        final Image image = Image.builder()
                .ownerId(user.getId())
                .objectKey(objectKey)
                .originalFileName(originalFileName)
                .extension(extension)
                .size(size)
                .build();

        // 10) DB에 저장
        final Image savedImage = imageRepository.save(image);

        // 11) 이미지에 접근할 수 있는 URL 조립
        final String imageUrl = fileNameUtil.buildImageUrl(savedImage.getObjectKey());

        // 12) 파일 저장 (부모 디렉토리 없으면 자동 생성)
        try{
            localFileStorage.saveMultipartFile(multipartFile, objectKey);
        } catch (RuntimeException ex){
            throw new CustomException(ExceptionCode.FILE_SAVE_FAILED);
        }

        return new ImageResponseDto(imageUrl);
    }

    public ResponseEntity<Resource> getImage(final String objectKey){
        final Resource file;
        try {
            file = localFileStorage.getImage(objectKey);
        } catch (RuntimeException ex){
            throw new CustomException(ExceptionCode.FILE_NOT_FOUND);
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", "image/" + objectKey.substring(objectKey.lastIndexOf('.')+1));

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(file);
    }

    @Transactional
    public void deleteImage(
            final String objectKey,
            final Long userId
    ){

        final Image image = imageRepository.findByObjectKey(objectKey)
                .orElseThrow(() -> new CustomException(ExceptionCode.FILE_NOT_FOUND));

        if(!image.getOwnerId().equals(userId)){
            throw new CustomException(ExceptionCode.PERMISSION_DENIED);
        }

        imageRepository.delete(image);

        try{
            localFileStorage.deleteImage(objectKey);
        } catch (RuntimeException ex){
            throw new CustomException(ExceptionCode.FILE_NOT_FOUND);
        } catch (IOException ex){
            throw new CustomException(ExceptionCode.FILE_DELETE_FAILED);
        }
    }
}
