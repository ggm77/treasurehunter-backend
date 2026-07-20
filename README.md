# TreasureHunter Backend

분실물 찾기에 게이미피케이션 요소를 도입하여 보물찾기처럼 즐거운 경험을 제공하고, 안전한 보상 시스템을 구축하는 **TreasureHunter** 서비스의 백엔드 리포지토리입니다.

유실물 습득을 '퀘스트(Quest)', 습득자를 '헌터(Hunter)'로 재정의하여 보상과 재미를 통해 자발적인 회수를 유도하는 위치기반(LBS) 분실물 매칭 플랫폼입니다.

## 💡 문제 인식

- 유실물 발생 건수는 초연결 사회 진입과 함께 연평균 두 자릿수 성장률로 증가하고 있으나, 처리 방식은 여전히 오프라인 방문·텍스트 게시판 위주에 머물러 있어 실제 반환율이 낮습니다.
- 분실물의 상당수는 분실 직후 1시간 내외의 '골든타임'에 현장 인근에 머물러 있지만, 기존 시스템(습득 → 기관 보관 → 경찰서 이송 → 데이터 등록)은 여기서 평균 3~5일의 물리적 지연이 발생해 실시간 위치 정보를 활용하지 못합니다.
- 습득자 입장에서는 반환에 따르는 시간·법적 리스크(점유이탈물횡령죄 우려 등)에 비해 보상 절차가 까다로워, 습득물을 보고도 지나치는 '방관자 효과'가 발생합니다.

## ✨ 서비스 차별성

| 구분 | LOST112 (공공) | 커뮤니티 게시판 | **TreasureHunter** |
|---|---|---|---|
| 신뢰도 | 높음 | 낮음 (사례금 미지급, 안전 문제) | 높음 (신원 인증 + 평판 시스템) |
| 반응 속도 | 느림 (3~5일 행정 지연) | 빠르지만 휘발성 | **실시간 LBS 매칭** |
| 회수 유인 | 약함 | 개인 협의 | **게이미피케이션 보상 체계** |

넛지(Nudge) 기반으로 습득 행위를 '귀찮은 일'에서 '이득이 되는 퀘스트'로 인식을 전환시키고, 전 국민의 스마트폰을 분산 센서처럼 활용하는 초연결 인프라 기반 집단지성 네트워크를 지향합니다.

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
