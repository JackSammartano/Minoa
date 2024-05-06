package jack.Minoa.Controller;

import jack.Minoa.Entity.Event;
import jack.Minoa.Request.EventRequest;
import jack.Minoa.Response.WorkShiftResponse;
import jack.Minoa.Service.EventService;
import jack.Minoa.Service.WorkShiftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;
    private final WorkShiftService workShiftService;

    public EventController(EventService eventService, WorkShiftService workShiftService) {
        this.eventService = eventService;
        this.workShiftService = workShiftService;
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody EventRequest eventRequest){
        Event event = eventService.createEvent(eventRequest);
        return ResponseEntity.ok(event);
    }
    @PostMapping("/workshift/{eventId}")
    public ResponseEntity<WorkShiftResponse> createWorkShift(@PathVariable Long eventId) {
        return ResponseEntity.ok(workShiftService.createWorkShift(eventId));
    }

}
