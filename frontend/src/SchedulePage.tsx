import { useState, useEffect } from 'react';
import FullCalendar from '@fullcalendar/react'
import timeGridPlugin from '@fullcalendar/timegrid'
import { useRef } from 'react'

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
}

export default function SchedulePage() {
  const [courses, setCourses] = useState<Course[]>([]);

  const loadCourses = async () => {
    const res = await fetch('/schedule/items');
    const items: Course[] = await res.json();
    setCourses(items);

    //adding events to fullcalendar
    const calendarApi = calendarRef.current?.getApi()
    if (!calendarApi) return
    calendarApi.removeAllEvents()

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
    await fetch('/schedule/items', { method: 'POST', body: courseId });
    removeEvents(courseId);
    loadCourses();
  };

  useEffect(() => {
      loadCourses();
      }, []);


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
      const res = await fetch('/download-pdf');
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
        <button onClick={handleDownload} style={{ margin: '10px', width: 'auto'}}>Download PDF</button>

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
      </div>
    </div>
  );
}

