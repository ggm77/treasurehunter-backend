package com.treasurehunter.treasurehunter.domain.admin.point.service;

import com.treasurehunter.treasurehunter.domain.admin.point.dto.PointRequestDto;
import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserRepository userRepository;

    /**
     * 유저의 포인트 강제 변경을 위한 메서드
     * isIncrease 파라미터를 통해 증가 시킬지, 감소 시킬지 정할 수 있다.
     * @param targetUserId 포인트 변경 당할 유저
     * @param requestUserId 요청한 유저 id
     * @param pointRequestDto 포인트 정보가 담긴 DTO
     * @param isIncrease 증가 시킬지 감소 시킬지 여부
     */
    @Transactional
    public void updateUserPoint(
            final Long targetUserId,
            final Long requestUserId,
            final PointRequestDto pointRequestDto,
            final boolean isIncrease
    ){

        // 1) 요청한 유저 조회
        final User requestUser = userRepository.findById(requestUserId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) 관리자인지 확인
        if(!requestUser.getRole().equals(Role.ADMIN)){
            throw new CustomException(ExceptionCode.PERMISSION_DENIED);
        }

        // 3) 포인트 바꿀 유저 조회
        final User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 4) 포인트 변경
        if(isIncrease){
            targetUser.addPoint(pointRequestDto.getAmount());
        } else{
            //요청한 값 보다 가진 포인트가 적은 경우 처리
            try {
                targetUser.consumePoint(pointRequestDto.getAmount());
            } catch (IllegalArgumentException ex){
                throw new CustomException(ExceptionCode.POINT_NOT_ENOUGH);
            }
        }
    }
}
