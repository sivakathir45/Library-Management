/**
 * PSG iTech Library - Main JavaScript Module
 * Handles Global Navigation, Dropdowns, Search, and Session UI state.
 */

document.addEventListener('DOMContentLoaded', function () {
    initNavigation();
    initScrollButtons();
    initSearchHandler();
    checkCurrentUserState();
    if (typeof initPasswordVisibilityToggles === 'function') {
        initPasswordVisibilityToggles();
    }
});

/**
 * Initializes navbar dropdowns and mobile toggles.
 */
function initNavigation() {
    // Profile Dropdown Toggle
    const profileTrigger = document.getElementById('profileTrigger');
    const profileDropdown = document.getElementById('profileDropdownMenu');

    if (profileTrigger && profileDropdown) {
        profileTrigger.addEventListener('click', function (e) {
            e.stopPropagation();
            profileDropdown.classList.toggle('show');
        });

        document.addEventListener('click', function (e) {
            if (!profileTrigger.contains(e.target) && !profileDropdown.contains(e.target)) {
                profileDropdown.classList.remove('show');
            }
        });
    }

    // Mobile Hamburger Menu
    const navToggle = document.getElementById('navToggle');
    const navMiddle = document.querySelector('.nav-middle');

    if (navToggle && navMiddle) {
        navToggle.addEventListener('click', function () {
            if (navMiddle.style.display === 'flex') {
                navMiddle.style.display = 'none';
            } else {
                navMiddle.style.display = 'flex';
                navMiddle.style.flexDirection = 'column';
                navMiddle.style.position = 'absolute';
                navMiddle.style.top = '100%';
                navMiddle.style.left = '0';
                navMiddle.style.right = '0';
                navMiddle.style.background = '#FFFFFF';
                navMiddle.style.padding = '16px';
                navMiddle.style.borderBottom = '1px solid #E2E8F0';
                navMiddle.style.boxShadow = '0 4px 10px rgba(0,0,0,0.06)';
            }
        });
    }
}

/**
 * Initializes search bar submission across navbar and hero banner.
 */
function initSearchHandler() {
    const navSearchForm = document.getElementById('navSearchForm');
    if (navSearchForm) {
        navSearchForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const type = document.getElementById('navSearchType')?.value || 'all';
            const query = document.getElementById('navSearchInput')?.value.trim() || '';
            window.location.href = `browse.html?type=${encodeURIComponent(type)}&q=${encodeURIComponent(query)}`;
        });
    }

    const heroSearchForm = document.getElementById('heroSearchForm');
    if (heroSearchForm) {
        heroSearchForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const query = document.getElementById('heroSearchInput')?.value.trim() || '';
            window.location.href = `browse.html?type=all&q=${encodeURIComponent(query)}`;
        });
    }
}

/**
 * Adds horizontal scrolling controls for category and book shelves.
 */
function initScrollButtons() {
    document.querySelectorAll('[data-scroll-target]').forEach(button => {
        button.addEventListener('click', function () {
            const targetId = this.getAttribute('data-scroll-target');
            const direction = this.getAttribute('data-scroll-dir') === 'left' ? -300 : 300;
            const container = document.getElementById(targetId);
            if (container) {
                container.scrollBy({ left: direction, behavior: 'smooth' });
            }
        });
    });
}

/**
 * Inspects session state or fetches user info to dynamically toggle
function checkCurrentUserState() {
    let cachedUser = null;
    try {
        const stored = sessionStorage.getItem('psgitech_user') || localStorage.getItem('psgitech_user');
        if (stored) {
            cachedUser = JSON.parse(stored);
        }
    } catch (e) {}

    // Immediate display if remembered
    if (cachedUser && cachedUser.name) {
        updateNavForLoggedInUser(cachedUser);
    } else {
        updateNavForLoggedOutUser();
    }

    fetch('profile', {
        headers: { 'Accept': 'application/json', 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(res => {
        if (res.ok) return res.json();
        return cachedUser;
    })
    .then(user => {
        if (user && user.name) {
            updateNavForLoggedInUser(user);
            sessionStorage.setItem('psgitech_user', JSON.stringify(user));
            if (localStorage.getItem('psgitech_remember_me') === 'true' || localStorage.getItem('psgitech_user')) {
                localStorage.setItem('psgitech_user', JSON.stringify(user));
            }
        } else if (!cachedUser) {
            updateNavForLoggedOutUser();
        }
    })
    .catch(() => {
        if (cachedUser) {
            updateNavForLoggedInUser(cachedUser);
        } else {
            updateNavForLoggedOutUser();
        }
    });
}

function updateNavForLoggedInUser(user) {
    const loggedOutSection = document.getElementById('navLoggedOut');
    const loggedInSection = document.getElementById('navLoggedIn');
    const navUsername = document.getElementById('navUsername');
    const navAvatar = document.getElementById('navAvatar');
    const navDashboardLink = document.getElementById('navDashboardLink');

    if (loggedOutSection) loggedOutSection.style.display = 'none';
    if (loggedInSection) loggedInSection.style.display = 'flex';
    if (navUsername) navUsername.textContent = user.name;
    if (navAvatar) navAvatar.textContent = (user.name || 'U').charAt(0).toUpperCase();

    if (navDashboardLink) {
        if (user.role === 'ADMIN') {
            navDashboardLink.href = 'admin-users.html';
            navDashboardLink.textContent = 'Admin Dashboard';
        } else if (user.role === 'LIBRARIAN') {
            navDashboardLink.href = 'librarian-dashboard.html';
            navDashboardLink.textContent = 'Librarian Dashboard';
        } else {
            navDashboardLink.href = 'student-home.html';
            navDashboardLink.textContent = 'Student Dashboard';
        }
    }
}

function updateNavForLoggedOutUser() {
    const loggedOutSection = document.getElementById('navLoggedOut');
    const loggedInSection = document.getElementById('navLoggedIn');

    if (loggedOutSection) loggedOutSection.style.display = 'flex';
    if (loggedInSection) loggedInSection.style.display = 'none';
}
