package com.treasurehunter.treasurehunter.domain.post.repository;

import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.post.entity.PostType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PostRepository extends JpaRepository<Post, Long> {

    //최신순으로 게시글 조회
    Slice<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Slice<Post> findAllByTypeOrderByCreatedAtDesc(PostType postType, Pageable pageable);

    //텍스트 기반 게시글 검색
    @Query(value =
            """
                SELECT *
                FROM post
                WHERE MATCH(title, content)
                    AGAINST (:query IN BOOLEAN MODE)
                ORDER BY created_at DESC
            """,
            nativeQuery = true
    )
    Slice<Post> searchByFullText(
            @Param("query") String query,
            Pageable pageable
    );

    @Query(value =
            """
                SELECT *
                FROM post
                WHERE MATCH(title, content)
                    AGAINST (:query IN BOOLEAN MODE)
                            AND type = :postType
                ORDER BY created_at DESC
            """,
            nativeQuery = true
    )
    Slice<Post> searchByFullTextAndType(
            @Param("query") String query,
            @Param("postType") String postType,
            Pageable pageable
    );

    //위치기반 조회
    @Query("""
        SELECT p
        FROM Post p
        WHERE p.lat BETWEEN :minLat AND :maxLat
          AND p.lon BETWEEN :minLon AND :maxLon
          AND p.lat IS NOT NULL
          AND p.lon IS NOT NULL
        ORDER BY p.createdAt DESC
    """
    )
    Slice<Post> findNearbyByBoundingBox(
            @Param("minLat") BigDecimal minLat,
            @Param("minLon") BigDecimal minLon,
            @Param("maxLat") BigDecimal maxLat,
            @Param("maxLon") BigDecimal maxLon,
            Pageable pageable
    );

    @Query("""
        SELECT p
        FROM Post p
        WHERE p.lat BETWEEN :minLat AND :maxLat
          AND p.lon BETWEEN :minLon AND :maxLon
          AND p.type = :postType
          AND p.lat IS NOT NULL
          AND p.lon IS NOT NULL
        ORDER BY p.createdAt DESC
    """
    )
    Slice<Post> findNearbyByBoundingBoxAndType(
            @Param("minLat") BigDecimal minLat,
            @Param("minLon") BigDecimal minLon,
            @Param("maxLat") BigDecimal maxLat,
            @Param("maxLon") BigDecimal maxLon,
            @Param("postType") PostType postType,
            Pageable pageable
    );


    Long countByAuthorId(Long userId);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + :delta WHERE p.id = :postId")
    int addViewCount(@Param("postId") Long postId, @Param("delta") Long delta);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    int increaseLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId AND p.likeCount > 0")
    int decreaseLikeCount(@Param("postId") Long postId);
}
