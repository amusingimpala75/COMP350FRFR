import { Toaster, toast } from "react-hot-toast";
import { useEffect, useState, useRef } from 'react';


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
  semester:string;
  credits: number;
}

export default function SearchPage() {
  const [query, setQuery] = useState('');
  const [department, setDepartment] = useState('ALL');
  const [professor, setProfessor] = useState('ALL');
  const [courses, setCourses] = useState<Course[]>([]);
  const [departments, setDepartments] = useState<string[]>([]);
  const [professors, setProfessors] = useState<string[]>([]);
  const [schedule, setSchedule] = useState<Set<string>>(new Set());
  const [days, setDays] = useState<Set<string>>(new Set());
  const [credits, setCredits] = useState<string>('ALL');
  const [timeStart, setTimeStart] = useState<string>('00:01');
  const [timeEnd, setTimeEnd] = useState<string>('23:59');
  const [availableCredits, setAvailableCredits] = useState<string[]>([]);
  const [availableTimes, setAvailableTimes] = useState<string[]>([]);
  const didMount = useRef(false);
  const isClearing = useRef(false);

  const getCourseId = (course: Course) =>
    `${course.subject}${course.number}${course.section}`;

  // --- SEARCH ---
  const search = async () => {
    setCourses([]);
    const res = await fetch('/search', {
      method: 'POST',
      headers: { 'Content-Type': 'text/plain' },
      body: query.trim(),
    });

    const items: Course[] = await res.json();

    //remove unwanted ZLOAD courses
    const filtered = items.filter(c => c.subject !== 'ZLOAD');
    setCourses(filtered);
  };
  // sending course info to backend
  useEffect(() => {
    if (!didMount.current) {
      didMount.current = true;
      return;
    }

    if (isClearing.current) {
      isClearing.current = false;
      return;
    }

    search();
  }, [department, professor, days, credits, timeStart, timeEnd]);


  //Toggles a course in the user's schedule and syncs with the backend.
  //If adding a course introduces a time conflict, the backend response is used to trigger a user notification.
  const toggleCourse = async (course: Course) => {
    const newSchedule = new Set(schedule);
    const courseId = getCourseId(course)

    //send the course identifier to the backend
    const result = await fetch('/schedule/items', {
      method: 'POST',
      headers: { 'Content-Type': 'text/plain' },
      body: courseId,
    });
    const text = await result.text();


    if(text == "Added"){
        newSchedule.add(courseId);
    }else if(text == "Removed"){
        newSchedule.delete(courseId);
    }else{
        toast(text)
    }

    setSchedule(newSchedule);
  };

  // updating days selected
  const toggleDay = async (day: string) => {
    const newDays = new Set(days);
    if (newDays.has(day)) newDays.delete(day);
    else newDays.add(day);
    const newArray = Array.from(newDays);
    await updateFilter('days', Array.from(days), newArray);
    setDays(newDays);
  };
  // defining default values for filters
  const isDefaultValue = (value: any) => {
    return value === 'ALL'
      || (Array.isArray(value) && value.length === 0)
      || (value.start === '00:01' && value.end === '23:59');
  };

  const updateFilter = async (type: string, oldValue: any, newValue: any) => {
    // remove old filter
    if (!isDefaultValue(oldValue)) {
      await fetch('/search/filter', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type, value: oldValue }),
      });
    }

    // add new filter
    if (!isDefaultValue(newValue)) {
      await fetch('/search/filter', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type, value: newValue }),
      });
    }
  };

  // updating all the filters

  const updateDept = async (event: React.ChangeEvent<HTMLSelectElement>) => {
    const updated = event.target.value;
    await updateFilter('department', department, updated);
    setDepartment(updated);
  };

  const updateProfessor = async (event: React.ChangeEvent<HTMLSelectElement>) => {
    const updated = event.target.value;
    await updateFilter('professor', professor, updated);
    setProfessor(updated);
  };

  const updateCredits = async (value: string) => {
    await updateFilter('credits', credits, value);
    setCredits(value);
  };

  const updateTimeStart = async (start: string) => {
    const newValue = { start, end: timeEnd };
    const oldValue = { start: timeStart, end: timeEnd };

    await updateFilter('timeRange', oldValue, newValue);
    setTimeStart(start);
  };

  const updateTimeEnd = async (end: string) => {
    const newValue = { start: timeStart, end };
    const oldValue = { start: timeStart, end: timeEnd };

    await updateFilter('timeRange', oldValue, newValue);
    setTimeEnd(end);
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

      setAvailableCredits(
         Array.from(new Set(items.map(c => c.credits).filter(c => c != null))).map(String).sort()
       );

      const times = items
        .flatMap(c => c.times?.map(t => t.start_time) || [])
        .filter(t => t); // remove empty
      setAvailableTimes(Array.from(new Set(times)).sort());
    };

    const fetchResults = async() => {
      const res = await fetch("/search/results");
      const items = (await res.json()) as Course[];
      setCourses(items.filter(c => c.subject !== 'ZLOAD'));
    };
    fetchResults();

    const fetchQuery = async() => {
      const res = await fetch("/search/query");
      const text = await res.text();
      setQuery(text);
    }

    fetchQuery();

    fetchCourses();

    const setFilters = async () => {
      const resp = await fetch('/search/filter');
      for (const filter of await resp.json()) {
        switch (filter.type) {
          case "department":
            setDepartment(filter.value);
            break;
          case "professor":
            setProfessor(filter.value);
            break;
          case "credits":
            setCredits(filter.value);
            break;
          case "days":
            setDays(new Set(filter.value));
            break;
          case "timeRange":
            // [TODO] this isn't working for some reason
            if (filter.value.start !== "00:01") {
              setTimeStart(filter.value.start + ':00');
            }
            if (filter.value.end !== "23:59") {
              setTimeEnd(filter.value.end + ':00');
            }
            break;
        }
      }
    };

    setFilters();
  }, []);

  // --- LOAD CURRENT SCHEDULE ---
  useEffect(() => {
    const fetchSchedule = async () => {
      try {
        const res = await fetch('/schedule/items');
        const items: Course[] = await res.json();
        const ids = new Set(items.map(c => getCourseId(c)));
        setSchedule(ids);
      } catch (err) {
        console.error('Failed to load schedule', err);
      }
    };

    fetchSchedule();
  }, []);


  const clearAllFilters = async () => {
    isClearing.current = true;

    const currentFilters = await fetch('/search/filter').then(res => res.json());

    for (const { type, value } of currentFilters) {
      await fetch('/search/filter', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type, value }),
      });
    }

    setDepartment('ALL');
    setProfessor('ALL');
    setDays(new Set());
    setCredits('ALL');
    setTimeStart('00:01');
    setTimeEnd('23:59');

    setCourses([]); // clear results
  };

  return (
    <div className="layout">
    <div><Toaster/></div>
      {/* LEFT SIDEBAR */}
      <div className="sidebar">
        <h3>Filters</h3>

        <h4> Department & Professor</h4>

        <select value={department} onChange={updateDept}>
          <option value="ALL">All Departments</option>
          {departments.map(dept => <option key={dept} value={dept}>{dept}</option>)}
        </select>
        <select value={professor} onChange={updateProfessor}>
          <option value="ALL">All Professors</option>
          {professors.map(prof => <option key={prof} value={prof}>{prof}</option>)}
        </select>

        <h4>Days and Time Range</h4>

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

        <div className="time-range">
          <select value={timeStart} onChange={e => updateTimeStart(e.target.value)}>
            <option value="00:01">Start</option>
              {availableTimes.map(t => <option key={t} value={t}>{t}</option>)}
          </select>
          <span>to</span>
          <select value={timeEnd} onChange={e => updateTimeEnd(e.target.value)}>
            <option value="23:59">End</option>
              {availableTimes.map(t => <option key={t} value={t}>{t}</option>)}
          </select>
        </div>

        <h4>Credits</h4>

        <select value={credits} onChange={e => updateCredits(e.target.value)}>
          <option value="ALL">All</option>
          {availableCredits
            .slice()                       // copy the array so we don’t mutate the original
            .sort((a, b) => Number(a) - Number(b))  // numeric sort
            .map(c => <option key={c} value={c}>{c}</option>)
          }
        </select>

        <h5></h5>
        <button className="clear-btn" onClick={clearAllFilters}>
          Clear All Filters
        </button>

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
                let inSchedule = schedule.has(courseId)

              return (
                <li key={courseId} className="course-row">
                  <button
                    className="course-btn"
                    onClick={() => toggleCourse(course)}
                  >
                    {inSchedule ? '-' : '+'}
                  </button>

                  <span className="course-text">
                    {course.subject}{course.number} {course.section} — {course.name} — {
                      course.times?.length
                        ? Array.from(
                            course.times.reduce((acc, t) => {
                              const key = `${t.start_time?.slice(0,5)} - ${t.end_time?.slice(0,5)}`;
                              if (!acc.has(key)) acc.set(key, []);
                              acc.get(key)!.push(t.day);
                              return acc;
                            }, new Map<string, string[]>())
                          )
                            .map(([time, days]) => `${days.join("")}, ${time}`)
                            .join(" & ")
                        : "TBA"
                    } — {course.faculty}
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
