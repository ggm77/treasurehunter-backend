package com.treasurehunter.treasurehunter.domain.post.service;

import com.treasurehunter.treasurehunter.domain.post.domain.ItemCategory;
import com.treasurehunter.treasurehunter.domain.post.domain.Post;
import com.treasurehunter.treasurehunter.domain.post.domain.PostType;
import com.treasurehunter.treasurehunter.domain.post.dto.PostRequestDto;
import com.treasurehunter.treasurehunter.domain.post.dto.PostResponseDto;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import com.treasurehunter.treasurehunter.domain.user.domain.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PostResponseDto createPost(
            final PostRequestDto postRequestDto,
            final Long userId
    ){

        // 1) 게시물 유형과 카테고리 검증 (enum으로 변환해야하는 값 검증)
        final String providedType = postRequestDto.getType();
        final String providedItemCategory = postRequestDto.getItemCategory();

        final PostType postType;
        final ItemCategory itemCategory;
        try{
            postType = PostType.valueOf(providedType);
            itemCategory = ItemCategory.valueOf(providedItemCategory);
        } catch (IllegalArgumentException ex){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 게시글과 연관 관계 가질 유저 정보 가져오기
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 3) Post 엔티티 만들기
        final Post post = Post.builder()
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .type(postType)
                .author(user)
                .itemCategory(itemCategory)
                .lat(postRequestDto.getLat())
                .lon(postRequestDto.getLon())
                .lostAt(postRequestDto.getLostAt())
                .isAnonymous(postRequestDto.getIsAnonymous())
                .isCompleted(false)
                .build();

        // 4) DB에 저장
        final Post savedPost = postRepository.save(post);

        return new PostResponseDto(savedPost);
    }
}
