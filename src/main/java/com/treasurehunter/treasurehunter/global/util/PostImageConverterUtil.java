package com.treasurehunter.treasurehunter.global.util;

import com.treasurehunter.treasurehunter.domain.post.domain.Post;
import com.treasurehunter.treasurehunter.domain.post.domain.image.PostImage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Component
public class PostImageConverterUtil {

    public List<PostImage> toPostImage(final List<String> imageUrls, final Post savedPost) {

        //null 대응
        if(imageUrls == null || imageUrls.isEmpty()) {
            return Collections.emptyList();
        }

        //요소가 null인 경우 제거
        final List<String> validUrls = imageUrls.stream()
                .filter(Objects::nonNull)
                .toList();

        //null제거후 빈 리스트 대응
        if(validUrls.isEmpty()) {
            return Collections.emptyList();
        }

        return IntStream.range(0, validUrls.size())
                        .mapToObj(i -> new PostImage(validUrls.get(i), i, savedPost))
                        .toList();
    }
}
