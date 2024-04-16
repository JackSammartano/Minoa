package jack.Minoa.Entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ShiftChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "giving_waiter_id", nullable = false)
    private Waiter givingWaiter; // Cameriere che dà il cambio

    @ManyToOne
    @JoinColumn(name = "receiving_waiter_id", nullable = false)
    private Waiter receivingWaiter; // Cameriere che riceve il cambio

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Events event; // Evento associato al cambio
}