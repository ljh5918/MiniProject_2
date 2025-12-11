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
let currentUserEmail = null; // 댓글 작성자 확인용

/**
 * ==========================================
 * 2. 초기화 (Window Load)
 * ==========================================
 */
window.onload = function() {
    checkLoginStatus(); 
    fetchAndRenderMovies('/movies/popular', 'movieContainer');
};

/**
 * ==========================================
 * 3. 인증 관련 함수 (Auth)
 * ==========================================
 */
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
    currentUserEmail = user.email; // 댓글 본인 확인용 저장

    document.getElementById("guestNav").style.display = "none";
    document.getElementById("userNav").style.display = "block";
    document.getElementById("personalSection").style.display = "block";
    
    const userName = user.nickname || user.name || user.email;
    document.getElementById("userName").innerText = userName;

    // 개인 추천 영화 (인기 영화 활용)
    fetchAndRenderMovies('/movies/popular', 'personalContainer', 5, 10);
}

function handleLoginFailure() {
    isUserLoggedIn = false;
    currentUserEmail = null;

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

/**
 * ==========================================
 * 4. 영화 데이터 조회 및 렌더링
 * ==========================================
 */
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

/**
 * ==========================================
 * 5. 모달, 찜하기, 댓글
 * ==========================================
 */

// 영화 상세보기 모달
function openModal(movieId) {
    currentMovieId = movieId; 
    const modal = document.getElementById('movieModal');
    
    // 1. UI 초기화 (로딩 중 표시)
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
    document.getElementById('modalPoster').src = "https://placehold.co/300x450/000000/ffffff?text=Loading...";
    document.getElementById('modalTitle').innerText = "로딩 중...";
    document.getElementById('modalOverview').innerText = "내용을 불러오는 중입니다...";
    
    // 버튼 초기화
    isLiked = false;
    updateLikeButtonUI();

    // 2. 상세 정보 API 호출 (GET /movies/detail/{id})
    fetch(`/movies/detail/${movieId}`)
        .then(res => {
            if (!res.ok) throw new Error("영화 상세 정보 로드 실패");
            return res.json();
        })
        .then(movie => {
            currentMovieData = movie; // 찜하기용 데이터 저장
            
            // 텍스트 정보 바인딩
            document.getElementById('modalTitle').innerText = movie.title;
            
            // 줄거리 (없으면 '정보 없음' 표시)
            document.getElementById('modalOverview').innerText = movie.overview || "상세 줄거리 정보가 없습니다.";
            
            // 포스터 이미지 (DTO는 posterPath로 옴)
            document.getElementById('modalPoster').src = getPosterUrl(movie.posterPath || movie.poster_path);
            
            // 찜 여부 확인 (로그인 시)
            if (isUserLoggedIn) {
                checkIfFavorite(movieId);
            }
            
            // 댓글 로드
            loadComments(movieId);

            // 추천 영화 로드
            return fetch(`/movies/recommend/${movieId}`);
        })
        .then(res => res.json())
        .then(recommends => renderRecommends(recommends))
        .catch(err => {
            console.error(err);
            document.getElementById('modalTitle').innerText = "정보를 불러올 수 없습니다.";
        });
}

// 찜 여부 확인 (목록 조회 방식)
async function checkIfFavorite(targetId) {
    try {
        const res = await fetch("/favorite/list", { credentials: 'include' });
        if (res.ok) {
            const favorites = await res.json();
            // 받아온 목록(Movie Entity List) 중에 현재 ID와 같은 게 있는지 확인
            isLiked = favorites.some(fav => fav.movieId === targetId);
            updateLikeButtonUI();
        }
    } catch (e) {
        console.error("찜 목록 조회 실패:", e);
    }
}

function updateLikeButtonUI() {
    const likeBtn = document.querySelector(".btn-like");
    if (!likeBtn) return;
    
    if (isLiked) {
        likeBtn.innerText = "♥ 찜 취소";
        likeBtn.classList.add("favorited"); // CSS 빨간색 적용
    } else {
        likeBtn.innerText = "♡ 찜하기";
        likeBtn.classList.remove("favorited");
    }
}

// 찜하기 토글
async function toggleLike() {
    if (!isUserLoggedIn) {
        alert("로그인이 필요한 서비스입니다.");
        return;
    }

    const url = isLiked ? "/favorite/delete" : "/favorite/add";
    
    const bodyData = {
        movieId: Number(currentMovieId), 
        title: currentMovieData.title,
        posterPath: currentMovieData.poster_path || currentMovieData.posterPath
    };

    try {
        const res = await fetch(url, {
            method: 'POST',
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

// 모달 외부 클릭 닫기
function closeModal() {
    document.getElementById('movieModal').style.display = 'none';
    document.body.style.overflow = 'auto';
}


/* -------------------------------------------
   🔥 댓글 기능 (Comments)
--------------------------------------------*/

/** 댓글 불러오기 */
function loadComments(movieId) {
    const container = document.getElementById("commentList");
    // 로딩 중 표시가 필요하면 여기에 작성

    fetch(`/comments/${movieId}`, {
        method: "GET",
        credentials: "include"
    })
    .then(res => {
        // 404 등 에러 처리
        if (!res.ok) return []; 
        return res.json();
    })
    .then(data => {
        renderComments(data);
    })
    .catch(err => {
        console.error("댓글 로드 중 에러:", err);
        container.innerHTML = "<p>댓글을 불러올 수 없습니다.</p>";
    });
}

/** 댓글 렌더링 */
function renderComments(comments) {
    const container = document.getElementById("commentList");
    container.innerHTML = "";

    // 1. 데이터가 없거나 빈 배열일 경우
    if (!comments || comments.length === 0) {
        container.innerHTML = "<p style='color:#777; padding:10px;'>아직 댓글이 없습니다. 첫 번째 댓글을 남겨보세요!</p>";
        return;
    }

    // 2. 댓글 목록 반복 렌더링
    comments.forEach(c => {
        const id = c.commentId || c.id; 
        const email = c.userEmail || c.email || c.nickname; 
        const content = c.content;
        const date = c.createdAt || c.createdDate || "";

        // 현재 로그인한 사용자가 작성자인지 확인
        const isOwner = currentUserEmail && (currentUserEmail === email);

        const item = document.createElement("div");
        item.className = "comment-item";

        item.innerHTML = `
            <div class="meta">
                <span style="color:#fff; font-weight:bold;">${email}</span> 
                <span style="color:#666; font-size:11px; margin-left:8px;">${formatDate(date)}</span>
            </div>
            <div class="content" style="color:#ddd; margin-top:4px; white-space: pre-wrap;">${content}</div>

            <div class="comment-actions" style="margin-top:8px; text-align:right;">
                ${isOwner ? `
                    <button onclick="editComment(${id}, '${content.replace(/'/g, "\\'")}')" style="background:#444; color:#fff; border:none; border-radius:4px; padding:4px 8px;">수정</button>
                    <button onclick="deleteComment(${id})" style="background:#c00; color:#fff; border:none; border-radius:4px; padding:4px 8px;">삭제</button>
                ` : ``}
            </div>
        `;

        container.appendChild(item);
    });
}

/** 날짜 포맷팅 함수 */
function formatDate(dateData) {
    if (!dateData) return "";
    
    // 만약 Java LocalDateTime이 배열로 넘어올 경우
    if (Array.isArray(dateData)) {
        return `${dateData[0]}-${String(dateData[1]).padStart(2,'0')}-${String(dateData[2]).padStart(2,'0')} ` +
               `${String(dateData[3]).padStart(2,'0')}:${String(dateData[4]).padStart(2,'0')}`;
    }
    // 문자열일 경우
    return new Date(dateData).toLocaleString();
}

/** 댓글 등록 함수 (HTML에서 onclick="postComment()"로 호출하거나 EventListener 사용) */
async function postComment() {
    const input = document.getElementById("commentInput");
    const text = input.value.trim();
    
    if (!text) return alert("댓글 내용을 입력하세요!");
    if (!currentMovieId) return alert("영화 정보가 로드되지 않았습니다.");

    try {
        const res = await fetch('/comments', {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({
                movieId: Number(currentMovieId),
                content: text
            })
        });

        if (res.status === 401) {
            alert("로그인이 필요합니다.");
            return;
        }
        
        if (!res.ok) throw new Error("댓글 등록 실패");

        // 성공 시 입력창 비우고 목록 갱신
        input.value = "";
        loadComments(currentMovieId);

    } catch (err) {
        console.error(err);
        alert("댓글 등록 중 오류가 발생했습니다.");
    }
}

/** 댓글 삭제 */
async function deleteComment(id) {
    if (!confirm("댓글을 삭제하시겠습니까?")) return;

    try {
        const res = await fetch(`/comments/${id}`, {
            method: "DELETE",
            credentials: "include"
        });

        if (!res.ok) throw new Error("삭제 실패");

        loadComments(currentMovieId);

    } catch (err) {
        console.error(err);
        alert("댓글 삭제 오류");
    }
}

/** 댓글 수정 */
function editComment(id, oldContent) {
    const newText = prompt("새 댓글 내용을 입력하세요.", oldContent);
    if (!newText) return;

    fetch(`/comments/${id}`, {
        method: "PUT",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(newText)
    })
        .then(res => {
            if (!res.ok) throw new Error("수정 실패");
            loadComments(currentMovieId);
        })
        .catch(err => {
            console.error(err);
            alert("댓글 수정 오류");
        });
}

/** -------------------------------------------
 * 기타 (모달 외부 클릭 / Enter 검색)
 --------------------------------------------*/
window.onclick = function(e) { 
    if (e.target == document.getElementById('movieModal')) closeModal(); 
}

document.getElementById('searchInput').addEventListener("keypress", function(event) {
    if (event.key === "Enter") searchMovies();
});


