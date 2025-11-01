package com.treasurehunter.treasurehunter.domain.post.service;

import com.treasurehunter.treasurehunter.domain.post.entity.ItemCategory;
import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.post.entity.PostType;
import com.treasurehunter.treasurehunter.domain.post.entity.image.PostImage;
import com.treasurehunter.treasurehunter.domain.post.dto.PostRequestDto;
import com.treasurehunter.treasurehunter.domain.post.dto.PostResponseDto;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import com.treasurehunter.treasurehunter.domain.post.repository.image.PostImageRepository;
import com.treasurehunter.treasurehunter.domain.post.repository.like.PostLikeRepository;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.event.domain.EventPublisher;
import com.treasurehunter.treasurehunter.global.event.model.PostCreateEvent;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.util.EnumUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostLikeRepository postLikeRepository;
    private final EnumUtil enumUtil;
    private final EventPublisher eventPublisher;

    //entityManager.flush()를 사용하기 위함
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 게시글 등록하는 메서드
     * @param postRequestDto 게시글 요청 DTO
     * @param userId 유저 id
     * @return 등록된 게시글 PostResponseDto
     */
    @Transactional
    public PostResponseDto createPost(
            final PostRequestDto postRequestDto,
            final Long userId
    ){
        //null 값 검사는 dto에서

        // 1) 게시물 유형과 카테고리 검증 및 변환
        final PostType postType = enumUtil.toEnum(PostType.class, postRequestDto.getType())
                .orElseThrow(() -> new CustomException(ExceptionCode.INVALID_REQUEST));
        final ItemCategory itemCategory = enumUtil.toEnum(ItemCategory.class, postRequestDto.getItemCategory())
                .orElseThrow(() -> new CustomException(ExceptionCode.INVALID_REQUEST));

        // 2) 게시글과 연관 관계 가질 유저 정보 가져오기
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 3) 포인트 잔량 확인 및 소비
        try{
            //포인트 부족시 IllegalArgumentException 발생
            user.consumePoint(postRequestDto.getSetPoint());
        } catch (IllegalArgumentException ex){
            throw new CustomException(ExceptionCode.POINT_NOT_ENOUGH);
        }

        // 4) Post 엔티티 만들기
        final Post post = Post.builder()
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .type(postType)
                .author(user)
                .images(new ArrayList<>())
                .setPoint(postRequestDto.getSetPoint())
                .itemCategory(itemCategory)
                .lat(postRequestDto.getLat())
                .lon(postRequestDto.getLon())
                .lostAt(postRequestDto.getLostAt())
                .isAnonymous(postRequestDto.getIsAnonymous())
                .isCompleted(false)
                .build();

        // 5) 이미지 연관 관계 설정
        //리스트가 null인 경우, 요소가 null인경우, 빈경우 예외처리
        final List<String> validUrls = Optional.ofNullable(postRequestDto.getImages())
                .orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        //null제거후 빈 리스트 대응
        if(!validUrls.isEmpty()) {
            for(int i = 0; i < validUrls.size(); i++) {
                final PostImage img = PostImage.builder()
                        .url(validUrls.get(i))
                        .imageIndex(i)
                        .build();
                img.updatePost(post);
                //post 엔티티에 집어 넣기
                post.getImages().add(img);
            }
        }

        // 6) 게시글 DB에 저장
        final Post savedPost = postRepository.save(post);

        // 7) 배지 수여용 이벤트 발생 시키기
        eventPublisher.publish(
                PostCreateEvent.builder()
                        .userId(userId)
                        .postId(savedPost.getId())
                        .build()
        );

        return new PostResponseDto(savedPost);
    }

    /**
     * 게시글 하나 조회하는 메서드
     * @param postId 게시글 id
     * @return 조회된 게시글 PostResponseDto
     */
    public PostResponseDto getPost(final Long postId){

        final Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionCode.POST_NOT_EXIST));

        return new PostResponseDto(post);
    }

    /**
     * 등록된 게시글 업데이트 하는 메서드
     * PostRequestDto의 images가 []이라면 게시글의 사진을 모두 삭제함
     * @param postRequestDto 게시글 요청 DTO
     * @param userId 유저 id
     * @return 변경된 게시글 PostResponseDto
     */
    @Transactional
    public PostResponseDto updatePost(
            final Long postId,
            final PostRequestDto postRequestDto,
            final Long userId
    ){

        // 1) 게시글 조회
        final Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionCode.POST_NOT_EXIST));

        // 2) 게시글이 완료 처리 되어있는 경우 처리
        if(post.isCompleted()){
            throw new CustomException(ExceptionCode.POST_IS_COMPLETED);
        }

        // 3) 자신의 게시글인지 검사
        if(!userId.equals(post.getAuthor().getId())){
            throw new CustomException(ExceptionCode.PERMISSION_DENIED);
        }

        // 4) 게시물 유형과 카테고리 검증 및 변환 (비어있다면 null)
        final PostType postType;
        final ItemCategory itemCategory;
        if(postRequestDto.getType() != null && !postRequestDto.getType().isEmpty()){
            postType = enumUtil.toEnum(PostType.class, postRequestDto.getType())
                    .orElseThrow(() -> new CustomException(ExceptionCode.INVALID_REQUEST));
        } else {
            postType = null;
        }
        if(postRequestDto.getItemCategory() != null && !postRequestDto.getItemCategory().isEmpty()) {
            itemCategory = enumUtil.toEnum(ItemCategory.class, postRequestDto.getItemCategory())
                    .orElseThrow(() -> new CustomException(ExceptionCode.INVALID_REQUEST));
        } else {
            itemCategory = null;
        }

        // 5) 비어있지 않으면 변경
        // 포인트 수정
        if(postRequestDto.getSetPoint() != null){
            final User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

            final int oldPoint = post.getSetPoint();
            final int newPoint = postRequestDto.getSetPoint();
            // 포인트 부족시
            if(user.getPoint() + oldPoint - newPoint < 0){
                throw new CustomException(ExceptionCode.POINT_NOT_ENOUGH);
            }
            //게시글 포인트 업데이트
            post.updateSetPoint(newPoint);
            //유저 포인트 업데이트
            user.adjustPointForPostUpdate(oldPoint, newPoint);
        }

        // 이미지 수정
        // null이면 무시, []이라면 사진 삭제, 요소가 존재하면 변경
        if(postRequestDto.getImages() != null){
            //연관 되어있는 PostImage 전량 삭제
            post.getImages().clear();

            //이미지 새로 저장
            //리스트가 null인 경우, 요소가 null인경우, 빈경우 예외처리
            final List<String> validUrls = Optional.ofNullable(postRequestDto.getImages())
                    .orElseGet(List::of).stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();

            //null제거후 빈 리스트 대응
            if(!validUrls.isEmpty()) {
                for(int i = 0; i < validUrls.size(); i++) {
                    final PostImage img = PostImage.builder()
                            .url(validUrls.get(i))
                            .imageIndex(i)
                            .build();
                    img.updatePost(post);
                    //post 엔티티에 집어 넣기
                    post.getImages().add(img);
                }
            }
        }

        // 게시물 유형 수정
        if(postType != null){
            post.updateType(postType);
        }
        // 카테고리 수정
        if(itemCategory != null){
            post.updateItemCategory(itemCategory);
        }

        // 나머지 정보들 수정
        Optional.ofNullable(postRequestDto.getTitle()).filter(s -> !s.isBlank()).ifPresent(post::updateTitle);
        Optional.ofNullable(postRequestDto.getContent()).filter(s -> !s.isBlank()).ifPresent(post::updateContent);
        Optional.ofNullable(postRequestDto.getLat()).ifPresent(post::updateLat);
        Optional.ofNullable(postRequestDto.getLon()).ifPresent(post::updateLon);
        Optional.ofNullable(postRequestDto.getLostAt()).ifPresent(post::updateLostAt);
        Optional.ofNullable(postRequestDto.getIsAnonymous()).ifPresent(post::updateIsAnonymous);

        return new PostResponseDto(post);
    }

    /**
     * 게시글 삭제하는 메서드
     * 게시글이 완료된 상태가 아니고 포인트가 걸려있으면 유저에게 환급해줌
     * @param postId 게시글 id
     * @param userId 유저 id
     */
    @Transactional
    public void deletePost(final Long postId, final Long userId){

        // 1) 게시글 및 유저 조회
        final Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionCode.POST_NOT_EXIST));

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) 자신의 게시글인지 확인
        if(!post.getAuthor().getId().equals(user.getId())){
            throw new CustomException(ExceptionCode.PERMISSION_DENIED);
        }

        // 3) 왼료된 게시글이 아닐 때 게시물에 설정 되어있던 포인트 회수
        if(!post.isCompleted()){
            user.addPoint(post.getSetPoint());
        }

        // 4) 자식 정리

        //리뷰 존재하면 정리(리뷰는 삭제 X)
        if(post.getReview() != null){
            post.getReview().detachPost();
        }

        // 5) 게시글 삭제
        postRepository.delete(post);
    }
}
