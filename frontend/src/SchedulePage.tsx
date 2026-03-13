import { useState, useEffect } from 'react';

interface Course {
  subject: string;
  number: string;
  section: string;
  name: string;
}

export default function SchedulePage() {
  const [courses, setCourses] = useState<Course[]>([]);

  const loadCourses = async () => {
    const res = await fetch('/scheduleItems');
    const items: Course[] = await res.json();
    setCourses(items);
  };

  const removeCourse = async (courseId: string) => {
    await fetch('/addOrDelete', { method: 'POST', body: courseId });
    loadCourses();
  };

  useEffect(() => { loadCourses(); }, []);

  return (
    <div className="layout">
      <div className="main">
        <h1>User Schedule</h1>
        <ul>
          {courses.map(course => {
            const courseId = `${course.subject}${course.number}${course.section}`;
            return (
              <li key={courseId}>
                {course.subject}{course.number} {course.section} — {course.name}
                <button onClick={() => removeCourse(courseId)}>Remove Course</button>
              </li>
            );
          })}
        </ul>
      </div>
    </div>
  );
}
