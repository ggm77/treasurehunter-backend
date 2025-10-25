package com.treasurehunter.treasurehunter.domain.file.image.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //사진 업로드한 유저 ID (유저와 연관 관계를 맺을 이유가 없어서 추적용으로 아이디만 저장)
    @Column(nullable = false)
    private Long ownerId;

    // "file/image/ab/cd/<sha256>.jpg" 형태로만 저장 (S3 대비)
    @Column(length = 255, nullable = false)
    private String objectKey;

    //원본 파일명(확장자 포함)
    @Column(length = 260, nullable = false)
    private String originalFileName;

    //파일 확장자( "."은 제외하고 저장
    @Column(nullable = false)
    private String extension;

    //파일크기(byte)
    @Column(nullable = false)
    private Long size;

    //파일 등록 일시
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Image(
            final Long ownerId,
            final String objectKey,
            final String originalFileName,
            final String extension,
            final Long size
    ){
        this.ownerId = ownerId;
        this.objectKey = objectKey;
        this.originalFileName = originalFileName;
        this.extension = extension;
        this.size = size;
    }
}
