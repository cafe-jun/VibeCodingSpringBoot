# VibeCodingSpringBoot 🚀

대규모 트래픽과 높은 동시성을 처리하기 위해 최적화된 **Spring Boot 백엔드 아키텍처 데모 및 스터디 프로젝트**입니다. 

## 🛠 Tech Stack (기술 스택)
* **Language:** Java 17 (Record, Text Blocks, var 적극 활용)
* **Framework:** Spring Boot 3.x
* **Build Tool:** Gradle
* **Database:** PostgreSQL (MVCC 기반 성능 최적화 및 JSONB 활용)
* **Security:** Spring Security + JWT (Stateless 인증)
* **Template Engine:** Thymeleaf (+ Thymeleaf Layout Dialect)
* **ORM:** Spring Data JPA + Hibernate

## 📂 Project Structure (프로젝트 구조)
이 프로젝트는 **도메인 주도 설계(Package by Feature)** 방식으로 패키징되어 있어, 각 기능의 결합도를 낮추고 마이크로서비스(MSA)로의 확장이 용이합니다.

```text
src/main/
├── java/com/vibecoding/demo/
│   ├── global/              # 전역 설정(Security, ErrorHandler, JWT 유틸 등)
│   ├── domain/
│   │   ├── member/          # 회원 도메인 (인증/가입/정보수정)
│   │   └── board/           # 게시판 도메인 (조회수, 좋아요 등 본진)
│   └── Application.java     # 메인 실행 클래스
│
└── resources/
    ├── application.yml      # 환경변수 기반 보안/DB 통합 설정
    ├── templates/
    │   ├── layout/          # 공통 화면 뼈대 (default_layout.html 등)
    │   ├── fragments/       # 공통 화면 조각 (header, footer 등)
    │   └── {domain}/        # 각 도메인별 실제 HTML 뷰 파일
    └── static/              # CSS, JS, Image 등 정적 파일
```

## 🔐 Environment Variables (환경변수 설정)
보안상 안전한 배포를 위해 `application.yml`의 민감한 정보들은 환경변수로 치환되어 있습니다.
로컬 환경에서는 IDE 설정이나 시스템 환경변수에 아래 값들을 등록하여 사용하세요. (기본값 설정이 되어있으므로 로컬 테스트 시 생략 가능합니다)

* `DB_URL`: PostgreSQL 데이터베이스 주소 (ex: `jdbc:postgresql://localhost:5432/vibecoding`)
* `DB_USERNAME`: 데이터베이스 계정명
* `DB_PASSWORD`: 데이터베이스 비밀번호
* `JWT_SECRET`: JWT 토큰 암호화 시드 (Base64 인코딩된 긴 문자열)

## 📌 Core Architecture Focus (주요 아키텍처 규칙)
* **NoOffset 페이징:** 대량 데이터 무한 스크롤 성능 저하를 극복하기 위한 커서 기반 페이징 처리.
* **CQRS & Redis:** 읽기/쓰기 분리 및 조회수, 인기글의 Cache 처리를 통한 읽기 성능 극대화 (Thundering Herd 방지용 Request Collapsing 도입).
* **동시성 제어:** '좋아요' 와 같은 높은 경합이 일어나는 기능에 분산 락(Redisson) 및 낙관적 락(Optimistic lock) 결합.
* **Transactional Outbox:** Kafka 연동 시 메세지 유실 방지와 데이터 정합성을 위한 Outbox Pattern 기반 이벤트 발행.
* **Deep Hierarchy:** 무한 depth 댓글 처리를 위한 Closure Table / Adjacency List 형태의 DB 모델링.

> 자세한 세부 룰(Rule)은 `.agents/workflows` 폴더 내의 각 `.md` 파일들을 참고하세요.
