# User Authentication Workflow & Rules

이 워크플로우는 `회원 (User/Member)` 도메인의 가입, 로그인, 인증 및 인가 처리를 구현할 때 AI 에이전트와 개발자가 반드시 준수해야 하는 도메인 특화 규칙입니다. 

## 1. 인증 아키텍처 핵심 (Authentication Core)

### 1-1. Spring Security & JWT (Stateless)
* **Rule:** 사용자 세션을 기반으로 하는 Stateful 상태가 아닌, **JWT (JSON Web Token)**를 이용한 Stateless 인증 구조를 사용합니다.
* **Reason:** 서버의 확장성(Horizontal Scaling)과 대규모 트래픽 분산을 위해 세션 클러스터링 없이 토큰만으로 유효함을 증명할 수 있어야 합니다.
* **Action:** `SecurityFilterChain` 설정 시 `sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)`를 반드시 선언합니다.

### 1-2. 토큰 구조 (Access & Refresh Token)
* **Rule:** 보안을 위해 Access Token과 Refresh Token의 이중 토큰 구조를 갖습니다.
* **Action:** 
  * `Access Token`: 유효기간을 짧게(예: 30분~1시간) 설정하고 Authorization 헤더(`Bearer `)로 주고받습니다.
  * `Refresh Token`: 유효기간을 길게(예: 1주~2주) 설정하고, 탈취 방지를 위해 **HttpOnly 보안 쿠키**로 응답하거나 **Redis**에 화이트리스트/블랙리스트로 관리하여 언제든 권한을 회수할 수 있게 구성합니다.

## 2. 보안 및 개인정보 (Security & Privacy)

### 2-1. 비밀번호 단방향 암호화
* **Rule:** 데이터베이스에 사용자 비밀번호를 평문(Plain text)으로 저장하는 것을 엄격히 금지합니다.
* **Action:** 반드시 Spring Security가 제공하는 `BCryptPasswordEncoder` (또는 더 강력한 알고리즘)를 사용하여 저장하고 검증합니다.

### 2-2. 인가(Authorization) 제어
* **Rule:** 일반 유저와 어드민 등 권한 계층이 필요한 경우 Role-Based 제어를 분명히 합니다.
* **Action:** JWT Payload에 권한(Role) 정보를 담고, 컨트롤러 레이어에서 `@PreAuthorize` 어노테이션이나 Security 설정(RequestMatchers)을 통해 접근 제한을 강화합니다.

## 3. Implementation Checklist

회원 인증 구조를 개발할 때 다음 단계(// turbo-all)를 체크하세요:

- [ ] **엔티티 구성:** `members` 테이블에 인증 권한 필드(`role`)가 존재하는가? 비밀번호 필드 길이가 BCrypt 해시 길이를 넉넉히 수용할 수 있는가?
- [ ] **필터 개발:** `OncePerRequestFilter`를 상속받은 `JwtAuthenticationFilter`가 올바르게 헤더를 파싱하고 SecurityContextHolder에 유저 정보를 세팅하고 있는가?
- [ ] **예외 처리 분리:** 토큰 만료(Expired)와 토큰 변조(SignatureException)를 구분하는 커스텀 `AuthenticationEntryPoint`와 `AccessDeniedHandler`가 설정되어 프론트엔드가 토큰 재발급 여부를 판단할 수 있도록 구성되었는가?
- [ ] **비밀번호 저장 검증:** 회원가입 로직에서 `passwordEncoder.encode()`가 필수적으로 맵핑되어 있는가?
