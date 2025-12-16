/* =========================================
   1. 전역 변수
   ========================================= */
const IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
const NO_POSTER_URL = "https://placehold.co/300x450/000000/ffffff?text=No+Poster";

let isUserLoggedIn = false;
let currentMovieId = null;
let currentMovieData = null;
let isLiked = false;
let currentUserEmail = null;

//let popularCurrentPage = 1; // 추가 
let currentPage = 1;
let totalPages = 500; // TMDB popular는 최대 500페이지

/* =========================================
   2. 초기화
   ========================================= */
window.onload = function() {
    checkLoginStatus();
    updateMovieCount();
    fetchAndRenderMovies('/movies/popular', 'popularContainer', 'GRID');
    loadPopularMovies(1); // 추가
};

function updateMovieCount() {
    fetch('/movies/count')
        .then(res => res.json())
        .then(count => {
            document.getElementById('dbCountBadge').innerText = `💾 ${count}개 저장됨`;
        })
        .catch(err => console.error("개수 로드 실패", err));
}

/* =========================================
   3. 인증 및 추천 로직 (복구됨)
   ========================================= */
async function checkLoginStatus() {
    try {
        const res = await fetch("/user/me", { method: "GET", credentials: "include" });
        if (res.ok) {
            const user = await res.json();
            handleLoginSuccess(user);
        } else {
            handleLoginFailure();
        }
    } catch (e) { handleLoginFailure(); }
}

function handleLoginSuccess(user) {
    isUserLoggedIn = true;
    currentUserEmail = user.email;
    document.getElementById("guestNav").style.display = "none";
    document.getElementById("userNav").style.display = "block";
    document.getElementById("userName").innerText = user.nickname || user.name || "회원";

    // ✅ [기능 복구] 추천 섹션 보이기 및 로드
    document.getElementById("recommendSection1").style.display = "block";
    document.getElementById("recommendSection2").style.display = "block";
    loadDualRecommendations(user.nickname || "회원");
}

function handleLoginFailure() {
    isUserLoggedIn = false;
    currentUserEmail = null;
    document.getElementById("guestNav").style.display = "block";
    document.getElementById("userNav").style.display = "none";
    document.getElementById("recommendSection1").style.display = "none";
    document.getElementById("recommendSection2").style.display = "none";
}

async function logout() {
    try {
        await fetch("/user/logout", { method: "POST", credentials: "include" });
        alert("로그아웃 되었습니다.");
        window.location.reload();
    } catch (e) { console.error(e); }
}

/* =========================================
   [수정됨] 메인 화면 2개 추천 섹션 로드 로직
   ========================================= */
async function loadDualRecommendations(userName) {
    try {
        const favRes = await fetch("/favorite/list", { credentials: "include" });
        if (!favRes.ok) throw new Error();
        const favorites = await favRes.json();

        const title1 = document.getElementById("recommendTitle1");
        const title2 = document.getElementById("recommendTitle2");

        // [Case A] 찜한 영화가 없을 때 -> 인기/최신 영화로 대체
        if (!favorites || favorites.length === 0) {
            title1.innerText = `✨ ${userName}님! 영화를 찜해보세요`;
            title2.innerText = `🍿 요즘 뜨는 영화들`;
            fetchAndRenderMovies('/movies/popular', 'recommendContainer1', 'ROW');
            fetchAndRenderMovies('/movies/now_playing', 'recommendContainer2', 'ROW');
            return;
        }

        // 찜 목록 셔플 (매번 다른 기준을 잡기 위해)
        const shuffled = favorites.sort(() => 0.5 - Math.random());

        // ---------------------------------------------------------
        // [섹션 1] 기존 유지: 랜덤 1개 영화 기준 추천
        // ---------------------------------------------------------
        const target1 = shuffled[0];
        const id1 = target1.movieId || target1.id;
        title1.innerHTML = `✨ 찜한 <span style="color:#e50914">'${target1.title}'</span>과(와) 비슷한 작품`;
        fetchAndRenderMovies(`/movies/recommend/${id1}`, 'recommendContainer1', 'ROW');


        // ---------------------------------------------------------
        // [섹션 2] 고도화: 최대 5개 영화의 '공통 추천작' (교집합)
        // ---------------------------------------------------------
        
        // 1. 표본 선정 (최대 5개)
        const sampleMovies = shuffled.slice(0, 5); 

        // 2. 5개 영화의 추천 리스트를 동시에 가져옴 (병렬 처리)
        const promises = sampleMovies.map(movie => {
            const mId = movie.movieId || movie.id;
            return fetch(`/movies/recommend/${mId}`)
                .then(res => res.json())
                .catch(() => []); // 에러 나면 빈 배열 반환
        });

        const allResults = await Promise.all(promises);

        // 3. 중복 횟수 카운팅
        const movieMap = new Map(); // Key: 영화ID, Value: {영화객체, count}

        allResults.flat().forEach(movie => {
            if (movieMap.has(movie.id)) {
                const data = movieMap.get(movie.id);
                data.count++; // 중복 횟수 증가
            } else {
                movieMap.set(movie.id, { ...movie, count: 1 });
            }
        });

        // 4. 필터링 (2번 이상 등장한 영화만) & 정렬 (많이 겹칠수록 앞으로)
        const overlaps = Array.from(movieMap.values())
            .filter(item => item.count >= 2) // ⭐ 핵심: 1번 이상 겹침 (즉, count 2 이상)
            .sort((a, b) => b.count - a.count); // 많이 겹친 순 정렬

        // 5. 렌더링 또는 폴백(Fallback)
        if (overlaps.length > 0) {
            title2.innerHTML = `🧠 이런영화는 어떠세요? (찜목록들에서 추천착 겹치는 영화)`;
            
            // 이미 데이터를 가지고 있으므로 fetchAndRenderMovies 대신 바로 renderMovies 호출
            renderMovies(overlaps, 'recommendContainer2', 'ROW');
        } else {
            // 겹치는게 하나도 없으면 (취향이 너무 다양하면) 평점 높은 영화 보여줌
            title2.innerText = `🍿 이런영화는 어떠세요?`;
            fetchAndRenderMovies('/movies/popular', 'recommendContainer2', 'ROW');
        }

    } catch (e) { 
        console.error("추천 로딩 에러:", e); 
    }
}

/* =========================================
   4. 공통 렌더링 (ROW vs GRID)
   ========================================= */
function fetchAndRenderMovies(url, containerId, type = 'GRID') {
    fetch(url)
        .then(res => res.json())
        .then(data => {renderMovies(data, containerId, type); updateMovieCount();})
        .catch(err => console.error(err));
}



function renderMovies(movies, containerId, type) {
    const container = document.getElementById(containerId);
    container.innerHTML = '';
    
    if (!movies || movies.length === 0) return;

    // ⭐ GRID → 14개 / ROW → 20개
    let list;
    if (type === 'GRID') {
        list = movies.slice(0, 14);
    } else {
        list = movies.slice(0, 20);
    }

    list.forEach(movie => {
        const posterSrc = getPosterUrl(movie.poster_path || movie.posterPath);
        const card = document.createElement('div');
        card.className = 'movie-card';
        card.onclick = () => openModal(movie.id);
        card.innerHTML = `
            <img src="${posterSrc}">
            <h3>${movie.title}</h3>
        `;
        container.appendChild(card);
    });
}


function getPosterUrl(path) {
    if (!path || path === "null" || path.trim() === "") return NO_POSTER_URL;
    return IMAGE_BASE_URL + (path.startsWith('/') ? path : '/' + path);
}

function searchMovies() {
    const query = document.getElementById('searchInput').value;
    if (!query) return alert("검색어 입력!");
    
    // 1. 토글 상태 확인
    const useApi = document.getElementById('apiModeToggle').checked;
    
    // 2. 제목 업데이트
    const modeLabel = useApi ? "[API 수집]" : "[DB 검색]";
    document.getElementById('sectionTitle').innerText = `${modeLabel} '${query}' 검색 결과`;

    // ⭐ [핵심 추가] 검색 시 다른 섹션(추천, 찜 등)은 숨기기!
    const rec1 = document.getElementById('recommendSection1');
    const rec2 = document.getElementById('recommendSection2');
    
    // 섹션이 존재하면 안 보이게 설정 (display: none)
    if(rec1) rec1.style.display = 'none';
    if(rec2) rec2.style.display = 'none';

    // 3. 백엔드 요청
    fetchAndRenderMovies(`/movies/search?q=${query}&useApi=${useApi}`, 'popularContainer', 'GRID');
}

/* =========================================
   5. 모달
   ========================================= */
function openModal(movieId) {
    currentMovieId = movieId; 
    const modal = document.getElementById('movieModal');
    
    modal.style.display = 'flex'; 
    document.body.style.overflow = 'hidden';

    document.getElementById('modalPoster').src = "https://placehold.co/300x450/000000/ffffff?text=Loading...";
    document.getElementById('modalTitle').innerText = "로딩 중...";
    document.getElementById('modalOverview').innerText = "";
    document.getElementById('recommendContainer').innerHTML = ""; 
    document.getElementById('commentList').innerHTML = ""; 
    
    isLiked = false;
    updateLikeButtonUI();

    fetch(`/movies/detail/${movieId}`)
        .then(res => res.json())
        .then(movie => {
            currentMovieData = movie;
            document.getElementById('modalTitle').innerText = movie.title;
            document.getElementById('modalOverview').innerText = movie.overview || "줄거리가 제공되지 않았습니다.";
			
			// ⭐ [핵심] 비디오 처리 로직
			    const posterImg = document.getElementById('modalPoster');
			    const videoFrame = document.getElementById('modalVideo');

			    if (movie.videoKey) {
			        
			        //posterImg.style.display = 'none';
			        //videoFrame.style.display = 'block';
			        
			        // 자동재생(autoplay=1) & 음소거(mute=1, 브라우저 정책상 필수)
			        videoFrame.src = `https://www.youtube.com/embed/${movie.videoKey}?autoplay=1&mute=1&controls=1&modestbranding=1`;
			    
			    } else {
			        // 2. 예고편이 없으면 -> 비디오 숨기고 포스터 보여줌
			        videoFrame.style.display = 'none';
			        posterImg.style.display = 'block';
			        videoFrame.src = ""; // 영상 끄기
			        
			        if (movie.poster_path) {
			            posterImg.src = "https://image.tmdb.org/t/p/w500" + movie.poster_path;
			        }
			    }

			    modal.style.display = 'flex';
			
			const overviewEl = document.getElementById('modalOverview');

			if (movie.overview && movie.overview.trim() !== "") {
			    // 줄거리가 있을 때
			    overviewEl.innerText = movie.overview;
			    overviewEl.style.display = 'block';
			    overviewEl.style.textAlign = 'left';
			    overviewEl.style.color = '#bbb';
			    overviewEl.style.height = 'auto';
			    overviewEl.style.justifyContent = ''; 
			    overviewEl.style.alignItems = '';
			} else {
			    //  줄거리가 없을 때
			    overviewEl.innerText = "줄거리가 제공되지 않았습니다.";
			    
			    overviewEl.style.display = 'flex';
			    overviewEl.style.justifyContent = 'center';
			    overviewEl.style.alignItems = 'center';    
			    overviewEl.style.height = '70%';           
			    overviewEl.style.minHeight = '70px';      
			    overviewEl.style.color = '#777';      
			    overviewEl.style.textAlign = 'center';
			}
            document.getElementById('modalPoster').src = getPosterUrl(movie.posterPath || movie.poster_path);
            
            if (isUserLoggedIn) checkIfFavorite(movieId);
            loadComments(movieId);
            return fetch(`/movies/recommend/${movieId}`);
        })
        .then(res => res.json())
        .then(recommends => renderModalRecommends(recommends))
        .catch(err => {
            document.getElementById('modalTitle').innerText = "정보 로드 실패";
        });
}

function closeModal() {
    document.getElementById('movieModal').style.display = 'none';
    document.body.style.overflow = 'auto';
	const modal = document.getElementById('movieModal');
	    modal.style.display = 'none';
	    const videoFrame = document.getElementById('modalVideo');
	    videoFrame.src = "";
	    
	    // 다시 포스터 모드로 복귀
	    document.getElementById('modalPoster').style.display = 'block';
	    videoFrame.style.display = 'none';
}
window.onclick = function(e) { if (e.target == document.getElementById('movieModal')) closeModal(); }

/* 모달 내부 추천: 4개 고정 (1200px 스타일에 맞춤) */
function renderModalRecommends(movies) {
    const container = document.getElementById('recommendContainer');
    container.innerHTML = '';
    if (!movies || movies.length === 0) {
        container.innerHTML = '<p class="no-data-msg" style="color:#777;">비슷한 작품이 없습니다.</p>';
        return;
    }
    // 4개만 자르기
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

// ... (이하 찜하기, 댓글 로직 동일) ...
async function checkIfFavorite(targetId) {
    try {
        const res = await fetch("/favorite/list", { credentials: 'include' });
        if (res.ok) {
            const favorites = await res.json();
            isLiked = favorites.some(fav => (fav.movieId || fav.id) === targetId);
            updateLikeButtonUI();
        }
    } catch (e) {}
}
function updateLikeButtonUI() {
    const btn = document.querySelector(".btn-like");
    if(!btn) return;
    
    if (isLiked) {
        btn.innerText = "♥ 찜 취소";
        btn.classList.add("favorited");
    } else {
        btn.innerText = "♡ 찜하기";
        btn.classList.remove("favorited");
    }
}
async function toggleLike() {
    if (!isUserLoggedIn) return alert("로그인 필요");
    const url = isLiked ? "/favorite/delete" : "/favorite/add";
    try {
        const res = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            credentials: 'include',
            body: JSON.stringify({
                movieId: Number(currentMovieId),
                title: currentMovieData.title,
                posterPath: currentMovieData.poster_path || currentMovieData.posterPath
            })
        });
        if (res.ok) { isLiked = !isLiked; updateLikeButtonUI(); }
    } catch(e) { alert("오류"); }
}
function loadComments(movieId) {
    const c = document.getElementById("commentList");
    fetch(`/comments/${movieId}`, { credentials: "include" })
        .then(r=>r.json()).then(d=>renderComments(d)).catch(()=>c.innerHTML="<p>로딩 실패</p>");
}
function renderComments(comments) {
    const c = document.getElementById("commentList");
    c.innerHTML = "";
    if(!comments || comments.length===0) { c.innerHTML="<p style='color:#777;padding:10px;'>첫 댓글을 남겨보세요</p>"; return; }
    comments.forEach(cm => {
        const isOwner = currentUserEmail && (currentUserEmail === (cm.userEmail||cm.email));
        const item = document.createElement("div");
        item.className = "comment-item";
        item.innerHTML = `
            <div class="meta">
                <span style="color:#eee;font-weight:bold;">${cm.userEmail||"익명"}</span>
                <span>${new Date(cm.createdAt||cm.createdDate).toLocaleDateString()}</span>
            </div>
            <div class="content">${cm.content}</div>
            ${isOwner ? `<div style="text-align:right;"><button onclick="deleteComment(${cm.commentId||cm.id})" class="btn-delete">삭제</button></div>` : ''}
        `;
        c.prepend(item);
    });
    c.scrollTop = c.scrollHeight;
}
async function postComment() {
    const inp = document.getElementById("commentInput");
    if(!inp.value.trim()) return alert("내용 입력");
    if(!isUserLoggedIn) return alert("로그인 필요");
    await fetch('/comments', {
        method:"POST", headers:{"Content-Type":"application/json"}, credentials:"include",
        body: JSON.stringify({movieId:Number(currentMovieId), content:inp.value.trim()})
    });
    inp.value=""; loadComments(currentMovieId);
}
async function deleteComment(id) {
    if(!confirm("삭제하시겠습니까?")) return;
    await fetch(`/comments/${id}`, {method:"DELETE", credentials:"include"});
    loadComments(currentMovieId);
}






// 추가 



function loadPopularMovies(page = 1) {
    currentPage = page;

    fetch(`/movies/popular?page=${page}`)
        .then(res => res.json())
        .then(data => {
            renderMovies(data, 'popularContainer', 'GRID');
            updatePaginationUI();
        })
        .catch(err => console.error(err));
}



function changePage(delta) {
    const newPage = currentPage + delta;

    if (newPage > 0 && newPage <= totalPages) {
        loadPopularMovies(newPage);
   //     window.scrollTo({ top: 0, behavior: 'smooth' });
    }
}


function updatePaginationUI() {
    const pageInfo = document.getElementById("pageInfo");
    const prevBtn = document.getElementById("prevBtn");

    pageInfo.innerText = `Page ${currentPage}`;
    prevBtn.disabled = currentPage === 1;
}







document.getElementById('searchInput').addEventListener("keypress", e=>{if(e.key==="Enter") searchMovies()});
document.getElementById('commentInput').addEventListener("keypress", e=>{if(e.key==="Enter" && !e.shiftKey) {e.preventDefault(); postComment();}});