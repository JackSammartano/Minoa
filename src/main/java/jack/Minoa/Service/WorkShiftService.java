package jack.Minoa.Service;

import jack.Minoa.Entity.Event;
import jack.Minoa.Entity.ShiftBackup;
import jack.Minoa.Entity.Waiter;
import jack.Minoa.Repository.EventRepository;
import jack.Minoa.Repository.ShiftBackupRepository;
import jack.Minoa.Repository.WaiterRepository;
import jack.Minoa.Response.WorkShiftResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WorkShiftService {

    private final EventService eventService;
    private final EventRepository eventRepository;
    private final WaiterService waiterService;
    private final WaiterRepository waiterRepository;
    private final ShiftBackupRepository shiftBackupRepository;

    public WorkShiftService(EventService eventService, EventRepository eventRepository, WaiterService waiterService, WaiterRepository waiterRepository, ShiftBackupRepository shiftBackupRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.waiterService = waiterService;
        this.waiterRepository = waiterRepository;
        this.shiftBackupRepository = shiftBackupRepository;
    }

    public WorkShiftResponse createWorkShift(Long eventId){
        ShiftBackup shiftBackup = new ShiftBackup();
        Event event = eventService.readEvent(eventId);
        shiftBackup.setEvent(event);
        List<Waiter> workShift = new ArrayList<>();
        String message = "";

        //Calcolo quanti camerieri servono in base al tipo di evento
        int waitersNeeded = getWaitersNeeded(event);

        //Costruisco le liste dei camerieri che mi servono
        Optional<Event> existingEvent = eventService.existingEventByDateAndMealType(event.getDate(), event.getMealType(), eventId);
        Map<String, List<Waiter>> waitersList = calculateWaitersList(existingEvent);
        List<Waiter> maleWaiters = waitersList.get("male");
        List<Waiter> femaleWaiters = waitersList.get("female");
        List<Waiter> secondaryWaiters = waitersList.get("secondary");

        int maleWaiterSize = maleWaiters.size();
        int femaleWaiterSize = femaleWaiters.size();
        int secondaryWaiterSize = secondaryWaiters.size();

        int totalWaiters = maleWaiterSize + femaleWaiterSize + secondaryWaiterSize;

        //Caso specifico in cui il numero totale dei camerieri richiesti è maggiore o uguale al numero totale dei camerieri presenti in squadra
        //in questo caso non devo aggiornare i campi Latest dei camerieri, il turno non cambia e viene segnalato quanti EXTRA servono
        if(waitersNeeded >= totalWaiters ){
            workShift.addAll(maleWaiters);
            workShift.addAll(femaleWaiters);
            workShift.addAll(secondaryWaiters);
            event.setWaiters(workShift);
            eventRepository.save(event);
            saveEventsInWaiter(workShift, event);

            int extraWaitersNeeded = waitersNeeded - totalWaiters;
            return WorkShiftResponse.builder()
                    .workShift(workShift)
                    .Message("Il turno comprende tutti i camerieri, sono necessari camerieri EXTRA n.  "+ extraWaitersNeeded)
                    .build();
        }

        //Stabilisco quanti camerieri delle diverse categorie mi servono
        Map<String, Integer> waitersByGender = calculateWaitersNeededByGender(waitersNeeded);
        int waitersMaleNeeded = waitersByGender.get("male");
        int waitersFemaleNeeded = waitersByGender.get("female");


        if(event.getEventLocation().equals(Event.EventLocation.COLORADO)){
            Waiter coloradoDirector = waiterService.getWaiterByNameAndSurname("Enzo", "Marrone");
            workShift.add(coloradoDirector);
            waitersMaleNeeded--;

        }
        /* -------------------------------------------------------------------------- TURNO DELLE DONNE -------------------------------------------------------------------------- */

        if(waitersFemaleNeeded != 0){
            workShift.addAll(getShift(femaleWaiters, waitersFemaleNeeded,existingEvent,workShift));
            shiftBackup.setLastWaiterFemale(workShift.get(workShift.size() - 1));
        }
        /* -------------------------------------------------------------------------- TURNO DEGLI UOMINI -------------------------------------------------------------------------- */

        if(waitersMaleNeeded >= (maleWaiterSize + secondaryWaiterSize)){
            int additionalWaitersNeeded = waitersMaleNeeded - (maleWaiterSize + secondaryWaiterSize);
            if(additionalWaitersNeeded != 0){
                List<Waiter> updatedFemaleWaiters = waiterService.getAllWaitersByBelongingGroup(Waiter.BelongingGroup.FEMALE);
                List<Waiter> femaleWaitersAvailable = getAvailableWaitersToWork(updatedFemaleWaiters, workShift);
                if(femaleWaitersAvailable.size() >= additionalWaitersNeeded){
                    workShift.addAll(getShift(updatedFemaleWaiters, additionalWaitersNeeded,existingEvent,workShift));
                    shiftBackup.setLastWaiterFemale(workShift.get(workShift.size() - 1));
                }
            }
            workShift.addAll(maleWaiters);
            shiftBackup.setLastWaiterMale(workShift.get(workShift.size() - 1));
            workShift.addAll(secondaryWaiters);
            shiftBackup.setLastWaiterSecondary(workShift.get(workShift.size() - 1));
            event.setWaiters(workShift);
            eventRepository.save(event);
            saveEventsInWaiter(workShift, event);
            shiftBackupRepository.save(shiftBackup);
            message = message + (" - Il turno comprende tutti i ragazzi, il loro turno non è stato aggiornato, sono state aggiunte ragazze n. "+additionalWaitersNeeded);
            return WorkShiftResponse.builder()
                    .workShift(workShift)
                    .Message(message)
                    .build();
        }

        if(waitersMaleNeeded != 0){
            if(waitersMaleNeeded > 2){
                int secondaryWaitersNeeded = (int) Math.floor((float) waitersMaleNeeded / 3);
                int tempWaitersMaleNeeded = waitersMaleNeeded - secondaryWaitersNeeded;
                workShift.addAll(getShift(maleWaiters, tempWaitersMaleNeeded,existingEvent,workShift));
                shiftBackup.setLastWaiterMale(workShift.get(workShift.size() - 1));
                workShift.addAll(getShift(secondaryWaiters, secondaryWaitersNeeded,existingEvent,workShift));
                shiftBackup.setLastWaiterSecondary(workShift.get(workShift.size() - 1));
            } else{
                workShift.addAll(getShift(maleWaiters, waitersMaleNeeded,existingEvent,workShift));
                shiftBackup.setLastWaiterMale(workShift.get(workShift.size() - 1));
                shiftBackup.setLastWaiterSecondary(secondaryWaiters.get(getLatestWaiterIndex(secondaryWaiters)));
            }
        }
        /* -------------------------------------------------------------------------- FINE -------------------------------------------------------------------------- */

        event.setWaiters(workShift);
        eventRepository.save(event);
        saveEventsInWaiter(workShift, event);
        shiftBackupRepository.save(shiftBackup);
        return WorkShiftResponse.builder()
                .workShift(workShift)
                .Message(message)
                .build();
    }

    public List<Waiter> readWorkShift(Long eventId){
        Event event = eventService.readEvent(eventId);
        if(event.getWaiters().isEmpty()){
            throw new RuntimeException("WorkShihf not found in event with id " +eventId);
        }
        return event.getWaiters();
    }

    public void saveEventsInWaiter(List<Waiter> workShift, Event event){
        for(Waiter w : workShift){
            Waiter waiter = waiterService.readWaiter(w.getId());
            if(waiter.getEvents() == null){
                waiter.setEvents(new ArrayList<>());
            }
            waiter.getEvents().add(event);
            waiterRepository.save(waiter);
        }
    }

    public int getLatestWaiterIndex(List<Waiter> waiters) {
        int index = 0;
        for (Waiter waiter : waiters) {
            if (waiter.isLatest()) {
                return index;
            }
            index++;
        }
        return -1; // Se non viene trovato alcun cameriere con latest = true
    }


    public List<Waiter> getShift (List<Waiter> waiters, int n, Optional<Event> existingEvent, List<Waiter> workShift){
        if(n >= waiters.size()){
            return new ArrayList<>(waiters);
        }
        int startIndex = 0;
        List<Waiter> result = new ArrayList<>();
        waiters.sort(Comparator.comparingInt(Waiter::getPositionOrder));
        if(existingEvent.isPresent()){
            List<Waiter> originalWaiters = waiterService.getAllWaitersByBelongingGroup(waiters.get(0).getBelongingGroup());
            Waiter firstToWorkWaiter = getFirstWaiterToWork(originalWaiters);
            Waiter latestWaiterToWork = originalWaiters.get(getLatestWaiterIndex(originalWaiters));
            for(Waiter w : waiters){
                if (Objects.equals(w.getId(), firstToWorkWaiter.getId())) {
                    break;
                }
                startIndex++;
            }
            startConfiguration(waiters, n, workShift, startIndex, result);
            updateLatestWaiters(latestWaiterToWork, result.get(result.size() - 1));
        } else{
            startIndex = getLatestWaiterIndex(waiters) + 1;
            startConfiguration(waiters, n, workShift, startIndex, result);
            updateLatestWaiters(waiters.get(startIndex - 1), result.get(result.size() - 1));
        }
        return result;
    }

    private void startConfiguration(List<Waiter> waiters, int n, List<Waiter> workShift, int startIndex, List<Waiter> result) {
        int condition = 0;
        for(int i = startIndex; condition < n; i++){
            if(i > (waiters.size() - 1)){
                i = 0;
            }
            if(!workShift.contains(waiters.get(i))){
                result.add(waiters.get(i));
                condition++;
            }
        }
    }

    public void updateLatestWaiters(Waiter previousLatest, Waiter newestLatest){
        Waiter latestWaiter = waiterService.readWaiter(previousLatest.getId());
        latestWaiter.setLatest(false);
        waiterRepository.save(latestWaiter);
        Waiter newestLatestWaiter = waiterService.readWaiter(newestLatest.getId());
        newestLatestWaiter.setLatest(true);
        waiterRepository.save(newestLatestWaiter);
    }

    public List<Waiter> getAvailableWaitersToWork(List<Waiter> waiters, List<Waiter> workShift){
        List<Waiter> result = new ArrayList<>();

        for(Waiter avaibleWaiter : waiters){
            boolean isWorking = false;
            for(Waiter workingWaiter : workShift){
                if(Objects.equals(avaibleWaiter.getId(), workingWaiter.getId())){
                    isWorking = true;
                    break;
                }
            }
            if(!isWorking){
                result.add(avaibleWaiter);
            }
        }
        return result;
    }

    public int getWaitersNeeded(Event event) {
        int diners = event.getDiners();
        int result = 0;

        if (event.getEventstype().equals(Event.Eventstype.MATRIMONIO)) {
            result = Math.max(0, Math.round((float) diners / 12.5f) - 1);
        }
        if (event.getEventstype().equals(Event.Eventstype.BANCHETTO)) {
            result = Math.max(0, Math.round((float) diners / 15f) - 1);
        }
        if (event.getEventLocation().equals(Event.EventLocation.COLORADO)) {
            result++;
        }
        return result;
    }

    public Map<String, Integer> calculateWaitersNeededByGender(int waitersNeeded) {
        int waitersMaleNeeded = 0;
        int waitersFemaleNeeded = 0;

        if (waitersNeeded % 2 == 1) {
            // Se il numero totale di camerieri necessari è dispari, sottrai uno per rendere pari
            // poi dividi equamente, assegnando uno extra ai camerieri femminili
            waitersNeeded--;
            waitersMaleNeeded = waitersNeeded / 2;
            waitersFemaleNeeded = (waitersNeeded / 2) + 1;
        } else {
            // Se il numero è pari, distribuisci equamente tra maschi e femmine
            waitersMaleNeeded = waitersFemaleNeeded = waitersNeeded / 2;
        }

        return Map.of("male", waitersMaleNeeded, "female", waitersFemaleNeeded);
    }

    public Map<String, List<Waiter>> calculateWaitersList(Optional<Event> existingEvent){

        List<Waiter> maleWaiters = waiterService.getAllWaitersByBelongingGroup(Waiter.BelongingGroup.MALE);
        List<Waiter> femaleWaiters = waiterService.getAllWaitersByBelongingGroup(Waiter.BelongingGroup.FEMALE);
        List<Waiter> secondaryWaiters = waiterService.getAllWaitersByBelongingGroup(Waiter.BelongingGroup.SECONDARY);

        if(existingEvent.isPresent()){
            maleWaiters = getAvailableWaitersToWork(maleWaiters, existingEvent.get().getWaiters());
            femaleWaiters = getAvailableWaitersToWork(femaleWaiters, existingEvent.get().getWaiters());
            secondaryWaiters = getAvailableWaitersToWork(secondaryWaiters, existingEvent.get().getWaiters());
        }
        return Map.of("male", maleWaiters, "female", femaleWaiters, "secondary", secondaryWaiters);
    }

    public Waiter getFirstWaiterToWork(List<Waiter> waiters){
        int index = getLatestWaiterIndex(waiters);
        return waiters.get(index + 1);
    }

}