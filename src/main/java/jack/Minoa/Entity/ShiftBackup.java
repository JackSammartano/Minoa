package jack.Minoa.Entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class ShiftBackup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "last_waiterMALE_id")
    private Waiter lastWaiterMale;

    @ManyToOne
    @JoinColumn(name = "last_waiterFEMALE_id")
    private Waiter lastWaiterFemale;

    @ManyToOne
    @JoinColumn(name = "last_waiterSECONDARY_id")
    private Waiter lastWaiterSecondary;
}
