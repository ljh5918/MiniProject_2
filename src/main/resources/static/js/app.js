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
async function checkLoginStatus() {
    try {
        const res = await fetch("/user/me", {
            method: "GET",
            credentials: "include"
        });

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

function showUserNav(user) {
    document.getElementById("guestNav").style.display = "none";
    document.getElementById("userNav").style.display = "block";
    document.getElementById("personalSection").style.display = "block";
    document.getElementById("userName").innerText = user.nickname || user.email;

    fetch('/test/popular')
        .then(res => res.json())
        .then(data => renderMovies(data.slice(5, 10), 'personalContainer'));
}

function showGuestNav() {
    document.getElementById("guestNav").style.display = "block";
    document.getElementById("userNav").style.display = "none";
    document.getElementById("personalSection").style.display = "none";
}


/** -------------------------------------------
 *  3. 로그아웃
 --------------------------------------------*/
async function logout() {
    await fetch("/user/logout", { method: "POST", credentials: "include" });
    alert("로그아웃 완료");
    location.reload();
}


/** -------------------------------------------
 *  4. 영화 검색
 --------------------------------------------*/
function searchMovies() {
    const query = document.getElementById('searchInput').value;
    if (!query) return alert("검색어를 입력하세요!");

    document.getElementById('sectionTitle').innerText = `'${query}' 검색 결과`;

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

    if (!movies || movies.length === 0) {
        container.innerHTML = '<p>결과가 없습니다.</p>';
        return;
    }

    movies.forEach(movie => {
        const posterSrc = movie.poster_path ? IMAGE_BASE_URL + movie.poster_path : NO_POSTER_URL;

        const card = document.createElement('div');
        card.className = 'movie-card';
        card.onclick = () => openModal(movie);

        card.innerHTML = `
            <img src="${posterSrc}" alt="${movie.title}">
            <h3>${movie.title}</h3>
        `;

        container.appendChild(card);
    });
}


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
function renderRecommends(movies) {
    const container = document.getElementById('recommendContainer');
    container.innerHTML = '';

    if (!movies || movies.length === 0) {
        container.innerHTML = '<p class="no-data-msg">비슷한 작품 정보가 없습니다.</p>';
        return;
    }

    movies.slice(0, 4).forEach(movie => {
        const posterSrc = movie.poster_path ? IMAGE_BASE_URL + movie.poster_path : NO_POSTER_URL;

        const card = document.createElement('div');
        card.className = 'recommend-card';
        card.onclick = () => openModal(movie);

        card.innerHTML = `
            <img src="${posterSrc}">
            <p>${movie.title}</p>
        `;

        container.appendChild(card);
    });
}


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
        });

        if (res.status === 401) {
            alert("로그인이 필요한 서비스입니다.");
            return;
        }

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
}

loadCurrentUser(); // 실행!!





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
