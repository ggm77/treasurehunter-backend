package com.treasurehunter.treasurehunter.domain.notification.service.event;

import com.treasurehunter.treasurehunter.domain.notification.dto.NotificationDto;
import com.treasurehunter.treasurehunter.domain.notification.entity.token.NotificationToken;
import com.treasurehunter.treasurehunter.domain.notification.repository.token.NotificationTokenRepository;
import com.treasurehunter.treasurehunter.domain.notification.service.NotificationService;
import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import com.treasurehunter.treasurehunter.domain.post.repository.PostRepository;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.event.model.PostCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationTokenRepository notificationTokenRepository;
    private final NotificationService notificationService;

    public void sendLocalPostNotification(final PostCreateEvent postCreateEvent) {

        // 1) 게시글 조회
        final Post post = postRepository.findById(postCreateEvent.getPostId())
                .orElse(null);

        // 2) 게시글 못찾은 경우 로그 찍고 리턴
        if(post == null) {
            log.warn("Post not exist. postId={}", postCreateEvent.getPostId());
            return;
        }

        // 3) 근처 유저 조회
        final BigDecimal lat = post.getLat();
        final BigDecimal lon = post.getLon();
        final BigDecimal minLat = lat.subtract(BigDecimal.valueOf(0.045));
        final BigDecimal maxLat = lat.add(BigDecimal.valueOf(0.045));
        final BigDecimal minLon = lon.subtract(BigDecimal.valueOf(0.056));
        final BigDecimal maxLon = lon.add(BigDecimal.valueOf(0.056));
        final List<User> users = userRepository.findByLatBetweenAndLonBetween(
                minLat,
                maxLat,
                minLon,
                maxLon
        );

        // 4) 메세지 빌드
        final NotificationDto notificationForm = NotificationDto.builder()
                .title("근처에 게시물이 등록 되었어요!")
                .body("서비스에서 확인해 보세요")
                .url("https://treasurehunter.seohamin.com/home")
                .profileImage("https://treasurehunter.seohamin.com/api/v1/file/image?objectKey=6e/70/6e70fb140c9735ba56271064c5e900ba5998c8e5fc30610005691bfefbd8c482.png")
                .build();

        // 5) 유저별로 메세지 전송
        for (final User user : users) {
            final NotificationDto notificationDto = notificationForm;
            final Long userId = user.getId();
            notificationDto.setTargetUserId(user.getId());
            final List<String> tokens = notificationTokenRepository.findByUser_Id(userId).stream()
                    .map(NotificationToken::getToken)
                    .toList();
            // 6) 플랫폼별로 메세지 전송
            for (final String token : tokens) {
                notificationDto.setToken(token);
                notificationService.sendNotification(notificationDto);
            }
        }
    }
}
