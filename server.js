/**
 * PSG iTech Library - Integrated Development & Mock Servlet Server
 * Full REST API Support + Static Serving + Remember Me + Session Persistence
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');
const querystring = require('querystring');
const crypto = require('crypto');

const PORT = process.env.PORT || 8080;
const WEBAPP_DIR = path.join(__dirname, 'src', 'main', 'webapp');
const DATA_FILE = path.join(__dirname, 'dev_users.json');

// MIME types for static assets
const MIME_TYPES = {
    '.html': 'text/html; charset=UTF-8',
    '.css': 'text/css; charset=UTF-8',
    '.js': 'application/javascript; charset=UTF-8',
    '.json': 'application/json; charset=UTF-8',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon'
};

// Seed Users Database
const DEFAULT_USERS = [
    {
        userId: 1,
        name: 'Admin Office',
        email: 'admin@psgitech.ac.in',
        phone: '9876543210',
        role: 'ADMIN',
        active: true,
        password: 'AdminPassword@123',
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
        mustChangePassword: false,
        password: 'Librarian@123',
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
        password: 'InitialPassword@123',
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
        password: 'Student@123',
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
        password: 'Student@123',
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
        password: 'Student@123',
        createdAt: '2026-06-22 09:10:00'
    },
    {
        userId: 7,
        name: 'Saran',
        email: '25z304@psgitech.ac.in',
        phone: '8903991618',
        registerNumber: '715525104210',
        department: 'CSE',
        year: 2,
        role: 'STUDENT',
        active: true,
        password: 'Password@123',
        createdAt: '2026-10-31 00:00:00'
    }
];

let users = [];
try {
    if (fs.existsSync(DATA_FILE)) {
        users = JSON.parse(fs.readFileSync(DATA_FILE, 'utf-8'));
    } else {
        users = JSON.parse(JSON.stringify(DEFAULT_USERS));
        fs.writeFileSync(DATA_FILE, JSON.stringify(users, null, 2));
    }
} catch (e) {
    users = JSON.parse(JSON.stringify(DEFAULT_USERS));
}

function saveUsers() {
    try {
        fs.writeFileSync(DATA_FILE, JSON.stringify(users, null, 2));
    } catch (e) {
        console.error('Failed to save users:', e);
    }
}

// =============================================================================
// Feature 2: Book Management Data Store & Inventory Helpers
// =============================================================================
const BOOKS_FILE = path.join(__dirname, 'dev_books.json');

const DEFAULT_BOOKS = [
    {
        bookId: 1,
        isbn: '978-0262033848',
        title: 'Introduction to Algorithms',
        author: 'Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein',
        publisher: 'MIT Press',
        edition: '4th Edition',
        publicationYear: 2022,
        category: 'Data Structures',
        department: 'CSE',
        description: 'The definitive modern algorithms textbook covering asymptotic analysis, sorting, dynamic programming, greedy algorithms, and graph theory.',
        coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9780262033848-L.jpg',
        archived: false,
        createdAt: '2026-01-01 00:00:00'
    },
    {
        bookId: 2,
        isbn: '978-0132350884',
        title: 'Clean Code: A Handbook of Agile Software Craftsmanship',
        author: 'Robert C. Martin',
        publisher: 'Prentice Hall',
        edition: '1st Edition',
        publicationYear: 2008,
        category: 'Programming',
        department: 'CSE',
        description: 'Best practices and clean architecture patterns for writing robust, maintainable, and readable software systems.',
        coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg',
        archived: false,
        createdAt: '2026-01-02 00:00:00'
    },
    {
        bookId: 3,
        isbn: '978-0136042594',
        title: 'Artificial Intelligence: A Modern Approach',
        author: 'Stuart Russell, Peter Norvig',
        publisher: 'Pearson',
        edition: '4th Edition',
        publicationYear: 2020,
        category: 'Artificial Intelligence',
        department: 'AI&DS',
        description: 'Comprehensive university reference on search algorithms, probabilistic reasoning, machine learning, and natural language processing.',
        coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9780136042594-L.jpg',
        archived: false,
        createdAt: '2026-01-03 00:00:00'
    },
    {
        bookId: 4,
        isbn: '978-0132126953',
        title: 'Computer Networks',
        author: 'Andrew S. Tanenbaum, David J. Wetherall',
        publisher: 'Pearson',
        edition: '5th Edition',
        publicationYear: 2011,
        category: 'Computer Science',
        department: 'CSE',
        description: 'Covers protocol architecture, physical layers, data links, wireless transmission, routing algorithms, and internet security.',
        coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9780132126953-L.jpg',
        archived: false,
        createdAt: '2026-01-04 00:00:00'
    },
    {
        bookId: 5,
        isbn: '978-1119456339',
        title: 'Operating System Concepts',
        author: 'Abraham Silberschatz, Peter B. Galvin, Greg Gagne',
        publisher: 'Wiley',
        edition: '10th Edition',
        publicationYear: 2018,
        category: 'Computer Science',
        department: 'CSE',
        description: 'Core foundational textbook covering process scheduling, multi-threading, concurrency, memory management, and file systems.',
        coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9781119456339-L.jpg',
        archived: false,
        createdAt: '2026-01-05 00:00:00'
    },
    {
        bookId: 6,
        isbn: '978-0078022159',
        title: 'Database System Concepts',
        author: 'Abraham Silberschatz, Henry F. Korth, S. Sudarshan',
        publisher: 'McGraw-Hill Education',
        edition: '7th Edition',
        publicationYear: 2019,
        category: 'Computer Science',
        department: 'IT',
        description: 'Comprehensive guide to relational data models, SQL queries, transaction processing, indexing, and distributed databases.',
        coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9780078022159-L.jpg',
        archived: false,
        createdAt: '2026-01-06 00:00:00'
    },
    {
        bookId: 7,
        isbn: '978-0199464689',
        title: 'Linear Algebra and Its Applications',
        author: 'Gilbert Strang',
        publisher: 'Cengage Learning',
        edition: '4th Edition',
        publicationYear: 2016,
        category: 'Mathematics',
        department: 'GENERAL',
        description: 'Essential linear algebra concepts for computing, including vector spaces, matrix factorization, eigenvalues, and SVD.',
        coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9780199464689-L.jpg',
        archived: false,
        createdAt: '2026-01-07 00:00:00'
    },
    {
        bookId: 8,
        isbn: '978-0199339136',
        title: 'Microelectronic Circuits',
        author: 'Adel S. Sedra, Kenneth C. Smith',
        publisher: 'Oxford University Press',
        edition: '7th Edition',
        publicationYear: 2014,
        category: 'Electronics',
        department: 'ECE',
        description: 'Foundational curriculum textbook analyzing operational amplifiers, diode circuits, MOSFETs, and analog signal processing.',
        coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9780199339136-L.jpg',
        archived: false,
        createdAt: '2026-01-08 00:00:00'
    },
    {
        bookId: 9,
        isbn: '978-1260440232',
        title: 'Java: The Complete Reference',
        author: 'Herbert Schildt',
        publisher: 'McGraw-Hill Education',
        edition: '12th Edition',
        publicationYear: 2021,
        category: 'Programming',
        department: 'IT',
        description: 'Authoritative guide to Java programming syntax, OOP principles, collections framework, concurrency utilities, and I/O streams.',
        coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9781260440232-L.jpg',
        archived: false,
        createdAt: '2026-01-09 00:00:00'
    },
    {
        bookId: 10,
        isbn: '978-0134685991',
        title: 'Effective Java',
        author: 'Joshua Bloch',
        publisher: 'Addison-Wesley Professional',
        edition: '3rd Edition',
        publicationYear: 2018,
        category: 'Programming',
        department: 'CSE',
        description: 'Industry standard manual of best programming practices in the Java programming language.',
        coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg',
        archived: false,
        createdAt: '2026-01-10 00:00:00'
    }
];

const DEFAULT_COPIES = [
    { copyId: 1, copyCode: 'PSG-BK1-C01', bookId: 1, status: 'AVAILABLE', archived: false, createdAt: '2026-01-01 00:00:00' },
    { copyId: 2, copyCode: 'PSG-BK1-C02', bookId: 1, status: 'AVAILABLE', archived: false, createdAt: '2026-01-01 00:00:00' },
    { copyId: 3, copyCode: 'PSG-BK1-C03', bookId: 1, status: 'AVAILABLE', archived: false, createdAt: '2026-01-01 00:00:00' },
    { copyId: 4, copyCode: 'PSG-BK1-C04', bookId: 1, status: 'AVAILABLE', archived: false, createdAt: '2026-01-01 00:00:00' },
    { copyId: 5, copyCode: 'PSG-BK2-C01', bookId: 2, status: 'AVAILABLE', archived: false, createdAt: '2026-01-02 00:00:00' },
    { copyId: 6, copyCode: 'PSG-BK2-C02', bookId: 2, status: 'AVAILABLE', archived: false, createdAt: '2026-01-02 00:00:00' },
    { copyId: 7, copyCode: 'PSG-BK2-C03', bookId: 2, status: 'AVAILABLE', archived: false, createdAt: '2026-01-02 00:00:00' },
    { copyId: 8, copyCode: 'PSG-BK3-C01', bookId: 3, status: 'AVAILABLE', archived: false, createdAt: '2026-01-03 00:00:00' },
    { copyId: 9, copyCode: 'PSG-BK3-C02', bookId: 3, status: 'AVAILABLE', archived: false, createdAt: '2026-01-03 00:00:00' },
    { copyId: 10, copyCode: 'PSG-BK3-C03', bookId: 3, status: 'AVAILABLE', archived: false, createdAt: '2026-01-03 00:00:00' },
    { copyId: 11, copyCode: 'PSG-BK4-C01', bookId: 4, status: 'AVAILABLE', archived: false, createdAt: '2026-01-04 00:00:00' },
    { copyId: 12, copyCode: 'PSG-BK4-C02', bookId: 4, status: 'AVAILABLE', archived: false, createdAt: '2026-01-04 00:00:00' },
    { copyId: 13, copyCode: 'PSG-BK4-C03', bookId: 4, status: 'AVAILABLE', archived: false, createdAt: '2026-01-04 00:00:00' },
    { copyId: 14, copyCode: 'PSG-BK5-C01', bookId: 5, status: 'AVAILABLE', archived: false, createdAt: '2026-01-05 00:00:00' },
    { copyId: 15, copyCode: 'PSG-BK5-C02', bookId: 5, status: 'AVAILABLE', archived: false, createdAt: '2026-01-05 00:00:00' },
    { copyId: 16, copyCode: 'PSG-BK5-C03', bookId: 5, status: 'AVAILABLE', archived: false, createdAt: '2026-01-05 00:00:00' },
    { copyId: 17, copyCode: 'PSG-BK6-C01', bookId: 6, status: 'AVAILABLE', archived: false, createdAt: '2026-01-06 00:00:00' },
    { copyId: 18, copyCode: 'PSG-BK6-C02', bookId: 6, status: 'AVAILABLE', archived: false, createdAt: '2026-01-06 00:00:00' },
    { copyId: 19, copyCode: 'PSG-BK6-C03', bookId: 6, status: 'AVAILABLE', archived: false, createdAt: '2026-01-06 00:00:00' },
    { copyId: 20, copyCode: 'PSG-BK7-C01', bookId: 7, status: 'AVAILABLE', archived: false, createdAt: '2026-01-07 00:00:00' },
    { copyId: 21, copyCode: 'PSG-BK7-C02', bookId: 7, status: 'AVAILABLE', archived: false, createdAt: '2026-01-07 00:00:00' },
    { copyId: 22, copyCode: 'PSG-BK8-C01', bookId: 8, status: 'AVAILABLE', archived: false, createdAt: '2026-01-08 00:00:00' },
    { copyId: 23, copyCode: 'PSG-BK8-C02', bookId: 8, status: 'AVAILABLE', archived: false, createdAt: '2026-01-08 00:00:00' },
    { copyId: 24, copyCode: 'PSG-BK9-C01', bookId: 9, status: 'AVAILABLE', archived: false, createdAt: '2026-01-09 00:00:00' },
    { copyId: 25, copyCode: 'PSG-BK9-C02', bookId: 9, status: 'AVAILABLE', archived: false, createdAt: '2026-01-09 00:00:00' },
    { copyId: 26, copyCode: 'PSG-BK9-C03', bookId: 9, status: 'AVAILABLE', archived: false, createdAt: '2026-01-09 00:00:00' },
    { copyId: 27, copyCode: 'PSG-BK10-C01', bookId: 10, status: 'AVAILABLE', archived: false, createdAt: '2026-01-10 00:00:00' },
    { copyId: 28, copyCode: 'PSG-BK10-C02', bookId: 10, status: 'AVAILABLE', archived: false, createdAt: '2026-01-10 00:00:00' }
];

let bookData = { books: DEFAULT_BOOKS, copies: DEFAULT_COPIES };
try {
    if (fs.existsSync(BOOKS_FILE)) {
        bookData = JSON.parse(fs.readFileSync(BOOKS_FILE, 'utf-8'));
    } else {
        fs.writeFileSync(BOOKS_FILE, JSON.stringify(bookData, null, 2));
    }
} catch (e) {
    bookData = { books: DEFAULT_BOOKS, copies: DEFAULT_COPIES };
}

function saveBooks() {
    try {
        fs.writeFileSync(BOOKS_FILE, JSON.stringify(bookData, null, 2));
    } catch (e) {
        console.error('Failed to save books:', e);
    }
}

function computeBookInventory(bookId) {
    const copies = bookData.copies.filter(c => c.bookId === bookId);
    let totalCopies = copies.length;
    let availableCopies = 0;
    let borrowedCopies = 0;
    let allocatedCopies = 0;
    let transferPendingCopies = 0;
    let archivedCopies = 0;

    for (const c of copies) {
        if (c.archived || c.status === 'ARCHIVED') {
            archivedCopies++;
        } else if (c.status === 'AVAILABLE') {
            availableCopies++;
        } else if (c.status === 'BORROWED') {
            borrowedCopies++;
        } else if (c.status === 'ALLOCATED') {
            allocatedCopies++;
        } else if (c.status === 'TRANSFER_PENDING') {
            transferPendingCopies++;
        }
    }
    const totalActiveCopies = totalCopies - archivedCopies;
    return {
        bookId,
        totalCopies,
        totalActiveCopies,
        availableCopies,
        borrowedCopies,
        allocatedCopies,
        transferPendingCopies,
        archivedCopies
    };
}

function enrichBook(book) {
    const inv = computeBookInventory(book.bookId);
    return {
        ...book,
        inventory: inv,
        availableCopies: inv.availableCopies,
        totalActiveCopies: inv.totalActiveCopies
    };
}

// Active session store & remember tokens store
const sessions = new Map();
const rememberTokens = new Map();

// Helper to parse cookies
function parseCookies(req) {
    const list = {};
    const rc = req.headers.cookie;
    if (rc) {
        rc.split(';').forEach(cookie => {
            const parts = cookie.split('=');
            list[parts.shift().trim()] = decodeURI(parts.join('='));
        });
    }
    return list;
}

// Helper to parse request body
function parseRequestBody(req) {
    return new Promise((resolve) => {
        let body = '';
        req.on('data', chunk => { body += chunk.toString(); });
        req.on('end', () => {
            const contentType = req.headers['content-type'] || '';
            if (contentType.includes('application/json')) {
                try {
                    resolve(JSON.parse(body || '{}'));
                } catch (e) {
                    resolve({});
                }
            } else {
                resolve(querystring.parse(body));
            }
        });
    });
}

function sanitizeUser(u) {
    if (!u) return null;
    const copy = { ...u };
    delete copy.password;
    delete copy.passwordHash;
    return copy;
}

const server = http.createServer(async (req, res) => {
    const parsedUrl = url.parse(req.url, true);
    let pathname = parsedUrl.pathname;

    // Normalize Tomcat context path /psgitech-library/
    if (pathname.startsWith('/psgitech-library')) {
        pathname = pathname.substring('/psgitech-library'.length) || '/';
    }

    if (pathname === '/' || pathname === '') {
        pathname = '/index.html';
    }

    const cookies = parseCookies(req);
    let currentUser = null;

    // Session identification
    if (cookies.psgitech_session && sessions.has(cookies.psgitech_session)) {
        currentUser = sessions.get(cookies.psgitech_session);
    } else if (cookies.psgitech_remember_me && rememberTokens.has(cookies.psgitech_remember_me)) {
        const userId = rememberTokens.get(cookies.psgitech_remember_me);
        currentUser = users.find(u => u.userId === userId && u.active !== false);
        if (currentUser) {
            const newSid = crypto.randomBytes(16).toString('hex');
            sessions.set(newSid, currentUser);
            res.setHeader('Set-Cookie', `psgitech_session=${newSid}; Path=/; HttpOnly`);
        }
    }

    // CORS & Content negotiation
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, X-Requested-With');

    if (req.method === 'OPTIONS') {
        res.writeHead(200);
        res.end();
        return;
    }

    // =========================================================================
    // API Endpoints: Register, Login, Logout, Profile, Change Password, Admin
    // =========================================================================

    // 1. POST /register
    if (pathname === '/register' && req.method === 'POST') {
        const body = await parseRequestBody(req);
        const name = (body.name || '').trim();
        const registerNumber = (body.registerNumber || body.regNo || '').trim();
        const email = (body.email || '').trim().toLowerCase();
        const phone = (body.phone || '').trim();
        const department = (body.department || 'CSE').trim();
        const year = parseInt(body.year || '1', 10);
        const password = body.password || '';
        const confirmPassword = body.confirmPassword || '';

        if (!name) {
            res.writeHead(400, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'Full Name is required.' }));
            return;
        }

        if (!email) {
            res.writeHead(400, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'Email address is required.' }));
            return;
        }

        if (!password) {
            res.writeHead(400, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'Password is required.' }));
            return;
        }

        if (confirmPassword && password !== confirmPassword) {
            res.writeHead(400, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'Password and Confirm Password do not match.' }));
            return;
        }

        // Check if user already exists
        const existing = users.find(u => u.email.toLowerCase() === email || (registerNumber && u.registerNumber && u.registerNumber.toLowerCase() === registerNumber.toLowerCase()));
        if (existing) {
            // Update existing record for friendly user registration experience
            existing.name = name;
            existing.phone = phone || existing.phone;
            existing.department = department || existing.department;
            existing.year = year || existing.year;
            existing.password = password;
            existing.active = true;
            saveUsers();

            const sid = crypto.randomBytes(16).toString('hex');
            sessions.set(sid, existing);
            const rawToken = crypto.randomBytes(24).toString('hex');
            rememberTokens.set(rawToken, existing.userId);

            res.writeHead(200, {
                'Content-Type': 'application/json',
                'Set-Cookie': [
                    `psgitech_session=${sid}; Path=/; HttpOnly; Max-Age=${30 * 24 * 3600}`,
                    `psgitech_remember_me=${rawToken}; Path=/; HttpOnly; Max-Age=${30 * 24 * 3600}`
                ]
            });
            res.end(JSON.stringify({
                success: true,
                message: `Registration successful! Welcome, ${name}.`,
                redirect: 'student-home.html',
                user: sanitizeUser(existing)
            }));
            return;
        }

        // Create new student
        const newStudent = {
            userId: users.length + 1,
            name: name,
            email: email,
            phone: phone || '',
            registerNumber: registerNumber || 'REG' + Date.now().toString().slice(-6),
            department: department || 'CSE',
            year: year || 1,
            role: 'STUDENT',
            active: true,
            password: password,
            createdAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
        };

        users.push(newStudent);
        saveUsers();

        // Establish session and persistent Remember Me immediately for smooth student onboarding
        const sid = crypto.randomBytes(16).toString('hex');
        sessions.set(sid, newStudent);
        const rawToken = crypto.randomBytes(24).toString('hex');
        rememberTokens.set(rawToken, newStudent.userId);

        res.writeHead(200, {
            'Content-Type': 'application/json',
            'Set-Cookie': [
                `psgitech_session=${sid}; Path=/; HttpOnly; Max-Age=${30 * 24 * 3600}`,
                `psgitech_remember_me=${rawToken}; Path=/; HttpOnly; Max-Age=${30 * 24 * 3600}`
            ]
        });
        res.end(JSON.stringify({
            success: true,
            message: `Account registered successfully! Welcome, ${name}.`,
            redirect: 'student-home.html',
            user: sanitizeUser(newStudent)
        }));
        return;
    }

    // 2. POST /login
    if (pathname === '/login' && req.method === 'POST') {
        const body = await parseRequestBody(req);
        const loginKey = (body.identifier || body.loginKey || body.email || body.registerNumber || '').trim().toLowerCase();
        const password = body.password || '';
        const rememberMe = body.rememberMe === 'true' || body.rememberMe === 'on' || body.rememberMe === true;

        if (!loginKey || !password) {
            res.writeHead(400, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'Email/Register Number and Password are required.' }));
            return;
        }

        const user = users.find(u => {
            const matchEmail = u.email && u.email.toLowerCase() === loginKey;
            const matchReg = u.registerNumber && u.registerNumber.toLowerCase() === loginKey;
            const matchEmp = u.employeeId && u.employeeId.toLowerCase() === loginKey;
            return matchEmail || matchReg || matchEmp;
        });

        if (!user || user.password !== password) {
            res.writeHead(401, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'Invalid credentials. Please verify your username and password.' }));
            return;
        }

        if (user.active === false) {
            res.writeHead(403, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'Your account is deactivated. Please contact the Library Administrator.' }));
            return;
        }

        // Establish session
        const sid = crypto.randomBytes(16).toString('hex');
        sessions.set(sid, user);

        const cookieHeaders = [`psgitech_session=${sid}; Path=/; HttpOnly; Max-Age=${30 * 24 * 3600}`];

        if (rememberMe) {
            const rawToken = crypto.randomBytes(24).toString('hex');
            rememberTokens.set(rawToken, user.userId);
            cookieHeaders.push(`psgitech_remember_me=${rawToken}; Path=/; HttpOnly; Max-Age=${30 * 24 * 3600}`);
        }

        let redirectUrl = 'student-home.html';
        if (user.role === 'ADMIN') {
            redirectUrl = 'admin-users.html';
        } else if (user.role === 'LIBRARIAN') {
            redirectUrl = user.mustChangePassword ? 'first-login-change-password.html' : 'librarian-dashboard.html';
        }

        res.writeHead(200, {
            'Content-Type': 'application/json',
            'Set-Cookie': cookieHeaders
        });
        res.end(JSON.stringify({
            success: true,
            message: `Login successful! Welcome, ${user.name}.`,
            redirect: redirectUrl,
            user: sanitizeUser(user)
        }));
        return;
    }

    // 3. POST /logout
    if (pathname === '/logout') {
        if (cookies.psgitech_session) sessions.delete(cookies.psgitech_session);
        if (cookies.psgitech_remember_me) rememberTokens.delete(cookies.psgitech_remember_me);

        res.writeHead(200, {
            'Content-Type': 'application/json',
            'Set-Cookie': [
                'psgitech_session=; Path=/; Max-Age=0',
                'psgitech_remember_me=; Path=/; Max-Age=0'
            ]
        });
        res.end(JSON.stringify({ success: true, message: 'Logged out successfully.', redirect: 'login.html?logged_out=true' }));
        return;
    }

    // 4. GET /profile or POST /profile
    if (pathname === '/profile') {
        const user = currentUser || users[3]; // Default to student if not logged in

        if (req.method === 'GET') {
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify(sanitizeUser(user)));
            return;
        } else if (req.method === 'POST') {
            const body = await parseRequestBody(req);
            if (user) {
                if (body.name) user.name = body.name.trim();
                if (body.email) user.email = body.email.trim();
                if (body.phone) user.phone = body.phone.trim();
                if (body.department && user.role === 'STUDENT') user.department = body.department.trim();
                if (body.year && user.role === 'STUDENT') user.year = parseInt(body.year, 10);
                saveUsers();
            }
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: true, message: 'Profile updated successfully!', user: sanitizeUser(user) }));
            return;
        }
    }

    // 5. POST /change-password
    if (pathname === '/change-password' && req.method === 'POST') {
        const body = await parseRequestBody(req);
        const user = currentUser || users[3];
        const currentPassword = body.currentPassword;
        const newPassword = body.newPassword;
        const confirmPassword = body.confirmPassword;

        if (!currentPassword || !newPassword || !confirmPassword) {
            res.writeHead(400, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'All password fields are required.' }));
            return;
        }

        if (newPassword !== confirmPassword) {
            res.writeHead(400, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'New Password and Confirmation do not match.' }));
            return;
        }

        if (user && user.password && user.password !== currentPassword) {
            res.writeHead(400, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'Current password does not match existing password.' }));
            return;
        }

        if (user) {
            user.password = newPassword;
            user.mustChangePassword = false;
            saveUsers();
        }

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, message: 'Password updated successfully!' }));
        return;
    }

    // 6. GET/POST /admin/users
    if (pathname === '/admin/users') {
        if (req.method === 'GET') {
            const q = (parsedUrl.query.q || '').toLowerCase();
            const role = (parsedUrl.query.role || 'ALL').toUpperCase();
            const status = (parsedUrl.query.status || 'ALL').toUpperCase();

            const filtered = users.filter(u => {
                const matchQ = !q
                    || (u.name && u.name.toLowerCase().includes(q))
                    || (u.email && u.email.toLowerCase().includes(q))
                    || (u.registerNumber && u.registerNumber.toLowerCase().includes(q))
                    || (u.employeeId && u.employeeId.toLowerCase().includes(q));

                const matchRole = role === 'ALL' || u.role === role;

                let matchStatus = true;
                if (status === 'ACTIVE') matchStatus = u.active !== false;
                if (status === 'INACTIVE') matchStatus = u.active === false;

                return matchQ && matchRole && matchStatus;
            }).map(sanitizeUser);

            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: true, count: filtered.length, users: filtered }));
            return;
        } else if (req.method === 'POST') {
            const body = await parseRequestBody(req);
            const action = body.action;

            if (action === 'create_librarian') {
                const newLib = {
                    userId: users.length + 1,
                    name: (body.name || '').trim(),
                    email: (body.email || '').trim(),
                    phone: (body.phone || '').trim(),
                    employeeId: (body.employeeId || '').trim().toUpperCase(),
                    role: 'LIBRARIAN',
                    active: true,
                    mustChangePassword: true,
                    password: body.initialPassword || 'InitialPassword@123',
                    createdAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
                };
                users.unshift(newLib);
                saveUsers();
                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: true, message: `Librarian account created for ${newLib.name}.`, librarian: sanitizeUser(newLib) }));
                return;
            } else if (action === 'toggle_status') {
                const targetId = parseInt(body.userId, 10);
                const active = body.active === 'true' || body.active === true;
                const target = users.find(u => u.userId === targetId);
                if (target) {
                    target.active = active;
                    saveUsers();
                    res.writeHead(200, { 'Content-Type': 'application/json' });
                    res.end(JSON.stringify({ success: true, message: `Account status updated to ${active ? 'ACTIVE' : 'INACTIVE'}` }));
                    return;
                }
            }
        }
    }

    // =========================================================================
    // Feature 2: Book Management API Endpoints
    // =========================================================================

    // 1. GET /books (Catalog listing & Featured shelves)
    if (pathname === '/books' && req.method === 'GET') {
        const isFeatured = parsedUrl.query.featured === 'true';
        const limit = parseInt(parsedUrl.query.limit, 10) || 50;
        let result = bookData.books.filter(b => !b.archived).map(enrichBook);
        if (isFeatured) {
            result = result.slice(0, limit);
        }
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, count: result.length, books: result }));
        return;
    }

    // 2. GET /books/search (Multi-criteria parameterized search)
    if (pathname === '/books/search' && req.method === 'GET') {
        const q = (parsedUrl.query.q || '').trim().toLowerCase();
        const searchType = (parsedUrl.query.searchType || 'all').toLowerCase();
        const category = (parsedUrl.query.category || 'ALL').toUpperCase();
        const department = (parsedUrl.query.department || 'ALL').toUpperCase();
        const availability = (parsedUrl.query.availability || 'ALL').toUpperCase();
        const includeArchived = parsedUrl.query.includeArchived === 'true';

        const enriched = bookData.books.map(enrichBook);
        const filtered = enriched.filter(b => {
            if (!includeArchived && b.archived) return false;

            if (q) {
                if (searchType === 'title' && !b.title.toLowerCase().includes(q)) return false;
                else if (searchType === 'author' && !b.author.toLowerCase().includes(q)) return false;
                else if (searchType === 'isbn') {
                    const cleanIsbn = (b.isbn || '').replace(/[^0-9X]/gi, '').toLowerCase();
                    const cleanQ = q.replace(/[^0-9X]/gi, '').toLowerCase();
                    if (!cleanIsbn.includes(cleanQ)) return false;
                } else if (searchType === 'all') {
                    const inTitle = b.title.toLowerCase().includes(q);
                    const inAuthor = b.author.toLowerCase().includes(q);
                    const inIsbn = (b.isbn || '').replace(/[^0-9X]/gi, '').toLowerCase().includes(q.replace(/[^0-9X]/gi, '').toLowerCase());
                    if (!inTitle && !inAuthor && !inIsbn) return false;
                }
            }

            if (category !== 'ALL' && b.category.toUpperCase() !== category) return false;
            if (department !== 'ALL' && b.department.toUpperCase() !== department) return false;

            if (availability === 'AVAILABLE' && b.inventory.availableCopies <= 0) return false;
            if (availability === 'UNAVAILABLE' && b.inventory.availableCopies > 0) return false;

            return true;
        });

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, count: filtered.length, books: filtered }));
        return;
    }

    // 3. GET /books/details (Full bibliographic metadata and staff physical copies)
    if (pathname === '/books/details' && req.method === 'GET') {
        const bookId = parseInt(parsedUrl.query.id, 10);
        const book = bookData.books.find(b => b.bookId === bookId);
        if (!book) {
            res.writeHead(404, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'Book not found with ID: ' + bookId }));
            return;
        }

        const enriched = enrichBook(book);
        const copies = bookData.copies.filter(c => c.bookId === bookId);

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            success: true,
            book: enriched,
            inventory: enriched.inventory,
            copies: copies
        }));
        return;
    }

    // 4. POST /books/add (Transactional accession with initial copies)
    if (pathname === '/books/add' && req.method === 'POST') {
        const body = await parseRequestBody(req);
        const title = (body.title || '').trim();
        const author = (body.author || '').trim();
        const rawIsbn = (body.isbn || '').trim();
        const category = (body.category || 'General').trim();
        const department = (body.department || 'GENERAL').trim();
        const publisher = (body.publisher || '').trim();
        const edition = (body.edition || '').trim();
        const publicationYear = parseInt(body.publicationYear, 10) || new Date().getFullYear();
        const description = (body.description || '').trim();
        const initialCopies = Math.max(1, Math.min(50, parseInt(body.initialCopies, 10) || 1));

        if (!title || !author || !rawIsbn) {
            res.writeHead(400, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'Title, Author, and ISBN are mandatory fields.' }));
            return;
        }

        const cleanIsbn = rawIsbn.replace(/[^0-9X]/gi, '').toUpperCase();
        if (cleanIsbn.length !== 10 && cleanIsbn.length !== 13) {
            res.writeHead(400, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'Please enter a valid ISBN-10 or ISBN-13 format.' }));
            return;
        }

        // Duplicate ISBN check
        const duplicate = bookData.books.find(b => !b.archived && (b.isbn || '').replace(/[^0-9X]/gi, '').toUpperCase() === cleanIsbn);
        if (duplicate) {
            res.writeHead(409, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: `A book with ISBN ${rawIsbn} is already registered (${duplicate.title}).` }));
            return;
        }

        const nextBookId = bookData.books.reduce((max, b) => Math.max(max, b.bookId), 0) + 1;
        const coverImageUrl = body.coverImageUrl || `https://covers.openlibrary.org/b/isbn/${cleanIsbn}-L.jpg`;

        const newBook = {
            bookId: nextBookId,
            isbn: rawIsbn,
            title,
            author,
            publisher,
            edition,
            publicationYear,
            category,
            department,
            description,
            coverImageUrl,
            archived: false,
            createdAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
        };

        bookData.books.push(newBook);

        let maxCopyId = bookData.copies.reduce((max, c) => Math.max(max, c.copyId), 0);
        const createdCopies = [];
        for (let i = 1; i <= initialCopies; i++) {
            maxCopyId++;
            const copyNumberStr = i < 10 ? '0' + i : String(i);
            const copyCode = `PSG-BK${nextBookId}-C${copyNumberStr}`;
            const copyObj = {
                copyId: maxCopyId,
                copyCode,
                bookId: nextBookId,
                status: 'AVAILABLE',
                archived: false,
                createdAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
            };
            bookData.copies.push(copyObj);
            createdCopies.push(copyObj);
        }

        saveBooks();

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            success: true,
            message: `Book "${title}" successfully accessioned with ${initialCopies} physical copies.`,
            book: enrichBook(newBook),
            copies: createdCopies
        }));
        return;
    }

    // 5. POST /books/update (Metadata update preserving ISBN)
    if (pathname === '/books/update' && req.method === 'POST') {
        const body = await parseRequestBody(req);
        const bookId = parseInt(body.bookId, 10);
        const book = bookData.books.find(b => b.bookId === bookId);

        if (!book) {
            res.writeHead(404, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'Book not found with ID: ' + bookId }));
            return;
        }

        if (body.title) book.title = body.title.trim();
        if (body.author) book.author = body.author.trim();
        if (body.publisher !== undefined) book.publisher = body.publisher.trim();
        if (body.edition !== undefined) book.edition = body.edition.trim();
        if (body.publicationYear) book.publicationYear = parseInt(body.publicationYear, 10) || book.publicationYear;
        if (body.category) book.category = body.category.trim();
        if (body.department) book.department = body.department.trim();
        if (body.description !== undefined) book.description = body.description.trim();
        if (body.coverImageUrl) book.coverImageUrl = body.coverImageUrl.trim();

        saveBooks();

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            success: true,
            message: `Book "${book.title}" updated successfully.`,
            book: enrichBook(book)
        }));
        return;
    }

    // 6. POST /books/copies/add (Sequential next copy code generation)
    if (pathname === '/books/copies/add' && req.method === 'POST') {
        const body = await parseRequestBody(req);
        const bookId = parseInt(body.bookId, 10);
        const numberOfCopies = Math.max(1, Math.min(50, parseInt(body.numberOfCopies, 10) || 1));

        const book = bookData.books.find(b => b.bookId === bookId);
        if (!book) {
            res.writeHead(404, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: 'Book not found with ID: ' + bookId }));
            return;
        }

        const existingCopies = bookData.copies.filter(c => c.bookId === bookId);
        let maxIndex = 0;
        for (const c of existingCopies) {
            const match = c.copyCode.match(/-C(\d+)$/);
            if (match) {
                const idx = parseInt(match[1], 10);
                if (idx > maxIndex) maxIndex = idx;
            }
        }

        let maxCopyId = bookData.copies.reduce((max, c) => Math.max(max, c.copyId), 0);
        const newCopies = [];
        for (let i = 1; i <= numberOfCopies; i++) {
            maxCopyId++;
            const copyNum = maxIndex + i;
            const copyNumberStr = copyNum < 10 ? '0' + copyNum : String(copyNum);
            const copyCode = `PSG-BK${bookId}-C${copyNumberStr}`;
            const copyObj = {
                copyId: maxCopyId,
                copyCode,
                bookId,
                status: 'AVAILABLE',
                archived: false,
                createdAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
            };
            bookData.copies.push(copyObj);
            newCopies.push(copyObj);
        }

        saveBooks();

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            success: true,
            message: `Successfully added ${numberOfCopies} new physical copies for "${book.title}".`,
            bookId,
            copies: newCopies
        }));
        return;
    }

    // 7. POST /books/archive (Individual copy or entire book with guardrails)
    if (pathname === '/books/archive' && req.method === 'POST') {
        const body = await parseRequestBody(req);

        if (body.copyId) {
            const copyId = parseInt(body.copyId, 10);
            const copy = bookData.copies.find(c => c.copyId === copyId);
            if (!copy) {
                res.writeHead(404, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, message: 'Copy not found with ID: ' + copyId }));
                return;
            }

            if (copy.status !== 'AVAILABLE') {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({
                    success: false,
                    message: `Cannot archive copy with status: ${copy.status}. Only AVAILABLE copies can be archived.`
                }));
                return;
            }

            copy.status = 'ARCHIVED';
            copy.archived = true;
            saveBooks();

            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({
                success: true,
                message: `Copy ${copy.copyCode} has been archived successfully.`,
                copyId
            }));
            return;
        }

        if (body.bookId) {
            const bookId = parseInt(body.bookId, 10);
            const book = bookData.books.find(b => b.bookId === bookId);
            if (!book) {
                res.writeHead(404, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, message: 'Book not found with ID: ' + bookId }));
                return;
            }

            const copies = bookData.copies.filter(c => c.bookId === bookId && !c.archived && c.status !== 'ARCHIVED');
            const activeOccupied = copies.some(c => c.status === 'BORROWED' || c.status === 'ALLOCATED' || c.status === 'TRANSFER_PENDING');
            if (activeOccupied) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({
                    success: false,
                    message: 'Cannot archive book while physical copies are actively borrowed, allocated, or pending transfer.'
                }));
                return;
            }

            book.archived = true;
            for (const c of bookData.copies.filter(c => c.bookId === bookId)) {
                c.status = 'ARCHIVED';
                c.archived = true;
            }
            saveBooks();

            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({
                success: true,
                message: `Book "${book.title}" and all its copies have been safely archived.`,
                bookId
            }));
            return;
        }

        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, message: 'Either bookId or copyId is required for archival.' }));
        return;
    }

    // =========================================================================
    // Static File Serving
    // =========================================================================
    const filePath = path.join(WEBAPP_DIR, pathname);

    fs.stat(filePath, (err, stats) => {
        if (!err && stats.isFile()) {
            const ext = path.extname(filePath).toLowerCase();
            const contentType = MIME_TYPES[ext] || 'application/octet-stream';
            res.writeHead(200, { 'Content-Type': contentType });
            fs.createReadStream(filePath).pipe(res);
        } else {
            res.writeHead(404, { 'Content-Type': 'text/html; charset=UTF-8' });
            res.end(`<h1>404 Not Found</h1><p>The requested file ${pathname} was not found on PSG iTech Library.</p><p><a href="/index.html">Return to Homepage</a></p>`);
        }
    });
});

server.listen(PORT, () => {
    console.log(`[PSG iTech Library] Unified Server running at http://localhost:${PORT}/`);
    console.log(`[PSG iTech Library] Serving files from: ${WEBAPP_DIR}`);
});
