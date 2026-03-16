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
    const res = await fetch('/scheduleItems');
    const items: Course[] = await res.json();
    setCourses(items);
    for(const course of items){
        for(const time of course.times){
            //has a day, a start time, and an end time
            console.log(time.day, time.start_time, time.end_time)
            //addEvent((course.subject+course.number+course.section),)
        }
    }
    //TODO: do something here to add all the events to the calendar
  };

  const removeCourse = async (courseId: string) => {
    await fetch('/addOrDelete', { method: 'POST', body: courseId });
    //removeEvent(); //TODO: Change this and send in the course to remove
    loadCourses();
  };

  useEffect(() => {
      loadCourses();
      //addEvent();
      }, []);


  const calendarRef = useRef<FullCalendar>(null)


//   const addEvent = (code:string, name:string, daysArray:number[], start:string, end:string) => {
//     const calendarApi = calendarRef.current?.getApi()
//
//     calendarApi?.addEvent({
//       id: code,  //courseId
//       title: name,
//       daysOfWeek: daysArray,        // Monday
//       startTime: start,//"10:00",
//       endTime: end
//     })
//   }

//   const removeEvent = () => {
//     const calendarApi = calendarRef.current?.getApi()
//
//     const event = calendarApi?.getEventById("course1")
//     event?.remove()
//   }




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

//this is where the live schedule would be  //hitting remove course should refresh the calendar as well
