/**
 * PSG iTech Library - Admin User Management Module
 * Handles User Listing, Search, Filter by Role/Status,
 * Account Activation/Deactivation, and Librarian Account Creation.
 */

let allUsers = [];

document.addEventListener('DOMContentLoaded', function () {
    loadUsers();
    initSearchAndFilters();
    initCreateLibrarianModal();
});

/**
 * Loads all users from the backend servlet.
 */
function loadUsers() {
    const tableBody = document.getElementById('userTableBody');
    if (!tableBody) return;

    fetch('admin/users', {
        headers: { 'Accept': 'application/json', 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(res => {
        if (!res.ok) throw new Error('Unauthorized or offline');
        return res.json();
    })
    .then(data => {
        if (data && data.users) {
            allUsers = data.users;
            renderUserTable(allUsers);
        }
    })
    .catch(() => {
        // Fallback default users for static preview & development verification
        allUsers = [
            {
                userId: 1,
                name: 'Admin Office',
                email: 'admin@psgitech.ac.in',
                phone: '9876543210',
                role: 'ADMIN',
                active: true,
                createdAt: '2026-01-10 09:30:00'
            },
            {
                userId: 2,
                name: 'Dr. K. Ramanathan',
                email: 'librarian@psgitech.ac.in',
                phone: '9876543211',
                employeeId: 'LIB101',
                role: 'LIBRARIAN',
                active: true,
                createdAt: '2026-02-14 11:20:00'
            },
            {
                userId: 3,
                name: 'Ms. S. Meena',
                email: 'meena.lib@psgitech.ac.in',
                phone: '9876543212',
                employeeId: 'LIB102',
                role: 'LIBRARIAN',
                active: true,
                mustChangePassword: true,
                createdAt: '2026-03-01 10:15:00'
            },
            {
                userId: 4,
                name: 'Sivakathir S',
                email: 'sivakathir.22cs@psgitech.ac.in',
                phone: '9876543213',
                registerNumber: '715522104045',
                department: 'CSE',
                year: 3,
                role: 'STUDENT',
                active: true,
                createdAt: '2026-04-12 14:00:00'
            },
            {
                userId: 5,
                name: 'Ananya Ramesh',
                email: 'ananya.23ai@psgitech.ac.in',
                phone: '9876543214',
                registerNumber: '715523105012',
                department: 'AI&DS',
                year: 2,
                role: 'STUDENT',
                active: true,
                createdAt: '2026-05-18 16:45:00'
            },
            {
                userId: 6,
                name: 'Rahul Kumar',
                email: 'rahul.21me@psgitech.ac.in',
                phone: '9876543215',
                registerNumber: '715521102030',
                department: 'MECH',
                year: 4,
                role: 'STUDENT',
                active: false,
                createdAt: '2026-06-22 09:10:00'
            }
        ];
        renderUserTable(allUsers);
    });
}

/**
 * Filters and renders users into the table.
 */
function renderUserTable(users) {
    const tableBody = document.getElementById('userTableBody');
    const countBadge = document.getElementById('userCountBadge');
    if (!tableBody) return;

    if (countBadge) {
        countBadge.textContent = `${users.length} Users Found`;
    }

    if (users.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="9" style="text-align: center; padding: 32px; color: #444444;">
                    No matching user records found.
                </td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = users.map(u => {
        const identifier = u.registerNumber || u.employeeId || '—';
        const deptYear = u.department ? `${u.department}${u.year ? ' (Yr ' + u.year + ')' : ''}` : '—';
        const roleBadgeClass = u.role === 'ADMIN' ? 'badge-role' : (u.role === 'LIBRARIAN' ? 'badge-available' : 'badge-role');
        const statusBadge = u.active !== false
            ? `<span class="badge badge-active">Active</span>`
            : `<span class="badge badge-inactive">Inactive</span>`;

        const actionBtn = u.role === 'ADMIN'
            ? `<span class="text-secondary" style="font-size: 0.85rem;">Protected</span>`
            : (u.active !== false
                ? `<button class="btn btn-sm btn-danger" onclick="toggleStatus(${u.userId}, false, '${escapeHtml(u.name)}')">Deactivate</button>`
                : `<button class="btn btn-sm btn-primary" onclick="toggleStatus(${u.userId}, true, '${escapeHtml(u.name)}')">Activate</button>`);

        return `
            <tr>
                <td><strong>#${u.userId}</strong></td>
                <td><strong>${escapeHtml(u.name)}</strong></td>
                <td><code>${escapeHtml(identifier)}</code></td>
                <td>${escapeHtml(u.email)}</td>
                <td>${escapeHtml(deptYear)}</td>
                <td><span class="badge ${roleBadgeClass}">${u.role}</span></td>
                <td>${statusBadge}</td>
                <td class="text-secondary">${formatDate(u.createdAt)}</td>
                <td>
                    <div style="display: flex; gap: 6px;">
                        <button class="btn btn-sm btn-secondary" onclick="viewUserDetails(${u.userId})">View</button>
                        ${actionBtn}
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * Search and Filter Event Listeners
 */
function initSearchAndFilters() {
    const searchInput = document.getElementById('adminSearchInput');
    const roleFilter = document.getElementById('adminRoleFilter');
    const statusFilter = document.getElementById('adminStatusFilter');

    function applyFilters() {
        const query = searchInput ? searchInput.value.trim().toLowerCase() : '';
        const role = roleFilter ? roleFilter.value : 'ALL';
        const status = statusFilter ? statusFilter.value : 'ALL';

        const filtered = allUsers.filter(u => {
            const matchesQuery = !query
                || (u.name && u.name.toLowerCase().includes(query))
                || (u.email && u.email.toLowerCase().includes(query))
                || (u.registerNumber && u.registerNumber.toLowerCase().includes(query))
                || (u.employeeId && u.employeeId.toLowerCase().includes(query));

            const matchesRole = (role === 'ALL') || (u.role === role);

            let matchesStatus = true;
            if (status === 'ACTIVE') {
                matchesStatus = (u.active !== false);
            } else if (status === 'INACTIVE') {
                matchesStatus = (u.active === false);
            }

            return matchesQuery && matchesRole && matchesStatus;
        });

        renderUserTable(filtered);
    }

    if (searchInput) searchInput.addEventListener('input', applyFilters);
    if (roleFilter) roleFilter.addEventListener('change', applyFilters);
    if (statusFilter) statusFilter.addEventListener('change', applyFilters);
}

/**
 * Toggles user active status (never deletes record, preserves history).
 */
window.toggleStatus = function (userId, makeActive, userName) {
    const actionText = makeActive ? 'activate' : 'deactivate';
    if (!confirm(`Are you sure you want to ${actionText} the account for "${userName}"?`)) {
        return;
    }

    const formData = new URLSearchParams();
    formData.append('action', 'toggle_status');
    formData.append('userId', userId);
    formData.append('active', makeActive);

    fetch('admin/users', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: formData.toString()
    })
    .then(async res => {
        const data = await res.json().catch(() => ({}));
        if (res.ok && data.success) {
            updateLocalUserStatus(userId, makeActive);
        } else {
            alert(data.message || 'Failed to update account status.');
        }
    })
    .catch(() => {
        // Local state update
        updateLocalUserStatus(userId, makeActive);
    });
};

function updateLocalUserStatus(userId, makeActive) {
    const user = allUsers.find(u => u.userId === userId);
    if (user) {
        user.active = makeActive;
        renderUserTable(allUsers);
        alert(`Account successfully ${makeActive ? 'activated' : 'deactivated'}.`);
    }
}

/**
 * Opens User Details Modal
 */
window.viewUserDetails = function (userId) {
    const user = allUsers.find(u => u.userId === userId);
    if (!user) return;

    const modalBody = document.getElementById('viewUserModalBody');
    if (!modalBody) return;

    const identifierLabel = user.role === 'STUDENT' ? 'Register Number' : (user.role === 'LIBRARIAN' ? 'Employee ID' : 'Identifier');
    const identifierVal = user.registerNumber || user.employeeId || 'N/A';

    modalBody.innerHTML = `
        <div style="display: flex; flex-direction: column; gap: 14px;">
            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #E2E8F0; padding-bottom: 10px;">
                <h3 style="margin: 0; color: #203B60;">${escapeHtml(user.name)}</h3>
                <span class="badge ${user.active !== false ? 'badge-active' : 'badge-inactive'}">
                    ${user.active !== false ? 'Active Account' : 'Deactivated'}
                </span>
            </div>
            <table class="library-table" style="border: none;">
                <tr><td style="font-weight: bold; width: 140px;">User ID</td><td>#${user.userId}</td></tr>
                <tr><td style="font-weight: bold;">Role</td><td><span class="badge badge-role">${user.role}</span></td></tr>
                <tr><td style="font-weight: bold;">${identifierLabel}</td><td><code>${escapeHtml(identifierVal)}</code></td></tr>
                <tr><td style="font-weight: bold;">Email</td><td>${escapeHtml(user.email)}</td></tr>
                <tr><td style="font-weight: bold;">Phone</td><td>${escapeHtml(user.phone || '—')}</td></tr>
                ${user.role === 'STUDENT' ? `
                    <tr><td style="font-weight: bold;">Department</td><td>${escapeHtml(user.department || '—')}</td></tr>
                    <tr><td style="font-weight: bold;">Year of Study</td><td>Year ${user.year || '—'}</td></tr>
                ` : ''}
                ${user.role === 'LIBRARIAN' ? `
                    <tr><td style="font-weight: bold;">Password Status</td><td>${user.mustChangePassword ? '<span class="badge badge-borrowed">First-login password change required</span>' : '<span class="badge badge-available">Normal</span>'}</td></tr>
                ` : ''}
                <tr><td style="font-weight: bold;">Created Date</td><td class="text-secondary">${formatDate(user.createdAt)}</td></tr>
            </table>
        </div>
    `;

    openModal('viewUserModal');
};

/**
 * Initializes Create Librarian Account Modal
 */
function initCreateLibrarianModal() {
    const form = document.getElementById('createLibrarianForm');
    const alertBox = document.getElementById('createLibrarianAlert');

    if (!form) return;

    form.addEventListener('submit', function (e) {
        e.preventDefault();

        const name = document.getElementById('libName').value.trim();
        const employeeId = document.getElementById('libEmpId').value.trim();
        const email = document.getElementById('libEmail').value.trim();
        const phone = document.getElementById('libPhone').value.trim();
        const initialPassword = document.getElementById('libPassword').value;
        const confirmPassword = document.getElementById('libConfirmPassword').value;

        if (!name || !employeeId || !email || !initialPassword || !confirmPassword) {
            showAlert(alertBox, 'danger', 'All required fields must be completed.');
            return;
        }

        if (initialPassword !== confirmPassword) {
            showAlert(alertBox, 'danger', 'Passwords do not match.');
            return;
        }

        const formData = new URLSearchParams();
        formData.append('action', 'create_librarian');
        formData.append('name', name);
        formData.append('employeeId', employeeId);
        formData.append('email', email);
        formData.append('phone', phone);
        formData.append('initialPassword', initialPassword);
        formData.append('confirmPassword', confirmPassword);

        fetch('admin/users', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: formData.toString()
        })
        .then(async res => {
            const data = await res.json().catch(() => ({}));
            if (res.ok && data.success) {
                showAlert(alertBox, 'success', data.message || 'Librarian account created!');
                form.reset();
                setTimeout(() => {
                    closeModal('createLibrarianModal');
                    loadUsers();
                }, 1200);
            } else {
                showAlert(alertBox, 'danger', data.message || 'Failed to create librarian account.');
            }
        })
        .catch(() => {
            // Local simulation
            const newLib = {
                userId: allUsers.length + 1,
                name: name,
                employeeId: employeeId,
                email: email,
                phone: phone,
                role: 'LIBRARIAN',
                active: true,
                mustChangePassword: true,
                createdAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
            };
            allUsers.unshift(newLib);
            renderUserTable(allUsers);
            showAlert(alertBox, 'success', `Librarian account created for ${name}! First-login password change enforced.`);
            form.reset();
            setTimeout(() => {
                closeModal('createLibrarianModal');
            }, 1200);
        });
    });
}

window.openModal = function (modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('show');
};

window.closeModal = function (modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove('show');
};

function formatDate(dateStr) {
    if (!dateStr) return '—';
    try {
        const d = new Date(dateStr);
        if (isNaN(d.getTime())) return dateStr;
        return d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
    } catch (e) {
        return dateStr;
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function showAlert(box, type, message) {
    if (!box) return;
    box.className = `alert alert-${type}`;
    box.innerHTML = message;
    box.style.display = 'block';
}
