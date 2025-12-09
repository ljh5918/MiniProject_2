const IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

// 1. 페이지 로드 시 인기 영화 불러오기
window.onload = function() {
    fetch('/test/popular')
        .then(response => response.json())
        .then(data => renderMovies(data, 'movieContainer'));
};

// 2. 영화 검색 기능
function searchMovies() {
    const query = document.getElementById('searchInput').value;
    if (!query) {
        alert("검색어를 입력하세요!");
        return;
    }
    
    document.getElementById('sectionTitle').innerText = `'${query}' 검색 결과`;
    
    fetch(`/test/search?q=${query}`)
        .then(response => response.json())
        .then(data => renderMovies(data, 'movieContainer'));
}

// 3. 영화 목록 그리기 (공통 함수)
function renderMovies(movies, containerId) {
    const container = document.getElementById(containerId);
    container.innerHTML = ''; // 기존 내용 비우기

    if (movies.length === 0) {
        container.innerHTML = '<p>결과가 없습니다.</p>';
        return;
    }

    movies.forEach(movie => {
        // 포스터가 없으면 회색 박스 처리
        const posterSrc = movie.poster_path 
            ? IMAGE_BASE_URL + movie.poster_path 
            : 'https://via.placeholder.com/200x300?text=No+Image';

        const card = document.createElement('div');
        card.className = 'movie-card';
        card.onclick = () => openModal(movie.id); // 클릭 시 상세 모달 열기

        card.innerHTML = `
            <img src="${posterSrc}" alt="${movie.title}">
            <h3>${movie.title}</h3>
        `;
        container.appendChild(card);
    });
}

// 4. 상세 정보 및 추천 영화 모달 열기
function openModal(movieId) {
    // 상세 정보 가져오기
    fetch(`/test/detail/${movieId}`)
        .then(res => res.json())
        .then(movie => {
            document.getElementById('modalTitle').innerText = movie.title;
            document.getElementById('modalOverview').innerText = movie.overview || "줄거리 정보가 없습니다.";
            document.getElementById('modalPoster').src = movie.poster_path 
                ? IMAGE_BASE_URL + movie.poster_path 
                : 'https://via.placeholder.com/300x450';
            
            // 추천 영화 가져오기
            fetch(`/test/recommend/${movieId}`)
                .then(res => res.json())
                .then(recommends => renderRecommends(recommends));
            
            document.getElementById('movieModal').style.display = 'block';
        });
}

// 5. 모달 내부 추천 영화 그리기
function renderRecommends(movies) {
    const container = document.getElementById('recommendContainer');
    container.innerHTML = '';

    // 최대 4개만 보여주기
    movies.slice(0, 4).forEach(movie => {
        const posterSrc = movie.poster_path 
            ? IMAGE_BASE_URL + movie.poster_path 
            : 'https://via.placeholder.com/100x150';

        const card = document.createElement('div');
        card.className = 'recommend-card';
        // 추천 영화 클릭하면 해당 영화 상세로 다시 이동 (재귀 호출)
        card.onclick = () => openModal(movie.id); 
        
        card.innerHTML = `
            <img src="${posterSrc}">
            <p>${movie.title}</p>
        `;
        container.appendChild(card);
    });
}

// 6. 모달 닫기
function closeModal() {
    document.getElementById('movieModal').style.display = 'none';
}

// 모달 바깥 클릭 시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('movieModal');
    if (event.target == modal) {
        modal.style.display = "none";
    }
}