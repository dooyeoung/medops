## 개요

MedOps는 **이벤트 소싱(Event Sourcing)** 개념을 구현한 의료 CRM 시스템입니다. 헥사고날 아키텍처(Hexagonal Architecture)를 기반으로 하며, 병원 예약 관리, 환자 관리, 의료진 대시보드 등의 기능을 제공합니다.

### 주요 특징

-  **데이터 관리**: 병원 예약, 환자 관리, 의료진 스케줄링
-  **헥사고날 아키텍처**: 도메인 중심의 클린 아키텍처
-  **이벤트 소싱**: 의료 기록의 모든 변경사항을 이벤트로 추적
-  **실시간 대시보드**: 예약 현황, 매출 통계, 성과 분석
-  **JWT 인증**: 사용자/관리자 역할 기반 접근 제어

---
## 기술 스택

### 백엔드 (Spring Boot)
- **Java 17** + **Spring Boot 3.5.3**
- **Spring Security** + **JWT** (jjwt 0.12.6)
- **MongoDB** - 메인 데이터 저장소
- **Redis** - 캐싱 및 세션 관리
- **SpringDoc OpenAPI** - API 문서화
- **JaCoCo** - 코드 커버리지 (80% 이상)

### 프론트엔드 (React + TypeScript)
- **React 19.1.1** + **TypeScript 5.8.3**
- **Vite 7.1.2** - 빌드 도구
- **Tailwind CSS 4.1.12** - 스타일링
- **Radix UI** - UI 컴포넌트
- **React Router 7.8.1** - 라우팅
- **ECharts** + **Recharts** - 데이아키텍처

### 헥사고날 아키텍처 (Ports and Adapters)

**핵심 개념 (단순화):**
- **외부 → 포트 → 내부**: 모든 외부 연결은 포트(인터페이스)를 통해
- **의존성 역전**: 구현체가 인터페이스에 의존
- **도메인 격리**: 비즈니스 로직은 외부 기술과 분리

### 백엔드 디렉토리 구조

```
src/main/java/com/medops/
├── adapter/                     # 어댑터 레이어 (외부 시스템 연동)
│   ├── in/                      # 인바운드 어댑터 (외부에서 도메인으로)
│   │   ├── annotation/          # 커스텀 어노테이션 (@UserSession, @AdminSession)
│   │   ├── security/            # Spring Security 설정
│   │   └── web/                 # REST API 컨트롤러
│   │       ├── controller/      # REST 엔드포인트 (User, Admin, Dashboard API)
│   │       ├── exception/       # 글로벌 예외 처리
│   │       ├── request/         # 요청 DTO 클래스
│   │       └── resolver/        # 커스텀 파라미터 리졸버
│   └── out/                     # 아웃바운드 어댑터 (도메인에서 외부로)
│       ├── event/               # 이벤트 리스너
│       ├── persistence/         # 영속성 구현체
│       │   ├── eventstore/      # 이벤트 스토어 어댑터
│       │   ├── mongodb/         # MongoDB 어댑터
│       │   │   ├── adapter/     # 영속성 포트 구현체
│       │   │   ├── converter/   # 도메인 ↔ 문서 변환기
│       │   │   ├── document/    # MongoDB 문서 모델
│       │   │   └── repository/  # Spring Data MongoDB 저장소
│       │   └── redis/           # Redis 캐시 어댑터
│       └── security/            # JWT 토큰 어댑터
│
├── application/                 # 애플리케이션 레이어 (유스케이스)
│   ├── dto/                     # 애플리케이션 데이터 전송 객체
│   ├── eventsourcing/           # 이벤트 소싱 구현
│   │   ├── command/             # 커맨드 객체 및 실행기
│   │   ├── event/               # 도메인 이벤트 정의
│   │   ├── handler/             # 이벤트 핸들러
│   │   └── processor/           # 커맨드 프로세서
│   ├── port/                    # 포트 인터페이스
│   │   ├── in/                  # 인바운드 포트 (유스케이스)
│   │   │   ├── command/         # 커맨드 DTO
│   │   │   └── usecase/         # 유스케이스 인터페이스
│   │   └── out/                 # 아웃바운드 포트 (SPI)
│   └── service/                 # 유스케이스 구현체 (서비스)
│
├── domain/                      # 도메인 레이어 (비즈니스 로직)
│   ├── enums/                   # 도메인 열거형 (상태, 타입 등)
│   ├── event/                   # 도메인 이벤트
│   └── model/                   # 도메인 엔티티 (User, Hospital, MedicalRecord)
│
├── common/                      # 공통 유틸리티
│   ├── error/                   # 에러 코드 정의
│   ├── exception/               # 커스텀 예외
│   └── response/                # 공통 응답 포맷
│
└── config/                      # 설정 클래스 (Spring Configuration)
```

### 레이어 간 통신 관계

```mermaid
graph LR
    Controller[ Controller] --> UseCase[ UseCase]
    UseCase -.-> Service[️ Service]
    Service --> Port[ Port]
    Port -.-> Adapter[ Adapter]
    Adapter --> DB[( Database)]
    
    classDef interface fill:#f3e5f5,stroke:#7b1fa2,stroke-dasharray: 5 5
    classDef implementation fill:#e8f5e8,stroke:#2e7d32
    classDef external fill:#ffebee,stroke:#c62828
    
    class UseCase,Port interface
    class Controller,Service,Adapter implementation
    class DB,Domain external
```

**데이터 흐름과 의존성:**

애플리케이션의 데이터는 다음과 같이 흐릅니다:
`웹 요청` → `컨트롤러` → `유스케이스` → `서비스` → `포트` → `어댑터` → `데이터베이스`

하지만 의존성 방향은 반대입니다:
- `컨트롤러`는 `유스케이스 인터페이스`에 의존
- `서비스`가 `유스케이스 인터페이스`를 구현
- `서비스`는 `포트 인터페이스`에 의존  
- `어댑터`가 `포트 인터페이스`를 구현

이를 통해 외부 시스템(데이터베이스, API 등)이 변경되어도 핵심 비즈니스 로직은 영향받지 않습니다.

### 이벤트 소싱 구현

의료 기록 관리에 이벤트 소싱 패턴을 적용하여 모든 변경사항을 추적합니다:

```mermaid
flowchart LR
    Controller -->|Message| Proseccor

    Proseccor -->|QueryEvents| EventStore
    EventStore -->|Events| Proseccor

    EventStore --> Database

    EventStore -->|CollectEvents| Proseccor

    Proseccor -->|Command| CommandExecutors
    CommandExecutors -->|Events| Proseccor

    Proseccor -->|Events| EventHandlers
    EventHandlers -->|State| Proseccor
```

**실제 의료 예약 시나리오로 보는 이벤트 소싱:**

#### 1️⃣ 새로운 예약 생성
```
환자가 병원 예약을 생성하는 경우:
Controller → Processor → CommandExecutor → EventStore

• CreateMedicalRecordCommand 전달
• CommandExecutor가 MedicalRecordCreatedEvent 생성
• EventStore에 이벤트 저장 (version: 1)
• EventHandler가 스냅샷 생성
```

#### 2️⃣ 예약 상태 변경 (확정)
```
의사가 예약을 확정하는 경우:
Controller → Processor → [스냅샷 조회] → EventHandler → CommandExecutor → EventStore

• ConfirmMedicalRecordCommand 전달
• 기존 스냅샷에서 현재 상태 조회 (PENDING)
• EventHandler가 최신 상태로 복원
• StatusChangedEvent 생성 (PENDING → CONFIRMED)
• EventStore에 저장 (version: 2)
```

#### 3️⃣ 예약 취소 및 상태 복원
```
환자가 예약을 취소하고, 관리자가 과거 상태를 조회하는 경우:
Controller → Processor → EventStore [Query Events] → EventHandler

• CancelMedicalRecordCommand 실행 → StatusChangedEvent (version: 3)
• 과거 시점 조회 요청시:
  - EventStore에서 특정 version까지의 이벤트만 조회
  - EventHandler가 순차적으로 이벤트 재생
  - 해당 시점의 정확한 상태 복원
```

**이벤트 소싱의 핵심 이점:**
- **완전한 감사 추적**: 예약 생성 → 확정 → 취소 전 과정이 이벤트로 기록
- **시간 여행 디버깅**: "왜 이 예약이 취소되었나?" → 이벤트 히스토리 조회
- **성능 최적화**: 스냅샷으로 빠른 현재 상태 조회, 필요시에만 이벤트 재생
- **장애 복구**: 데이터 손실시 모든 이벤트를 재생하여 완전 복원

---
## 시작하기

### 사전 요구사항

- **Java 17+**
- **Node.js 18+**
- **MongoDB** (로컬 또는 클라우드)
- **Redis** (로컬 또는 클라우드)

### 환경 변수 설정

루트 디렉토리에 `.env.local` 파일 생성:

```bash
# Database
MONGODB_URI=mongodb://localhost:27017/medops
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION_HOURS=24

# Frontend
FRONTEND_URL=http://localhost:5173
```

### 백엔드 실행

```bash
# 프로젝트 루트에서
./gradlew bootRun

# 또는 개발 프로필로
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

### 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

##  API 문서

서버 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
---
##  테스트

### 백엔드 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 코드 커버리지 리포트 생성
./gradlew jacocoTestReport

# 커버리지 확인 (80% 이상 필수)
./gradlew jacocoTestCoverageVerification
```

### 프론트엔드 테스트

```bash
cd frontend

# 타입 체크
npm run build:check

# 린트 검사
npm run lint

# 코드 포맷팅
npm run format
```
---
##  배포

### Docker 컨테이너

```bash
# Docker 이미지 빌드
docker build -t medops .

# Docker Compose 실행 (MongoDB, Redis 포함)
docker-compose up -d
```

### 프로덕션 환경

```bash
# 백엔드 빌드
./gradlew build -x test

# 프론트엔드 빌드
cd frontend && npm run build
```
