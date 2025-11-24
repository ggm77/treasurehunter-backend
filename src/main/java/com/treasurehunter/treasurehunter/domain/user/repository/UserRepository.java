package com.treasurehunter.treasurehunter.domain.user.repository;

import com.treasurehunter.treasurehunter.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname);
    boolean existsById(Long id);
    List<User> findTop100ByOrderByPointDesc();
    List<User> findTop100ByOrderByReturnedItemsCountDesc();

    @Query("""
        SELECT u AS user, COUNT(p) AS foundCount
        FROM User u
        JOIN Post p ON p.author = u
        WHERE p.type = 'FOUND'
        GROUP BY u
        ORDER BY COUNT(p) DESC
    """)
    List<User> findTopFindsUsers(Pageable pageable);
}
