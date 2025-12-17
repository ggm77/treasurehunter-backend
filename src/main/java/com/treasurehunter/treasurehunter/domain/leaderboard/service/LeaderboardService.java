package com.treasurehunter.treasurehunter.domain.leaderboard.service;

import com.treasurehunter.treasurehunter.domain.leaderboard.dto.LeaderboardResponseDto;
import com.treasurehunter.treasurehunter.domain.leaderboard.entity.RankingType;
import com.treasurehunter.treasurehunter.domain.user.dto.UserFoundCountDto;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.util.EnumUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final EnumUtil enumUtil;
    private final UserRepository userRepository;

    public LeaderboardResponseDto getLeaderboard(final String rankingTypeStr) {

        // 1) 랭킹 조회할 방식 지정
        final RankingType rankingType = enumUtil.toEnum(RankingType.class, rankingTypeStr.toUpperCase())
                .orElseThrow(() -> new CustomException(ExceptionCode.INVALID_ENUM_VALUE));

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

        // 3) 리더보드 유저 아이디만 따로 추출
        final List<Long> userIds = leaderboard.stream()
                .map(User::getId)
                .toList();

        // 4) 추출한 아이디로 유저가 찾은 물건 개수 조회
        final List<UserFoundCountDto> foundCountDtoList = userRepository.findFoundCount(userIds);

        // 5) DTO에서 사용하기 쉽도록 Map<Long, Integer>로 변환
        final Map<Long, Integer> foundCountMap = foundCountDtoList.stream()
                .collect(Collectors.toMap(
                        UserFoundCountDto::getUserId,
                        dto -> Math.toIntExact(dto.getFoundCount())
                ));

        return new LeaderboardResponseDto(leaderboard, foundCountMap);
    }
}
