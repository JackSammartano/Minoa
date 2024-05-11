package jack.Minoa.Repository;

import jack.Minoa.Entity.Waiter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WaiterRepository extends JpaRepository<Waiter, Long> {

    List<Waiter> findAllByLatest(boolean latest);

    @Query("SELECT w FROM Waiter w WHERE w.belongingGroup = :belongingGroup")
    List<Waiter> findByBelongingGroup(@Param("belongingGroup") Waiter.BelongingGroup belongingGroup);

    Waiter findByNameAndSurname(String name, String surname);

}
