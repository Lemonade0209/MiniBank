# MiniBank

MiniBank는 Spring MVC와 JDBC를 순서대로 학습하며 만드는 미니 인터넷뱅킹 프로젝트입니다. 이체 일부 반영이나 동시 출금처럼 잔액 정합성이 깨질 수 있는 상황을 테스트로 재현하고 해결하는 것을 목표로 합니다.

HTTP 설계를 마치고 2026년 7월 21일에 Java 21, Spring Boot 3.5.16과 Gradle 8.14.3 기반 프로젝트를 생성했습니다. 기본 애플리케이션 컨텍스트 테스트는 통과했으며, 현재는 MVC1 서블릿 섹션의 요청·응답 기초를 학습하는 단계입니다.

## 프로젝트 목표

- 회원가입, 로그인과 로그아웃
- 계좌 개설·목록·상세
- 입금, 출금과 계좌이체
- 계좌별 거래내역
- MemoryRepository에서 JDBC Repository로 전환
- 이체 중간 실패 롤백 검증
- 동일 계좌 동시 출금 정합성 검증

최종 완료 기준은 다음과 같습니다.

> MiniBank는 작지만 직접 실행할 수 있고, 이체 롤백과 동시 출금 문제의 원인과 해결 방법을 코드와 테스트로 설명할 수 있다.

## 진행 일정

| 기간 | 단계 | 결과 |
|---|---|---|
| 7/13~7/19 | HTTP | 요청·인증·오류·데이터 설계 |
| 7/20~8/2 | MVC1 | 회원·계좌 Memory MVP |
| 8/3~8/16 | MVC2 | 로그인·검증·입출금 Memory MVP |
| 8/17~8/23 | DB1 전반 | H2와 JDBC 저장소 |
| 8/24~8/31 | DB1 후반 | 이체 트랜잭션과 동시성 검증 |

## 문서

| 문서 | 내용 |
|---|---|
| [요구사항](docs/requirements.md) | v1.0 기능과 업무 규칙 |
| [HTTP 요청 설계](docs/http-api.md) | URL, Method와 입력값 |
| [요청 흐름](docs/request-flow.md) | 화면 이동과 PRG 흐름 |
| [ERD 초안](docs/erd-draft.md) | 회원, 계좌와 거래내역 구조 |
| [MVC1 학습 노트](docs/mvc1-learning-notes.md) | 웹 애플리케이션 이해와 MVC1 학습 기록 |

## 예정 기술 스택

- Java 21
- Spring Boot 3.5.16, Spring MVC
- Thymeleaf
- H2, JDBC
- JUnit 5
- Gradle 8.14.3

Spring Security, JPA, Redis, Kafka와 복잡한 프론트엔드는 v1.0 범위에서 제외합니다.

## 구조 원칙

```text
HTTP 요청
  -> Controller
  -> Service
  -> Repository 인터페이스
  -> Memory 또는 JDBC 구현체
  -> H2
```

Controller는 요청 바인딩과 화면 이동을 맡고, Service는 계좌 소유권과 잔액 규칙을 처리합니다. 두 계층이 저장 기술에 직접 의존하지 않도록 Repository 인터페이스와 구현체를 분리합니다.

## 거래 정합성 전략

### 이체

출금 계좌 차감, 입금 계좌 증가와 거래내역 두 건 저장을 하나의 `@Transactional` Service 메서드에서 처리합니다. 중간에 런타임 예외가 발생하면 모든 변경이 롤백되어야 합니다.

고급 잠금 전략은 DB1 구현 과정에서 동시 요청 문제가 확인될 때 적용 여부를 결정합니다.

### 동시 출금

잔액을 읽고 나중에 차감하는 방식 대신 잔액 조건이 포함된 단일 UPDATE를 사용합니다.

```sql
update accounts
set balance = balance - ?
where id = ?
  and balance >= ?;
```

수정된 행이 한 건일 때만 출금 거래내역을 저장합니다. 10,000원 계좌에서 두 요청이 동시에 7,000원을 출금하면 한 건만 성공하고 최종 잔액은 3,000원이어야 합니다.

## 핵심 테스트 시나리오

1. 정상 이체 후 양쪽 잔액과 두 거래내역 확인
2. 출금 후 입금 직전 예외를 발생시켜 전체 롤백 확인
3. 두 스레드의 동시 출금 후 성공 횟수, 최종 잔액과 내역 수 확인

## 실행 방법

Spring Boot 프로젝트 생성과 기본 테스트를 완료했습니다. 아래 명령으로 전체 테스트와 애플리케이션을 실행합니다.

```bash
./gradlew clean test
./gradlew bootRun
```

## MVC1 시작 백로그

### 웹 애플리케이션 이해 섹션

- 7월 20일에는 MVC1의 웹 애플리케이션 이해 섹션을 학습하고 핵심 개념을 정리
- 이 단계에서는 Spring Boot 프로젝트를 생성하지 않음

### 서블릿 섹션의 프로젝트 생성 강의 완료 후

- [x] Java 21, Spring Web, Thymeleaf와 Test로 Spring Boot 프로젝트 생성
- [x] Gradle Wrapper와 기본 패키지 `com.lemonade0209.minibank` 설정
- [x] 기본 애플리케이션 컨텍스트 테스트 통과
- [x] Hello Servlet과 GET 쿼리 파라미터 요청 로그 확인

### 서블릿·JSP·MVC 패턴 학습 후

- `Member`, `MemberRepository`와 `MemoryMemberRepository` 작성
- `MemberService`에서 loginId 중복 검사
- 저장소와 Service 테스트 작성

### Spring MVC 구조 이해 섹션 완료 후

- `HomeController`와 Spring MVC 기반 홈 화면 구현
- `GET /health` 요청 매핑과 실행 확인

### Spring MVC 기본 기능과 웹 페이지 만들기 학습 후

- 회원가입 GET/POST와 로그인 폼 뼈대 구현
- `Account`, `AccountRepository`와 `MemoryAccountRepository` 작성
- 12자리 계좌번호 생성과 최대 5회 충돌 재시도
- 계좌 개설·회원별 목록·상세 화면 구현
- Controller가 Repository를 직접 호출하지 않는지 확인

MVC1 단계는 전체 테스트와 수동 시연을 통과한 뒤 `v0.1-mvc1-memory` 태그로 마감합니다. 세션 로그인, 입출금과 DB 연결은 다음 단계에서 구현합니다.

## 현재 상태

- [x] v1.0 요구사항 확정
- [x] URL과 HTTP Method 확정
- [x] PRG와 화면 요청 흐름 확정
- [x] 세션 인증과 오류 처리 기준 확정
- [x] ERD 초안과 MVC1 백로그 작성
- [x] Spring Boot 프로젝트 생성과 기본 테스트
- [x] Hello Servlet과 GET 쿼리 파라미터 요청 로그 확인
- [ ] 회원·계좌 Memory MVP
- [ ] 로그인·입출금 Memory MVP
- [ ] H2/JDBC 전환
- [ ] 이체 롤백과 동시 출금 테스트
- [ ] 최종 태그 `v1.0-db-consistency-mvp`

## 보안 관련 주의

이 프로젝트는 학습용입니다. v1.0에서는 Spring Security와 비밀번호 암호화를 다루지 않으므로 실제 금융 서비스나 운영 환경에 사용할 수 없습니다.
