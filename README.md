# TreasureHunter Backend

분실물 찾기에 게이미피케이션 요소를 도입하여 보물찾기처럼 즐거운 경험을 제공하고, 안전한 보상 시스템을 구축하는 **TreasureHunter** 서비스의 백엔드 리포지토리입니다.

## 🚀 주요 기능

- **위치 기반 서비스 (LBS)**: 사용자 위치 정보를 활용하여 주변 분실물 정보 제공 및 알림 서비스 지원.
- **실시간 채팅 시스템**: WebSocket을 통한 STOMP와 RabbitMQ를 사용하여 습득자와 주인 간의 1:1 인앱 채팅 구현.
- **익명 기능**: 익명으로 서비스를 이용 가능하도록 구현.
- **게이미피케이션**: 배지(Badge) 시스템 및 리더보드(Leaderboard) 기능을 통해 사용자 참여도 증대.
- **보안 및 인증**:
    - JWT 및 Spring Security 기반의 보안 아키텍처.
    - 애플 로그인 및 OAuth2 기반 소셜 로그인 지원.
- **알림**: FCM을 통한 알림 기능 지원

## 🛠 기술 스택
- **Language**: Java 25
- **Framework**: Spring Boot 3.5.6
- **Build Tool**: Gradle
- **Database**: MariaDB 12.0.2, Redis 8.2.2
- **Message Queue**: RabbitMQ 4.2.0
- **ORM**: Spring Data JPA 3.5.6
- **Security**: Spring Security, JWT, OAuth2

## 🏗 프로젝트 구조

본 프로젝트는 Package‑by‑Feature + Layered Architecture로 설계 되었습니다.

```text
src/main/java/com/treasurehunter/treasurehunter/
├── domain/            # 비즈니스 로직 (User, Post, Chat, Badge, Leaderboard 등)
├── global/            # 공통 설정 (Auth, Config, Exception, Util 등) 
└── TreasurehunterApplication.java
```
