package com.treasurehunter.treasurehunter.domain.post.service.list;

import com.treasurehunter.treasurehunter.domain.post.dto.PostListItemResponseDto;
import com.treasurehunter.treasurehunter.domain.post.dto.list.PostListResponseDto;
import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.post.entity.PostType;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.util.EnumUtil;
import com.treasurehunter.treasurehunter.global.util.LatLonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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
    private static final BigDecimal KM_PER_DEGREE_LAT = BigDecimal.valueOf(111.32);

    private final PostRepository postRepository;
    private final EnumUtil enumUtil;
    private final LatLonUtil latLonUtil;

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
            final String latStr,
            final String lonStr,
            final String minLatStr,
            final String minLonStr,
            final String maxLatStr,
            final String maxLonStr,
            final Integer maxDistance,
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
            postListResponseDto = getPostsByText(query, postType, latStr, lonStr, pageable);
        }
        //범위 검색
        else if("bounds".equalsIgnoreCase(searchType)){
            postListResponseDto = getPostsByBound(
                    minLatStr,
                    minLonStr,
                    maxLatStr,
                    maxLonStr,
                    postType,
                    latStr,
                    lonStr,
                    pageable
            );
        }
        //거리순 검색
        else if ("distance".equalsIgnoreCase(searchType)){
            postListResponseDto = getPostsByDistance(
                    latStr,
                    lonStr,
                    maxDistance,
                    postType,
                    pageable
            );
        }
        //해당하는 searchType이 없는 경우
        else {
            postListResponseDto = getLatestPosts(postType, latStr, lonStr, pageable);
        }

        return postListResponseDto;
    }

    /**
     * 게시글을 최신순으로 가져오는 메서드
     * @param postType 게시물 유형에 따라서 거를 수 있게 하는 파라미터
     * @param latStr 거리 계산용 클라이언트의 위도
     * @param lonStr 거리 게산용 클라이언트의 경도
     * @param pageable 페이지네이션
     * @return 검색된 게시물 리스트와 게시글이 더 있는지 여부를 담은 DTO
     */
    private PostListResponseDto getLatestPosts(
            final PostType postType,
            final String latStr,
            final String lonStr,
            final Pageable pageable
    ) {

        // 위도 경도 변환
        final BigDecimal lat = toBigDecimal(latStr);
        final BigDecimal lon = toBigDecimal(lonStr);

        final Slice<Post> posts;
        //postType 존재하면 postType으로 거르기
        if(postType == null){
            posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            posts = postRepository.findAllByTypeOrderByCreatedAtDesc(postType, pageable);
        }

        final List<PostListItemResponseDto> postListItems = mapToListItemDto(posts, lat, lon);

        return new PostListResponseDto(
                postListItems,
                posts.hasNext(),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    /**
     * 게시글에서 특정 글자 검색을 하는 메서드
     * 검색어의 최소 길이는 3자에서 100자까지다.
     * 검색어에는 특수문자가 포함되지 않는다.
     * @param rawQuery 검색할 검색어
     * @param postType 게시물 유형에 따라서 거를 수 있게 하는 파라미터
     * @param latStr 거리 계산용 클라이언트의 위도
     * @param lonStr 거리 게산용 클라이언트의 경도
     * @param pageable 페이지네이션
     * @return 검색된 게시물 리스트와 게시글이 더 있는지 여부를 담은 DTO
     */
    private PostListResponseDto getPostsByText(
            final String rawQuery,
            final PostType postType,
            final String latStr,
            final String lonStr,
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

        // 6) DTO에 맞춰서 거리 계산
        final BigDecimal lat = toBigDecimal(latStr);
        final BigDecimal lon = toBigDecimal(lonStr);

        final List<PostListItemResponseDto> postListItems = mapToListItemDto(posts, lat, lon);

        return new PostListResponseDto(
                postListItems,
                posts.hasNext(),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    /**
     * 위도 경도의 최대 최솟값 기반으로 게시물을 검색하는 메서드
     * @param minLatStr 최소 위도
     * @param minLonStr 최소 경도
     * @param maxLatStr 최대 위도
     * @param maxLonStr 최대 경도
     * @param postType 게시물 유형에 따라서 거를 수 있게 하는 파라미터
     * @param latStr 거리 계산용 클라이언트의 위도
     * @param lonStr 거리 게산용 클라이언트의 경도
     * @param pageable 페이지네이션
     * @return 검색된 게시물 리스트와 게시글이 더 있는지 여부를 담은 DTO
     */
    private PostListResponseDto getPostsByBound(
            final String minLatStr,
            final String minLonStr,
            final String maxLatStr,
            final String maxLonStr,
            final PostType postType,
            final String latStr,
            final String lonStr,
            final Pageable pageable
    ){
        // 1) 위도 경도 BigDecimal로 변환 및 검증
        final BigDecimal minLat = toBigDecimal(minLatStr);
        final BigDecimal minLon = toBigDecimal(minLonStr);
        final BigDecimal maxLat = toBigDecimal(maxLatStr);
        final BigDecimal maxLon = toBigDecimal(maxLonStr);
        final BigDecimal lat = toBigDecimal(latStr);
        final BigDecimal lon = toBigDecimal(lonStr);

        // null 방지
        if(minLat == null || maxLat == null || minLon == null || maxLon == null) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

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

        // 3) DTO에 맞춰서 거리 계산
        final List<PostListItemResponseDto> postListItems = mapToListItemDto(posts, lat, lon);

        return new PostListResponseDto(
                postListItems,
                posts.hasNext(),
                minLatStr,
                minLonStr,
                maxLatStr,
                maxLonStr,
                null,
                null
        );
    }

    /**
     * 게시글을 가까운 순으로 조회하는 메서드
     * maxDistance를 통해서 범위를 지정할 수 있다.
     * @param latStr 중심 위도 문자열
     * @param lonStr 중심 경도 문자열
     * @param maxDistance 최대 거리 ( Km )
     * @param postType 게시물 유형에 따라서 거를 수 있게 하는 파라미터
     * @param pageable 페이지네이션
     * @return 검색된 게시물 리스트와 게시글이 더 있는지 여부를 담은 DTO
     */
    private PostListResponseDto getPostsByDistance(
            final String latStr,
            final String lonStr,
            final Integer maxDistance,
            final PostType postType,
            final Pageable pageable
    ){

        // 1) BigDecimal로 변환
        final BigDecimal lat = toBigDecimal(latStr);
        final BigDecimal lon = toBigDecimal(lonStr);

        // 2) NULL 검사
        if (maxDistance == null || lat == null || lon == null) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 3) 최대 거리 검사 ( 1~50Km )
        if(maxDistance > 50 || maxDistance < 1)  {
            throw new CustomException(ExceptionCode.INVALID_MAX_DISTANCE);
        }

        //위도 경도 검증
        if(
                lat.compareTo(MIN_LAT) < 0
                || lat.compareTo(MAX_LAT) > 0
                || lon.compareTo(MIN_LON) < 0
                || lon.compareTo(MAX_LON) > 0
        ){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 4) 최대 거리로 최대 최소 위도 계산
        final BigDecimal latDiff = BigDecimal.valueOf(maxDistance)
                .divide(KM_PER_DEGREE_LAT, 10, RoundingMode.HALF_UP);

        final BigDecimal minLat = lat.subtract(latDiff);
        final BigDecimal maxLat = lat.add(latDiff);

        // 5) 최대 거리로 최대 최소 경도 계산
        final BigDecimal minLon;
        final BigDecimal maxLon;

        //분모 0 방지
        if(lon.abs().compareTo(BigDecimal.valueOf(89.9)) > 0) {
            minLon = MIN_LON;
            maxLon = MAX_LON;
        } else {
            final double cosVal = Math.cos(Math.toRadians(lat.doubleValue()));

            final BigDecimal kmPerDegreeLat = KM_PER_DEGREE_LAT.multiply(BigDecimal.valueOf(cosVal));
            final BigDecimal lonDiff = BigDecimal.valueOf(maxDistance)
                    .divide(kmPerDegreeLat, 10, RoundingMode.HALF_UP);

            minLon = lon.subtract(lonDiff);
            maxLon = lon.add(lonDiff);
        }


        // 6) 거리순으로 조회
        final Slice<Post> posts;
        //postType 존재하면 postType으로 거르기
        if(postType == null) {
            posts = postRepository.findNearestByLatLon(
                    lat,
                    lon,
                    minLat,
                    minLon,
                    maxLat,
                    maxLon,
                    pageable
            );
        } else {
            posts = postRepository.findNearestByLatLonAndType(
                    lat,
                    lon,
                    minLat,
                    minLon,
                    maxLat,
                    maxLon,
                    postType.name(),
                    pageable
            );
        }

        // 7) DTO에 맞춰서 거리 계산
        final List<PostListItemResponseDto> postListItems = mapToListItemDto(posts, lat, lon);

        return new PostListResponseDto(
                postListItems,
                posts.hasNext(),
                null,
                null,
                null,
                null,
                latStr,
                lonStr
        );
    }

    /**
     * 문자열로 들어온 값을 BigDecimal로 변환하는 메서드
     * null이나 빈 문자열 들어오면 null을 반환
     * @param bigDecimalString 문자열로 된 BigDecimal
     * @return BigDecimal
     */
    private BigDecimal toBigDecimal(final String bigDecimalString) {
        try {
            if(bigDecimalString == null || bigDecimalString.isEmpty()) {
                return null;
            }

            return new BigDecimal(bigDecimalString.trim());
        } catch (NumberFormatException ex) {
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }
    }

    /**
     * Slice 형태로 들어온 게시글 리스트를 PostListItemResponseDto로 바꾸는 메서드
     * @param posts Slice 형태의 게시글
     * @param lat 거리 계산용 위도
     * @param lon 거리 계산용 경도
     * @return 리스트에 담긴 PostListItemResponseDto로
     */
    private List<PostListItemResponseDto> mapToListItemDto(
            final Slice<Post> posts,
            final BigDecimal lat,
            final BigDecimal lon
    ) {
        return posts.getContent().stream()
                .map(
                        post -> PostListItemResponseDto.builder()
                                .post(post)
                                .distance(latLonUtil.latLonDistance(post.getLat(), post.getLon(), lat, lon))
                                .build()
                )
                .toList();
    }
}
