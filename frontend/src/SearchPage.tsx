import { useState, useEffect } from 'react';
import { Toaster, toast } from "react-hot-toast";

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

function timeToSeconds(hms: string): number {
  const [h = '0', m = '0', s = '0'] = hms.split(':');
  return Number(h) * 3600 + Number(m) * 60 + Number(s);
}


export default function SearchPage() {
  const [query, setQuery] = useState('');
  const [department, setDepartment] = useState('ALL');
  const [professor, setProfessor] = useState('ALL');
  const [courses, setCourses] = useState<Course[]>([]);
  const [departments, setDepartments] = useState<string[]>([]);
  const [professors, setProfessors] = useState<string[]>([]);
  const [schedule, setSchedule] = useState<Set<Course>>(new Set());
  const [days, setDays] = useState<Set<string>>(new Set());

  // --- SEARCH ---
  const search = async () => {
    setCourses([]);
    const res = await fetch('/search', {
      method: 'POST',
      headers: { 'Content-Type': 'text/plain' },
      body: query.trim(),
    });

    const items: Course[] = await res.json();
    setCourses(items);
  };

  // --- TOGGLE COURSE ---
  const toggleCourse = async (course: Course) => {
    const courseId = `${course.subject}${course.number}${course.section}`;
    const newSchedule = new Set(schedule);


    //check for the errors first, return if found
    //then check if the course's time overlaps
    if (newSchedule.has(course)) {
        //remove from schedule if already schedule courses
        newSchedule.delete(course);
    }else{
        let alerted = false;
        //check each course in schedule to see if the times overlap
        for(const c of schedule){
            if(c.number === course.number && c.name === course.name && c.section != course.section){ //same class, different sections
                toast("already scheduled for a different section of this class");
                return;
            }

            //check each class time in schedule to see if the times overlap
            for(const time of course.times || []){
                 const tStartSec = timeToSeconds(time.start_time);
                 const tEndSec = timeToSeconds(time.end_time);
                 if(Number.isNaN(tStartSec) || Number.isNaN(tEndSec) || tEndSec <= tStartSec) continue;
                 for(const cl of c.times || []){
                    //only check if it's the same day
                    if(time.day != cl.day) continue;

                    const cStartSec = timeToSeconds(cl.start_time);
                    const cEndSec = timeToSeconds(cl.end_time);
                    if (Number.isNaN(cStartSec) || Number.isNaN(cEndSec) || cEndSec <= cStartSec) continue;

                    if(tStartSec < cEndSec && tEndSec > cStartSec){
                        //if there is an overlap in the time blocks
                        if(!alerted){
                            toast(`Course ${course.subject}${course.number}${course.section} overlaps with ${c.subject}${c.number}${c.section}`);
                            alerted = true;
                        }
                        return;
                    }

                 }
            }
        }

        //if no overlaps
        newSchedule.add(course);
    }

    setSchedule(newSchedule);

    await fetch('/schedule/items', {
      method: 'POST',
      headers: { 'Content-Type': 'text/plain' },
      body: courseId,
    });
  };

  const toggleDay = (day: string) => {
    const newDays = new Set(days);

    if (newDays.has(day)) newDays.delete(day);
    else newDays.add(day);

    setDays(newDays);
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

    fetchCourses();
  }, []);

  // --- LOAD CURRENT SCHEDULE ---
  useEffect(() => {
    const fetchSchedule = async () => {
      try {
        const res = await fetch('/schedule/items');
        const items: Course[] = await res.json();
        //const ids = new Set(items.map(c => `${c.subject}${c.number}${c.section}`));
        setSchedule(new Set(items));
      } catch (err) {
        console.error('Failed to load schedule', err);
      }
    };

    fetchSchedule();
  }, []);

  return (
    <div className="layout">
    <div><Toaster/></div>
      {/* LEFT SIDEBAR */}
      <div className="sidebar">
        <h3>Filters</h3>
        <select value={department} onChange={e => setDepartment(e.target.value)}>
          <option value="ALL">All Departments</option>
          {departments.map(dept => <option key={dept} value={dept}>{dept}</option>)}
        </select>
        <select value={professor} onChange={e => setProfessor(e.target.value)}>
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
            {courses
              .filter(course => {
                if (days.size === 0) return true;

                const courseDays = course.times?.map(t => t.day) ?? [];
                return courseDays.some(d => days.has(d));
              })
              .map(course => {
                const courseId = `${course.subject}${course.number}${course.section}`;
                let inSchedule = false;
                for(const c of schedule){
                    if(c.subject == course.subject && c.section == course.section && c.number == course.number){inSchedule=true;}
                }

                return (
                  <li key={courseId}>
                    {course.subject}{course.number} {course.section} — {course.name}
                    <button onClick={() => toggleCourse(course)}>
                      {inSchedule ? 'Remove Course' : 'Add Course'}
                    </button>
                  </li>
                );
              })}
          </ul>
        </div>
      </div>
    </div>
  );
}
