package jack.Minoa.Service;

import jack.Minoa.Entity.Event;
import jack.Minoa.Entity.ShiftBackup;
import jack.Minoa.Entity.Waiter;
import jack.Minoa.Repository.EventRepository;
import jack.Minoa.Repository.ShiftBackupRepository;
import jack.Minoa.Repository.WaiterRepository;
import jack.Minoa.Request.EventRequest;

import jack.Minoa.Response.WorkShiftResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ShiftBackupService shiftBackupService;
    private final WaiterService waiterService;
    private final WorkShiftService workShiftService;

    public EventService(EventRepository eventRepository, ShiftBackupService shiftBackupService, ShiftBackupRepository shiftBackupRepository, WaiterRepository waiterRepository, ShiftBackupService shiftBackupService1, WaiterService waiterService, WorkShiftService workShiftService) {
        this.eventRepository = eventRepository;
        this.shiftBackupService = shiftBackupService1;
        this.waiterService = waiterService;
        this.workShiftService = workShiftService;
    }

    public Event createEvent(EventRequest eventRequest){
         return eventRepository.save(Event.builder()
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
        if(event.getWaiters().isEmpty()){
            eventRepository.delete(event);
        }
        else{
            //Prima di eliminare l'evento, devo ripristinare lo stato precedente nell'ordine dei camerieri

            Event previousEvent = getPreviousEvent(event.getDate());
            List<Waiter> newestLatestWaiter = shiftBackupService.getLatestWaitersFromEvent(previousEvent);
            waiterService.resetLatestWaiters(newestLatestWaiter);
            ShiftBackup currentShiftBackup = shiftBackupService.getShiftbackupFromEvent(event);
            shiftBackupService.deleteShiftBackup(currentShiftBackup.getId());
            eventRepository.delete(event);
        }
    }

    public Event updateEvent(Long id, EventRequest eventRequest){
        Event event = readEvent(id);
        //Se uno di questi campi viene modificato devo forzare il ricalcolo del turno corrispondente
        if (event.getEventstype() != eventRequest.getEventstype() ||
                event.getDiners() != eventRequest.getDiners() ||
                event.getEventLocation() != eventRequest.getEventLocation()) {
            if(!event.getWaiters().isEmpty()){
                deleteEvent(id);
                Event forcedEvent = createEvent(eventRequest);
                WorkShiftResponse workShift = workShiftService.createWorkShift(forcedEvent.getId());
                return forcedEvent;
            }
        }
        event.setName(eventRequest.getName());
        event.setMealType(eventRequest.getMealType());
        event.setDate(eventRequest.getDate());
        event.setEventLocation(eventRequest.getEventLocation());
        event.setEventstype(eventRequest.getEventstype());
        event.setDiners(eventRequest.getDiners());
        return event;
    }

    public Event getPreviousEvent(LocalDate currentDate) {
        Pageable pageable = PageRequest.of(0, 1);
        List<Event> events = eventRepository.findEventsBeforeDate(currentDate, pageable );
        if (!events.isEmpty()) {
            return events.get(0); // Ritorna l'evento più vicino prima della data data
        }
        throw new RuntimeException("Nessun evento trovato con data precedente a " +currentDate);
    }

    public Optional<Event> existingEventByDateAndMealType(LocalDate date, Event.MealType mealType, Long eventId){
        return eventRepository.findByDateAndMealTypeExcludingEvent(date, mealType, eventId);
    }
}