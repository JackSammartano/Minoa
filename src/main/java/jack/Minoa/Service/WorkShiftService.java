package jack.Minoa.Service;

import jack.Minoa.Entity.Event;
import jack.Minoa.Entity.Waiter;
import jack.Minoa.Repository.EventRepository;
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

    public WorkShiftService(EventService eventService, EventRepository eventRepository, WaiterService waiterService, WaiterRepository waiterRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.waiterService = waiterService;
        this.waiterRepository = waiterRepository;
    }

    public WorkShiftResponse createWorkShift(Long eventId){
         Event event = eventService.readEvent(eventId);
         String message = "";
         int waitersNeeded = 0;

         //Calcolo quanti camerieri servono in base al tipo di evento
         if(event.getEventstype().equals(Event.Eventstype.MATRIMONIO)){
             waitersNeeded = (Math.round((float) event.getDiners() / 12.5f)) - 1;
         } else{
             if(event.getEventstype().equals(Event.Eventstype.BANCHETTO)){
                 waitersNeeded = Math.round(((float) event.getDiners() / 15f)) -1;
             }
         }

        List<Waiter> maleWaiters = waiterService.getAllWaitersByBelongingGroup(Waiter.BelongingGroup.MALE);
        List<Waiter> femaleWaiters = waiterService.getAllWaitersByBelongingGroup(Waiter.BelongingGroup.FEMALE);
        List<Waiter> secondaryWaiters = waiterService.getAllWaitersByBelongingGroup(Waiter.BelongingGroup.SECONDARY);

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
            int extraWaitersNeeded = waitersNeeded - totalWaiters;
            return WorkShiftResponse.builder()
                    .workShift(workShift)
                    .Message("Il turno comprende tutti i camerieri, sono necessari camerieri EXTRA n.  "+ extraWaitersNeeded)
                    .build();
        }

         //Stabilisco quanti camerieri delle diverse categorie mi servono
         int waitersMaleNeeded = 0;
         int waitersFemaleNeeded = 0;
         if(waitersNeeded % 2 == 1){
             waitersNeeded = waitersNeeded - 1;
             waitersMaleNeeded = waitersNeeded / 2;
             waitersFemaleNeeded = (waitersNeeded / 2) + 1;
         } else{
             waitersMaleNeeded = waitersFemaleNeeded = waitersNeeded / 2;
         }

        /* -------------------------------------------------------------------------- TURNO DELLE DONNE -------------------------------------------------------------------------- */

        List<Waiter> workShift = new ArrayList<>(getShift(femaleWaiters, waitersFemaleNeeded));

        /* -------------------------------------------------------------------------- TURNO DEGLI UOMINI -------------------------------------------------------------------------- */

        if(waitersMaleNeeded >= (maleWaiterSize + secondaryWaiterSize)){
            int addictionalWaitersNeeded = waitersMaleNeeded - (maleWaiterSize + secondaryWaiterSize);
            if(addictionalWaitersNeeded != 0){
                List<Waiter> updatedFemaleWaiters = waiterService.getAllWaitersByBelongingGroup(Waiter.BelongingGroup.FEMALE);
                List<Waiter> femaleWaitersAvaible = getAvaibleWaitersToWork(updatedFemaleWaiters, workShift);
                if(femaleWaitersAvaible.size() >= addictionalWaitersNeeded){
                    workShift.addAll(getShift(updatedFemaleWaiters, addictionalWaitersNeeded));
                }
            }
            workShift.addAll(maleWaiters);
            workShift.addAll(secondaryWaiters);
            event.setWaiters(workShift);
            eventRepository.save(event);
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
            workShift.addAll(getShift(secondaryWaiters, secondaryWaitersNeeded));
        } else{
            workShift.addAll(getShift(maleWaiters, waitersMaleNeeded));
        }

        return WorkShiftResponse.builder()
                .workShift(workShift)
                .Message(message)
                .build();
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

    public List<Waiter> getAvaibleWaitersToWork(List<Waiter> waiters, List<Waiter> workShift){
        List<Waiter> result = new ArrayList<>();
        for(int i = 0; i < (waiters.size() -1); i++){
            for(int j = 0; j < (workShift.size()-1); j++){
                if(Objects.equals(waiters.get(i).getId(), workShift.get(j).getId())){
                    result.add(waiters.get(i));
                    break;
                }
            }
        }
        return result;
    }
}