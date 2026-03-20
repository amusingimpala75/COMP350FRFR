import { BrowserRouter as Router, Routes, Route, Navigate, NavLink } from 'react-router-dom';
import SearchPage from './SearchPage';
import SchedulePage from './SchedulePage';
import './App.css';

function App() {
  return (
    <Router>
      //links for the different pages
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
          <Route path="/search" element={<SearchPage />} />
          <Route path="/schedule" element={<SchedulePage />} />
          <Route path="*" element={<Navigate to="/search" replace />} /> {/* Default to search */}
        </Routes>
      </main>
    </Router>
  );
}

export default App;
