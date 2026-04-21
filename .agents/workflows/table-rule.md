# Database Table & Schema Rules (PostgreSQL)

이 문서는 등록된 이슈(게시판, 무한 depth 댓글, 좋아요/조회수 처리, 인기글, Transaction Outbox 등)를 바탕으로 데이터베이스 테이블 구조의 뼈대를 정의하고, 개발 시 테이블 생성에 필요한 제약사항(Rules)을 명시합니다.

## 1. Core Tables (기본 도메인)

### 1-1. `members` (사용자)
게시글, 댓글, 좋아요 등의 주체가 되는 테이블입니다.
* `id` (BIGSERIAL / PK)
* `email` (VARCHAR / Unique / Index)
* `password` (VARCHAR)
* `nickname` (VARCHAR)
* `created_at`, `updated_at` (TIMESTAMPTZ)

### 1-2. `posts` (게시글)
무한 스크롤 및 NoOffset 페이징을 위해 인덱스 설계가 고도화되어야 하는 테이블입니다.
* `id` (BIGSERIAL / PK)
* `member_id` (BIGINT / FK / Index)
* `title` (VARCHAR)
* `content` (TEXT)
* `deleted_at` (TIMESTAMPTZ) : Soft Delete 적용 용도
* *(비정규화 통계 컬럼)* `view_count`, `like_count`, `comment_count` (BIGINT / Default 0) : 성능상 카운트를 엔티티에 비정규화하고, 동시성 이슈는 Redisson 혹은 스케줄러를 통해 보정합니다.

### 1-3. `comments` (댓글 - 무한 Depth 지원 구조)
"최대 2 depth"가 아닌 "무한 depth"를 지원하기 위해 재귀적 관계를 맺습니다. PostgreSQL의 Materialized Path 또는 Recursive CTE에 유리한 구조를 사용합니다.
* `id` (BIGSERIAL / PK)
* `post_id` (BIGINT / FK / Index)
* `member_id` (BIGINT / FK)
* `parent_id` (BIGINT / Nullable / FK) : 최상위 댓글이면 Null, 대댓글이면 부모 댓글의 ID. (Adjacency List 구조)
* `content` (TEXT)
* `is_deleted` (BOOLEAN) : 부모 댓글이 지워졌을 때 자식 댓글 유지를 위한 Soft Delete 처리

---

## 2. Interaction & Abusing Tables (상호작용 및 어뷰징 방지)

### 2-1. `post_likes` / `comment_likes` (좋아요)
유저당 1회만 좋아요를 누를 수 있는 제약을 위함입니다.
* `id` (BIGSERIAL / PK)
* `post_id` / `comment_id` (BIGINT / FK / Index)
* `member_id` (BIGINT / FK)
* `created_at` (TIMESTAMPTZ)
* **Rule:** `(post_id, member_id)` 조합에 반드시 `UNIQUE CONSTRAINT` (고유 제약조건)를 걸어 DB 단에서 중복 생성을 원천 차단합니다. 낙관적 락(`posts.version`)과 결합해 조회수를 보호합니다.

### 2-2. `post_view_logs` (조회수 어뷰징 기록)
"조회수 어뷰징 정책" 방어를 위한 테이블입니다. (성능이 중요하므로 Redis로 대체되기도 함)
* `id` (BIGSERIAL / PK)
* `post_id` (BIGINT / FK)
* `member_id` (BIGINT / Nullable: 비회원 처리용) 혹은 `ip_address` (VARCHAR)
* `viewed_at` (TIMESTAMPTZ)
* **Rule:** 일정 시간(예: 지난 24시간) 내 동일 회원이 접근한 이력을 캐시(Redis)나 이 테이블에서 확인하고, 중복이 아닐 경우에만 `posts.view_count`를 증가시킵니다.

---

## 3. Advanced Traffic & Architecture Tables (대규모 트래픽 아키텍처)

### 3-1. `trending_posts` (인기글 요약 테이블)
인기글/실시간 랭킹("CQRS", "조회수 캐시") 조회를 위한 분리된 읽기 전용 테이블(CQRS - Read Model)입니다.
* `id` (BIGSERIAL / PK)
* `post_id` (BIGINT / FK / Unique)
* `trending_score` (DECIMAL / Index) : 좋아요, 조회수, 가중치를 계산한 랭킹 점수
* `snapshot_at` (TIMESTAMPTZ)
* **Rule:** 사용자의 직접적인 조회 쿼리는 무거운 원본 `posts` 테이블 대신, `trending_posts` 테이블 혹은 Redis ZSet에서 캐시된 데이터를 반환하도록 설계합니다.

### 3-2. `outbox_events` (Transactional Outbox 테이블)
"인기글 Consumer/Producer + Transaction OutBox" 구현을 위한 필수 테이블입니다. 이벤트 유실을 100% 방지합니다.
* `event_id` (UUID / PK) : 메세지 발행용 식별자
* `aggregate_type` (VARCHAR) : (예: `POST_LIKED`, `POST_VIEWED`)
* `aggregate_id` (VARCHAR) : 연관된 도메인의 ID (게시글 ID 등)
* `payload` (JSONB) : Kafka에 발행될 메세지의 모든 정보(PostgreSQL 강점인 JSONB 활용)
* `status` (VARCHAR) : `INIT`(최초 저장), `PUBLISHED`(발행 완료) (Index 필요)
* `created_at` (TIMESTAMPTZ)
* **Rule:** 로컬 DB(예: 좋아요 추가) 트랜잭션과 **정확히 동일한 트랜잭션** 내에서 `outbox_events` 삽입을 완료해야 합니다. 이후 스케줄러나 CDC 툴(Debezium)이 `INIT` 상태의 이벤트를 읽어 Kafka로 전송합니다.
