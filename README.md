# PSG iTech Library - Resource and Queue Management System

**Official College Digital Library & User Management System**  
**PSG Institute of Technology and Applied Research (PSG iTech), Coimbatore**

---

## 1. Project Overview

**PsgItech Library** is an institutional digital library portal inspired by the visual elegance of Open Library, designed to provide college students, faculty librarians, and system administrators with a unified, academic, and secure digital library experience.

### User Roles Supported:
1. **Student**: Self-registration, book discovery, activity tracking, reading record, profile management.
2. **Librarian**: Operational user directory, desk operations, profile management, mandatory first-login password security.
3. **Admin**: Complete user administration, account activation/deactivation (preserving historical records), librarian account provisioning.

---

## 2. Strict Design System Implementation

The application strictly enforces the requested design requirements:

- **Typography**: 100% **Times New Roman** across the entire application (`* { font-family: "Times New Roman", Times, serif; }`). Every heading, text label, form input, button, table cell, and modal dialog adheres to this standard.
- **Headings**: Navy Blue (`#203B60`).
- **Normal Text**: Pure Black (`#000000`).
- **Secondary Information**: Dark Grey (`#444444`).
- **Color Palette**:
  - Primary Navy: `#203B60`
  - Secondary Navy: `#344F70`
  - Main Background: `#FFFFFF`
  - Featured/Warm Background: `#F8F6F0`
  - Secondary Background: `#EFECE6`
  - Borders: `#E2E8F0`
  - Success Badge: `#159B50` (soft background `#E8F8F0`)
  - Warning/Queue Badge: `#C58B24` (soft background `#FEF6E8`)
  - Danger/Overdue Badge: `#C0392B` (soft background `#FDE8E8`)
- **Buttons**:
  - Primary: `#203B60` background, white text, Times New Roman, subtle hover.
  - Secondary: `#FFFFFF` background, `#203B60` border, `#203B60` text.
  - Danger: `#C0392B` background, white text.

---

## 3. Technology Stack

- **Frontend**: HTML5, Vanilla CSS3 (`css/library-theme.css`), Vanilla JavaScript (`js/main.js`, `js/auth.js`, `js/admin.js`).
- **Backend**: Core Java, Java Servlets (`javax.servlet` 4.0.1 specification for Apache Tomcat).
- **Architecture**: Strict Java OOP principles (Inheritance, Polymorphism, Encapsulation, Abstraction).
- **Password Security**: BCrypt (`jbcrypt:0.4`) with cost factor 12.
- **Database**: MySQL 8.x via JDBC (`PreparedStatement`), with automatic in-memory fallback during local development/tests.
- **Build Tool**: Apache Maven (`pom.xml`, generating deployable WAR archive `psgitech-library.war`).

---

## 4. Folder Structure

```text
Oops/
├── pom.xml                                   # Maven Build Configuration
├── README.md                                 # Complete Documentation & Run Guide
├── server.js                                 # Lightweight Local HTTP Dev Server
├── database/
│   ├── schema.sql                            # MySQL Database Creation & DDL Tables
│   └── seed_data.sql                         # Initial Seed Data with Verified BCrypt Hashes
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/psgitech/library/
    │   │       ├── enums/
    │   │       │   └── Role.java             # STUDENT, LIBRARIAN, ADMIN
    │   │       ├── exception/
    │   │       │   ├── AccountInactiveException.java
    │   │       │   ├── InvalidCredentialsException.java
    │   │       │   ├── UserAlreadyExistsException.java
    │   │       │   └── UserNotFoundException.java
    │   │       ├── filter/
    │   │       │   ├── AuthenticationFilter.java    # Remember Me cookie & session restore
    │   │       │   └── AuthorizationFilter.java     # RBAC & first-login redirect guard
    │   │       ├── model/
    │   │       │   ├── User.java             # Abstract base class
    │   │       │   ├── Student.java          # Extends User (regNo, dept, year)
    │   │       │   ├── Librarian.java        # Extends User (employeeId, mustChangePassword)
    │   │       │   └── Admin.java            # Extends User
    │   │       ├── repository/
    │   │       │   └── UserRepository.java   # JDBC PreparedStatements for users & tokens
    │   │       ├── service/
    │   │       │   ├── AuthenticationService.java  # Login, BCrypt, Remember Me lifecycle
    │   │       │   └── UserService.java            # Registration, Profile, Password, RBAC
    │   │       ├── servlet/
    │   │       │   ├── AdminUserServlet.java       # /admin/users (list, search, toggle, create lib)
    │   │       │   ├── ChangePasswordServlet.java  # /change-password
    │   │       │   ├── LoginServlet.java           # /login
    │   │       │   ├── LogoutServlet.java          # /logout
    │   │       │   ├── ProfileServlet.java         # /profile
    │   │       │   └── RegisterServlet.java        # /register
    │   │       └── util/
    │   │           ├── DBConnection.java     # MySQL Connection Manager with dev fallback
    │   │           ├── PasswordUtil.java     # BCrypt hashing & SHA-256 token hashing
    │   │           └── ValidationUtil.java   # Email, phone, password strength, ID checks
    │   ├── resources/
    │   │   ├── db.properties                 # Database connection parameters
    │   │   ├── schema.sql                    # Classpath schema script
    │   │   └── seed_data.sql                 # Classpath seed script
    │   └── webapp/
    │       ├── WEB-INF/
    │       │   └── web.xml                   # Deployment Descriptor for Apache Tomcat
    │       ├── css/
    │       │   └── library-theme.css         # Master Stylesheet (Times New Roman, Navy Theme)
    │       ├── js/
    │       │   ├── admin.js                  # User listing, search, filter, modals, status
    │       │   ├── auth.js                   # Register, login, remember-me, profile, password
    │       │   └── main.js                   # Global navbar, dropdowns, search, book scrolls
    │       ├── admin-users.html              # Admin User Management
    │       ├── browse.html                   # Browse & Book Catalog (Open Library Style)
    │       ├── first-login-change-password.html # Mandatory First-Login Password Change
    │       ├── index.html                    # Public Homepage (Hero, Info Cards, Shelves)
    │       ├── librarian-dashboard.html      # Librarian Operations Dashboard
    │       ├── login.html                    # Sign In with Remember Me & Autocomplete
    │       ├── my-books.html                 # My Books (Borrowed, Returned, Overdue tabs)
    │       ├── profile.html                  # My Profile & Change Password
    │       ├── register.html                 # Student Registration Form
    │       └── student-home.html             # Student Dashboard (Activity Indicators)
    └── test/
        └── java/
            └── com/psgitech/library/
                ├── AuthenticationServiceTest.java # 4 Unit Tests (Login, Bad Pass, Inactive, Remember Me)
                ├── PasswordUtilTest.java          # 2 Unit Tests (BCrypt & Token Hashing)
                ├── UserServiceTest.java           # 6 Unit Tests (Reg, Duplicates, Pass Change, Toggle)
                └── ValidationUtilTest.java        # 5 Unit Tests (Email, Phone, Dept, Year, Strength)
```

---

## 5. Demonstration & Sample Accounts

All sample accounts use BCrypt hashed passwords stored in the database:

| Role | Username / Identifier | Email | Password | Details |
|---|---|---|---|---|
| **Admin** | `admin@psgitech.ac.in` | `admin@psgitech.ac.in` | `AdminPassword@123` | Full administrative privileges |
| **Librarian** | `LIB101` | `librarian@psgitech.ac.in` | `Librarian@123` | Active established staff member |
| **Librarian** | `LIB102` | `meena.lib@psgitech.ac.in` | `InitialPassword@123` | First login password change enforced |
| **Student** | `715522104045` | `sivakathir.22cs@psgitech.ac.in` | `Student@123` | CSE, 3rd Year (Active) |
| **Student** | `715523105012` | `ananya.23ai@psgitech.ac.in` | `Student@123` | AI&DS, 2nd Year (Active) |
| **Student** | `715521102030` | `rahul.21me@psgitech.ac.in` | `Student@123` | MECH, 4th Year (Deactivated test) |

---

## 6. Setup & Execution Instructions

### A. MySQL Database Setup
1. Start your local MySQL service (e.g. MySQL Server or XAMPP MySQL on port 3306).
2. Execute the setup scripts in order using MySQL CLI or MySQL Workbench:
   ```bash
   mysql -u root -p < database/schema.sql
   mysql -u root -p < database/seed_data.sql
   ```
3. Update `src/main/resources/db.properties` with your database credentials if different from `root`/`root`.

### B. Building with Maven & Running Unit Tests
Compile and run all 17 automated tests:
```bash
mvn clean test
```
*Result: 17/17 tests passing with 0 errors and 0 failures.*

Package the deployable WAR file:
```bash
mvn package
```
*Output: `target/psgitech-library.war`*

### C. Deploying to Apache Tomcat
1. Copy `target/psgitech-library.war` to your Apache Tomcat `webapps/` directory.
2. Start Apache Tomcat (`bin/startup.bat` or `bin/catalina.bat run`).
3. Open your browser and navigate to:
   ```text
   http://localhost:8080/psgitech-library/
   ```

### D. Instant Local Preview & Development Server
To preview and test the complete frontend and interface interactions without waiting for a full Tomcat startup:
```bash
node server.js
```
Open: `http://localhost:8080/`

---

## 7. Security Features Implemented

1. **BCrypt Password Hashing**: Passwords are never stored in plaintext. Passwords use BCrypt with salt rounds.
2. **Persistent Remember Me Tokens**: Tokens are stored using SHA-256 hashes in MySQL (`remember_tokens` table) with HTTP-only cookies and 14-day expiry. Tokens are automatically revoked upon logout or password change.
3. **Role-Based Authorization**: Standard servlet filter (`AuthorizationFilter`) blocks unauthorized access to admin/librarian routes.
4. **First-Login Enforcement**: New librarians must change their temporary password on first sign-in before accessing the dashboard.
5. **Account Deactivation**: Admins can deactivate accounts while preserving historical records and checkout history.
