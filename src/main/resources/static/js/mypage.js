const IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
const NO_POSTER_URL = "https://placehold.co/300x450/000000/ffffff?text=No+Poster";
let currentUser = null; // 현재 사용자 정보를 저장할 변수

window.onload = () => {
    checkLoginStatus();
};

/* ======================
   로그인 체크 및 초기화
====================== */
async function checkLoginStatus() {
    const res = await fetch("/user/me", { credentials: "include" });

    if (!res.ok) {
        alert("로그인이 필요합니다.");
        location.href = "/login.html";
        return;
    }

    currentUser = await res.json(); // 사용자 정보 저장
    
    document.getElementById("userNav").style.display = "block";
    
    // 환영 메시지 초기 설정
    updateWelcomeMessage(currentUser.nickname || currentUser.email);

    loadFavorites();
}

function updateWelcomeMessage(name) {
    document.getElementById("welcomeMsg").innerText = `${name}님`;
}

/* ======================
   모달 열기/닫기
====================== */
function openProfileModal() {
    if (!currentUser) return; // 비로그인 상태면 실행 안 함

    const modal = document.getElementById('profileModal');
    
    // 이메일과 닉네임 필드에 현재 값 채우기
    document.getElementById("modalEmailInput").value = currentUser.email;
    document.getElementById("modalNicknameInput").value = currentUser.nickname || '';
    
    // 비밀번호 필드 초기화
    document.getElementById("modalCurrentPw").value = "";
    document.getElementById("modalNewPw").value = "";

    modal.style.display = 'flex';
}

function closeProfileModal() {
    document.getElementById('profileModal').style.display = 'none';
}


/* ======================
   닉네임 변경 (모달용)
====================== */
async function updateNicknameFromModal() {
    const nicknameInput = document.getElementById("modalNicknameInput");
    const nickname = nicknameInput.value.trim();
    if (!nickname) return alert("닉네임을 입력하세요.");
    if (nickname === currentUser.nickname) return alert("기존 닉네임과 동일합니다.");

    const res = await fetch("/mypage/nickname", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ nickname })
    });

    const resultText = await res.text();
    alert(resultText);
    
    if (res.ok) {
        // 성공 시 메인 화면과 전역 변수 업데이트
        currentUser.nickname = nickname; // 전역 변수 업데이트
        updateWelcomeMessage(nickname); // 환영 메시지 업데이트
        closeProfileModal();
    }
}


/* ======================
   비밀번호 변경 (모달용)
====================== */
async function updatePasswordFromModal() {
    const currentPassword = document.getElementById("modalCurrentPw").value;
    const newPassword = document.getElementById("modalNewPw").value;

    if (!currentPassword || !newPassword)
        return alert("현재 비밀번호와 새 비밀번호를 모두 입력하세요.");
    
    if (currentPassword === newPassword)
        return alert("현재 비밀번호와 새 비밀번호가 동일합니다.");

    const res = await fetch("/mypage/password", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ currentPassword, newPassword })
    });

    const resultText = await res.text();
    alert(resultText);

    if (res.ok) {
        // 성공 시 필드 초기화 및 모달 닫기
        document.getElementById("modalCurrentPw").value = "";
        document.getElementById("modalNewPw").value = "";
        closeProfileModal();
    }
}


/* ======================
   찜 목록 (기존 로직 유지)
====================== */
async function loadFavorites() {
    const res = await fetch("/favorite/list", { credentials: "include" });
    if (!res.ok) return;

    const movies = await res.json();
    const container = document.getElementById("favoritesContainer");
    container.innerHTML = "";

    if (!movies.length) {
        container.innerHTML = "<p>찜한 영화가 없습니다. 메인 페이지에서 찜해보세요!</p>";
        return;
    }

    movies.forEach(movie => {
        const poster = movie.posterPath
            ? IMAGE_BASE_URL + movie.posterPath
            : NO_POSTER_URL;

        const card = document.createElement("div");
        card.className = "movie-card";
        card.innerHTML = `
            <img src="${poster}">
            <h3>${movie.title}</h3>
            <button class="btn-delete">삭제</button>
        `;

        card.querySelector("button").onclick =
            () => deleteFavorite(movie.movieId, card);

        container.appendChild(card);
    });
}

async function deleteFavorite(movieId, card) {
    if (!confirm("찜 목록에서 삭제하시겠습니까?")) return;
    
    const res = await fetch("/favorite/delete", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ movieId })
    });

    if (res.ok) {
        card.remove();
        alert("삭제 완료");
    } else {
        alert("삭제 실패");
    }
}

/* ======================
   로그아웃
====================== */
async function logout() {
    await fetch("/user/logout", { method: "POST", credentials: "include" });
    location.href = "/login.html";
}

