document.addEventListener('DOMContentLoaded', () => {
    const searchBox = document.getElementById('searchBox');
    const tableBody = document.getElementById('tableBody');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const currentPageEl = document.getElementById('currentPage');
    const totalPagesEl = document.getElementById('totalPages');

    let currentPage = parseInt(currentPageEl ? currentPageEl.innerText - 1 : 0, 10) || 0;
    let totalPages = parseInt(totalPagesEl ? totalPagesEl.innerText : 1, 10) || 1;

    async function fetchAndRender(q, page) {
        const url = `/products/search?q=${encodeURIComponent(q || '')}&page=${page}`;
        const resp = await fetch(url);
        if (!resp.ok) {
            console.error('検索に失敗しました', resp.status);
            return;
        }
        const data = await resp.json();
        renderTable(data.content || []);
        currentPage = data.number || 0;
        totalPages = data.totalPages || 1;
        updatePagination();
    }

    function renderTable(items) {
        if (!tableBody) return;
        tableBody.innerHTML = '';
        for (const p of items) {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${p.id}</td>
                <td>${escapeHtml(p.name || '')}</td>
                <td class="text-end">${p.quantity}</td>
                <td class="text-end">${p.unitPrice}</td>
                <td class="text-end">${(p.quantity || 0) * (p.unitPrice || 0)}</td>
                <td>${formatDate(p.createdAt)}</td>
                <td>
                    <a href="/products/edit/${p.id}" class="btn btn-sm btn-outline-primary">編集</a>
                    <a href="/products/details/${p.id}" class="btn btn-sm btn-outline-info">詳細</a>
                    <a href="/products/delete/${p.id}" class="btn btn-sm btn-outline-danger">削除</a>
                </td>`;
            tableBody.appendChild(tr);
        }
    }

    function updatePagination() {
        if (currentPageEl) currentPageEl.innerText = (currentPage + 1);
        if (totalPagesEl) totalPagesEl.innerText = totalPages;
        if (prevBtn) prevBtn.parentElement.classList.toggle('disabled', currentPage <= 0);
        if (nextBtn) nextBtn.parentElement.classList.toggle('disabled', currentPage >= totalPages - 1);
    }

    if (searchBox) {
        let timer = null;
        searchBox.addEventListener('input', () => {
            clearTimeout(timer);
            timer = setTimeout(() => {
                const q = searchBox.value;
                fetchAndRender(q, 0);
            }, 300);
        });
    }

    if (prevBtn) {
        prevBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (currentPage > 0) {
                const q = searchBox ? searchBox.value : '';
                fetchAndRender(q, currentPage - 1);
            }
        });
    }
    if (nextBtn) {
        nextBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (currentPage < totalPages - 1) {
                const q = searchBox ? searchBox.value : '';
                fetchAndRender(q, currentPage + 1);
            }
        });
    }

    function escapeHtml(unsafe) {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function formatDate(iso) {
        if (!iso) return '';
        try {
            const d = new Date(iso);
            const y = d.getFullYear();
            const m = String(d.getMonth() + 1).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            const hh = String(d.getHours()).padStart(2, '0');
            const mm = String(d.getMinutes()).padStart(2, '0');
            return `${y}-${m}-${day} ${hh}:${mm}`;
        } catch (e) {
            return iso;
        }
    }
});