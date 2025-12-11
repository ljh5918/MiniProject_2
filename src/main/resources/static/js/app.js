/**
 * ==========================================
 * 1. 전역 상수 및 변수 설정
 * ==========================================
 */
const IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
const NO_POSTER_URL = "https://placehold.co/300x450/000000/ffffff?text=No+Poster";

let isUserLoggedIn = false; 
let currentMovieId = null;  
let currentMovieData = null; // 찜하기 데이터 저장용
let isLiked = false;         // 선택한 영화 찜 상태

window.onload = function() {
    checkLoginStatus(); 
    fetchAndRenderMovies('/movies/popular', 'movieContainer');
};


// 인증 확인
async function checkLoginStatus() {
    try {
        const res = await fetch("/user/me", {
            method: "GET",
            credentials: "include" 
        });

        if (res.ok) {
            const user = await res.json();
            handleLoginSuccess(user);
        } else {
            handleLoginFailure();
        }
    } catch (e) {
        console.error("인증 확인 실패:", e);
        handleLoginFailure();
    }
}

function handleLoginSuccess(user) {
    isUserLoggedIn = true;
    document.getElementById("guestNav").style.display = "none";
    document.getElementById("userNav").style.display = "block";
    document.getElementById("personalSection").style.display = "block";
    
    const userName = user.nickname || user.name || user.email;
    document.getElementById("userName").innerText = userName;

    // 개인 추천 영화 (인기 영화 활용) 기능 추가 예정
    fetchAndRenderMovies('/movies/popular', 'personalContainer', 5, 10);
}

function handleLoginFailure() {
    isUserLoggedIn = false;
    document.getElementById("guestNav").style.display = "block";
    document.getElementById("userNav").style.display = "none";
    document.getElementById("personalSection").style.display = "none";
}

async function logout() {
    try {
        await fetch("/user/logout", { method: "POST", credentials: "include" });
        alert("로그아웃 되었습니다.");
        window.location.reload();
    } catch (e) {
        console.error("로그아웃 실패:", e);
    }
}



// 영화 데이터 조회 및 렌더링
function fetchAndRenderMovies(url, containerId, start = 0, end = undefined) {
    fetch(url)
        .then(res => res.json())
        .then(data => {
            const slicedData = end ? data.slice(start, end) : data;
            renderMovies(slicedData, containerId);
        })
        .catch(err => console.error(`데이터 로드 실패 (${url}):`, err));
}

function searchMovies() {
    const query = document.getElementById('searchInput').value;
    if (!query) return alert("검색어를 입력하세요!");
    
    document.getElementById('sectionTitle').innerText = `'${query}' 검색 결과`;
    fetchAndRenderMovies(`/movies/search?q=${query}`, 'movieContainer');
}

function renderMovies(movies, containerId) {
    const container = document.getElementById(containerId);
    container.innerHTML = '';
    
    if (!movies || movies.length === 0) {
        container.innerHTML = '<p>결과가 없습니다.</p>';
        return;
    }

    movies.forEach(movie => {
        // 안전한 포스터 URL 생성
        const posterSrc = getPosterUrl(movie.poster_path || movie.posterPath);
        
        const card = document.createElement('div');
        card.className = 'movie-card';
        card.onclick = () => openModal(movie.id);
        card.innerHTML = `
            <img src="${posterSrc}" alt="${movie.title}">
            <h3>${movie.title}</h3>
        `;
        container.appendChild(card);
    });
}

function renderRecommends(movies) {
    const container = document.getElementById('recommendContainer');
    container.innerHTML = '';

    if (!movies || movies.length === 0) {
        container.innerHTML = '<p class="no-data-msg">비슷한 작품 정보가 없습니다.</p>';
        return;
    }

    movies.slice(0, 4).forEach(movie => {
        const posterSrc = getPosterUrl(movie.poster_path || movie.posterPath);

        const card = document.createElement('div');
        card.className = 'recommend-card';
        card.onclick = () => openModal(movie.id); 
        card.innerHTML = `
            <img src="${posterSrc}">
            <p>${movie.title}</p>
        `;
        container.appendChild(card);
    });
}

function getPosterUrl(path) {
    if (!path || path === "null" || path.trim() === "") {
        return NO_POSTER_URL;
    }
    return IMAGE_BASE_URL + (path.startsWith('/') ? path : '/' + path);
}

// 영화 상세보기 모달 , 찜하기

function openModal(movieId) {
    currentMovieId = movieId; 
    const modal = document.getElementById('movieModal');
    
    // UI 초기화
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
    document.getElementById('modalPoster').src = "https://placehold.co/300x450/000000/ffffff?text=Loading...";
    document.getElementById('modalTitle').innerText = "로딩 중...";
    
    // 버튼 초기화
    isLiked = false;
    updateLikeButtonUI();

    fetch(`/movies/detail/${movieId}`)
        .then(res => res.json())
        .then(movie => {
            currentMovieData = movie; // 찜하기 요청시 들어갈 영화 데이터
            
            document.getElementById('modalTitle').innerText = movie.title;
            document.getElementById('modalOverview').innerText = movie.overview || "내용 없음";
            document.getElementById('modalPoster').src = getPosterUrl(movie.poster_path || movie.posterPath);
            
            // ★ 팀원 코드에는 check API가 없으므로 List를 조회해서 확인해야 함
            if (isUserLoggedIn) {
                checkIfFavorite(movieId);
            }
            
            return fetch(`/movies/recommend/${movieId}`);
        })
        .then(res => res.json())
        .then(recommends => renderRecommends(recommends))
        .catch(err => {
            console.error(err);
        });
}

// 찜 여부 확인 (목록 조회 방식)
async function checkIfFavorite(targetId) {
    try {
        const res = await fetch("/favorite/list", { credentials: 'include' });
        if (res.ok) {
            const favorites = await res.json();
            // 받아온 목록(Movie Entity List) 중에 현재 ID와 같은 게 있는지 확인
            // 팀원 코드 Entity: movieId 필드 확인
            isLiked = favorites.some(fav => fav.movieId === targetId);
            updateLikeButtonUI();
        }
    } catch (e) {
        console.error("찜 목록 조회 실패:", e);
    }
}

function updateLikeButtonUI() {
    const likeBtn = document.querySelector(".btn-like");
    if (isLiked) {
        likeBtn.innerText = "♥ 찜 취소";
        likeBtn.classList.add("favorited"); // CSS 빨간색 적용
    } else {
        likeBtn.innerText = "♡ 찜하기";
        likeBtn.classList.remove("favorited");
    }
}

// 찜하기 토글 (팀원 API: /favorite/add, /favorite/delete)
async function toggleLike() {
    if (!isUserLoggedIn) {
        alert("로그인이 필요한 서비스입니다.");
        return;
    }

    // 1. URL 및 Body 설정 (팀원 Controller 스펙 준수)
    const url = isLiked ? "/favorite/delete" : "/favorite/add";
    
    // 팀원 Controller는 Map<String, Object> body를 받음
    // movieId는 숫자(Long), 나머지는 문자열
    const bodyData = {
        movieId: Number(currentMovieId), 
        title: currentMovieData.title,
        posterPath: currentMovieData.poster_path || currentMovieData.posterPath
    };

    try {
        const res = await fetch(url, {
            method: 'POST', // 둘 다 POST 사용
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(bodyData)
        });

        if (res.ok) {
            isLiked = !isLiked; // 상태 반전
            updateLikeButtonUI();
            
            if (isLiked) alert("찜 목록에 추가되었습니다.");
            else alert("찜 목록에서 삭제되었습니다.");
        } else {
            const errorText = await res.text();
            alert("요청 실패: " + errorText);
        }
    } catch(e) { 
        console.error(e); 
        alert("서버 통신 오류");
    }
}

function closeModal() {
    document.getElementById('movieModal').style.display = 'none';
    document.body.style.overflow = 'auto';
}

// 엔터키 검색
document.getElementById('searchInput').addEventListener("keypress", function(event) {
    if (event.key === "Enter") searchMovies();
});

// 모달 외부 클릭 닫기
window.onclick = function(e) { 
    if (e.target == document.getElementById('movieModal')) closeModal(); 
}