-- University Resource Management System (URMS)
-- Simple SQLite Database Schema
PRAGMA foreign_keys = ON;

-- 0. User Table (operator login)
CREATE TABLE IF NOT EXISTS Users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))
);

-- 1. Categories Table
CREATE TABLE IF NOT EXISTS Categories (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_name TEXT NOT NULL UNIQUE,
    description TEXT,
    is_active INTEGER NOT NULL DEFAULT 1
);

-- 2. Resources Table
CREATE TABLE IF NOT EXISTS Resources (
    resource_id INTEGER PRIMARY KEY AUTOINCREMENT,
    resource_name TEXT NOT NULL,
    category_id INTEGER NOT NULL,
    resource_type TEXT NOT NULL DEFAULT 'CONSUMABLE',
    location TEXT,
    total_quantity INTEGER NOT NULL,
    available_quantity INTEGER NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),
    FOREIGN KEY (category_id)
        REFERENCES Categories(category_id)
);

-- 3. Allocations Table
CREATE TABLE IF NOT EXISTS Allocations (
    allocation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    resource_id INTEGER NOT NULL,
    borrower_id TEXT NOT NULL,
    borrower_name TEXT NOT NULL,
    borrower_type TEXT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    issue_date TEXT NOT NULL DEFAULT (datetime('now','localtime')),
    due_date TEXT NOT NULL,
    return_date TEXT,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    issued_by TEXT,
    remarks TEXT,
    FOREIGN KEY (resource_id)
        REFERENCES Resources(resource_id)
);
