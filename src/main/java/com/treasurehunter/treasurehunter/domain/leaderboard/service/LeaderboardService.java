package com.treasurehunter.treasurehunter.domain.leaderboard.service;

import com.treasurehunter.treasurehunter.domain.leaderboard.dto.LeaderboardResponseDto;
import com.treasurehunter.treasurehunter.domain.leaderboard.entity.RankingType;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.util.EnumUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final EnumUtil enumUtil;
    private final UserRepository userRepository;

    public LeaderboardResponseDto getLeaderboard(final String rankingTypeStr) {

        // 1) 랭킹 조회할 방식 지정
        final RankingType rankingType = enumUtil.toEnum(RankingType.class, rankingTypeStr.toUpperCase())
                .orElseThrow(() -> new CustomException(ExceptionCode.INVALID_REQUEST));

        // 2) 조회 방식에 맞게 유저 리스트 조회
        final List<User> leaderboard;
        if(rankingType.equals(RankingType.POINTS)){
            leaderboard = userRepository.findTop100ByOrderByPointDesc();
        }
        else if(rankingType.equals(RankingType.RETURNS)){
            leaderboard = userRepository.findTop100ByOrderByReturnedItemsCountDesc();
        }
        else if(rankingType.equals(RankingType.FINDS)){
            leaderboard = userRepository.findTopFindsUsers(PageRequest.of(0, 100));
        }
        else{
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        return new LeaderboardResponseDto(leaderboard);
    }
}
