import { BrowserRouter as Router, Routes, Route, Navigate, NavLink } from 'react-router-dom';
import type { ReactNode } from 'react';
import SearchPage from './SearchPage';
import SchedulePage from './SchedulePage';
import LoginPage from './LoginPage';
import SignupPage from './SignupPage';
import { useAuth } from './AuthContext';
import './App.css';

function ProtectedRoute({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="auth-loading">Loading...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

function App() {
  const { user, logout, loading } = useAuth();

  return (
    <Router>
      {/* Top navigation bar */}
      <header className="top-bar">
        <nav>
          {user ? (
            <>
              <NavLink to="/search" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Search</NavLink>
              <NavLink to="/schedule" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Schedule</NavLink>
              <button className="top-bar-btn" onClick={logout}>Logout</button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Login</NavLink>
              <NavLink to="/signup" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Sign Up</NavLink>
            </>
          )}
        </nav>
      </header>

      {/* Page content */}
      <main>
        <Routes>
          <Route path="/login" element={user ? <Navigate to="/search" replace /> : <LoginPage />} />
          <Route path="/signup" element={user ? <Navigate to="/search" replace /> : <SignupPage />} />
          <Route path="/search" element={<ProtectedRoute><SearchPage /></ProtectedRoute>} />
          <Route path="/schedule" element={<ProtectedRoute><SchedulePage /></ProtectedRoute>} />
          <Route
            path="*"
            element={
              loading
                ? <div className="auth-loading">Loading...</div>
                : <Navigate to={user ? '/search' : '/login'} replace />
            }
          />
        </Routes>
      </main>
    </Router>
  );
}

export default App;
