package jack.Minoa;

import jack.Minoa.Entity.Event;
import jack.Minoa.Request.EventRequest;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void createEvent(EventRequest eventRequest){
        eventRepository.save(Event.builder()
                        .name(eventRequest.getName())
                        .eventstype(eventRequest.getEventstype())
                        .diners(eventRequest.getDiners())
                        .date(eventRequest.getDate())
                        .mealType(eventRequest.getMealType())
                        .eventLocation(eventRequest.getEventLocation())
                .build());
    }

    public Event readEvent(Long id){
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id "+ id));
    }

    public void deleteEvent(Long id){
        Event event = readEvent(id);
        eventRepository.delete(event);
    }
}
