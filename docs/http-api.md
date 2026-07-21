---
document_type: http_api_design
project_name: MiniBank
version: 1.1
created_at: 2026-07-15
updated_at: 2026-07-18
status: approved
scope: MiniBank v1.0 Spring MVC 화면 요청의 URL 및 HTTP Method 설계
related_documents:
  - requirements.md
  - request-flow.md
  - erd-draft.md
---

# MiniBank HTTP 요청 설계

## 1. 문서 목적

이 문서는 MiniBank v1.0에서 사용할 URL과 HTTP Method를 구현 전에 확정하기 위한 문서다.

MiniBank는 JSON REST API가 아니라 Spring MVC와 Thymeleaf를 사용하는 서버 렌더링 웹 애플리케이션이다. GET 요청은 화면을 조회하고, POST 요청은 HTML Form을 처리한 뒤 다른 GET 화면으로 redirect한다.

이 문서에서는 URL, Method, 입력값, 응답 상태와 캐시 기준을 정한다. 세션 쿠키, 로그인 복귀와 상세 PRG 흐름은 `request-flow.md`에서 다룬다.

## 2. 기본 원칙

1. URL은 `members`, `accounts`, `transactions`, `transfers` 같은 리소스 이름을 중심으로 작성한다.
2. 화면과 데이터 조회는 GET, 서버 상태 변경은 POST를 사용한다.
3. HTML Form을 기준으로 하므로 v1.0에서는 PUT, PATCH, DELETE를 사용하지 않는다.
4. POST 처리에 성공하면 PRG(Post/Redirect/Get)를 적용한다.
5. 경로에 있는 `accountId`를 권한 정보로 믿지 않고 Service에서 소유권을 다시 확인한다.
6. 로그인 상태는 회원 정보가 아닌 세션 ID 쿠키로 유지한다.

`/members/add`, `/accounts/add`, `/transfers/new`의 `add`와 `new`는 REST 리소스의 동작을 표현하려는 것이 아니라 HTML 입력 화면을 구분하기 위한 MVC 경로다.

### 2.1 입력값 표기

| 표기 | 의미 | 예시 |
|---|---|---|
| Path | URL 경로에 포함되는 필수값 | `/accounts/{accountId}` |
| Query | 조회 조건이나 선택적인 이동 정보 | `?fromAccountId=1` |
| Form | `application/x-www-form-urlencoded` 요청 본문 | `amount=10000` |

- `accountId`는 시스템 내부 식별자인 양의 정수다.
- 금액은 원 단위의 1 이상 정수이며 Java `long` 범위로 처리한다.
- 비밀번호와 금액은 쿼리 문자열에 넣지 않는다.

## 3. 리소스

| 리소스 | 식별자 | 관계와 사용 방식 |
|---|---|---|
| Member | `memberId` | 한 회원이 여러 계좌를 소유한다. 회원 ID는 URL에 직접 노출하지 않는다. |
| Account | `accountId`, `accountNumber` | URL에는 내부 ID, 이체 대상 입력에는 계좌번호를 사용한다. |
| AccountTransaction | `transactionId` | 계좌의 하위 리소스이며 수정·삭제하지 않는다. |
| Transfer | `transferGroupId` | 한 번의 이체에서 생긴 출금·입금 내역 2건을 연결한다. |
| Session | 세션 ID 쿠키 | 서버에서 로그인 회원을 찾는 데 사용한다. |

```text
Member 1 ── N Account 1 ── N AccountTransaction
                     \
                      └── Transfer가 두 계좌의 거래내역을 연결
```

## 4. URL·Method 매핑

| 영역 | 기능 | Method | URL | 인증 | 입력값 | 결과 |
|---|---|---|---|---|---|---|
| 공통 | 홈 | `GET` | `/` | 불필요 | 없음 | 홈 화면 |
| 공통 | 상태 확인 | `GET` | `/health` | 불필요 | 없음 | `200 OK`와 간단한 상태 문자열 |
| 회원 | 회원가입 폼 | `GET` | `/members/add` | 불필요 | 없음 | 회원가입 화면 |
| 회원 | 회원가입 처리 | `POST` | `/members/add` | 불필요 | Form: `loginId`, `password`, `name` | `/login`으로 redirect |
| 인증 | 로그인 폼 | `GET` | `/login` | 불필요 | 선택 Query: `redirectURL` | 로그인 화면 |
| 인증 | 로그인 처리 | `POST` | `/login` | 불필요 | Form: `loginId`, `password`, 선택 `redirectURL` | 안전한 원래 GET 경로 또는 `/accounts`로 redirect |
| 인증 | 로그아웃 | `POST` | `/logout` | 필요 | 없음 | 세션 종료 후 `/`로 redirect |
| 계좌 | 내 계좌 목록 | `GET` | `/accounts` | 필요 | 없음 | 본인 계좌 목록 |
| 계좌 | 계좌 개설 폼 | `GET` | `/accounts/add` | 필요 | 없음 | 계좌 개설 확인 화면 |
| 계좌 | 계좌 개설 처리 | `POST` | `/accounts/add` | 필요 | 없음 | `/accounts`로 redirect |
| 계좌 | 계좌 상세 | `GET` | `/accounts/{accountId}` | 필요 | Path: `accountId` | 본인 계좌 상세 화면 |
| 거래 | 입금 | `POST` | `/accounts/{accountId}/deposit` | 필요 | Path: `accountId`, Form: `amount` | 계좌 상세로 redirect |
| 거래 | 출금 | `POST` | `/accounts/{accountId}/withdraw` | 필요 | Path: `accountId`, Form: `amount` | 계좌 상세로 redirect |
| 이체 | 이체 폼 | `GET` | `/transfers/new` | 필요 | 선택 Query: `fromAccountId` | 이체 화면 |
| 이체 | 이체 처리 | `POST` | `/transfers/new` | 필요 | Form: `fromAccountId`, `toAccountNumber`, `amount` | 출금 계좌 상세로 redirect |
| 거래내역 | 계좌 거래내역 | `GET` | `/accounts/{accountId}/transactions` | 필요 | Path: `accountId` | 최신순 거래내역 화면 |

## 5. 기능별 결정사항

### 5.1 회원가입

- 폼 조회와 처리는 `/members/add`를 함께 사용하고 Method로 구분한다.
- 필수값 누락이나 로그인 ID 중복 시 회원을 저장하지 않는다.
- 검증 실패 화면에 비밀번호를 다시 노출하지 않는다.

### 5.2 로그인과 로그아웃

- 로그인은 세션을 만들기 때문에 POST로 처리한다.
- 로그아웃도 세션을 없애는 상태 변경이므로 POST만 제공한다.
- 로그인 성공 후에는 유효한 `redirectURL` 또는 `/accounts`로 이동한다.

### 5.3 계좌 개설

- v1.0에서는 계좌 이름과 초기 잔액을 입력받지 않는다.
- 시스템이 계좌번호를 만들고 잔액 0원의 계좌를 생성한다.
- `/accounts/add`의 GET 화면은 계좌 개설 여부를 확인하는 용도로 사용한다.

### 5.4 입금과 출금

- 입금·출금 폼은 계좌 상세 화면에 배치한다.
- 따라서 별도의 입금·출금 GET URL은 만들지 않는다.
- 두 요청 모두 계좌 존재 여부, 소유권, 금액을 확인하며 출금은 잔액도 확인한다.
- 성공한 잔액 변경과 거래내역 저장은 함께 처리한다.

### 5.5 계좌이체

- 출금 계좌는 내부 ID인 `fromAccountId`, 입금 계좌는 사용자가 확인할 수 있는 `toAccountNumber`로 받는다.
- 출금 계좌 소유권, 입금 계좌 존재 여부, 동일 계좌 여부, 금액과 잔액을 확인한다.
- 성공하면 두 계좌의 잔액과 같은 `transferGroupId`를 가진 거래내역 2건을 반영한다.
- HTTP 문서에서는 요청 계약만 정한다. 실제 원자성과 롤백은 DB1 단계의 `TransferService` 트랜잭션에서 보장한다.
- Controller는 `toAccountNumber`를 입금 계좌 ID로 바꾸지 않는다. 계좌 조회와 검증은 `TransferService`가 담당한다.

### 5.6 거래내역

- 거래내역은 계좌의 하위 리소스로 표현한다.
- 최신 거래부터 보여주며 개별 상세·수정·삭제 URL은 제공하지 않는다.

## 6. 인증과 권한

| URL | 비로그인 접근 | 로그인 후 확인할 것 |
|---|---|---|
| `/`, `/members/add`, `/login` | 허용 | 없음 |
| `/logout` | 차단 | 현재 세션 |
| `/accounts`, `/accounts/add` | 차단 | 로그인 회원 기준 조회·생성 |
| `/accounts/{accountId}` | 차단 | 계좌 존재 여부와 소유권 |
| `/accounts/{accountId}/deposit` | 차단 | 계좌 존재 여부, 소유권, 금액 |
| `/accounts/{accountId}/withdraw` | 차단 | 계좌 존재 여부, 소유권, 금액, 잔액 |
| `/accounts/{accountId}/transactions` | 차단 | 계좌 존재 여부와 소유권 |
| `/transfers/**` | 차단 | 출금 계좌 소유권, 입금 계좌 존재 여부, 동일 계좌 여부 |

Controller는 로그인 회원 ID와 요청값을 Service에 전달한다. 소유권, 잔액, 거래 가능 여부는 Service에서 판단한다.

## 7. 로그인 복귀 경로

`redirectURL`은 편의를 위한 값이지만 검증 없이 사용하면 외부 사이트로 보내는 오픈 리다이렉트가 될 수 있다.

- `/`로 시작하는 애플리케이션 내부 경로만 허용한다.
- `//example.com`, `http://...`, `https://...` 형태는 거부한다.
- 복귀 대상은 GET 화면으로 제한한다.
- 로그인 전에 차단된 POST 요청은 자동으로 재실행하지 않는다.
- 값이 없거나 유효하지 않으면 `/accounts`를 사용한다.
- 쿼리 파라미터로 전달할 때는 URL encoding을 적용한다.

## 8. 응답 흐름 기준

| 상황 | 상태 코드와 처리 방향 |
|---|---|
| GET 성공 | `200 OK`, Thymeleaf 화면 반환 |
| POST 성공 | 3xx redirect로 정해진 GET URL에 이동 |
| 폼 입력 검증 실패 | `200 OK`, 입력값과 오류를 담아 같은 폼 반환 |
| 비로그인 접근 | `302 Found`, 로그인 화면으로 redirect |
| 계좌 없음 | `404 Not Found`, 데이터 비노출 |
| 권한 없음 | `403 Forbidden`, 데이터 변경 없음 |
| 예상하지 못한 오류 | `500 Internal Server Error`, 관련 트랜잭션 롤백 |

구체적인 redirect 상태 코드는 Spring MVC 구현 단계에서 기본 동작을 확인한 뒤 정한다. 잔액 부족과 동일 계좌 이체처럼 사용자가 입력을 고칠 수 있는 오류는 데이터를 바꾸지 않고 해당 화면을 다시 보여준다. Repository 예외의 내부 메시지나 스택 트레이스는 사용자 화면에 노출하지 않는다.

## 9. 캐시 기준

| 응답 | `Cache-Control` | 이유 |
|---|---|---|
| 계좌·거래내역·이체 화면 | `no-store` | 잔액과 개인 금융 정보 저장 방지 |
| 로그인·회원가입 폼 | `no-store` | 인증 입력과 오류 화면 저장 방지 |
| 홈 화면 | `no-cache` | 재검증 후 최신 로그인 메뉴 표시 |
| 버전 없는 정적 리소스 | `public, max-age=3600` | 개발 중 변경 반영과 기본 캐시 균형 |
| 해시가 붙은 정적 리소스 | `public, max-age=31536000, immutable` | 파일명이 바뀔 때만 새 리소스 사용 |

v1.0에서 ETag나 조건부 요청을 직접 구현하지 않는다. Spring과 브라우저의 기본 동작을 사용하고, 개인 데이터 응답의 `no-store`만 명시적으로 확인한다.

## 10. 기능별 URL 확인

| 기능 | URL |
|---|---|
| 상태 확인 | `GET /health` |
| 회원가입 | `GET/POST /members/add` |
| 로그인 | `GET/POST /login` |
| 로그아웃 | `POST /logout` |
| 계좌 개설 | `GET/POST /accounts/add` |
| 내 계좌 목록 | `GET /accounts` |
| 계좌 상세 | `GET /accounts/{accountId}` |
| 입금 | `POST /accounts/{accountId}/deposit` |
| 출금 | `POST /accounts/{accountId}/withdraw` |
| 계좌이체 | `GET/POST /transfers/new` |
| 거래내역 | `GET /accounts/{accountId}/transactions` |

## 11. Controller 역할

| Controller | 담당 요청 |
|---|---|
| `HomeController` | 홈 |
| `HealthController` | 애플리케이션 상태 확인 |
| `MemberController` | 회원가입 폼·처리 |
| `LoginController` | 로그인·로그아웃 |
| `AccountController` | 계좌 목록·개설·상세, 입금·출금 |
| `TransferController` | 이체 폼·처리 |
| `TransactionController` | 계좌별 거래내역 |

Controller는 요청 바인딩, 검증 결과 처리와 화면 이동을 맡는다. 계좌 소유권, 잔액과 거래 규칙은 Service에서 처리한다.

## 12. 만들지 않는 URL

- 회원 정보 수정·탈퇴와 비밀번호 재설정
- 계좌 해지·삭제
- 거래내역 개별 상세·수정·삭제
- 이체 내역 별도 조회
- 관리자 기능
- REST JSON API와 Swagger/OpenAPI
- Spring Security 기반 인증 API

## 13. 구현 전 확인

- [x] 화면 조회와 상태 변경 요청의 URL·Method를 정리했다.
- [x] 각 POST 요청의 성공 후 이동 경로를 정했다.
- [x] 인증 여부, 입력값과 대표 오류 응답을 구분했다.
- [ ] Spring MVC의 실제 redirect 상태 코드를 확인한다.
- [ ] 개인 데이터와 정적 리소스의 캐시 헤더를 실행 환경에서 확인한다.

## 14. 관련 문서 상태

| 문서 | 정할 내용 | 상태 |
|---|---|---|
| `request-flow.md` | PRG, 세션 쿠키, 보호 경로와 로그인 복귀 | 완료 |
| `erd-draft.md` | 회원·계좌·거래내역 관계와 정합성 기준 | 구현 기준 승인 |
