import { useState, useEffect } from 'react';
import './App.css';

interface Course {
  subject: string;
  number: string;
  section: string;
  name: string;
  faculty: string[];
}

function App() {
  const [query, setQuery] = useState('');
  const [department, setDepartment] = useState('ALL');
  const [courses, setCourses] = useState<Course[]>([]);
  const [departments, setDepartments] = useState<string[]>([]);
  const [professor, setProfessor] = useState('ALL');
  const [professors, setProfessors] = useState<string[]>([]);

  const search = async () => {
    //if (!query.trim()) return;
    // comment this out so that empty inputs can be made (i.e. no query and no filter)
    // so that every class can be seen with no input

    await fetch('/search', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        query: query.trim(),
        department: department,
        professor: professor,
      }),
    });

    const res = await fetch('/search');
    const items: Course[] = await res.json();
    setCourses(items);

    const depts = Array.from(new Set(items.map(c => c.subject))).sort();
        setDepartments(depts);
  };

  //populate dropdown with departments on load
  useEffect(() => {
    fetch('/courses')
      .then(res => res.json())
      .then((items: Course[]) => {

        const depts = Array.from(
          new Set(
            items
              .map(c => c.subject)
              .filter(d => d && d !== "ZLOAD")
          )
        ).sort();
        setDepartments(depts);

        const profs = Array.from(
          new Set(
            items
              .flatMap(c => c.faculty || [])
              .filter(p =>
                p &&
                !p.includes("Staff, -") &&
                p !== "-"
              ).map(p => p.replace(/,?\s*PhD\.?/i, "").trim())
          )
        ).sort();
        setProfessors(profs);

      });
  }, []);

  return (
    <div className="layout">

      {/* LEFT SIDEBAR */}
      <div className="sidebar">
        <h3>Filters</h3>
        <select
          value={department}
          onChange={(e) => setDepartment(e.target.value)}
        >
          <option value="ALL">All Departments</option>
          {departments.map((dept) => (
            <option key={dept} value={dept}>
              {dept}
            </option>
          ))}
        </select>

        <select
          value={professor}
          onChange={(e) => setProfessor(e.target.value)}
        >
          <option value="ALL">All Professors</option>

          {professors.map((prof) => (
            <option key={prof} value={prof}>
              {prof}
            </option>
          ))}
        </select>
      </div>

      {/* MAIN CONTENT */}
      <div className="main">

        {/* Search Card */}
        <div className="card search-card">
          <h2>HALL Monitor's Scheduler</h2>

          <input
            placeholder="Search Query"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') search();
            }}
          />

          <button onClick={search}>Search</button>
        </div>

        {/* Results Card */}
        <div className="card results-card">
          <h3>Results</h3>
          <ul>
            {courses.map((course, idx) => (
              <li key={idx}>
                {course.subject}
                {course.number} {course.section} — {course.name}
              </li>
            ))}
          </ul>
        </div>

      </div>
    </div>
  );
}

export default App;