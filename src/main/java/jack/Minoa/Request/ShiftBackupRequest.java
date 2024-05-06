package jack.Minoa.Request;

import jack.Minoa.Entity.Event;
import jack.Minoa.Entity.Waiter;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShiftBackupRequest {

    private Event event;

    private List<Waiter> lastWaiters;
}
