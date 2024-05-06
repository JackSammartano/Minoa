package jack.Minoa.Repository;

import jack.Minoa.Entity.Event;
import jack.Minoa.Entity.ShiftBackup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShiftBackupRepository extends JpaRepository<ShiftBackup, Long> {

    ShiftBackup findShiftBackupByEvent(Event event);

}
