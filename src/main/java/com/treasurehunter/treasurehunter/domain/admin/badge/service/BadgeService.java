package com.treasurehunter.treasurehunter.domain.admin.badge.service;

import com.treasurehunter.treasurehunter.domain.admin.badge.entity.Badge;
import com.treasurehunter.treasurehunter.domain.admin.badge.dto.BadgeRequestDto;
import com.treasurehunter.treasurehunter.domain.admin.badge.dto.BadgeResponseDto;
import com.treasurehunter.treasurehunter.domain.admin.badge.entity.BadgeName;
import com.treasurehunter.treasurehunter.domain.admin.badge.repository.BadgeRepository;
import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.domain.userBadge.repository.UserBadgeRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.util.EnumUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final EnumUtil enumUtil;

    /**
     * 뱃지 정보 생성하는 메서드
     * 어드민만 이용가능하다.
     * @param badgeRequestDto 뱃지 정보 DTO
     * @param userId 요청하는 유저 ID
     * @return 저장된 뱃지의 DTO
     */
    public BadgeResponseDto createBadge(
            final BadgeRequestDto badgeRequestDto,
            final Long userId
    ){
        // 0) null 검사는 DTO에서 함

        // 1) 유저 권한 체크를 위한 유저 조회
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) 유저가 뱃지를 생성할 수 있는 권한을 가졌는지 확인
        if(user.getRole() != Role.ADMIN) {
            throw new CustomException(ExceptionCode.PERMISSION_DENIED);
        }

        // 3) 뱃지 이름 Enum 변환
        final BadgeName badgeName = enumUtil.toEnum(BadgeName.class, badgeRequestDto.getName())
                .orElseThrow(() -> new CustomException(ExceptionCode.INVALID_REQUEST));

        // 4) 뱃지 엔티티 생성
        final Badge badge = Badge.builder()
                .name(badgeName)
                .description(badgeRequestDto.getDescription())
                .build();

        // 5) DB에 뱃지 정보 저장
        final Badge savedBadge = badgeRepository.save(badge);

        return new BadgeResponseDto(savedBadge);
    }

    /**
     * 뱃지 조회하는 메서드
     * 어드민만 이용가능하지만 크게 중요한 API가 아니라서
     * 유저 정보를 조회하진 않음. 전부 필터에서 처리
     * @param badgeId 조회할 뱃지 ID
     * @return 조회한 뱃지 정보의 DTO
     */
    public BadgeResponseDto getBadge(
            final Long badgeId
    ){
        // 1) 뱃지 정보 조회
        final Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new CustomException(ExceptionCode.BADGE_NOT_EXIST));

        return new BadgeResponseDto(badge);
    }

    /**
     * 뱃지 정보 수정하는 메서드
     * 어드민만 이용가능하다.
     * @param badgeId 수정할 뱃지 ID
     * @param badgeRequestDto 수정할 뱃지 정보 DTO
     * @param userId 요청한 유저 ID
     * @return 수정된 뱃지 정보 DTO
     */
    @Transactional
    public BadgeResponseDto updateBadge(
            final Long badgeId,
            final BadgeRequestDto badgeRequestDto,
            final Long userId
    ){
        // 1) 유저 권환 체크를 위한 유저 조회
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) 유저 권한 체크
        if(user.getRole() != Role.ADMIN) {
            throw new CustomException(ExceptionCode.PERMISSION_DENIED);
        }

        // 3) 뱃지 정보 조회
        final Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new CustomException(ExceptionCode.BADGE_NOT_EXIST));

        // 4) 배지 이름 Enum으로 변환 및 업데이트
        if(badgeRequestDto.getName() != null) {
            final BadgeName badgeName = enumUtil.toEnum(BadgeName.class, badgeRequestDto.getName())
                    .orElseThrow(() -> new CustomException(ExceptionCode.INVALID_REQUEST));
            // 업데이트
            badge.updateName(badgeName);
        }

        // 5) 설명 비어있지 않다면 변경
        Optional.ofNullable(badgeRequestDto.getDescription()).filter(s -> !s.isBlank()).ifPresent(badge::updateDescription);

        return new BadgeResponseDto(badge);
    }

    /**
     * 뱃지 정보 삭제하는 메서드
     * 유저가 해당 뱃지를 이미 소유 중이라면 삭제가 불가능하다.
     * 어드민만 이용가능하다.
     * @param badgeId 삭제할 뱃지 ID
     * @param userId 요청한 유저 ID
     */
    @Transactional
    public void deleteBadge(
            final Long badgeId,
            final Long userId
    ){
        // 1) 유저 권한 체크를 위한 유저 조회
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) 유저 권한 체크
        if(user.getRole() != Role.ADMIN) {
            throw new CustomException(ExceptionCode.PERMISSION_DENIED);
        }

        // 3) 삭제 가능한지 체크 (해당 뱃지를 얻은 유저가 있는지)
        if(userBadgeRepository.existsByBadgeId(badgeId)) {
            throw new CustomException(ExceptionCode.BADGE_ALREADY_OWNED);
        }

        // 4) 뱃지가 존재하는지 확인
        final Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new CustomException(ExceptionCode.BADGE_NOT_EXIST));

        // 5) 뱃지 삭제
        badgeRepository.delete(badge);
    }
}
