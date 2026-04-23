import { BrowserRouter as Router, Routes, Route, Navigate, NavLink} from 'react-router-dom';
import { useEffect, useState } from 'react'
import SearchPage from './SearchPage';
import SchedulePage from './SchedulePage';
import './App.css';

function App() {
  const [scheduleId, setScheduleId] = useState<number | null>(null);

  useEffect(() => {
    const saved = localStorage.getItem("scheduleId");
    if (saved) setScheduleId(Number(saved));
  }, []);

  useEffect(() => {
    if (scheduleId !== null) {
      localStorage.setItem("scheduleId", String(scheduleId));
    }
  }, [scheduleId]);

  return (
    <Router>
      {/* Top navigation bar */}
      <header className="top-bar">
        <nav>
          <NavLink to="/search" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Search</NavLink>
          <NavLink to="/schedule" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Schedule</NavLink>
        </nav>
      </header>

      {/* Page content */}
      <main>
        <Routes>
          // content per page
          <Route path="/search" element={<SearchPage scheduleId={scheduleId}/>} />
          <Route path="/schedule" element={<SchedulePage scheduleId={scheduleId} setScheduleId={setScheduleId}/>} />
          <Route path="*" element={<Navigate to="/search" replace />} /> {/* Default to search */}
        </Routes>
      </main>
    </Router>
  );
}

export default App;
