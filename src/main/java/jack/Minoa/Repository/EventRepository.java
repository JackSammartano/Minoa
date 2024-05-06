package jack.Minoa.Repository;

import jack.Minoa.Entity.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.date < :currentDate ORDER BY e.date DESC")
    List<Event> findEventsBeforeDate(@Param("currentDate") LocalDate currentDate, Pageable pageable);
}
