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
        String message = "";

        //Calcolo quanti camerieri servono in base al tipo di evento
        int waitersNeeded = getWaitersNeeded(event);

        List<Waiter> maleWaiters = waiterService.getAllWaitersByBelongingGroup(Waiter.BelongingGroup.MALE);
        List<Waiter> femaleWaiters = waiterService.getAllWaitersByBelongingGroup(Waiter.BelongingGroup.FEMALE);
        List<Waiter> secondaryWaiters = waiterService.getAllWaitersByBelongingGroup(Waiter.BelongingGroup.SECONDARY);
        Optional<Event> existingEvent =eventService.existingEventByDateAndMealType(event.getDate(), event.getMealType(), eventId);

        if(existingEvent.isPresent()){
            maleWaiters = getAvailableWaitersToWork(maleWaiters, existingEvent.get().getWaiters());
            femaleWaiters = getAvailableWaitersToWork(femaleWaiters, existingEvent.get().getWaiters());
            secondaryWaiters = getAvailableWaitersToWork(secondaryWaiters, existingEvent.get().getWaiters());
        }

        int maleWaiterSize = maleWaiters.size();
        int femaleWaiterSize = femaleWaiters.size();
        int secondaryWaiterSize = secondaryWaiters.size();

        int totalWaiters = maleWaiterSize + femaleWaiterSize + secondaryWaiterSize;

        //Caso specifico in cui il numero totale dei camerieri richiesti è maggiore o uguale al numero totale dei camerieri presenti in squadra
        //in questo caso non devo aggiornare i campi Latest dei camerieri, il turno non cambia e viene segnalato quanti EXTRA servono
        if(waitersNeeded >= totalWaiters ){
            List<Waiter> workShift = new ArrayList<>();
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

        /* -------------------------------------------------------------------------- TURNO DELLE DONNE -------------------------------------------------------------------------- */

        List<Waiter> workShift = new ArrayList<>(getShift(femaleWaiters, waitersFemaleNeeded));
        shiftBackup.setLastWaiterFemale(workShift.get(workShift.size() - 1));

        /* -------------------------------------------------------------------------- TURNO DEGLI UOMINI -------------------------------------------------------------------------- */

        if(waitersMaleNeeded >= (maleWaiterSize + secondaryWaiterSize)){
            int addictionalWaitersNeeded = waitersMaleNeeded - (maleWaiterSize + secondaryWaiterSize);
            if(addictionalWaitersNeeded != 0){
                List<Waiter> updatedFemaleWaiters = waiterService.getAllWaitersByBelongingGroup(Waiter.BelongingGroup.FEMALE);
                List<Waiter> femaleWaitersAvailable = getAvailableWaitersToWork(updatedFemaleWaiters, workShift);
                if(femaleWaitersAvailable.size() >= addictionalWaitersNeeded){
                    workShift.addAll(getShift(updatedFemaleWaiters, addictionalWaitersNeeded));
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
            message = message + (" - Il turno comprende tutti i ragazzi, il loro turno non è stato aggiornato, sono state aggiunte ragazze n. "+addictionalWaitersNeeded);
            return WorkShiftResponse.builder()
                    .workShift(workShift)
                    .Message(message)
                    .build();
        }

        if(waitersMaleNeeded > 2){
            int secondaryWaitersNeeded = Math.round((float)waitersMaleNeeded / 3);
            int tempWaitersMaleNeeded = waitersMaleNeeded - secondaryWaitersNeeded;
            workShift.addAll(getShift(maleWaiters, tempWaitersMaleNeeded));
            shiftBackup.setLastWaiterMale(workShift.get(workShift.size() - 1));
            workShift.addAll(getShift(secondaryWaiters, secondaryWaitersNeeded));
            shiftBackup.setLastWaiterSecondary(workShift.get(workShift.size() - 1));
        } else{
            workShift.addAll(getShift(maleWaiters, waitersMaleNeeded));
            shiftBackup.setLastWaiterMale(workShift.get(workShift.size() - 1));
            shiftBackup.setLastWaiterSecondary(secondaryWaiters.get(getIndexofLatestWaiter(secondaryWaiters)));
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

    public int getIndexofLatestWaiter(List<Waiter> waiters) {
        int index = 0;
        for (Waiter waiter : waiters) {
            if (waiter.isLatest()) {
                return index;
            }
            index++;
        }
        return -1; // Se non viene trovato alcun cameriere con latest = true
    }

    /**
     * Calcola e restituisce un elenco di camerieri selezionati per un turno, basandosi sull'ordine di posizione.
     *
     * @param waiters La lista completa di camerieri disponibili. Ogni cameriere deve avere un metodo `getPositionOrder()`
     *                che ritorna un intero rappresentante la loro posizione nell'ordine di lavoro.
     * @param n Il numero di camerieri desiderati per il turno. Se `n` è maggiore o uguale al numero di camerieri disponibili,
     *          la funzione restituirà tutti i camerieri presenti nell'elenco.
     * @return Una lista di camerieri selezionati per il turno. La selezione inizia dal cameriere successivo a quello che ha lavorato
     *         per ultimo, circolando alla fine della lista se necessario, fino a raggiungere il numero richiesto `n`.
     * @throws IndexOutOfBoundsException Se l'indice calcolato supera i limiti dell'array senza una gestione appropriata.
     *         Questa eccezione può verificarsi se la funzione `getIndexofLatestWaiter` ritorna un indice non valido o se non ci sono controlli
     *         adeguati quando `startIndex` viene decrementato.
     *
     * Dettagli sull'implementazione:
     * 1. Se `n` è maggiore o uguale alla dimensione della lista `waiters`, la funzione restituisce una copia dell'intera lista.
     * 2. I camerieri vengono ordinati utilizzando un comparatore basato sul metodo `getPositionOrder`.
     * 3. La selezione dei camerieri inizia dal cameriere successivo a quello che ha lavorato per ultimo, come determinato dalla funzione `getIndexofLatestWaiter`.
     * 4. La funzione cicla attraverso la lista dei camerieri per selezionare `n` camerieri, ripartendo dall'inizio della lista se necessario.
     * 5. Alla fine, la funzione `updateLatestWaiters` viene chiamata per aggiornare il registro degli ultimi camerieri che hanno lavorato.
     */
    public List<Waiter> getShift (List<Waiter> waiters, int n){
        if(n >= waiters.size()){
            return new ArrayList<>(waiters);
        }
        List<Waiter> result = new ArrayList<>();
        waiters.sort(Comparator.comparingInt(Waiter::getPositionOrder));
        int startIndex = getIndexofLatestWaiter(waiters) + 1;
        int condition = 0;
        for(int i = startIndex; condition < n; i++){
            if(i > (waiters.size() - 1)){
                i = 0;
            }
            result.add(waiters.get(i));
            condition++;
        }
        updateLatestWaiters(waiters.get(startIndex - 1), result.get(result.size() - 1));
        return result;
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

        if (event.getEventstype().equals(Event.Eventstype.MATRIMONIO)) {
            return Math.max(0, Math.round((float) diners / 12.5f) - 1);
        }
        if (event.getEventstype().equals(Event.Eventstype.BANCHETTO)) {
            return Math.max(0, Math.round((float) diners / 15f) - 1);
        }

        throw new RuntimeException("Non è stato possibile calcolare il numero dei camerieri necessari");
    }

    public Map<String, Integer> calculateWaitersNeededByGender(int waitersNeeded) {
        int waitersMaleNeeded = 0;
        int waitersFemaleNeeded = 0;

        if (waitersNeeded % 2 == 1) {
            // Se il numero totale di camerieri necessari è dispari, sottrai uno per rendere pari
            // poi dividi equamente, assegnando uno extra ai camerieri femminili
            waitersMaleNeeded = waitersNeeded / 2;
            waitersFemaleNeeded = waitersNeeded / 2 + 1;
        } else {
            // Se il numero è pari, distribuisci equamente tra maschi e femmine
            waitersMaleNeeded = waitersFemaleNeeded = waitersNeeded / 2;
        }

        return Map.of("male", waitersMaleNeeded, "female", waitersFemaleNeeded);
    }

}