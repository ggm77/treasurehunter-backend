package com.treasurehunter.treasurehunter.domain.user.service.oauth;

import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.entity.oauth.UserOauth2Accounts;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.auth.oauth.dto.UserOauth2AccountsRequestDto;
import com.treasurehunter.treasurehunter.global.auth.oauth.dto.UserOauth2AccountsResponseDto;
import com.treasurehunter.treasurehunter.domain.user.repository.oauth.UserOauth2AccountsRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserOauth2Service {

    private final UserRepository userRepository;
    private final UserOauth2AccountsRepository userOauth2AccountsRepository;

    /**
     * OAuth를 통해서 등록 할 때 회원가입을 진행하는 서비스
     * global에 있는 OAuth관련 컴포넌트들과 소통함
     * @param userOauth2AccountsRequestDto
     * @return
     */
    public UserOauth2AccountsResponseDto upsertOAuthUser(final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto) {
        //provider 입력값 null 검사
        if(userOauth2AccountsRequestDto.getProvider() == null || userOauth2AccountsRequestDto.getProvider().isEmpty()){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        //provider user id 입력값 null 검사
        if(userOauth2AccountsRequestDto.getProviderUserId() == null || userOauth2AccountsRequestDto.getProviderUserId().isEmpty()){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        //엑세스 토큰 입력값 null 검사
        if(userOauth2AccountsRequestDto.getAccessToken() == null || userOauth2AccountsRequestDto.getAccessToken().isEmpty()){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        //oauth로 이미 가입했는지 확인
        final Optional<UserOauth2Accounts> userOauth2Accounts = userOauth2AccountsRepository.findByProviderAndProviderUserId(
                userOauth2AccountsRequestDto.getProvider(),
                userOauth2AccountsRequestDto.getProviderUserId()
        );

        //oauth로 이미 가입했다면
        if(userOauth2Accounts.isPresent()){
            //회원가입 되어 있으므로 그냥 보냄
            return new UserOauth2AccountsResponseDto(userOauth2Accounts.get());
        } else {
            //가입 안되어있으므로 회원가입
            //oauth를 통한 가입시 처음 닉네임은 무조건 "temp"
            final User savedUser = userRepository.save(new User(userOauth2AccountsRequestDto));

            //유저에 oauth 추가
            final UserOauth2Accounts savedUserOauth2Accounts = userOauth2AccountsRepository.save(new UserOauth2Accounts(userOauth2AccountsRequestDto, savedUser));

            return new UserOauth2AccountsResponseDto(savedUserOauth2Accounts);
        }
    }
}
