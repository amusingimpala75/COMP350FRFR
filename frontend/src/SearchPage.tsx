import { useState, useEffect } from 'react';

interface CourseTime {
  day: string;
  start_time: string;
  end_time: string;
}

interface Course {
  subject: string;
  number: string;
  section: string;
  name: string;
  faculty: string[];
  times: CourseTime[];
}

export default function SearchPage() {
  const [query, setQuery] = useState('');
  const [department, setDepartment] = useState('ALL');
  const [professor, setProfessor] = useState('ALL');
  const [days, setDays] = useState<Set<string>>(new Set());
  const [courses, setCourses] = useState<Course[]>([]);
  const [departments, setDepartments] = useState<string[]>([]);
  const [professors, setProfessors] = useState<string[]>([]);
  const [schedule, setSchedule] = useState<Set<string>>(new Set());

  // --- SEARCH ---
  const search = async () => {
    const res = await fetch('/search', {
      method: 'POST',
      headers: { 'Content-Type': 'text/plain' },
      body: query.trim(),
    });

    const items: Course[] = await res.json();

    // Deduplicate courses
    const seen = new Set<string>();
    const uniqueCourses: Course[] = [];
    for (const c of items) {
      const id = `${c.subject}${c.number}${c.section}`;
      if (!seen.has(id)) {
        seen.add(id);
        uniqueCourses.push(c);
      }
    }

    setCourses(uniqueCourses);
  };

  // --- TOGGLE COURSE ---
  const toggleCourse = async (course: Course) => {
    const courseId = `${course.subject}${course.number}${course.section}`;
    const newSchedule = new Set(schedule);
    if (newSchedule.has(courseId)) newSchedule.delete(courseId);
    else newSchedule.add(courseId);
    setSchedule(newSchedule);

    await fetch('/schedule/items', {
      method: 'POST',
      headers: { 'Content-Type': 'text/plain' },
      body: courseId,
    });
  };

  // --- DEPARTMENT FILTER ---
  const updateDept = async (event: React.ChangeEvent<HTMLSelectElement>) => {
    const old = department;
    const updated = event.target.value;

    if (old !== 'ALL') {
      await fetch('/search/filter', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'department', value: old }),
      });
    }

    if (updated !== 'ALL') {
      await fetch('/search/filter', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'department', value: updated }),
      });
    }

    setDepartment(updated);
    search();
  };

  // --- PROFESSOR FILTER ---
  const updateProf = async (event: React.ChangeEvent<HTMLSelectElement>) => {
    const old = professor;
    const updated = event.target.value;

    if (old !== 'ALL') {
      await fetch('/search/filter', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'professor', value: old }),
      });
    }

    if (updated !== 'ALL') {
      await fetch('/search/filter', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'professor', value: updated }),
      });
    }

    setProfessor(updated);
    search();
  };

  // --- DAYS FILTER ---
  const toggleDay = async (day: string) => {
    const newDays = new Set(days);
    let method: 'POST' | 'DELETE';
    if (newDays.has(day)) {
      newDays.delete(day);
      method = 'DELETE';
    } else {
      newDays.add(day);
      method = 'POST';
    }

    setDays(newDays);

    await fetch('/search/filter', {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ type: 'day', value: day }),
    });

    search();
  };

  // --- LOAD COURSES FOR FILTERS ---
  useEffect(() => {
    const fetchCourses = async () => {
      const res = await fetch('/courses');
      const items: Course[] = await res.json();

      setDepartments(
        Array.from(new Set(items.map(c => c.subject).filter(d => d && d !== 'ZLOAD'))).sort()
      );

      setProfessors(
        Array.from(
          new Set(
            items
              .flatMap(c => c.faculty || [])
              .filter(p => p && !p.includes('Staff, -') && p !== '-')
              .map(p => p.replace(/,?\s*PhD\.?/i, '').trim())
          )
        ).sort()
      );
    };

    const fetchFilters = async () => {
      const resp = await fetch('/search/filter');
      const savedFilters: { type: string; value: string }[] = await resp.json();
      for (const filter of savedFilters) {
        if (filter.type === 'department') setDepartment(filter.value);
        if (filter.type === 'professor') setProfessor(filter.value);
        if (filter.type === 'day') setDays(prev => new Set(prev.add(filter.value)));
      }
    };

    fetchCourses();
    fetchFilters();
  }, []);

  // --- LOAD CURRENT SCHEDULE ---
  useEffect(() => {
    const fetchSchedule = async () => {
      try {
        const res = await fetch('/schedule/items');
        const items: Course[] = await res.json();
        const ids = new Set(items.map(c => `${c.subject}${c.number}${c.section}`));
        setSchedule(ids);
      } catch (err) {
        console.error('Failed to load schedule', err);
      }
    };

    search();
    fetchSchedule();
  }, []);

  return (
    <div className="layout">
      {/* LEFT SIDEBAR */}
      <div className="sidebar">
        <h3>Filters</h3>

        <select value={department} onChange={updateDept}>
          <option value="ALL">All Departments</option>
          {departments.map(dept => <option key={dept} value={dept}>{dept}</option>)}
        </select>

        <select value={professor} onChange={updateProf}>
          <option value="ALL">All Professors</option>
          {professors.map(prof => <option key={prof} value={prof}>{prof}</option>)}
        </select>

        <h4>Days</h4>
        <div className="day-selector">
          {["M","T","W","R","F"].map(day => (
            <button
              key={day}
              className={`day-btn ${days.has(day) ? "selected" : ""}`}
              onClick={() => toggleDay(day)}
            >
              {day}
            </button>
          ))}
        </div>
      </div>

      {/* MAIN CONTENT */}
      <div className="main">
        <div className="card search-card">
          <h2>HALL Monitor's Scheduler</h2>
          <input
            placeholder="Search Query"
            value={query}
            onChange={e => setQuery(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && search()}
          />
          <button onClick={search}>Search</button>
        </div>

        <div className="card results-card">
          <h3>Results</h3>
          <ul>
            {courses.map(course => {
              const courseId = `${course.subject}${course.number}${course.section}`;
              const inSchedule = schedule.has(courseId);

              return (
                <li key={courseId} className="course-row">
                  <button
                    className="course-btn"
                    onClick={() => toggleCourse(course)}
                  >
                    {inSchedule ? '-' : '+'}
                  </button>

                  <span className="course-text">
                    {course.subject}{course.number} {course.section} — {course.name}
                  </span>
                </li>
              );
            })}
          </ul>
        </div>
      </div>
    </div>
  );
}
