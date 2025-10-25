package com.treasurehunter.treasurehunter.domain.file.image.repository;

import com.treasurehunter.treasurehunter.domain.file.image.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
