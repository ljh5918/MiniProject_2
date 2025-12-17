MovieSense – TMDB 기반 영화 추천 서비스

---

## 1. 프로젝트 소개

**MovieSense**는 TMDB(The Movie Database) 외부 API를 활용하여 실제 영화 데이터를 기반으로 한 **영화 추천 웹 서비스**입니다.
Spring Boot 기반 REST API 서버와 HTML + JavaScript 프론트엔드를 분리하여 구현하였으며, JWT + HttpOnly Cookie 인증 방식을 적용해 보안성과 사용자 경험을 동시에 고려하였습니다.

본 프로젝트는 **DB JOIN 활용**, **외부 API 연동**, **JWT 인증/인가**, **Service Layer 테스트**를 핵심 목표로 하는 미니프로젝트입니다.

---

## 2. 개발 환경

### Backend

* Java 17
* Spring Boot 3.x
* Spring Security
* Spring Data JPA
* JWT (Json Web Token)
* MySQL / H2 (개발 환경)
* JUnit5

### Frontend

* HTML5
* CSS3
* JavaScript (ES6)
* Fetch API

### External API

* TMDB (The Movie Database)

---

## 3. 프로젝트 구조

src/main/java/com/mycom/myapp
├─ comment         # 댓글 도메인
├─ contorller 	   # PageController
├─ favorite        # 찜(즐겨찾기) 도메인
├─ movie           # 영화 도메인 (TMDB 연동 + 캐싱)
├─ user            # 사용자 / 인증 도메인
├─ mypage          # 마이페이지 집계 컨트롤러
├─ global          # 공통 설정 (Security, JWT 등)
└─ MiniProject2Application.java

src/main/resources
├─ static
│  ├─ css
│  └─ js
├─ index.html
├─ login.html
├─ mypage.html
├─ register.html
└─ savedmovies.html

---

## 4. 주요 기능

### 4.1 사용자 인증

* 회원가입 / 로그인 / 로그아웃
* JWT 기반 인증
* **HttpOnly Cookie**에 Access Token 저장
* `/user/me` API를 통한 로그인 상태 확인

### 4.2 영화 정보

* TMDB 인기 영화 조회
* 영화 검색 (DB 우선 조회 → TMDB API 호출)
* 영화 상세 정보 조회

### 4.3 찜(Favorite)

* 영화 찜 추가 / 삭제 (토글)
* 내가 찜한 영화 목록 조회
* User + Favorite + Movie **JOIN 쿼리 사용**

### 4.4 댓글(Comment)

* 영화별 댓글 조회
* 댓글 작성 / 삭제 (작성자 권한 체크)
* User + Comment + Movie **JOIN 쿼리 사용**

### 4.5 추천 시스템

* 사용자의 최근 찜한 영화를 기준으로 TMDB 추천 API 호출
* 로그인 사용자에게 개인화 추천 제공

---

## 5. DB 설계 개요

### 핵심 설계 포인트

* **Movie 테이블은 TMDB 영화 ID를 PK로 사용 **
* Favorite, Comment 테이블에서 Movie를 FK로 참조
* 찜/댓글 발생 시 Movie 정보 자동 캐싱

### 주요 테이블

* users : 사용자 계정 정보
* movie : TMDB 영화 캐싱 정보
* favorite : 사용자 찜 정보
* comment : 영화 댓글 정보

---

## 6. 핵심 로직 설명

### Movie 캐싱 로직

```java
public Movie getOrSaveMovie(Long tmdbId) {
    return movieRepository.findById(tmdbId)
        .orElseGet(() -> {
            TmdbDto info = tmdbClient.getMovieInfo(tmdbId);
            Movie movie = new Movie(info.getId(), info.getTitle(), info.getPosterPath());
            return movieRepository.save(movie);
        });
}
```

* 찜 또는 댓글 작성 시 Movie 테이블에 데이터가 없으면 TMDB API 호출 후 저장
* 이후 JOIN 쿼리에서 재사용

---

## 7. REST API 요약

### 인증 (/user)

* POST /user/register : 회원가입
* POST /user/login : 로그인 (JWT 발급, Cookie 저장)
* POST /user/logout : 로그아웃
* GET /user/me : 로그인 사용자 정보

### 영화 (/movies)

* GET /movies/popular : 인기 영화 조회
* GET /movies/search : 영화 검색
* GET /movies/{id} : 영화 상세
* GET /movies/{id}/recommend : 연관 추천

### 찜 (/favorites)

* GET /favorites/list : 찜 목록 조회
* POST /favorites/add : 찜 추가
* DELETE /favorites/delete : 찜 삭제

### 댓글 (/comments)

* GET /comments?movieId={id} : 댓글 조회
* POST /comments : 댓글 작성
* PUT / comments?commentId={id} : 댓글 수정
* DELETE /comments?commentId={id} : 댓글 삭제

---

## 8. 테스트

* Service Layer 중심 단위 테스트 작성

* 주요 테스트 항목

  * 회원가입 성공
  * 영화 캐싱 로직 성공
  * 찜하기 성공
  * 댓글 작성 성공
  * 추천 목록 조회 성공

* 테스트 커버리지 **30% 이상 달성**

---

## 9. 실행 방법

1. 프로젝트 클론
2. `application.properties`에 DB 및 TMDB API Key 설정
3. Spring Boot 애플리케이션 실행
4. 브라우저에서 `http://localhost:8080` 접속

---

## 10. 기대 효과 및 배운 점

* 외부 API 연동 및 데이터 캐싱 전략 경험
* JWT + Spring Security 인증 흐름 이해
* DB JOIN 기반 데이터 설계 및 조회 경험
* 테스트 코드 작성을 통한 품질 관리 경험

---

## 11. 향후 개선 사항

* 검색 기능 고도화 (DB 중심 검색)
* 페이징 처리 및 아카이브 페이지 추가
* 데이터 시각화 기능 추가
* CI/CD 및 배포 환경 구성

---

## 12. 팀 정보

* 프로젝트명 : MovieSense
* 개발 인원 : 2명
* 개발 기간 : 7일

---

**© 2025 MovieSense Team**
