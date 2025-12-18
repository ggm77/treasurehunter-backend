package com.treasurehunter.treasurehunter.domain.post.service.list;

import com.treasurehunter.treasurehunter.domain.post.dto.list.PostListResponseDto;
import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.post.entity.PostType;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.util.EnumUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PostListService {

    @Value("${full.text.search.min.len}")
    private int FULL_TEXT_SEARCH_MIN_LEN;

    @Value("${full.text.search.max.len}")
    private int FULL_TEXT_SEARCH_MAX_LEN;

    //위도 경도 검사를 위한 상수값
    private static final BigDecimal MIN_LAT = BigDecimal.valueOf(-90);
    private static final BigDecimal MAX_LAT = BigDecimal.valueOf(90);
    private static final BigDecimal MIN_LON = BigDecimal.valueOf(-180);
    private static final BigDecimal MAX_LON = BigDecimal.valueOf(180);

    private final PostRepository postRepository;
    private final EnumUtil enumUtil;

    /**
     * 게시물 검색을 제공 하는 메서드
     * searchType에 따라서 적절한 메서드를 호출한다.
     * @param searchType 검색 유형
     * @param query searchType이 text일 때 사용하는 검색 쿼리
     * @param minLatStr searchType이 bounds일 때 사용하는 최소 위도
     * @param minLonStr searchType이 bounds일 때 사용하는 최소 경도
     * @param maxLatStr searchType이 bounds일 때 사용하는 최대 위도
     * @param maxLonStr searchType이 bounds일 때 사용하는 최대 경도
     * @param postTypeStr 게시물 유형에 따라서 거를 수 있게 하는 파라미터
     * @param size 페이지네이션의 size
     * @param page 페이지네이션의 page
     * @return 검색된 게시물 리스트와 게시글이 더 있는지 여부를 담은 DTO
     */
    public PostListResponseDto searchPosts(
            final String searchType,
            final String query,
            final String minLatStr,
            final String minLonStr,
            final String maxLatStr,
            final String maxLonStr,
            final String postTypeStr,
            final Integer size,
            final Integer page
    ){

        // 1) postTypeStr이 존재하면 검사
        final PostType postType;
        if(postTypeStr != null && !postTypeStr.isEmpty()) {
            //Enum 검사
            postType = enumUtil.toEnum(PostType.class, postTypeStr)
                    .orElseThrow(() -> new CustomException(ExceptionCode.INVALID_ENUM_VALUE));
        } else {
            postType = null;
        }

        // 2) size와 page 검사
        if(size == null || size <= 0 || size > 100) {
            throw new CustomException(ExceptionCode.INVALID_PAGING_PARAMETER);
        }
        if(page == null || page < 0) {
            throw new CustomException(ExceptionCode.INVALID_PAGING_PARAMETER);
        }

        // 3) 페이지네이션을 위한 객체 생성
        final Pageable pageable = PageRequest.of(page, size);

        // 4) searchType에 따라서 분기 처리
        final PostListResponseDto postListResponseDto;
        //텍스트 검색
        if("text".equalsIgnoreCase(searchType)){
            postListResponseDto = getPostsByText(query, postType, pageable);
        }
        //범위 검색
        else if("bounds".equalsIgnoreCase(searchType)){
            postListResponseDto = getPostsByBound(
                    minLatStr,
                    minLonStr,
                    maxLatStr,
                    maxLonStr,
                    postType,
                    pageable
            );
        }
        //해당하는 searchType이 없는 경우
        else {
            postListResponseDto = getLatestPosts(postType, pageable);
        }

        return postListResponseDto;
    }

    /**
     * 게시글을 최신순으로 가져오는 메서드
     * @param postType 게시물 유형에 따라서 거를 수 있게 하는 파라미터
     * @param pageable 페이지네이션
     * @return 검색된 게시물 리스트와 게시글이 더 있는지 여부를 담은 DTO
     */
    private PostListResponseDto getLatestPosts(
            final PostType postType,
            final Pageable pageable
    ) {
        final Slice<Post> posts;
        //postType 존재하면 postType으로 거르기
        if(postType == null){
            posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            posts = postRepository.findAllByTypeOrderByCreatedAtDesc(postType, pageable);
        }

        return new PostListResponseDto(posts.getContent(), posts.hasNext());
    }

    /**
     * 게시글에서 특정 글자 검색을 하는 메서드
     * 검색어의 최소 길이는 3자에서 100자까지다.
     * 검색어에는 특수문자가 포함되지 않는다.
     * @param rawQuery 검색할 검색어
     * @param postType 게시물 유형에 따라서 거를 수 있게 하는 파라미터
     * @param pageable 페이지네이션
     * @return 검색된 게시물 리스트와 게시글이 더 있는지 여부를 담은 DTO
     */
    private PostListResponseDto getPostsByText(
            final String rawQuery,
            final PostType postType,
            final Pageable pageable
    ){
        // 1) 쿼리가 빈 경우 예외 처리
        if(rawQuery == null || rawQuery.isEmpty()) {
            throw new CustomException(ExceptionCode.INVALID_QUERY);
        }

        // 2) 쿼리 정규화
        final String normalizedQuery = rawQuery
                .trim() //앞뒤 공백 제거
                .replaceAll("\\s+", " ") //공백 여러개를 하나로
                .replaceAll("[^0-9a-zA-Z가-힣\\s]", ""); //특수문자 제거 (한글, 영어, 숫자, 공백만 남김)

        // 3) 쿼리 길이 검증
        if(
                normalizedQuery.isBlank()
                || normalizedQuery.trim().length() < FULL_TEXT_SEARCH_MIN_LEN
                || normalizedQuery.trim().length() > FULL_TEXT_SEARCH_MAX_LEN
        ){
            throw new CustomException(ExceptionCode.INVALID_QUERY);
        }

        // 4) 접두사 검색을 위한 와일드 카드 추가
        final String query = normalizedQuery + "*";

        // 5) full-text 검색
        final Slice<Post> posts;
        //postType 존재하면 postType으로 거르기
        if(postType == null) {
            posts = postRepository.searchByFullText(query, pageable);
        } else {
            posts = postRepository.searchByFullTextAndType(query, postType.name(), pageable);
        }

        return new PostListResponseDto(posts.getContent(), posts.hasNext());
    }

    /**
     * 위도 경도의 최대 최솟값 기반으로 게시물을 검색하는 메서드
     * @param minLatStr 최소 위도
     * @param minLonStr 최소 경도
     * @param maxLatStr 최대 위도
     * @param maxLonStr 최대 경도
     * @param postType 게시물 유형에 따라서 거를 수 있게 하는 파라미터
     * @param pageable 페이지네이션
     * @return 검색된 게시물 리스트와 게시글이 더 있는지 여부를 담은 DTO
     */
    private PostListResponseDto getPostsByBound(
            final String minLatStr,
            final String minLonStr,
            final String maxLatStr,
            final String maxLonStr,
            final PostType postType,
            final Pageable pageable
    ){
        // 1) 위도 경도 BigDecimal로 변환 및 검증
        final BigDecimal minLat;
        final BigDecimal minLon;
        final BigDecimal maxLat;
        final BigDecimal maxLon;
        try {
            //문자열에서 BigDecimal로 변환
            minLat = new BigDecimal(minLatStr.trim());
            minLon = new BigDecimal(minLonStr.trim());
            maxLat = new BigDecimal(maxLatStr.trim());
            maxLon = new BigDecimal(maxLonStr.trim());

            //범위 검증
            if(minLat.compareTo(MIN_LAT) < 0 || maxLat.compareTo(MAX_LAT) > 0
                    || minLon.compareTo(MIN_LON) < 0 || maxLon.compareTo(MAX_LON) > 0
            ) {
                throw new CustomException(ExceptionCode.INVALID_REQUEST);
            }

            //순서 검증
            if(minLat.compareTo(maxLat) > 0 || minLon.compareTo(maxLon) > 0) {
                throw new CustomException(ExceptionCode.INVALID_REQUEST);
            }

        } catch (NullPointerException | NumberFormatException ex) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 위치 기반으로 조회
        final Slice<Post> posts;
        //postType 존재하면 postType으로 거르기
        if(postType == null) {
            posts = postRepository.findNearbyByBoundingBox(
                    minLat,
                    minLon,
                    maxLat,
                    maxLon,
                    pageable
            );
        } else {
            posts = postRepository.findNearbyByBoundingBoxAndType(
                    minLat,
                    minLon,
                    maxLat,
                    maxLon,
                    postType,
                    pageable
            );
        }

        return new PostListResponseDto(posts.getContent(), posts.hasNext());
    }
}
