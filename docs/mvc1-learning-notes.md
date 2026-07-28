# Spring MVC1 학습 노트

## 7/20 - 웹 애플리케이션 이해

### 학습 범위

- Spring MVC 1편 섹션 1 소개
- Spring MVC 1편 섹션 2 웹 애플리케이션 이해 전체
- 강의 자료: `스프링MVC-웹애플리케이션이해v2020-03-08-1759552.pdf`

오늘은 웹 애플리케이션의 기본 구조와 Servlet, 멀티스레드, SSR을 이해하는 날이다. 실행 가이드의 학습 게이트에 따라 Spring Boot 프로젝트와 Controller 코드는 아직 만들지 않는다.

---

## 1. HTTP와 웹 시스템

### HTTP로 주고받는 것

클라이언트와 서버는 HTTP 메시지로 통신한다. HTML과 일반 텍스트뿐 아니라 이미지, 음성, 영상, 파일, JSON과 XML처럼 거의 모든 형태의 데이터를 HTTP로 전송할 수 있다. 서버끼리 데이터를 주고받을 때도 HTTP를 사용할 수 있다.

### Web Server와 WAS

| 구분 | 주된 역할 | 예시 |
|---|---|---|
| Web Server | HTTP 요청을 받고 정적 리소스를 제공 | Nginx, Apache |
| WAS | Web Server 기능과 함께 프로그램 코드를 실행하여 동적 응답을 제공 | Tomcat, Jetty, Undertow |

정적 리소스는 HTML, CSS, JavaScript, 이미지처럼 이미 만들어진 파일이다. 동적 응답은 요청 정보와 데이터베이스 조회 결과 등에 따라 실행 시점에 만들어지는 HTML 또는 JSON이다.

두 서버의 기능 경계는 완전히 명확하지 않다. Web Server도 프로그램 실행 기능을 포함할 수 있고 WAS도 정적 리소스를 제공할 수 있다. Java에서는 Servlet Container 기능을 제공하는 서버를 보통 WAS라고 부른다.

### Web Server, WAS, DB 구성

간단한 시스템은 WAS와 DB만으로 구성할 수 있다. 규모가 커지면 Web Server가 정적 리소스를 담당하고, 프로그램 실행이 필요한 요청을 WAS에 넘기는 구조를 사용할 수 있다.

```text
Browser
  -> Web Server: 정적 HTML, CSS, JavaScript, 이미지
  -> WAS: Servlet과 애플리케이션 로직
  -> DB: 데이터 저장과 조회
```

역할을 분리하면 정적 리소스 요청과 애플리케이션 요청의 증가에 맞춰 Web Server와 WAS를 각각 확장할 수 있다. WAS나 DB에 장애가 생겼을 때 Web Server가 오류 화면을 제공하는 구성도 가능하다.

---

## 2. Servlet

### Servlet이 필요한 이유

Servlet 없이 웹 서버를 직접 구현하면 애플리케이션 개발자가 다음 작업을 모두 처리해야 한다.

- TCP/IP 연결과 소켓 관리
- HTTP 요청 메시지 파싱
- HTTP Method, URL과 Content-Type 확인
- 요청 본문에서 필요한 값 추출
- HTTP 응답 상태, Header와 Body 생성
- 응답 전송과 연결 종료

WAS와 Servlet이 네트워크 연결과 HTTP 메시지 처리를 맡아주기 때문에 개발자는 요청에서 필요한 값을 읽고 비즈니스 로직을 실행한 뒤 응답을 만드는 일에 집중할 수 있다.

### 요청과 응답 흐름

```text
1. Browser가 HTTP 요청 전송
2. WAS가 HTTP 요청을 파싱
3. WAS가 HttpServletRequest와 HttpServletResponse 생성
4. Servlet Container가 URL에 매핑된 Servlet의 service(request, response) 호출
5. Servlet이 요청을 읽고 응답 객체에 결과 작성
6. WAS가 HTTP 응답 메시지를 만들어 Browser에 전송
```

- `HttpServletRequest`는 HTTP 요청 정보를 편리하게 읽도록 돕는다.
- `HttpServletResponse`는 HTTP 응답 정보를 편리하게 작성하도록 돕는다.
- Servlet은 URL 매핑 정보에 따라 호출된다.

### Servlet Container

Tomcat처럼 Servlet을 지원하는 WAS를 Servlet Container라고 한다. Container는 Servlet의 생성, 초기화, 호출과 종료 생명주기를 관리한다. JSP도 내부에서는 Servlet으로 변환되어 사용된다.

Servlet 객체는 일반적으로 하나만 만들어 재사용된다. 객체를 요청마다 새로 만들지 않아 효율적이지만, 여러 요청이 같은 Servlet 인스턴스에 동시에 접근할 수 있다는 점을 주의해야 한다.

---

## 3. 멀티스레드

### Thread가 필요한 이유

Thread는 프로그램 코드를 실행하는 단위이다. 하나의 Thread가 한 요청을 처리하는 동안 다른 요청까지 같은 Thread가 맡으면, 앞의 처리가 끝날 때까지 뒤의 요청이 기다려야 한다. WAS는 여러 요청을 동시에 처리하기 위해 여러 Thread를 사용한다.

### 요청마다 새 Thread를 만드는 방식의 문제

요청마다 Thread를 새로 만들면 한 요청의 처리가 늦어져도 다른 요청을 처리할 수 있다. 그러나 Thread 생성 비용이 크고, Thread마다 메모리를 사용하며, Thread 전환에도 비용이 든다. 요청 수만큼 제한 없이 생성하면 CPU와 메모리의 한계를 넘어 서버가 응답하지 못할 수 있다.

### Thread Pool

WAS는 필요한 Thread를 미리 만들어 Thread Pool에서 관리한다. 요청이 오면 사용 가능한 Thread를 할당하고, 처리가 끝나면 Thread를 종료하지 않고 Pool에 반환하여 재사용한다. 최대 Thread 수에 도달하면 새 요청은 기다리거나 설정에 따라 거절된다.

- 최대 Thread 수가 너무 작으면 서버 자원이 남아도 요청 대기가 길어진다.
- 최대 Thread 수가 너무 크면 많은 요청이 들어왔을 때 CPU와 메모리가 고갈될 수 있다.
- 적절한 값은 애플리케이션 로직, CPU 연산량, 메모리 사용량과 외부 I/O 대기 시간에 따라 달라진다.
- 실제 서비스와 비슷한 조건에서 Apache ab, JMeter, nGrinder 같은 도구로 성능 테스트를 해야 한다.

강의 자료에서는 Tomcat의 최대 Thread 기본 설정 예시로 200개를 사용한다. 실제 값은 사용하는 버전과 서버 설정에서 다시 확인해야 한다.

### 개발자가 기억할 점

WAS가 Thread 생성과 Pool 관리를 담당하므로 Servlet 개발자가 Thread를 직접 생성하고 관리할 필요는 없다. Servlet 코드는 한 요청의 흐름에 집중해서 작성할 수 있지만, 여러 Thread가 접근하는 공유 객체와 공유 변수는 별개의 문제이므로 안전하게 다뤄야 한다.

---

## 4. 공유 상태가 위험한 이유

여러 요청은 서로 다른 Thread에서 실행되지만 같은 Servlet 인스턴스를 사용할 수 있다. 따라서 요청마다 달라지는 값을 Servlet의 필드에 저장하면 다음과 같은 문제가 발생할 수 있다.

```text
요청 A가 Servlet 필드에 username=kim 저장
-> 요청 A의 처리가 끝나기 전에 요청 B가 username=lee로 변경
-> 요청 A가 lee를 자신의 값으로 읽는 오류 발생
```

요청 전용 값은 메서드의 지역 변수로 사용하거나 요청 객체를 통해 전달해야 한다. 여러 Thread가 함께 사용하는 상태는 값이 동시에 변경될 가능성을 먼저 생각해야 한다.

MiniBank에서는 잔액처럼 여러 요청이 동시에 변경할 수 있는 값이 특히 중요하다. 다만 잔액 정합성의 실제 구현은 JDBC와 트랜잭션을 학습한 뒤 진행하며, 7/20에는 공유 상태가 왜 위험한지만 이해한다.

---

## 5. 정적 리소스, 동적 HTML과 HTTP API

### 정적 리소스

서버가 미리 저장된 HTML, CSS, JavaScript, 이미지와 영상 파일을 그대로 전달한다.

### 동적 HTML

WAS가 데이터베이스 조회와 애플리케이션 로직을 실행한 뒤 완성된 HTML을 만들어 전달한다. 브라우저는 받은 HTML을 해석해서 화면에 표시한다.

### HTTP API

서버가 HTML 대신 주로 JSON 형식의 데이터를 전달한다. 앱, 별도의 웹 클라이언트 또는 다른 서버가 데이터를 받아 각자의 방식으로 처리한다. HTTP API는 모바일 앱과 서버, JavaScript 웹 클라이언트와 서버, 서버와 서버 사이의 통신에 사용할 수 있다.

---

## 6. SSR과 CSR

| 구분 | SSR | CSR |
|---|---|---|
| HTML 생성 위치 | 서버 | 브라우저 |
| 최초 응답 | 완성된 HTML | HTML 틀과 JavaScript |
| 이후 데이터 처리 | 새 HTML을 서버에서 받아 표시 | HTTP API로 데이터를 받고 JavaScript로 화면 변경 |
| 잘 맞는 화면 | 비교적 정적이고 단순한 화면 | 복잡하고 동적인 사용자 경험 |
| 대표 기술 | JSP, Thymeleaf | React, Vue.js |

SSR에서는 서버가 최종 HTML을 만들어 브라우저에 전달한다. CSR에서는 브라우저가 HTML과 JavaScript를 받은 뒤 HTTP API로 데이터를 요청하고, JavaScript가 결과 화면을 만든다. React와 Vue.js를 사용하더라도 CSR과 SSR을 함께 사용할 수 있으므로 두 방식이 언제나 완전히 분리되는 것은 아니다.

HTTP API와 SSR은 같은 기준의 반대말이 아니다. HTTP API는 서버가 데이터 중심의 응답을 제공하는 방식이고, SSR은 최종 HTML을 서버에서 만드는 렌더링 방식이다.

### MiniBank가 SSR 웹 애플리케이션인 이유 - 3문장

MiniBank의 핵심 화면은 회원가입, 로그인, 계좌 조회처럼 서버의 데이터를 보여주고 Form을 제출하는 흐름이므로 복잡한 클라이언트 렌더링이 필요하지 않다. 여러 종류의 외부 클라이언트에 공개할 JSON API가 현재 범위에 없으므로 별도의 프론트엔드 애플리케이션을 두면 학습 범위와 구조만 불필요하게 커진다. 따라서 Spring MVC와 Thymeleaf를 사용하는 SSR 방식을 선택해 HTTP 요청 흐름, Servlet, 검증, 세션과 비즈니스 로직 학습에 집중한다.

---

## 7. Java 웹 기술의 흐름

```text
Servlet
-> JSP
-> Servlet + JSP를 조합한 MVC 패턴
-> 여러 MVC Framework
-> Spring MVC
-> Spring Boot와 내장 WAS
```

- Servlet은 HTML 응답을 Java 코드로 작성하기 불편했다.
- JSP는 HTML 작성은 편했지만 화면과 비즈니스 로직이 섞이기 쉬웠다.
- MVC 패턴과 MVC Framework는 Controller, View와 Model의 역할을 나누는 방향으로 발전했다.
- Spring MVC는 Java 웹 애플리케이션의 대표적인 MVC Framework가 되었다.
- Spring Boot는 WAS를 포함한 실행 가능한 JAR를 만들 수 있어 별도의 WAS 설치와 WAR 배포 과정을 단순화했다.
- Spring WebFlux는 비동기·논블로킹 처리와 적은 Thread로 높은 동시 처리를 지향하지만 현재 MiniBank의 학습·구현 범위에는 포함하지 않는다.

서버에서 HTML을 만드는 View 기술로는 JSP, FreeMarker, Velocity와 Thymeleaf 등이 있다. MiniBank에서는 자연스러운 HTML 형태를 유지하고 Spring MVC와 통합하기 좋은 Thymeleaf를 사용할 예정이다.

---

## 8. 7/20 완료 확인

- [x] MiniBank가 JSON API 중심이 아니라 SSR 웹 애플리케이션인 이유를 3문장으로 정리했다.
- [x] `Browser -> WAS -> Servlet -> HTTP 응답` 흐름을 정리했다.
- [x] 멀티스레드 환경에서 공유 상태가 위험한 이유를 정리했다.
- [x] 오늘은 Spring Boot 프로젝트와 Controller를 구현하지 않는다.
- [ ] 노트를 보지 않고 Web Server와 WAS, Servlet과 Thread Pool, SSR과 CSR의 차이를 직접 설명해 본다.

### 다음 학습 게이트

MVC1 서블릿 섹션의 프로젝트 생성 강의를 마친 뒤에만 Spring Boot 프로젝트를 생성한다. 그전에는 `HomeController`, 회원가입 코드와 Thymeleaf 기반 MiniBank 화면을 만들지 않는다.

---

## 7/22 - 서블릿 요청·응답

### Form 파라미터

HTML Form의 `name` 값과 Servlet에서 읽는 파라미터 이름은 같아야 한다.

```text
<input name="loginId">
-> request.getParameter("loginId")
```

실습에서는 `username`, `age`를 POST Form으로 전송했고, `RequestParamServlet`에서 `request.getParameter()`로 값을 확인했다. MiniBank 회원가입 Form에는 나중에 `loginId`, `password`, `name`을 사용한다.

### 요청 본문

`POST /request-body-string` 요청의 본문은 `request.getInputStream()`으로 읽을 수 있다. `StreamUtils.copyToString(..., UTF_8)`으로 문자열로 변환해 콘솔에 출력했다.

### Redirect

`response.sendRedirect("/request-param-form.html")`은 브라우저에 `302`와 `Location` 헤더를 보낸다. 브라우저는 Location 주소로 새 GET 요청을 보낸다.

### 7/21~7/22 Servlet 핵심 함수

| 구분 | 함수·도구 | 언제 사용하는가 |
|---|---|---|
| Servlet 등록 | `@WebServlet(urlPatterns = "/...")` | URL과 Servlet을 연결할 때 사용한다. |
| 요청 처리 | `service(request, response)` | 모든 HTTP Method를 직접 처리할 때 사용한다. `HttpServlet`의 기본 `service()`는 Method에 따라 `doGet()`·`doPost()`로 분기한다. |
| GET·POST 분리 | `doGet()`, `doPost()` | GET과 POST의 처리 흐름이 다를 때 각각 구현한다. |
| 요청 Method 확인 | `request.getMethod()` | 현재 요청이 GET·POST 등 무엇인지 확인한다. |
| URL·쿼리 확인 | `request.getRequestURI()`, `request.getQueryString()` | 요청 경로와 `?key=value` 부분을 확인한다. |
| Form·쿼리 단일값 | `request.getParameter("name")` | URL 쿼리나 `application/x-www-form-urlencoded` Form의 값을 읽는다. HTML의 `name`과 문자열을 같게 쓴다. |
| 같은 이름의 여러 값 | `request.getParameterValues("name")` | 체크박스처럼 같은 이름으로 여러 값을 보냈을 때 사용한다. |
| 전체 Form·쿼리 값 | `request.getParameterNames()` | 들어온 파라미터 이름 전체를 확인할 때 사용한다. |
| POST 한글 처리 | `request.setCharacterEncoding("UTF-8")` | 파라미터를 읽기 전에 요청 본문의 문자 인코딩을 지정한다. |
| 헤더 확인 | `request.getHeader("...")`, `request.getHeaderNames()` | `User-Agent`, `Content-Type` 같은 요청 헤더를 확인한다. |
| 본문 읽기 | `request.getInputStream()` | JSON이나 일반 텍스트처럼 요청 본문을 직접 읽을 때 사용한다. `getParameter()`로 JSON 본문을 읽을 수는 없다. |
| 본문 문자열 변환 | `StreamUtils.copyToString(inputStream, UTF_8)` | `InputStream`으로 읽은 바이트를 UTF-8 문자열로 바꾼다. |
| 응답 종류 지정 | `response.setContentType("text/html")` | 응답 본문이 HTML, JSON, 일반 텍스트 중 무엇인지 브라우저에 알린다. JSON은 `application/json`을 쓴다. |
| 응답 인코딩 | `response.setCharacterEncoding("UTF-8")` | 한글 등 응답 문자열의 문자 인코딩을 지정한다. |
| 응답 본문 작성 | `response.getWriter().write("...")` | HTML·일반 텍스트·JSON 문자열을 응답 본문에 쓴다. |
| redirect | `response.sendRedirect("/path")` | 브라우저에 302와 `Location`을 보내 다른 URL로 새 요청하게 한다. |

### JSON과 ObjectMapper

`ObjectMapper`는 Java 객체와 JSON 문자열을 서로 변환하는 Jackson 도구다. HTML Form 값을 읽는 `getParameter()`와 용도가 다르다.

| 상황 | 순서 |
|---|---|
| JSON 요청을 객체로 읽기 | `getInputStream()` → 문자열 변환 → `objectMapper.readValue(json, 대상클래스)` |
| Java 객체를 JSON 응답으로 보내기 | `objectMapper.writeValueAsString(객체)` → `setContentType("application/json")` → `getWriter().write(json)` |

예를 들어 Java 객체를 JSON 응답으로 바꿀 때는 `ObjectMapper`의 `writeValueAsString()`이 JSON 문자열을 만들고, `HttpServletResponse`가 그 문자열을 브라우저에 보낸다. JSON 요청을 받을 때는 반대로 `readValue()`로 JSON 문자열을 Java 객체로 바꾼다.

현재 MiniBank는 SSR Form 기반 애플리케이션이므로 JSON API를 구현하지 않는다. 여기서는 Servlet 요청·응답의 데이터 형식을 구분하기 위한 학습 내용으로만 기록한다.

### 완료 확인

- [x] POST Form 파라미터 확인
- [x] 요청 본문 문자열 확인
- [x] redirect 응답 확인
- [x] MiniBank Form 입력값과 Servlet 파라미터 이름의 관계 확인

### 다음 학습 게이트

서블릿·JSP·MVC 패턴의 회원 예제에서 도메인과 저장소를 확인한 뒤에만 `Member`와 `MemoryMemberRepository`를 만든다.

## 7/24 - MiniBank 계층별 역할

| Controller | Service | Repository |
|---|---|---|
| HTTP 요청과 파라미터를 받고, Service를 호출한 뒤 응답 화면이나 redirect 경로를 결정한다. | 로그인 ID 중복, 계좌 소유권, 잔액과 거래 가능 여부 같은 비즈니스 규칙을 판단한다. | 회원과 계좌 같은 데이터를 저장하고 조회하며, 저장 방식의 세부 구현을 담당한다. |

MiniBank의 요청 처리는 `Controller -> Service -> Repository` 방향으로 흐른다. Controller는 웹 요청과 응답에 집중하고 비즈니스 규칙을 Service에 위임하며, Service는 데이터가 필요할 때 Repository 인터페이스를 사용한다. Controller가 Repository를 직접 호출하지 않으면 웹 흐름과 저장 방식을 분리할 수 있고, 나중에 Memory Repository를 JDBC Repository로 교체해도 Controller의 흐름을 유지할 수 있다.

## 7/25 - FrontController와 MVC 프레임워크

### 기존 MVC Controller마다 반복되던 공통 작업

Servlet과 JSP로 MVC를 직접 구성하면 각 Controller가 Servlet으로 등록되고, 처리 결과를 보여줄 JSP 경로를 만든 뒤 `RequestDispatcher`로 `forward`하는 코드가 반복된다. 요청 URI에 맞는 Controller를 찾는 일, View를 호출하는 일과 JSP 경로의 공통 접두사·접미사를 붙이는 일은 개별 기능보다 MVC 프레임워크에 가까운 공통 작업이다.

MiniBank에서도 회원가입, 로그인 폼, 계좌 개설과 계좌 목록 Controller마다 이런 코드를 직접 작성하면 실제 요청 처리보다 기반 코드가 더 많이 반복된다.

### FrontController가 공통 처리를 맡는 장점

FrontController는 모든 요청의 공통 입구가 되어 요청 URI에 맞는 Controller를 찾고, Controller가 반환한 결과를 View로 연결한다. 공통 처리를 한곳에 두면 개별 Controller는 회원가입이나 계좌 조회처럼 자신이 담당하는 요청에 집중할 수 있고, 공통 동작을 변경할 때도 여러 Controller를 각각 수정할 필요가 없다.

```text
Browser
  -> FrontController
  -> URI에 맞는 Controller 조회와 호출
  -> ModelView 반환
  -> ViewResolver로 실제 View 찾기
  -> View 렌더링
  -> HTTP 응답
```

### Controller 인터페이스가 필요한 이유

FrontController가 여러 Controller를 같은 방식으로 호출하려면 공통 호출 규칙이 필요하다. Controller 인터페이스는 입력으로 무엇을 받고 결과로 무엇을 반환할지 정하므로, FrontController는 회원 Controller인지 계좌 Controller인지 알지 못해도 동일한 메서드로 실행할 수 있다.

MiniBank에서는 회원가입과 계좌 목록의 처리 내용은 다르지만, 요청을 처리하고 Model과 View 정보를 반환한다는 규칙은 같게 만들 수 있다. 새로운 기능을 추가할 때도 이 규칙을 구현한 Controller를 Controller Map에 등록하면 FrontController의 기본 흐름을 유지할 수 있다.

### ModelView에 들어가는 정보

ModelView에는 응답에 사용할 논리적인 View 이름과 View에 전달할 Model 데이터가 들어간다.

예를 들어 MiniBank 계좌 목록 요청이라면 다음과 같이 표현할 수 있다.

```text
viewName: "accounts/list"
model:
  memberName -> "홍길동"
  accounts   -> 회원의 계좌 목록
```

Controller는 `HttpServletRequest`에 직접 데이터를 저장하거나 JSP로 `forward`하지 않고 ModelView를 반환한다. 따라서 Controller는 Servlet 기술에 대한 의존을 줄이고 요청 처리 결과를 만드는 역할에 더 집중할 수 있다.

### View 이름과 실제 JSP 경로를 분리하는 이유

Controller가 `/WEB-INF/views/accounts/list.jsp` 같은 실제 경로를 직접 반환하면 모든 Controller에 `/WEB-INF/views/`와 `.jsp`가 반복된다. 대신 `accounts/list`라는 논리적인 View 이름만 반환하고 ViewResolver가 접두사와 접미사를 붙이면 중복을 없앨 수 있다.

```text
논리 View 이름: accounts/list
ViewResolver:   /WEB-INF/views/ + accounts/list + .jsp
실제 JSP 경로: /WEB-INF/views/accounts/list.jsp
```

View 파일의 기본 위치나 View 기술이 바뀌어도 Controller의 논리 이름은 유지할 수 있다는 장점도 있다. 강의의 직접 만든 프레임워크는 JSP를 사용하지만, MiniBank의 실제 화면은 이후 Spring MVC와 Thymeleaf로 구현한다.

### FrontController와 일반 Controller의 역할 차이

| 구분 | 역할 |
|---|---|
| FrontController | 요청을 한곳에서 받고, URI에 맞는 Controller를 찾고, 호출 결과를 ViewResolver와 View에 연결한다. |
| 일반 Controller | 담당 요청의 파라미터를 읽고 필요한 Service를 호출한 뒤 Model 데이터와 논리 View 이름을 반환한다. |

MiniBank의 FrontController에 해당하는 프레임워크 영역은 전체 요청 흐름을 조정하고, 회원이나 계좌 Controller는 각 기능의 웹 요청을 처리한다. 비즈니스 규칙은 일반 Controller에도 두지 않고 `MemberService`, 이후의 `AccountService` 같은 Service에 맡긴다.

### MiniBank에 직접 만든 MVC 프레임워크를 적용하지 않는 이유

이번 학습의 목적은 FrontController 구조를 직접 만들어 보며 Spring MVC가 해결하는 공통 문제를 이해하는 것이다. MiniBank 기능을 강의용 프레임워크 위에 다시 구현하지 않고, Spring MVC 구조 이해 섹션을 마친 뒤 Spring MVC Controller로 구현한다.

### 완료 확인

- [x] 기존 Controller에서 반복되는 View 이동 코드를 설명할 수 있다.
- [x] FrontController가 공통 처리를 담당하는 이유를 설명할 수 있다.
- [x] Controller 인터페이스가 공통 호출 규칙을 만든다는 점을 설명할 수 있다.
- [x] ModelView의 논리 View 이름과 Model 데이터를 설명할 수 있다.
- [x] ViewResolver가 논리 View 이름을 실제 경로로 바꾸는 과정을 설명할 수 있다.
- [x] FrontController와 일반 Controller의 역할 차이를 MiniBank 요청에 연결했다.
- [ ] 강의 예제 코드를 실행하고 요청 흐름을 노트 없이 설명해 본다.

### 다음 학습 게이트

MVC 프레임워크 만들기 후반을 마친 뒤 직접 만든 구조와 Spring MVC의 구성 요소가 어떻게 대응하는지 정리한다. `HomeController`는 Spring MVC 구조 이해 섹션을 완료한 뒤에 구현한다. `HealthController`와 `GET /health` 상태 문자열 응답은 Spring MVC 기본 기능에서 `@ResponseBody`를 학습한 뒤에 구현한다.

## 7/26 - 직접 만든 MVC 프레임워크와 Spring MVC의 대응

### 구성 요소의 대응 관계

| 직접 만든 MVC 프레임워크 | Spring MVC | 역할 |
|---|---|---|
| FrontController | DispatcherServlet | 요청을 공통 입구에서 받아 전체 처리 흐름을 조정한다. |
| Controller Map | HandlerMapping | 요청 URL에 맞는 Controller 또는 Handler를 찾는다. |
| Controller 호출 방식 | HandlerAdapter | 서로 다른 형태의 Handler를 정해진 방식으로 호출한다. |
| ModelView | ModelAndView | View에 전달할 Model과 논리 View 이름을 함께 담는다. |
| MyView | View | Model 데이터를 사용해 응답 화면을 렌더링한다. |
| viewName 변환 | ViewResolver | 논리 View 이름으로 실제 View를 찾는다. |

직접 만든 프레임워크에서는 FrontController가 Controller Map에서 요청 URI에 맞는 Controller를 찾았다. Controller의 형식이 달라지면 FrontController가 각 형식을 직접 알아야 하므로, 공통 호출 방법을 제공하는 Adapter를 두어 다양한 Controller를 같은 흐름에서 실행할 수 있게 했다.

Spring MVC도 같은 원리로 `DispatcherServlet`이 `HandlerMapping`을 통해 Handler를 찾고, 그 Handler를 지원하는 `HandlerAdapter`를 사용해 호출한다. 따라서 DispatcherServlet은 Controller의 구체적인 호출 방법을 직접 알지 않아도 된다.

### 직접 만든 MVC 프레임워크의 요청 흐름

```text
Browser
  -> FrontController가 요청을 받음
  -> Controller Map에서 요청 URI에 맞는 Controller를 찾음
  -> Controller를 지원하는 Adapter를 찾음
  -> Adapter가 Controller를 호출
  -> ModelView를 반환
  -> ViewResolver가 논리 View 이름으로 MyView를 찾음
  -> MyView가 Model을 사용해 JSP를 렌더링
  -> HTTP 응답
```

### Spring MVC 요청 흐름과 비교

향후 구현할 MiniBank 회원가입 화면 요청을 예로 들면 다음과 같은 흐름으로 대응시킬 수 있다. 아직 `MemberController`를 구현한 것은 아니며, Spring MVC의 실제 내부 흐름은 `스프링 MVC 구조 이해` 섹션에서 확인한다.

```text
GET /members/add
  -> DispatcherServlet이 요청을 받음
  -> HandlerMapping이 요청을 처리할 MemberController의 Handler를 찾음
  -> DispatcherServlet이 Handler를 실행할 HandlerAdapter를 찾음
  -> HandlerAdapter가 Handler를 호출
  -> 처리 결과가 Model과 논리 View 이름으로 정리됨
  -> ViewResolver가 논리 View 이름에 해당하는 View를 찾음
  -> Thymeleaf View가 Model 데이터로 HTML을 렌더링
  -> 렌더링된 HTML을 HTTP 응답으로 전달
```

강의에서 직접 만든 프레임워크는 JSP를 렌더링하지만 MiniBank는 이후 Thymeleaf를 사용한다. View 기술이 달라도 Controller가 논리 View 이름과 Model을 반환하고, ViewResolver가 실제 View를 찾는 역할 분리는 유지된다.

### 7/26 완료 확인

- [x] FrontController와 DispatcherServlet의 역할을 대응시켰다.
- [x] Controller Map, HandlerMapping과 HandlerAdapter의 역할 차이를 정리했다.
- [x] 직접 만든 프레임워크와 Spring MVC의 요청 흐름을 비교했다.
- [x] MiniBank 기능과 Spring MVC Controller를 미리 구현하지 않았다.
- [ ] 요청 흐름을 노트 없이 직접 설명해 본다.

### 다음 학습 게이트

Spring MVC 구조 이해 섹션을 완료한 뒤 `HomeController`와 `GET /`를 구현한다. `HealthController`와 `GET /health` 상태 문자열 응답은 Spring MVC 기본 기능에서 `@ResponseBody`를 학습한 뒤에 구현한다. 회원가입 Controller는 요청 매핑과 파라미터를 학습한 뒤에 구현한다.

## 7/27 - Spring MVC 구조 이해

- Spring MVC1 섹션 6을 모두 수강했다.
- `HomeController`에 `GET /`를 매핑하고 `home` View를 반환하도록 구현했다.
- `templates/home.html`을 작성하고 브라우저에서 홈 화면을 확인했다.
- `DispatcherServlet -> HandlerMapping -> HandlerAdapter -> Controller -> ViewResolver` 처리 흐름을 설명했다.
- 전체 테스트가 통과했다.

현재 홈 화면은 동작까지 확인했지만, Controller 기반 Thymeleaf View의 정식 학습 완료 판정은 `HTTP 응답 - 정적 리소스, 뷰 템플릿` 수강 후 다시 확인한다.

### 다음 학습 게이트

7/28에는 Spring MVC 기본 기능의 `프로젝트 생성`, `로깅 간단히 알아보기`, `요청 매핑`을 학습한다. 요청 매핑 예제까지 확인한 뒤 `MemberController`의 `GET /members/add`, `POST /members/add`, `GET /login` 매핑만 작성한다. Form 바인딩과 회원 저장은 7/29의 요청 파라미터 강의 이후, `HealthController`와 `GET /health`는 7/30의 `HTTP 응답 - HTTP API, 메시지 바디에 직접 입력` 수강 이후에 구현한다.
