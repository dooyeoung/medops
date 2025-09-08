# MedOps - Medical Operations Management System

> **강남언니 지원을 위한 프로젝트** - 의료 CRM 시스템으로 병원 예약, 환자 관리, 의료진 대시보드를 제공합니다.

## 📋 개요

MedOps는 **이벤트 소싱(Event Sourcing)** 개념을 구현한 의료 CRM 시스템입니다. 헥사고날 아키텍처(Hexagonal Architecture)를 기반으로 하며, 병원 예약 관리, 환자 관리, 의료진 대시보드 등의 기능을 제공합니다.

### 주요 특징

- 🏥 **의료 전문 CRM**: 병원 예약, 환자 관리, 의료진 스케줄링
- 📊 **실시간 대시보드**: 예약 현황, 매출 통계, 성과 분석
- ⚡ **이벤트 소싱**: 의료 기록의 모든 변경사항을 이벤트로 추적
- 🏗️ **헥사고날 아키텍처**: 도메인 중심의 클린 아키텍처
- 🔐 **JWT 인증**: 사용자/관리자 역할 기반 접근 제어
- 📱 **반응형 웹**: 모바일 친화적 React 인터페이스

## 🚀 기술 스택

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
- **ECharts** + **Recharts** - 데이터 시각화
- **Axios** - HTTP 클라이언트

## 🏗️ 아키텍처

### 헥사고날 아키텍처 (Ports and Adapters)

```
┌─────────────────────────────────────────────────────────────┐
│                    Adapter Layer                            │
│  ┌─────────────────┐                  ┌─────────────────┐   │
│  │   REST API      │                  │   MongoDB       │   │
│  │   Controllers   │                  │   Persistence   │   │
│  └─────────────────┘                  └─────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                 Application Layer                           │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Use Cases (Commands & Queries)                        │ │
│  │  - UserRegistrationUseCase                             │ │
│  │  - MedicalRecordViewUseCase                            │ │
│  │  - DashboardService                                    │ │
│  └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                    Domain Layer                             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Entities: User, Admin, Hospital, MedicalRecord        │ │
│  │  Value Objects: Status Enums, DTOs                     │ │
│  │  Domain Events: Medical Record Events                  │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 이벤트 소싱 구현

의료 기록 관리에 이벤트 소싱 패턴을 적용하여 모든 변경사항을 추적합니다:

- **Event Store**: 모든 의료 기록 변경을 이벤트로 저장
- **Snapshots**: 성능 최적화를 위한 스냅샷 생성
- **Event Replay**: 이벤트 재생을 통한 상태 복원
- **Audit Trail**: 완전한 감사 추적 기능

```java
// 이벤트 문서 구조
@Document("medops_medical_record_events")
public class MedicalRecordEventDocument {
    private String recordId;
    private String eventType;
    private Instant createdAt;
    private MedicalRecordStatus status;
    private Map<String, Object> payload;
    private Integer version;
}
```

## 🚦 시작하기

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

## 📝 API 문서

서버 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🧪 테스트

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

## 📊 모니터링 및 성능

### 코드 품질
- **JaCoCo**: 80% 이상 코드 커버리지 유지
- **E2E 테스트**: 실제 데이터베이스를 사용한 통합 테스트
- **ESLint + Prettier**: 일관된 코드 스타일

### 데이터베이스 성능
- **MongoDB**: 인덱스 최적화된 쿼리
- **Redis**: 세션 및 캐시 성능 최적화
- **이벤트 스토어**: 배치 처리 및 스냅샷 전략

## 🔐 보안

- **JWT 토큰**: stateless 인증 시스템
- **역할 기반 접근 제어**: 사용자/관리자 권한 분리
- **CORS 설정**: 프론트엔드 도메인 제한
- **입력 검증**: Bean Validation을 통한 데이터 검증

## 📦 배포

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

## 📋 TODO - 개선 사항

### 🚀 기능 확장
- [ ] **실시간 알림 시스템**: WebSocket을 통한 예약 상태 변경 알림
- [ ] **모바일 앱**: React Native 또는 Flutter 앱 개발
- [ ] **결제 시스템**: PG사 연동 (토스페이먼츠, 이니시스 등)
- [ ] **SMS/이메일 알림**: 예약 확인/변경 자동 알림
- [ ] **다국어 지원**: i18n 국제화 기능
- [ ] **API 버전 관리**: RESTful API 버전 전략 수립

### ⚡ 성능 최적화
- [ ] **데이터베이스 최적화**: 
  - MongoDB 인덱스 전략 개선
  - 쿼리 성능 모니터링
  - 커넥션 풀 튜닝
- [ ] **캐싱 전략**: 
  - Redis 캐시 정책 세분화
  - CDN 도입 검토
  - 브라우저 캐싱 최적화
- [ ] **이벤트 소싱 최적화**:
  - 스냅샷 생성 주기 조정
  - 이벤트 스토어 파티셔닝
  - 배치 이벤트 처리

### 🛡️ 보안 강화
- [ ] **OAuth 2.0**: 소셜 로그인 (구글, 카카오) 연동
- [ ] **2FA 인증**: 관리자 계정 이중 인증
- [ ] **API Rate Limiting**: DDoS 방어를 위한 요청 제한
- [ ] **보안 헤더**: OWASP 보안 가이드라인 적용
- [ ] **취약점 스캐닝**: 정기적인 보안 감사

### 📊 모니터링 및 운영
- [ ] **APM 도구**: New Relic, DataDog 등 성능 모니터링
- [ ] **로그 수집**: ELK Stack (Elasticsearch, Logstash, Kibana)
- [ ] **메트릭 수집**: Prometheus + Grafana 대시보드
- [ ] **헬스 체크**: 서비스 상태 모니터링
- [ ] **알람 시스템**: 장애 감지 및 알림 자동화

### 🧪 품질 향상
- [ ] **테스트 커버리지**: 90% 이상 목표
- [ ] **E2E 테스트**: Cypress 또는 Playwright 도입
- [ ] **성능 테스트**: JMeter를 통한 부하 테스트
- [ ] **코드 품질**: SonarQube 정적 분석 도구
- [ ] **자동화된 배포**: CI/CD 파이프라인 구축

### 🏗️ 아키텍처 개선
- [ ] **마이크로서비스**: 도메인별 서비스 분리 검토
- [ ] **CQRS 패턴**: Command와 Query 분리 고도화
- [ ] **이벤트 버스**: Apache Kafka 도입 검토
- [ ] **서비스 메시**: Istio 또는 Linkerd 도입
- [ ] **컨테이너 오케스트레이션**: Kubernetes 배포

### 📚 문서화
- [ ] **API 문서**: OpenAPI 스펙 상세화
- [ ] **아키텍처 문서**: C4 모델 기반 시스템 문서
- [ ] **개발자 가이드**: 온보딩 및 개발 프로세스
- [ ] **운영 가이드**: 배포 및 장애 대응 매뉴얼
- [ ] **사용자 매뉴얼**: 최종 사용자 가이드

## 👥 기여하기

1. 이 저장소를 포크합니다
2. 새로운 기능 브랜치를 만듭니다 (`git checkout -b feature/AmazingFeature`)
3. 변경사항을 커밋합니다 (`git commit -m 'Add some AmazingFeature'`)
4. 브랜치에 푸시합니다 (`git push origin feature/AmazingFeature`)
5. Pull Request를 생성합니다

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 📞 지원

문제가 있거나 질문이 있으시면 이슈를 생성해주세요.

---

**강남언니 지원 프로젝트** - MedOps로 의료 서비스의 디지털 혁신을 경험하세요! 🏥✨