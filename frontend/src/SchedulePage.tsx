import { useState, useEffect } from 'react';
import FullCalendar from '@fullcalendar/react'
import timeGridPlugin from '@fullcalendar/timegrid'
import { Toaster, toast } from "react-hot-toast";
import { useRef } from 'react'


//for sending into the /schedule/items endpoint
type Term = 'Fall' | 'Winter' | 'Spring' | 'Summer';
const TERMS: Term[] = ['Fall', 'Winter', 'Spring', 'Summer'];


interface Schedule {
  id: number;
  name: string;
}

interface CourseTime {
  day: string
  start_time: string
  end_time: string
}

interface Course {
  id: number;
  subject: string;
  number: string;
  section: string;
  name: string;
  times: CourseTime[];
  semester: string;
}

type SchedulePageProps = {
  scheduleId: number | null;
  setScheduleId: (id: number | null) => void;
  userId: number;
};

export default function SchedulePage({
    scheduleId,
    setScheduleId,
    userId
  }: SchedulePageProps) {
  const [courses, setCourses] = useState<Course[]>([]);
  const [activeTerm, setActiveTerm] = useState<Term>('Fall');
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [newScheduleName, setNewScheduleName] = useState('');
  //const calendarRef = useRef<FullCalendar>(null);

  const loadSchedules = async () => {
    const res = await fetch(`/schedules?userId=${userId}`);
    const data: Schedule[] = await res.json();
    setSchedules(data);
  };

  const loadCourses = async (term: Term) => {
    if (scheduleId == null) return;
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
            addEvent(course.id, course.name, [day], time.start_time, time.end_time);
        }
    }
  };

  const removeCourse = async (courseId: number) => {
    if (scheduleId == null) return;
    //remove from calendar
    removeEvents(courseId);
    await fetch(`/schedule/items?courseId=${courseId}&userId=${userId}&scheduleId=${scheduleId}`, { method: 'POST' });
    loadCourses(activeTerm);
  };

  useEffect(() => {
    if (scheduleId !== null) {
      loadCourses(activeTerm);
    }
  }, [activeTerm, scheduleId]);

  useEffect(() => {
    loadSchedules();
  }, []);

  useEffect(() => {
    if (schedules.length === 0) return;

    const hasSelectedSchedule = scheduleId != null && schedules.some(s => s.id === scheduleId);
    if (!hasSelectedSchedule) {
      setScheduleId(schedules[0].id);
    }
  }, [schedules, scheduleId]);


  const calendarRef = useRef<FullCalendar>(null)


  const addEvent = (id:number, name:string, daysArray:number[], start:string, end:string) => {
    const calendarApi = calendarRef.current?.getApi()
    calendarApi?.addEvent({
      id: String(id),
      title: name,
      daysOfWeek: daysArray,
      startTime: start,
      endTime: end
    })
  }

  //removing the course from the visual display
  const removeEvents = (courseId: number) => {  //remove all events with the course code (ACCT101A)
    const calendarApi = calendarRef.current?.getApi()
    if (!calendarApi) return
    const events = [...calendarApi.getEvents()];
    for(const event of events){
        if (event.id && event.id === String(courseId)) {
              event.remove();
        }
    }
  }

  // For adding a new schedule
  const createSchedule = async () => {
    if (newScheduleName.trim() === "") return;

    const res = await fetch(
      `/schedule?userId=${userId}&scheduleName=${encodeURIComponent(newScheduleName)}`,
      { method: 'POST' }
    );

    if (!res.ok) {
      const error = await res.json();

      if (res.status === 409) {
        toast(error.error);
      }
      return;
    }

    const schedule: Schedule = await res.json();

    setSchedules(prev => [...prev, schedule]);
    setScheduleId(schedule.id);
    setNewScheduleName("");
  };

  const deleteSchedule = async () => {
    if (scheduleId == null) return;

    const res = await fetch(
      `/schedule?userId=${userId}&scheduleId=${scheduleId}`,
      { method: 'DELETE' }
    );

    if (!res.ok) {
      if (res.status === 404) {
        toast.error("Schedule not found");
      } else if (res.status === 400) {
        toast.error("You must have at least 1 schedule");
      } else {
        toast.error("Failed to delete schedule");
      }
      return;
    }

    toast.success("Schedule deleted");

    setSchedules(prev => {
      const remaining = prev.filter(s => s.id !== scheduleId);
      setScheduleId(remaining.length > 0 ? remaining[0].id : null);
      return remaining;
    });
  };

  const confirmDeleteSchedule = () => { // A popup window to confirm deletion of a schedule
    if (scheduleId == null) return;

    toast((t) => (
      <div>
        <p>Delete this schedule?</p>
        <div style={{ display: 'flex', gap: '8px', marginTop: '8px' }}>
          <button
            onClick={() => {
              deleteSchedule();
              toast.dismiss(t.id);
            }}
            style={{ background: 'darkred', color: 'white', padding: '4px 8px' }}
          >
            Yes
          </button>
          <button onClick={() => toast.dismiss(t.id)}>
            Cancel
          </button>
        </div>
      </div>
    ), { duration: 8000 });
  };

  //for downloading the schedule pdf
  const handleDownload = async (): Promise<void> => {
      if (!scheduleId) return;
      const res = await fetch(`/download-pdf?userId=${userId}&scheduleId=${scheduleId}`);
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

  const removeAllCourses = async () => {
        //all the ones from activeTerm
        const toRemove: Course[] = courses.filter(c => c.semester.includes(activeTerm));
        console.log(`${activeTerm} term num courses removed: ${toRemove.length}`);
        await Promise.all(toRemove.map(c => removeCourse(c.id)));
    };




  return (
    <div className="layout">
      <div><Toaster/></div>
      <div className="main">
        <div style={{ position: 'absolute', top: 10, right: 10, display: 'flex', gap: '8px', alignItems: 'center' }}>

          <span style={{ color: 'white' }}>Schedule:</span>

          <select
            value={scheduleId ?? schedules[0]?.id ?? ""}
            onChange={(e) => setScheduleId(Number(e.target.value))}
            style={{ padding: '6px 10px', width: '180px' }}
          >
            {schedules.map((sched) => (
              <option key={sched.id} value={sched.id}>
                {sched.name}
              </option>
            ))}
          </select>

          {/* NEW INPUT */}
          <input
            type="text"
            placeholder="New schedule"
            value={newScheduleName}
            onChange={(e) => setNewScheduleName(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') createSchedule();
            }}
            style={{ padding: '6px', width: '140px' }}
          />

          {/* NEW BUTTON */}
          <button
            onClick={createSchedule}
            disabled={!newScheduleName.trim()}
            style={{ padding: '6px 10px', cursor: 'pointer' }}
          >
            Add
          </button>

          {/* DELETE BUTTON */}
          <button
            onClick={confirmDeleteSchedule}
            disabled={!scheduleId}
            style={{
              padding: '6px 10px',
              cursor: 'pointer',
              backgroundColor: 'darkred',
              color: 'white'
            }}
          >
            Delete
          </button>
        </div>
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
            <button
                                onClick={removeAllCourses}
                                style={{
                                  marginLeft: 'auto',   // pushes it to the far right
                                  fontSize: '0.8rem',
                                  padding: '4px 8px',
                                  borderRadius: '6px',
                                  border: '1px solid #ccc',
                                  backgroundColor: '#f7f7f7',
                                  cursor: 'pointer',
                                }}
                              >
                                Clear Term
                              </button>

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
            return (
              <li key={course.id}>
                {course.semester} {course.subject}{course.number} {course.section} — {course.name}
                <button onClick={() => removeCourse(course.id)}>Remove Course</button>
              </li>
            );
          })}
        </ul>
        <button onClick={handleDownload} style={{ margin: '10px', width: 'auto'}}>Download PDF</button>


      </div>
    </div>
  );
}

