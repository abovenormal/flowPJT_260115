
## 기술 스택

### Backend
- Java 17
- Spring Boot
- JPA
- WebSocket

### Database
- MySQL
- H2

### Frontend
- Thymeleaf
- Tailwind CSS
- jQUery
- SockJS + STOMP

## 프로젝트 구조

```
src/main/java/com/example/extensionCheck/
├── ExtensionCheckApplication.java    # 애플리케이션 진입점
├── api/
│   ├── Controller/
│   │   ├── ExtensionController.java  # REST API 컨트롤러
│   │   └── WebController.java        # 웹 페이지 컨트롤러
│   ├── Service/
│   │   └── ExtensionService.java     # 비즈니스 로직
│   ├── exception/
│   │   ├── ExtensionErrorCode.java   # 에러 코드 정의
│   │   ├── ExtensionException.java   # 커스텀 예외
│   │   └── GlobalExceptionHandler.java
│   ├── request/
│   │   └── FixedBatchRequest.java    # 배치 요청 DTO
│   ├── response/
│   │   ├── ApiResponse.java          # 표준 응답 래퍼
│   │   ├── ExtensionResponse.java    # 확장자 응답 DTO
│   │   └── ExtensionListResponse.java
│   └── validator/
│       └── ExtensionValidator.java   # 입력값 검증
├── config/
│   └── WebSocketConfig.java          # WebSocket 설정
├── entity/
│   ├── Extensions.java               # 확장자 엔티티
│   └── ExtensionType.java            # 타입 enum (FIXED/CUSTOM)
└── repository/
    └── ExtensionsRepository.java     # JPA Repository
```

## API 명세

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/extensions` | 활성화된 확장자 목록 조회 |
| POST | `/api/extensions/add?customExtension={name}` | 커스텀 확장자 추가 |
| DELETE | `/api/extensions/custom/{extName}` | 커스텀 확장자 삭제 |
| POST | `/api/extensions/fixed/{extName}` | 고정 확장자 활성화 |
| DELETE | `/api/extensions/fixed/{extName}` | 고정 확장자 비활성화 |
| PATCH | `/api/extensions/fixed/batch` | 고정 확장자 일괄 업데이트 |

## 주요 로직

### 1. 확장자 타입
- **FIXED (고정)**: bat, cmd, com, cpl, exe, scr, js 등 시스템 정의 확장자
- **CUSTOM (커스텀)**: 사용자가 직접 추가하는 확장자 (최대 200개)

### 2. 실시간 동기화
WebSocket을 통해 확장자 변경 시 모든 클라이언트에 실시간 업데이트
- Full Update: 전체 데이터 전송
- Delta Update: 변경된 데이터만 전송 (배치 업데이트 시)

### 3. 유효성 검증
- 확장자 길이: 1~20자
- 허용 문자: 영문, 숫자만 (한글, 특수문자 불가)
- 중복 검사: 이미 존재하는 확장자 추가 불가
- 고정 확장자와 커스텀 확장자 간 충돌 방지