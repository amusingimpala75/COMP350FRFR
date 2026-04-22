import { useState, useEffect } from 'react';
import FullCalendar from '@fullcalendar/react'
import timeGridPlugin from '@fullcalendar/timegrid'
import { useRef } from 'react'


//for sending into the /schedule/items endpoint
type Term = 'Fall' | 'Winter' | 'Spring' | 'Summer';
const TERMS: Term[] = ['Fall', 'Winter', 'Spring', 'Summer'];

const userId = 154;
const scheduleId = 1;



interface CourseTime {
  day: string
  start_time: string
  end_time: string
}

interface Course {
  subject: string;
  number: string;
  section: string;
  name: string;
  times: CourseTime[];
  semester: string;
}

export default function SchedulePage() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [activeTerm, setActiveTerm] = useState<Term>('Fall');
  //const calendarRef = useRef<FullCalendar>(null);


  const loadCourses = async (term: Term) => {
    const res = await fetch(`/schedule/items?term=${encodeURIComponent(term)}&userId=${userId}&scheduleId=${scheduleId}`);
    const items: Course[] = await res.json();
    setCourses(items);

    //adding events to fullcalendar
    const calendarApi = calendarRef.current?.getApi()
    if (!calendarApi) return
    calendarApi.removeAllEvents() //refresh the calendar

    //convert days to their numerical representation and add each class day of a course to the calendar
    for(const course of items){
        for(const time of course.times){
            const dayMap: Record<string, number> = {
              M: 1,
              T: 2,
              W: 3,
              R: 4,
              F: 5
            };
            const day = dayMap[time.day];
            //add each class day as an event; remove based on the course code. Can't add just one event for the class since it could have multiple days with different times.
            addEvent(course.subject+course.number+course.section+time.day, course.name, [day], time.start_time, time.end_time);
        }
    }
  };

  const removeCourse = async (courseId: string) => {
    await fetch('/schedule/items?courseId=${courseId}&userId=${userId}&scheduleId=${scheduleId}', { method: 'POST', body: courseId });
    //remove from calendar
    removeEvents(courseId);
    loadCourses(activeTerm);
  };

  useEffect(() => {
      loadCourses(activeTerm);
      }, [activeTerm]);


  const calendarRef = useRef<FullCalendar>(null)


  const addEvent = (code:string, name:string, daysArray:number[], start:string, end:string) => {
    const calendarApi = calendarRef.current?.getApi()
    calendarApi?.addEvent({
      id: code,
      title: name,
      daysOfWeek: daysArray,
      startTime: start,
      endTime: end
    })
  }

  //removing the course from the visual display
  const removeEvents = (courseCode: string) => {  //remove all events with the course code (ACCT101A)
    const calendarApi = calendarRef.current?.getApi()
    if (!calendarApi) return
    const events = [...calendarApi.getEvents()];
    for(const event of events){
        if (event.id && event.id.includes(courseCode)) {
              event.remove();
        }
    }
  }



  //for downloading the schedule pdf
  const handleDownload = async (): Promise<void> => {
      const res = await fetch('/download-pdf?userId=${userId}&scheduleId=${scheduleId}');
      if (!res.ok) {
          throw new Error('Download failed');
      }

      //reads http response body, converts to a pdf file in memory
      const blob = await res.blob();

      //creates a fake url for the file, tells browser to download instead of redirecting
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'schedule.pdf';
      document.body.appendChild(a);
      //clicks link, removes temporary element
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
  };


  return (
    <div className="layout">
      <div className="main">
        <h1>User Schedule — {activeTerm}</h1>
        <div style={{
                display: 'flex',
                gap: 6,
                marginBottom: 12,
                alignItems: 'center'   // key fix
              }}>
                {TERMS.map(term => (
                  <button
                    key={term}
                    onClick={() => setActiveTerm(term)}
                    style={{
                      fontSize: '0.8rem',
                      padding: '4px 8px',
                      height: 'auto',          // prevent stretching
                      alignSelf: 'center',     // extra safety
                      borderRadius: '6px',
                      border: '1px solid #ccc',
                      backgroundColor: activeTerm === term ? '#ddd' : '#f7f7f7',
                      cursor: 'pointer',
                    }}
                  >
                    {term}
                  </button>
                ))}
              </div>
              {(activeTerm === 'Fall' || activeTerm === 'Spring') && (
                <div className="Schedule">
                      <FullCalendar
                      ref={calendarRef}
                      plugins={[timeGridPlugin]}
                      initialView="timeGridWeek"
                      weekends={false}
                      editable={false}
                      selectable={false}
                      eventStartEditable={false}
                      eventDurationEditable={false}
                      headerToolbar={false}
                      height="auto"
                      slotMinTime="08:00:00"
                      slotMaxTime="22:00:00"
                      dayHeaderFormat={{ weekday: 'short' }}
                      allDaySlot={false}

                  />
                </div>
                )}
        <ul>
          {courses.map(course => {
            const courseId = `${course.subject}${course.number}${course.section}${course.semester}`;
            return (
              <li key={courseId}>
                {course.semester} {course.subject}{course.number} {course.section} — {course.name}
                <button onClick={() => removeCourse(courseId)}>Remove Course</button>
              </li>
            );
          })}
        </ul>
        <button onClick={handleDownload} style={{ margin: '10px', width: 'auto'}}>Download PDF</button>


      </div>
    </div>
  );
}

