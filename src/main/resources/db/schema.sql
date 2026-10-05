-- University Resource Management System (URMS)
-- SQLite Database Schema with UUID Primary Keys
PRAGMA foreign_keys = ON;

-- 0. Admin Table (operator login & session management)
CREATE TABLE IF NOT EXISTS Admin (
    admin_id TEXT PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT,
    session_token TEXT,
    token_expiry TEXT,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))
);

-- 1. Users Table (campus directory: students & faculty borrowers)
CREATE TABLE IF NOT EXISTS Users (
    user_id TEXT PRIMARY KEY,
    enrollment_no TEXT NOT NULL UNIQUE,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('STUDENT', 'FACULTY')),
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))
);

-- 2. Categories Table
CREATE TABLE IF NOT EXISTS Categories (
    category_id TEXT PRIMARY KEY,
    category_name TEXT NOT NULL UNIQUE,
    description TEXT,
    is_active INTEGER NOT NULL DEFAULT 1
);

-- 3. Resources Table
CREATE TABLE IF NOT EXISTS Resources (
    resource_id TEXT PRIMARY KEY,
    resource_name TEXT NOT NULL,
    category_id TEXT NOT NULL,
    resource_type TEXT NOT NULL DEFAULT 'CONSUMABLE',
    location TEXT,
    total_quantity INTEGER NOT NULL,
    available_quantity INTEGER NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),
    FOREIGN KEY (category_id)
        REFERENCES Categories(category_id)
);

-- 4. Allocations Table
CREATE TABLE IF NOT EXISTS Allocations (
    allocation_id TEXT PRIMARY KEY,
    resource_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    issue_date TEXT NOT NULL DEFAULT (datetime('now','localtime')),
    due_date TEXT NOT NULL,
    return_date TEXT,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    issued_by TEXT,
    remarks TEXT,
    FOREIGN KEY (resource_id)
        REFERENCES Resources(resource_id),
    FOREIGN KEY (user_id)
        REFERENCES Users(user_id)
);
