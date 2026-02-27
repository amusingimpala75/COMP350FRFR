import { useState, useEffect } from 'react';
import './App.css';

interface Course {
  subject: string;
  number: string;
  section: string;
  name: string;
}

function App() {
  const [query, setQuery] = useState('');
  const [department, setDepartment] = useState('ALL');
  const [courses, setCourses] = useState<Course[]>([]);
  const [departments, setDepartments] = useState<string[]>([]);

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
          const depts = Array.from(new Set(items.map(c => c.subject))).sort();
          setDepartments(depts);
        });
    }, []);

  return (
    <div className="layout">

      {/* LEFT SIDEBAR */}
      <div className="sidebar">
        <h3>Departments</h3>
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
      </div>

      {/* MAIN CONTENT */}
      <div className="main">

        {/* Search Card */}
        <div className="card">
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