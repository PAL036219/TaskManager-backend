# Project Manager App

A full-stack project management web application built to streamline task tracking, team collaboration, and project analytics. This application features user authentication, role-based access control (Admin/Member), comprehensive project and task management capabilities, and an analytical dashboard.

## 🚀 Features

- **User Authentication**: Secure login and registration using JWT (JSON Web Tokens).
- **Role-Based Access Control (RBAC)**: Differentiated access for Admin and Member roles.
- **Project Management**: Create, update, view, and delete projects.
- **Task Tracking**: Assign tasks to team members, update statuses, and track progress.
- **Analytical Dashboard**: Visual insights into project metrics and task statuses using Recharts.
- **Responsive UI**: Modern, clean, and responsive user interface built with Tailwind CSS.

## 🛠️ Tech Stack

### Backend
- **Java 17**
- **Spring Boot 3.2**
- **Spring Security** (for Authentication & Authorization)
- **Spring Data MongoDB**
- **JWT (jjwt)** (for secure token-based authentication)
- **Lombok** (to reduce boilerplate code)
- **Maven** (Dependency Management)

### Frontend
- **React 19**
- **TypeScript**
- **Vite** (Build Tool)
- **Redux Toolkit** (State Management)
- **React Router v7** (Navigation)
- **Tailwind CSS** (Styling)
- **Recharts** (Data Visualization)
- **Axios** (API requests)
- **Lucide React** (Icons)

## 📂 Project Structure

The repository is organized into two main directories:

- `/backend`: Contains the Java Spring Boot REST API and configuration.
- `/frontend`: Contains the React/Vite frontend application.

## ⚙️ Prerequisites

Before you begin, ensure you have the following installed:
- [Java Development Kit (JDK) 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [Node.js](https://nodejs.org/) (v18 or higher recommended)
- [Maven](https://maven.apache.org/)
- [MongoDB](https://www.mongodb.com/) (Local instance or MongoDB Atlas cluster)

## 🚀 Getting Started

### 1. Database Configuration
By default, the backend expects a MongoDB connection. Update the connection URI in `backend/src/main/resources/application.properties` to point to your local MongoDB instance or MongoDB Atlas cluster.

### 2. Running the Backend

1. Navigate to the `backend` directory:
   ```bash
   cd backend
   ```
2. Build the project and download dependencies:
   ```bash
   mvn clean install
   ```
3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```
   The backend API will start on `http://localhost:8080`.

### 3. Running the Frontend

1. Open a new terminal and navigate to the `frontend` directory:
   ```bash
   cd frontend
   ```
2. Install the required npm packages:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
   The frontend will be accessible at the URL provided by Vite (usually `http://localhost:5173`).

## 📜 Scripts

### Frontend
- `npm run dev` - Starts the Vite development server.
- `npm run build` - Compiles TypeScript and builds the app for production.
- `npm run preview` - Previews the production build locally.

## 📄 License
This project is for educational purposes.
