# Project Instructions: mxon-spring-study

이 파일은 `mxon-spring-study` 프로젝트의 핵심 규칙과 가이드를 담고 있습니다. 모든 코드 생성 및 수정 시 이 규칙들을 **최우선 순위(Absolute Precedence)**로 준수해야 합니다.

## 1. 핵심 규칙 참조
상세 규칙은 `.agents/rules/` 디렉토리의 다음 문서들을 참조하십시오:
- [공통 코딩 가이드라인](.agents/rules/project-rule.md): Java 17+, PostgreSQL, JPA, 패키징 구조 등
- [데이터베이스 스키마 규칙](.agents/rules/table-rule.md): 테이블 설계, 제약조건, CQRS, Outbox 패턴 등
- [사용자 인증 워크플로우](.agents/rules/user-auth-rule.md): Spring Security, JWT, Stateless 인증 등

## 2. 작업 시 필수 준수 사항
- **Java 17+:** DTO는 `record`를 사용하고, 지역 변수는 `var`를 적극 활용합니다.
- **JPA:** 엔티티에 `@Setter` 사용을 금지하며, 연관관계는 항상 `LAZY` 로딩을 적용합니다.
- **DB Naming:** 테이블과 컬럼은 `snake_case`를 사용하며, 테이블명은 복수형으로 작성합니다.
- **Architecture:** 도메인별 패키징 구조(`domain/{feature}/...`)를 엄격히 따릅니다.
- **Validation:** 모든 변경 사항에 대해 기존 테스트를 확인하고 필요시 새 테스트를 작성합니다.

---
*이 문서는 Gemini CLI의 행동 지침으로 작동하며, 프로젝트의 기술적 무결성을 유지하기 위해 지속적으로 업데이트됩니다.*
