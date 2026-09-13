# Exam Training System

A Java/Vue training and examination platform with question banks, paper composition, exams, scoring, and learning workflows.

## Structure

- `backend/`: Spring Boot multi-module backend
- `frontend/`: Vue frontend for administrators and students

## Local development

1. Install Java 17+, Maven, Node.js and npm.
2. Create a local MySQL database and Redis instance.
3. Set database, Redis and token values through environment variables; never commit credentials.
4. Start the backend from `backend/` and the frontend from `frontend/`.

This repository contains source code only. Production databases, uploaded files, private deployment notes and real examination data are excluded.
