package jack.Minoa.Service;

import jack.Minoa.Entity.Event;
import jack.Minoa.Entity.ShiftBackup;
import jack.Minoa.Entity.Waiter;
import jack.Minoa.Repository.ShiftBackupRepository;
import jack.Minoa.Repository.WaiterRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ShiftBackupService {

    private final ShiftBackupRepository shiftBackupRepository;
    private final WaiterRepository waiterRepository;

    public ShiftBackupService(ShiftBackupRepository shiftBackupRepository, WaiterRepository waiterRepository) {
        this.shiftBackupRepository = shiftBackupRepository;
        this.waiterRepository = waiterRepository;
    }

    public void createShiftBackup(Event event){
        ShiftBackup shiftBackup = new ShiftBackup();
        shiftBackup.setEvent(event);

        if(!event.getWaiters().isEmpty()){
            List<Waiter> lastWaiters;
            List<Waiter> neededWaiters = waiterRepository.findAll();
            lastWaiters = neededWaiters.stream()
                    .filter(Waiter::isLatest)
                    .toList();
            for(Waiter w : lastWaiters){
                if(w.getBelongingGroup().equals(Waiter.BelongingGroup.MALE)){
                    shiftBackup.setLastWaiterMale(w);
                }
                if(w.getBelongingGroup().equals(Waiter.BelongingGroup.FEMALE)){
                    shiftBackup.setLastWaiterFemale(w);
                }
                if(w.getBelongingGroup().equals(Waiter.BelongingGroup.SECONDARY)){
                    shiftBackup.setLastWaiterSecondary(w);
                }
            }
            shiftBackupRepository.save(shiftBackup);
        } else throw new RuntimeException("Non esiste un turno per l'evento con id " +event.getId());

    }

    public ShiftBackup readShiftBackup (Long id){
        return shiftBackupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ShiftBackup not found with id " + id));
    }

    public void deleteShiftBackup(Long id){
        shiftBackupRepository.delete(readShiftBackup(id));
    }

    public List<Waiter> getLatestWaitersFromEvent(Event event){
        ShiftBackup shiftBackup = shiftBackupRepository.findShiftBackupByEvent(event);
        return Arrays.asList(shiftBackup.getLastWaiterMale(), shiftBackup.getLastWaiterFemale(), shiftBackup.getLastWaiterSecondary());
    }

    public ShiftBackup getShiftbackupFromEvent(Event event){
        return shiftBackupRepository.findShiftBackupByEvent(event);
    }
}
