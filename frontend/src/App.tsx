import { useState } from 'react';
import './App.css';

interface Course {
  subject: string;
  number: string;
  section: string;
  name: string;
}

function App() {
  const [query, setQuery] = useState('');
  const [courses, setCourses] = useState<Course[]>([]);

  // POST search query and load results
  const search = async () => {
    if (!query.trim()) return;

    await fetch('/search', {
      method: 'POST',
      body: query.trim(),
    });

    // Load results
    const res = await fetch('/search');
    const items: Course[] = await res.json();
    setCourses(items);
  };

  return (
    <>

      <div className="card">
        <input
          placeholder="search for courses"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              search();
            }
          }}
        />
        <button onClick={search}>Search</button>
        <ul>
          {courses.map((course, idx) => (
            <li key={idx}>
              {course.subject}{course.number} {course.section} — {course.name}
            </li>
          ))}
        </ul>
      </div>

    </>
  );
}

export default App;