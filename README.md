# 🧡 온기 (ONGI) - Backend

> **AI 기반 사진 정리 및 커뮤니티 서비스**  
> 사진 속 기억을 정리하고, 공유하며, 추억을 다시 꺼내보는 공간
![7](https://github.com/user-attachments/assets/7095b925-50e7-445d-864a-53b289a5880b)
![8](https://github.com/user-attachments/assets/d0f4cc59-e295-45cd-913d-18f7f7da021b)
![9](https://github.com/user-attachments/assets/2bc8f4a6-19d9-4640-bf1a-cded70b42793)
![10](https://github.com/user-attachments/assets/905acf4e-31a9-41ec-be7d-e6d19762b1b0)

---
## 카카오테크 부트캠프 파이널프로젝트 최우수상(1등) 수상
![20250801_174448](https://github.com/user-attachments/assets/4ac0ba86-d8db-4cc7-adf2-884048b20f7d)

---

## 🛠 Tech Stack

| Category     | Stack                                                              |
|--------------|--------------------------------------------------------------------|
| Language     | Java                                                            |
| Framework    | Spring Boot, Spring Data JPA,                  |
| DB & Cache   | MySQL, Redis                                      |
| Message Queue| Apache Kafka                                       |
| Build Tool   | Gradle                                                             |
| Others       | Flyway       |

---

## 📂 프로젝트 구조
```
main
├── java/ongi/ongibe
│   ├── domain                # 주요 도메인별 패키지 (비즈니스 로직 중심)
│   │   ├── ai                # AI 분석 단계별 Kafka Producer, Consumer 및 API 기반 통신 모듈
│   │   │   ├── aiInterface   # AI 연동을 위한 서비스 인터페이스 모음
│   │   │   ├── consumer      # Kafka Consumer (각 AI 분석 단계별 메시지 처리)
│   │   │   ├── controller    # AI 헬스체크용 컨트롤러
│   │   │   ├── dto           # AI 요청/응답 DTO
│   │   │   ├── entity        # AI 분석 상태 저장 엔티티
│   │   │   ├── event         # AI 분석 완료 시 발행되는 이벤트 객체
│   │   │   ├── exception     # Kafka 재시도 등 커스텀 예외 정의
│   │   │   ├── kafka         # Kafka 관련 유틸 서비스
│   │   │   ├── producer      # Kafka Producer
│   │   │   ├── repository    # AI 상태 저장용 Repository
│   │   │   └── service       # AI HTTP 요청 로직 구현체
│   │   ├── album             # 앨범 도메인
│   │   │   ├── controller    # 앨범, 댓글, 좋아요 API 엔드포인트
│   │   │   ├── dto           # 앨범 관련 요청/응답 DTO
│   │   │   ├── entity        # 앨범, 사진, 클러스터 등 도메인 엔티티
│   │   │   ├── event         # 앨범 관련 이벤트
│   │   │   ├── exception     # 앨범 도메인 예외 처리
│   │   │   ├── repository    # 앨범 관련 데이터 접근 레이어
│   │   │   ├── schedule      # 좋아요 동기화 등 정기 작업 Scheduler
│   │   │   └── service       # 앨범 도메인 서비스 로직
│   │   ├── auth              # 인증/인가 모듈
│   │   │   ├── config        # Kakao API 설정 클래스
│   │   │   ├── controller    # 로그인, 토큰 재발급 API
│   │   │   ├── dto           # 로그인 관련 요청/응답 DTO
│   │   │   ├── entity        # OAuthToken 등 엔티티
│   │   │   ├── repository    # OAuthToken 저장소
│   │   │   └── service       # 인증 로직 처리
│   │   ├── notification      # 알림 도메인
│   │   │   ├── controller    # 알림 조회 API
│   │   │   ├── dto           # 알림 응답 DTO
│   │   │   ├── entity        # 알림 테이블
│   │   │   ├── event         # 앨범 초대/생성 이벤트로부터 알림 생성
│   │   │   ├── repository    # 알림 저장소
│   │   │   └── service       # 알림 서비스
│   │   ├── place             # 장소 도메인
│   │   │   ├── entity        # 장소 엔티티
│   │   │   └── service       # 장소 매핑 서비스
│   │   └── user              # 사용자 도메인
│   │       ├── controller    # 사용자 정보, 통계 API
│   │       ├── dto           # 사용자 관련 DTO
│   │       ├── entity        # User 엔티티
│   │       ├── exception     # 사용자 예외
│   │       ├── repository    # 사용자 저장소
│   │       └── service       # 사용자 서비스
│   ├── cache                 # 캐시 관련 모듈
│   │   ├── album             # 앨범 캐싱 서비스
│   │   ├── event             # 캐시 관련 도메인 이벤트
│   │   └── user              # 사용자 캐싱 서비스
│   ├── global                # 공통 유틸 및 설정 모듈
│   │   ├── cache             # 공통 Redis 유틸
│   │   ├── config            # 공통 Config 클래스들
│   │   ├── eventlistener     # 전역 도메인 이벤트 리스너
│   │   ├── exception         # 인증/보안 등 전역 예외
│   │   │   └── handler       # 예외 핸들러
│   │   ├── kafka             # Kafka 리스너 설정
│   │   ├── s3                # S3 Presigned URL 발급 기능
│   │   │   └── dto           # Presigned URL 요청/응답 DTO
│   │   ├── security          # Spring Security 설정 및 필터
│   │   │   ├── config
│   │   │   ├── filter
│   │   │   └── util
│   │   └── util              # 공통 유틸 클래스 (DateUtil, JsonUtil 등)
│   ├── loadtest              # 성능 테스트용 임시 API
│   └── swagger               # Swagger용 응답 객체 정리
│       ├── album
│       ├── s3
│       └── user
└── resources
    ├── application-{dev,prod,test,local}.yml  # 환경별 설정 파일
    ├── db/migration                           # Flyway 기반 SQL 마이그레이션
    └── lua                                    # Redis Lua 스크립트 (좋아요/싫어요 처리)
```

---

## ✨ 주요 기능

### 📷 사진 업로드 & AI 분석
- S3 Presigned URL을 통해 이미지 업로드
- 업로드된 사진은 AI 서버와 연동하여 다음 정보 분석:
  - 중복 여부
  - 흔들림 여부
  - 미적 점수
  - 위치 메타데이터 → 행정구역 변환 (Kakao API)
  - 태그 추출 (카테고리 기반)

### 🏞 앨범
- AI 결과 기반 대표 사진 자동 선정
- 앨범 생성 시 장소별 사진 정리 및 통계 제공
- 무한스크롤 기반 앨범 조회 (썸네일, 각 앨범별 멤버 포함)

### 🤝 커뮤니티 & 초대
- 앨범 멤버 초대/탈퇴
- 댓글, 좋아요 기능
- 대표사진 기반 썸네일 제공

### 🚀 성능 최적화
- **Redis 캐시 적용** (Look-aside + Write-through 전략)
  - 메인페이지, 피드, 통계 캐싱
- **Kafka 비동기 처리**
  - AI 서버와의 통신을 Kafka 기반으로 비동기 처리하여 서버 부하 감소
- **DB 최적화**
  - 조회/통계용 쿼리 최적화 목적 인덱스 설정

---


## 🧾 ERD 요약

> 주요 테이블: `User`, `Album`, `Picture`, `Place`, `Feed`, `Comment`, `Like`, `FaceCluster` 등

(ERD 다이어그램 첨부 링크 또는 이미지)

---

## 📌 기타

- AI 서버 연동 문서: [`/docs/ai-api-spec.md`](./docs/ai-api-spec.md)
- API 명세서: [Swagger 링크 또는 Postman Collection]
- 트러블슈팅 모음: [`/docs/troubleshooting.md`](./docs/troubleshooting.md)

