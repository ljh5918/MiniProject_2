const IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
const NO_POSTER_URL = "https://placehold.co/300x450/000000/ffffff?text=No+Poster";

window.onload = () => {
    checkLoginStatus();
};

/* ======================
   로그인 체크
====================== */
async function checkLoginStatus() {
    const res = await fetch("/user/me", { credentials: "include" });

    if (!res.ok) {
        alert("로그인이 필요합니다.");
        location.href = "/login.html";
        return;
    }

    const user = await res.json();
    document.getElementById("userNav").style.display = "block";
    document.getElementById("welcomeMsg").innerText =
        `${user.nickname || user.email}님`;

    document.getElementById("emailInput").value = user.email;
    document.getElementById("nicknameInput").value = user.nickname;

    loadFavorites();
}

/* ======================
   찜 목록
====================== */
async function loadFavorites() {
    const res = await fetch("/favorite/list", { credentials: "include" });
    if (!res.ok) return;

    const movies = await res.json();
    const container = document.getElementById("favoritesContainer");
    container.innerHTML = "";

    if (!movies.length) {
        container.innerHTML = "<p>찜한 영화가 없습니다.</p>";
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
    const res = await fetch("/favorite/delete", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ movieId })
    });

    if (res.ok) {
        card.remove();
        alert("삭제 완료");
    }
}

/* ======================
   닉네임 변경
====================== */
async function updateNickname() {
    const nickname = document.getElementById("nicknameInput").value.trim();
    if (!nickname) return alert("닉네임 입력");

    const res = await fetch("/mypage/nickname", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ nickname })
    });

    alert(await res.text());
    if (res.ok) location.reload();
}

/* ======================
   비밀번호 변경
====================== */
async function updatePassword() {
    const currentPassword = document.getElementById("currentPw").value;
    const newPassword = document.getElementById("newPw").value;

    if (!currentPassword || !newPassword)
        return alert("비밀번호 입력");

    const res = await fetch("/mypage/password", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ currentPassword, newPassword })
    });

    alert(await res.text());
    if (res.ok) {
        document.getElementById("currentPw").value = "";
        document.getElementById("newPw").value = "";
    }
}

/* ======================
   로그아웃
====================== */
async function logout() {
    await fetch("/user/logout", { method: "POST", credentials: "include" });
    location.href = "/login.html";
}
