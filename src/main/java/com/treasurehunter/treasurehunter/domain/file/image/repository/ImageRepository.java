package com.treasurehunter.treasurehunter.domain.file.image.repository;

import com.treasurehunter.treasurehunter.domain.file.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByObjectKey(String objectKey);
}
