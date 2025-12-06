package com.treasurehunter.treasurehunter.domain.user.repository;

import com.treasurehunter.treasurehunter.domain.user.dto.UserFoundCountDto;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname);
    boolean existsById(Long id);
    List<User> findTop100ByOrderByPointDesc();
    List<User> findTop100ByOrderByReturnedItemsCountDesc();

    @Query("""
        SELECT u
        FROM User u
        LEFT JOIN Post p ON p.author = u AND p.type = 'FOUND'
        GROUP BY u
        ORDER BY COUNT(p) DESC
    """)
    List<User> findTopFindsUsers(Pageable pageable);

    @Query("""
        SELECT u.id AS userId,
               COUNT(p) AS foundCount
        FROM User u
        LEFT JOIN Post p
            ON p.author.id = u.id AND p.type = 'FOUND'
        WHERE u.id IN :userIds
        GROUP BY u.id
    """)
    List<UserFoundCountDto> findFoundCount(@Param("userIds") List<Long> userIds);
}
