////const IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
////// ▼ [추가] 포스터 없을 때 보여줄 대체 이미지 (검은 배경 + No Poster 텍스트)
////const NO_POSTER_URL = "https://placehold.co/300x450/000000/ffffff?text=No+Poster";
////// 1. 페이지 로드 시 실행
////window.onload = function() {
////    checkLoginStatus(); // 로그인 상태 확인
////    
////    // 인기 영화 불러오기 (공통)
////    fetch('/test/popular')
////        .then(response => response.json())
////        .then(data => renderMovies(data, 'movieContainer'));
////};
////
////// 2. 로그인 상태 체크 및 화면 전환
////function checkLoginStatus() {
////    const isLoggedIn = localStorage.getItem("isLoggedIn"); // 브라우저 저장소 확인
////
////    if (isLoggedIn === "true") {
////        // 로그인 상태 UI
////        document.getElementById("guestNav").style.display = "none";
////        document.getElementById("userNav").style.display = "block";
////        document.getElementById("personalSection").style.display = "block";
////        document.getElementById("userName").innerText = "김개발"; // 가짜 이름
////
////        // 개인 추천 영화 로드 (일단 인기 영화 API 재활용하거나 다른 API 호출)
////        fetch('/test/popular') 
////            .then(res => res.json())
////            .then(data => renderMovies(data.slice(5, 10), 'personalContainer')); // 5개만 슬쩍 보여주기
////    } else {
////        // 비로그인 상태 UI
////        document.getElementById("guestNav").style.display = "block";
////        document.getElementById("userNav").style.display = "none";
////        document.getElementById("personalSection").style.display = "none";
////    }
////}
////
////// 3. [시뮬레이션] 가짜 로그인
////function fakeLogin() {
////    localStorage.setItem("isLoggedIn", "true");
////    alert("로그인 되었습니다! (테스트용)");
////    location.reload(); // 새로고침해서 UI 반영
////}
////
////// 4. [시뮬레이션] 가짜 로그아웃
////function fakeLogout() {
////    localStorage.removeItem("isLoggedIn");
////    alert("로그아웃 되었습니다.");
////    location.reload();
////}
////
////// 5. [기능] 찜하기 버튼 토글
////function toggleLike() {
////    const isLoggedIn = localStorage.getItem("isLoggedIn");
////    if (isLoggedIn !== "true") {
////        alert("로그인이 필요한 서비스입니다.");
////        return;
////    }
////    alert("영화가 찜 목록에 저장되었습니다! (DB 연동 예정)");
////}
////
////// --- 아래는 기존 검색, 렌더링, 모달 관련 코드 (그대로 유지) ---
////
////function searchMovies() {
////    const query = document.getElementById('searchInput').value;
////    if (!query) return alert("검색어를 입력하세요!");
////    
////    document.getElementById('sectionTitle').innerText = `'${query}' 검색 결과`;
////    fetch(`/test/search?q=${query}`)
////        .then(res => res.json())
////        .then(data => renderMovies(data, 'movieContainer'));
////}
////
////function renderMovies(movies, containerId) {
////    const container = document.getElementById(containerId);
////    container.innerHTML = '';
////    if (!movies || movies.length === 0) {
////        container.innerHTML = '<p>결과가 없습니다.</p>';
////        return;
////    }
////    movies.forEach(movie => {
////        const posterSrc = movie.poster_path ? IMAGE_BASE_URL + movie.poster_path : NO_POSTER_URL;
////        const card = document.createElement('div');
////        card.className = 'movie-card';
////        card.onclick = () => openModal(movie.id);
////        card.innerHTML = `<img src="${posterSrc}" alt="${movie.title}"><h3>${movie.title}</h3>`;
////        container.appendChild(card);
////    });
////}
////
////// 상세 정보 모달 열기 (속도 개선 버전)
////function openModal(movieId) {
////    const modal = document.getElementById('movieModal');
////    
////    // 1. [즉시 실행] 모달 창부터 띄운다 (기다리지 않음!)
////    modal.style.display = 'block';
////    document.body.style.overflow = 'hidden'; // 뒤에 배경 스크롤 막기
////
////    // 2. [로딩 상태] 기존 데이터를 비우거나 '로딩중' 표시
////    document.getElementById('modalPoster').src = "https://placehold.co/300x450/000000/ffffff?text=Loading...";
////    document.getElementById('modalTitle').innerText = "로딩 중...";
////    document.getElementById('modalOverview').innerText = "";
////    document.getElementById('recommendContainer').innerHTML = ""; // 추천 목록 비우기
////
////    // 3. [비동기] 그 다음에 데이터를 가져와서 채워넣는다 (사용자는 창이 뜬 상태에서 기다림)
////    fetch(`/test/detail/${movieId}`)
////        .then(res => res.json())
////        .then(movie => {
////            document.getElementById('modalTitle').innerText = movie.title;
////            document.getElementById('modalOverview').innerText = movie.overview || " ";
////            document.getElementById('modalPoster').src = movie.poster_path 
////                ? IMAGE_BASE_URL + movie.poster_path 
////                : "https://placehold.co/300x450/000000/ffffff?text=No+Poster";
////            
////            // 추천 영화 가져오기
////            return fetch(`/test/recommend/${movieId}`);
////        })
////        .then(res => res.json())
////        .then(recommends => renderRecommends(recommends))
////        .catch(err => {
////            console.error(err);
////            document.getElementById('modalTitle').innerText = "정보를 불러올 수 없습니다.";
////        });
////}
////
////// 닫기 함수도 수정 (스크롤 풀기)
////function closeModal() {
////    document.getElementById('movieModal').style.display = 'none';
////    document.body.style.overflow = 'auto'; // 스크롤 다시 허용
////}
////
////function renderRecommends(movies) {
////    const container = document.getElementById('recommendContainer');
////    container.innerHTML = '';
////
////    // ⭐ [추가] 데이터가 없거나 비어있으면 안내 메시지 출력
////    if (!movies || movies.length === 0) {
////        container.innerHTML = '<p class="no-data-msg">비슷한 작품 정보가 없습니다.</p>';
////        return;
////    }
////
////    // 최대 4개만 보여주기
////    movies.slice(0, 4).forEach(movie => {
////        // 이미지가 없으면 검은 배경 이미지 사용
////        const posterSrc = movie.poster_path 
////            ? IMAGE_BASE_URL + movie.poster_path 
////            : "https://placehold.co/200x300/000000/ffffff?text=No+Poster";
////
////        const card = document.createElement('div');
////        card.className = 'recommend-card';
////        // 추천 영화 클릭하면 해당 영화 상세로 이동 (재귀 호출)
////        card.onclick = () => openModal(movie.id); 
////        
////        card.innerHTML = `
////            <img src="${posterSrc}">
////            <p>${movie.title}</p>
////        `;
////        container.appendChild(card);
////    });
////}
////
////window.onclick = function(e) { if (e.target == document.getElementById('movieModal')) closeModal(); }
////
////// 엔터키 검색 추가
////document.getElementById('searchInput').addEventListener("keypress", function(event) {
////    if (event.key === "Enter") searchMovies();
////});
//
const IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
const NO_POSTER_URL = "https://placehold.co/300x450/000000/ffffff?text=No+Poster";

let currentMovie = null; // 모달에서 현재 영화 정보 저장

// 1️⃣ 페이지 로드 시 실행
window.onload = function() {
    checkLoginStatus(); // 로그인 상태 확인

    // 인기 영화 불러오기
    fetch('/test/popular')
        .then(response => response.json())
        .then(data => renderMovies(data, 'movieContainer'));
};

// 2️⃣ 로그인 상태 체크 및 UI 적용
async function checkLoginStatus() {
    try {
        const res = await fetch("/user/me", { method: "GET", credentials: "include" });
        if (!res.ok) {
            return showGuestNav();
        }
        const user = await res.json();
        showUserNav(user);

    } catch (err) {
        console.error(err);
        showGuestNav();
    }
}

// 로그인 UI 적용
function showUserNav(user) {
    document.getElementById("guestNav").style.display = "none";
    document.getElementById("userNav").style.display = "block";
    document.getElementById("personalSection").style.display = "block";
    document.getElementById("userName").innerText = user.nickname || user.email;

    // 개인 추천 영화 (임시: 인기 영화 5~10번)
    fetch('/test/popular')
        .then(res => res.json())
        .then(data => renderMovies(data.slice(5, 10), 'personalContainer'));
}

// 비로그인 UI
function showGuestNav() {
    document.getElementById("guestNav").style.display = "block";
    document.getElementById("userNav").style.display = "none";
    document.getElementById("personalSection").style.display = "none";
}

// 3️⃣ 로그아웃
async function logout() {
    await fetch("/user/logout", { method: "POST", credentials: "include" });
    alert("로그아웃 완료");
    location.reload();
}

// 4️⃣ 영화 검색
function searchMovies() {
    const query = document.getElementById('searchInput').value;
    if (!query) return alert("검색어를 입력하세요!");

    document.getElementById('sectionTitle').innerText = `'${query}' 검색 결과`;
    fetch(`/test/search?q=${query}`)
        .then(res => res.json())
        .then(data => renderMovies(data, 'movieContainer'));
}

// 5️⃣ 영화 렌더링
function renderMovies(movies, containerId) {
    const container = document.getElementById(containerId);
    container.innerHTML = '';
    if (!movies || movies.length === 0) {
        container.innerHTML = '<p>결과가 없습니다.</p>';
        return;
    }

    movies.forEach(movie => {
        const posterSrc = movie.poster_path ? IMAGE_BASE_URL + movie.poster_path : NO_POSTER_URL;
        const card = document.createElement('div');
        card.className = 'movie-card';
        card.onclick = () => openModal(movie);
        card.innerHTML = `<img src="${posterSrc}" alt="${movie.title}"><h3>${movie.title}</h3>`;
        container.appendChild(card);
    });
}

// 6️⃣ 모달 열기
function openModal(movie) {
    currentMovie = movie;
    const modal = document.getElementById('movieModal');
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';

    document.getElementById('modalPoster').src = NO_POSTER_URL;
    document.getElementById('modalTitle').innerText = "로딩 중...";
    document.getElementById('modalOverview').innerText = "";
    document.getElementById('recommendContainer').innerHTML = "";

    // 영화 상세 정보 호출
    fetch(`/test/detail/${movie.id}`)
        .then(res => res.json())
        .then(detail => {
            document.getElementById('modalTitle').innerText = detail.title;
            document.getElementById('modalOverview').innerText = detail.overview || "줄거리 정보 없음";
            document.getElementById('modalPoster').src = detail.poster_path ? IMAGE_BASE_URL + detail.poster_path : NO_POSTER_URL;

            // 추천 영화 가져오기
            return fetch(`/test/recommend/${movie.id}`);
        })
        .then(res => res.json())
        .then(recommends => renderRecommends(recommends))
        .catch(err => {
            console.error(err);
            document.getElementById('modalTitle').innerText = "정보를 불러올 수 없습니다.";
        });
}

// 7️⃣ 모달 닫기
function closeModal() {
    document.getElementById('movieModal').style.display = 'none';
    document.body.style.overflow = 'auto';
}

// 8️⃣ 추천 영화 렌더링
function renderRecommends(movies) {
    const container = document.getElementById('recommendContainer');
    container.innerHTML = '';
    if (!movies || movies.length === 0) {
        container.innerHTML = '<p class="no-data-msg">비슷한 작품 정보가 없습니다.</p>';
        return;
    }

    movies.slice(0, 4).forEach(movie => {
        const posterSrc = movie.poster_path ? IMAGE_BASE_URL + movie.poster_path : "https://placehold.co/200x300/000000/ffffff?text=No+Poster";
        const card = document.createElement('div');
        card.className = 'recommend-card';
        card.onclick = () => openModal(movie);
        card.innerHTML = `<img src="${posterSrc}"><p>${movie.title}</p>`;
        container.appendChild(card);
    });
}

// 9️⃣ 찜하기
async function toggleLike() {
    if (!currentMovie) return;

    try {
        const res = await fetch('/favorite/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({
                movieId: currentMovie.id,
                title: currentMovie.title,
                posterPath: currentMovie.poster_path
            })
        });

        if (res.status === 401) {
            alert("로그인이 필요한 서비스입니다.");
            return;
        }

        if (!res.ok) throw new Error("찜 추가 실패");

        alert(`${currentMovie.title} 영화가 찜 목록에 추가되었습니다!`);
    } catch (err) {
        console.error(err);
        alert("찜 추가 중 오류가 발생했습니다.");
    }
}


// 모달 찜 버튼 연결
document.querySelector('.btn-like').onclick = toggleLike;

// 모달 외부 클릭 시 닫기
window.onclick = function(e) {
    if (e.target == document.getElementById('movieModal')) closeModal();
}

// 엔터키 검색
document.getElementById('searchInput').addEventListener("keypress", function(event) {
    if (event.key === "Enter") searchMovies();
});


