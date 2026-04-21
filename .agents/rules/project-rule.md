<project_context>
This workspace is dedicated to a high-performance Spring Boot application dealing with large-scale traffic, deep hierarchies, and data consistency.
Tech Stack Requirements:
- Language: Java 17
- Build Tool: Gradle
- Database: PostgreSQL
- Template Engine: Thymeleaf
- Security: Spring Security + JWT
</project_context>

<coding_guidelines>
## 1. Java 17 
- DTO 및 읽기 전용 데이터 객체 작성 시 가급적 `record` 키워드를 사용하여 불변성(Immutability)과 간결함을 확보한다.
- 타입 추론이 명확한 지역 변수에는 `var`를 적극 사용한다.
- 복잡한 쿼리(Native Query)나 긴 JSON 포맷 문자열은 Text Blocks (`"""`)를 사용한다.
- 패턴 매칭(Pattern Matching for instanceof)과 향상된 `switch` 문을 적극 활용한다.

## 2. PostgreSQL DB
- 대용량 데이터 생성을 대비해 PK(Primary Key) 식별자는 `BIGSERIAL` (JPA `GenerationType.IDENTITY`) 또는 성능에 따라 `UUID`를 사용한다.
- 비정형 데이터(동적 스키마 등)가 필요한 경우 PostgreSQL의 강력한 `JSONB` 타입을 적극 활용한다.
- Full-Text Search 도입 시 `GIN Index`와 `to_tsvector`를 활용해 검색을 최적화한다.

## 3. DB 테이블 및 컬럼 명명 규칙
- 테이블과 컬럼명은 모두 소문자 `snake_case`를 사용한다.
- 테이블명은 데이터를 나타내는 복수형(Plural)을 기본으로 한다 (예: `users`, `posts`, `comments`).
- 외래키(FK) 컬럼명은 참조하는 단수형 테이블 이름 뒤에 `_id`를 붙인다 (예: `user_id`, `post_id`).
- DB 내의 상태 및 논리 플래그는 `is_` 혹은 `has_` 접두사를 붙인다 (예: `is_deleted`, `is_published`).

## 4. 변수 규칙 (Variable Conventions)
- 기본적으로 `camelCase`를 사용한다.
- 무분별한 약어 사용을 지양하고, 누구나 의미를 알 수 있도록 명확히 작성한다 (예: `usr` 대신 `user`, `idx` 대신 `index`).
- 컬렉션/배열 타입의 변수는 컨테이너명(예: `boardList`)보다는 복수형 명사(예: `boards`)로 작성한다.

## 5. 함수 규칙 (Function Conventions)
- 동작을 나타내는 동사(Verb)로 시작하는 `camelCase`로 작성한다.
- 목적에 따른 네이밍 컨벤션을 통일한다:
  - 단건 조회: `get...` (값이 없으면 예외 발생) 또는 `find...` (값이 없을 수 있어 Optional 반환)
  - 다건 조회: `getAll...` 또는 `search...` (조건 검색)
  - 데이터 저장/생성: `save...` 또는 `create...`
  - 데이터 수정: `update...` 또는 `modify...`
  - 데이터 삭제: `delete...` 또는 `remove...`

## 6. JPA (Java Persistence API) 규칙
- 엔티티(Entity) 클래스 레벨에 롬복의 `@Data`나 `@Setter`를 절대 사용하지 않는다.
- 대신 `@Getter`와 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`를 기본으로 선언한다.
- 데이터를 수정해야 할 경우 Setter 대신 명확한 의도를 가진 비즈니스 메서드(예: `publishPost()`, `changePassword()`)를 엔티티 내에 직접 구현한다.
- 가장 흔하게 발생하는 N+1 쿼리 문제를 방지하기 위해 모든 연관관계 매핑(`@ManyToOne`, `@OneToOne`)은 반드시 `fetch = FetchType.LAZY`로 명시한다.
- 생성/수정 시간 자동 관리를 위해  `@MappedSuperclass`와 `@EntityListeners(AuditingEntityListener.class)`를 사용한 `BaseTimeEntity`를 만들어 상속받아 사용한다.

## 7. 디렉토리 패키징 규칙 (Directory Rule)
- 글로벌 Controller/Service 분리 방식보다 결합도가 낮은 도메인/기능별 패키징 구조(Package by Feature)를 지향한다.
- 도메인 구조 예시:
  `src/main/java/com/.../domain/board/`
   `- entity/` (JPA Entities)
   `- repository/` (Spring Data JPA Repositories & Custom QueryDSL)
   `- service/` (Business Logic & Facade)
   `- controller/` (REST APIs)
   `- dto/` (Request/Response Records)
- 인증, 보안, 설정, 공통 예외 처리 등 전체 영역에 영향을 미치는 코드는 `global/` 패키지로 분리한다. (예: `global/exception`, `global/config`, `global/security`)

## 8. High-Concurrency & Architecture (기존 백엔드 아키텍처 룰)
- 동시성 처리: Redisson 분산 락, DB 낙관적/비관적 락 우선 적용.
- 트래픽 최적화: `NoOffset` 페이징 구조, Redis Cache / Request Collapsing으로 부하 상쇄, CQRS 분리.
- 이벤트 워크플로우: 로컬 트랜잭션과 Kafka 이벤트를 묶어줄 Transactional Outbox Pattern 필수 적용.

## 9. UI 및 프론트엔드 (Thymeleaf)
- 컨트롤러 레이어에서 템플릿 반환 시 오타 방지와 유지보수성을 위해 `String` 대신 상수로 빼거나 Enum을 활용하여 View name을 반환하는 구조를 권장한다.
- 동적인 UI(SPA와 유사한 경험)가 필요한 구간은 Thymeleaf Fragments와 HTMX(또는 jQuery 픽스)를 혼합하여 사용할 것을 고려한다.
- 정적 리소스(CSS, JS) 처리 및 관리를 위해 layout dialect 패키지(예: `thymeleaf-layout-dialect`)를 사용하여 일관성 있는 화면 프레임을 구성한다.
- **템플릿 디렉토리 구조 (Directory Layout):**
  Spring Boot의 표준 권장 경로를 따르되, 도메인 주도 설계(Package by Feature)와 일관성이 있도록 화면 파일을 구성한다.
  - `src/main/resources/templates/layout/`: 공통 레이아웃 파일 (어플리케이션의 뼈대. 예: `default_layout.html`)
  - `src/main/resources/templates/fragments/`: 반복 사용되는 공통 조각 파일 (예: `header.html`, `footer.html`)
  - `src/main/resources/templates/{도메인명}/`: 도메인에 종속되는 실제 뷰 화면 페이지 (예: `board/list.html`, `board/detail.html`)
  - `src/main/resources/static/css|js|images/`: 정적 리소스 에셋 보관 장소
</coding_guidelines>
