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
