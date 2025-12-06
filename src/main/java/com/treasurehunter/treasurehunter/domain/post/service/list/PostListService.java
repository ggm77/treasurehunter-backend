package com.treasurehunter.treasurehunter.domain.post.service.list;

import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.post.dto.list.PostListResponseDto;
import com.treasurehunter.treasurehunter.domain.post.entity.PostType;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.util.EnumUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostListService {

    private final PostRepository postRepository;
    private final EnumUtil enumUtil;

    public PostListResponseDto getLatestPosts(final String postTypeStr) {

        // 1) postType 존재 여부에 따라 알맞게 DB에서 조회
        final List<Post> posts;
        if(postTypeStr != null && !postTypeStr.isEmpty()) {

            // 2) postType 존재하면 검사
            final PostType postType = enumUtil.toEnum(PostType.class, postTypeStr)
                    .orElseThrow(() -> new CustomException(ExceptionCode.INVALID_REQUEST));

            //DB에서 조회
            posts = postRepository.findAllByTypeOrderByCreatedAtDesc(postType);
        } else {
            //DB에서 조회
            posts = postRepository.findAllByOrderByCreatedAtDesc();
        }

        return new PostListResponseDto(posts);
    }
}
