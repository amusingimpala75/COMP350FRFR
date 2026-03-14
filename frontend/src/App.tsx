import { BrowserRouter as Router, Routes, Route, NavLink } from 'react-router-dom';
import SearchPage from './SearchPage';
import SchedulePage from './SchedulePage';
import './App.css';

function App() {
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
          <Route path="*" element={<SearchPage />} /> {/* Default to search */}
          <Route path="/search" element={<SearchPage />} />
          <Route path="/schedule" element={<SchedulePage />} />
        </Routes>
      </main>
    </Router>
  );
}

export default App;
