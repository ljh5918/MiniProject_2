<<<<<<< HEAD
/** -------------------------------------------
 *  기본 설정
 --------------------------------------------*/
const IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
const NO_POSTER_URL = "https://placehold.co/300x450/000000/ffffff?text=No+Poster";

let currentMovie = null;  // 모달에서 선택된 영화 저장


/** -------------------------------------------
 *  1. 페이지 로드
 --------------------------------------------*/
window.onload = function () {
    checkLoginStatus();

    fetch('/test/popular')
        .then(res => res.json())
        .then(data => renderMovies(data, 'movieContainer'));
};


/** -------------------------------------------
 *  2. 로그인 UI 처리
 --------------------------------------------*/
=======
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
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
async function checkLoginStatus() {
    try {
        const res = await fetch("/user/me", {
            method: "GET",
<<<<<<< HEAD
            credentials: "include"
        });

        if (!res.ok) {
            return showGuestNav();
        }

        const user = await res.json();
        showUserNav(user);
=======
            credentials: "include" 
        });
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede

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

<<<<<<< HEAD
function showUserNav(user) {
=======
function handleLoginSuccess(user) {
    isUserLoggedIn = true;
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
    document.getElementById("guestNav").style.display = "none";
    document.getElementById("userNav").style.display = "block";
    document.getElementById("personalSection").style.display = "block";
    
    const userName = user.nickname || user.name || user.email;
    document.getElementById("userName").innerText = userName;

<<<<<<< HEAD
    fetch('/test/popular')
        .then(res => res.json())
        .then(data => renderMovies(data.slice(5, 10), 'personalContainer'));
}

function showGuestNav() {
=======
    // 개인 추천 영화 (인기 영화 활용) 기능 추가 예정
    fetchAndRenderMovies('/movies/popular', 'personalContainer', 5, 10);
}

function handleLoginFailure() {
    isUserLoggedIn = false;
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
    document.getElementById("guestNav").style.display = "block";
    document.getElementById("userNav").style.display = "none";
    document.getElementById("personalSection").style.display = "none";
}

<<<<<<< HEAD

/** -------------------------------------------
 *  3. 로그아웃
 --------------------------------------------*/
=======
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
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

<<<<<<< HEAD

/** -------------------------------------------
 *  4. 영화 검색
 --------------------------------------------*/
=======
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
function searchMovies() {
    const query = document.getElementById('searchInput').value;
    if (!query) return alert("검색어를 입력하세요!");
    
    document.getElementById('sectionTitle').innerText = `'${query}' 검색 결과`;
<<<<<<< HEAD

    fetch(`/test/search?q=${query}`)
        .then(res => res.json())
        .then(data => renderMovies(data, 'movieContainer'));
}


/** -------------------------------------------
 *  5. 영화 카드 렌더링
 --------------------------------------------*/
function renderMovies(movies, containerId) {
    const container = document.getElementById(containerId);
    container.innerHTML = '';

=======
    fetchAndRenderMovies(`/movies/search?q=${query}`, 'movieContainer');
}

function renderMovies(movies, containerId) {
    const container = document.getElementById(containerId);
    container.innerHTML = '';
    
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
    if (!movies || movies.length === 0) {
        container.innerHTML = '<p>결과가 없습니다.</p>';
        return;
    }

    movies.forEach(movie => {
<<<<<<< HEAD
        const posterSrc = movie.poster_path ? IMAGE_BASE_URL + movie.poster_path : NO_POSTER_URL;

        const card = document.createElement('div');
        card.className = 'movie-card';
        card.onclick = () => openModal(movie);

=======
        // 안전한 포스터 URL 생성
        const posterSrc = getPosterUrl(movie.poster_path || movie.posterPath);
        
        const card = document.createElement('div');
        card.className = 'movie-card';
        card.onclick = () => openModal(movie.id);
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
        card.innerHTML = `
            <img src="${posterSrc}" alt="${movie.title}">
            <h3>${movie.title}</h3>
        `;
<<<<<<< HEAD

=======
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
        container.appendChild(card);
    });
}

<<<<<<< HEAD

/** -------------------------------------------
 *  6. 모달 열기 (영화 + 댓글)
 --------------------------------------------*/
function openModal(movie) {
    currentMovie = movie;

    const modal = document.getElementById('movieModal');
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';

    document.getElementById('modalPoster').src = NO_POSTER_URL;
    document.getElementById('modalTitle').innerText = "로딩 중...";
    document.getElementById('modalOverview').innerText = "";
    document.getElementById('recommendContainer').innerHTML = "";

    // 영화 상세 정보 로드
    fetch(`/test/detail/${movie.id}`)
        .then(res => res.json())
        .then(detail => {
            document.getElementById('modalTitle').innerText = detail.title;
            document.getElementById('modalOverview').innerText = detail.overview || "줄거리 정보 없음";
            document.getElementById('modalPoster').src =
                detail.poster_path ? IMAGE_BASE_URL + detail.poster_path : NO_POSTER_URL;

            return fetch(`/test/recommend/${movie.id}`);
        })
        .then(res => res.json())
        .then(recommends => renderRecommends(recommends))
        .catch(err => console.error("영화 상세 오류:", err));

    // 🔥 댓글 로드 추가
    loadComments(movie.id);
}


/** -------------------------------------------
 *  7. 모달 닫기
 --------------------------------------------*/
function closeModal() {
    document.getElementById('movieModal').style.display = 'none';
    document.body.style.overflow = 'auto';
}


/** -------------------------------------------
 *  8. 추천 영화 렌더링
 --------------------------------------------*/
=======
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
function renderRecommends(movies) {
    const container = document.getElementById('recommendContainer');
    container.innerHTML = '';

    if (!movies || movies.length === 0) {
        container.innerHTML = '<p class="no-data-msg">비슷한 작품 정보가 없습니다.</p>';
        return;
    }

    movies.slice(0, 4).forEach(movie => {
<<<<<<< HEAD
        const posterSrc = movie.poster_path ? IMAGE_BASE_URL + movie.poster_path : NO_POSTER_URL;

        const card = document.createElement('div');
        card.className = 'recommend-card';
        card.onclick = () => openModal(movie);

=======
        const posterSrc = getPosterUrl(movie.poster_path || movie.posterPath);

        const card = document.createElement('div');
        card.className = 'recommend-card';
        card.onclick = () => openModal(movie.id); 
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
        card.innerHTML = `
            <img src="${posterSrc}">
            <p>${movie.title}</p>
        `;
<<<<<<< HEAD

=======
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
        container.appendChild(card);
    });
}

<<<<<<< HEAD

/** -------------------------------------------
 *  9. 찜하기
 --------------------------------------------*/
async function toggleLike() {
    if (!currentMovie) return;

    try {
        const res = await fetch('/favorite/add', {
            method: 'POST',
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({
                movieId: currentMovie.id,
                title: currentMovie.title,
                posterPath: currentMovie.poster_path
            })
=======
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
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
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
<<<<<<< HEAD

        alert(`${currentMovie.title} 영화가 찜 목록에 추가되었습니다!`);

    } catch (err) {
        console.error(err);
        alert("찜 추가 오류 발생");
    }
}

document.querySelector('.btn-like').onclick = toggleLike;




/* -------------------------------------------
   🔥 로그인 사용자 이메일 불러오기
--------------------------------------------*/

let currentUserEmail = null;

async function loadCurrentUser() {
    try {
        const res = await fetch("/user/me", { credentials: "include" });

        if (res.status === 200) {
            const user = await res.json();
            currentUserEmail = user.email;   // 🔥 UserDto.email
        } else {
            currentUserEmail = null;
        }
    } catch (e) {
        currentUserEmail = null;
    }
=======
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
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
}

loadCurrentUser(); // 실행!!

<<<<<<< HEAD




/* -------------------------------------------
   🔥 댓글 기능
--------------------------------------------*/

/** 댓글 불러오기 */
function loadComments(movieId) {
    console.log("댓글 로딩 시작: Movie ID =", movieId);

    fetch(`/comments/${movieId}`, {
        method: "GET",
        credentials: "include"
    })
    .then(res => {
        if (!res.ok) throw new Error("댓글 조회 실패");
        return res.json();
    })
    .then(data => {
        console.log("서버 응답 데이터:", data); // 🔥 브라우저 콘솔(F12)에서 이 로그를 꼭 확인하세요!
        renderComments(data);
    })
    .catch(err => console.error("댓글 로드 중 에러:", err));
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
        // 🔥 필드명 안전 처리 (DTO 필드명이 id인지 commentId인지 몰라도 동작하게 함)
        // 서버 DTO가 { id: 1, email: "a@a.com" } 형태일 수도 있고
        // { commentId: 1, userEmail: "a@a.com" } 형태일 수도 있음
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

/** 날짜 포맷팅 함수 (배열[2024,12,11...] 또는 문자열 처리) */
function formatDate(dateData) {
    if (!dateData) return "";
    
    // 만약 Java LocalDateTime이 배열로 넘어올 경우 [2024, 5, 20, 14, 30, 0]
    if (Array.isArray(dateData)) {
        return `${dateData[0]}-${String(dateData[1]).padStart(2,'0')}-${String(dateData[2]).padStart(2,'0')} ` +
               `${String(dateData[3]).padStart(2,'0')}:${String(dateData[4]).padStart(2,'0')}`;
    }
    // 문자열일 경우 (2024-05-20T14:30:00)
    return new Date(dateData).toLocaleString();
}

/** 댓글 등록 */
document.getElementById("commentSubmitBtn").onclick = async function () {
    const input = document.getElementById("commentInput");
    const text = input.value.trim();
    
    if (!text) return alert("댓글 내용을 입력하세요!");
    if (!currentMovie) return alert("영화 정보가 로드되지 않았습니다.");

    try {
        const res = await fetch('/comments', {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({
                movieId: currentMovie.id,
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
        loadComments(currentMovie.id);

    } catch (err) {
        console.error(err);
        alert("댓글 등록 중 오류가 발생했습니다.");
    }
};

/** 댓글 삭제 */
async function deleteComment(id) {
    if (!confirm("댓글을 삭제하시겠습니까?")) return;

    try {
        const res = await fetch(`/comments/${id}`, {
            method: "DELETE",
            credentials: "include"
        });

        if (!res.ok) throw new Error("삭제 실패");

        loadComments(currentMovie.id);

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
            loadComments(currentMovie.id);
        })
        .catch(err => {
            console.error(err);
            alert("댓글 수정 오류");
        });
}







/** -------------------------------------------
 *  기타 (모달 외부 클릭 / Enter 검색)
 --------------------------------------------*/
window.onclick = function (e) {
    if (e.target == document.getElementById('movieModal')) closeModal();
};

document
    .getElementById('searchInput')
    .addEventListener("keypress", event => {
        if (event.key === "Enter") searchMovies();
    });
=======
// 모달 외부 클릭 닫기
window.onclick = function(e) { 
    if (e.target == document.getElementById('movieModal')) closeModal(); 
}
>>>>>>> 04d81efdf9ec1268bd3ca56434d5c8a75b578ede
