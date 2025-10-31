package com.treasurehunter.treasurehunter.domain.userBadge.service.list;

import com.treasurehunter.treasurehunter.domain.user.domain.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.domain.userBadge.domain.UserBadge;
import com.treasurehunter.treasurehunter.domain.userBadge.dto.UserBadgeResponseDto;
import com.treasurehunter.treasurehunter.domain.userBadge.dto.list.UserBadgeListResponseDto;
import com.treasurehunter.treasurehunter.domain.userBadge.repository.UserBadgeRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBadgeListService {

    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;

    /**
     * 유저가 가진 뱃지들의 정보를 가져오는 메서드
     * @param targetUserId 뱃지 정보를 가져올 유저 ID
     * @return 뱃지 정보가 담긴 DTO 리스트
     */
    @Transactional
    public UserBadgeListResponseDto getUserBadges(final Long targetUserId){

        // 1) 유저 존재 확인
        final User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) 유저가 가진 뱃지 조회
        final List<UserBadge> badges = userBadgeRepository.findByUserId(user.getId());

        return new UserBadgeListResponseDto(badges.stream()
                .map(UserBadgeResponseDto::new)
                .toList()
        );
    }
}
