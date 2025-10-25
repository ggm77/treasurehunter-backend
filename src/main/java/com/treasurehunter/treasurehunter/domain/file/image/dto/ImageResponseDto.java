package com.treasurehunter.treasurehunter.domain.file.image.dto;

import lombok.Getter;

@Getter
public class ImageResponseDto {
    private final String fileUrl;

    public ImageResponseDto(final String fileUrl){
        this.fileUrl = fileUrl;
    }
}
