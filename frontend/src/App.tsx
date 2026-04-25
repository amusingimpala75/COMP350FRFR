import { BrowserRouter as Router, Routes, Route, Navigate, NavLink} from 'react-router-dom';
import { useEffect, useState } from 'react'
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
  const [scheduleId, setScheduleId] = useState<number | null>(null);
  const { user, logout, loading } = useAuth();

  useEffect(() => {
    if (!user) {
      setScheduleId(null);
      return;
    }

    const scopedKey = `scheduleId:${user.id}`;
    const saved = localStorage.getItem(scopedKey);
    const parsed = saved ? Number(saved) : NaN;
    const savedScheduleId = Number.isFinite(parsed) && parsed > 0 ? parsed : null;

    let cancelled = false;

    const reconcileSchedule = async () => {
      try {
        const res = await fetch('/schedules');
        if (!res.ok) {
          throw new Error('Failed to load schedules');
        }

        const schedules = (await res.json()) as Array<{ id: number }>;
        if (cancelled) {
          return;
        }

        if (schedules.length === 0) {
          setScheduleId(null);
          localStorage.removeItem(scopedKey);
          return;
        }

        const hasSavedSchedule =
          savedScheduleId != null && schedules.some((schedule) => schedule.id === savedScheduleId);
        setScheduleId(hasSavedSchedule ? savedScheduleId : schedules[0].id);
      } catch {
        // Fallback to scoped local value if schedules request fails.
        setScheduleId(savedScheduleId);
      } finally {
        // Cleanup legacy key from older builds that stored one global schedule id.
        localStorage.removeItem("scheduleId");
      }
    };

    reconcileSchedule();

    return () => {
      cancelled = true;
    };
  }, [user?.id]);

  useEffect(() => {
    if (!user) {
      return;
    }

    const scopedKey = `scheduleId:${user.id}`;
    if (scheduleId !== null) {
      localStorage.setItem(scopedKey, String(scheduleId));
    } else {
      localStorage.removeItem(scopedKey);
    }
  }, [scheduleId, user?.id]);

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
          <Route path="/search" element={<ProtectedRoute><SearchPage userId={user?.id ?? 0} scheduleId={scheduleId}/></ProtectedRoute>} />
          <Route path="/schedule" element={<ProtectedRoute><SchedulePage userId={user?.id ?? 0} scheduleId={scheduleId} setScheduleId={setScheduleId}/></ProtectedRoute>} />
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
