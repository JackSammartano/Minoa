package jack.Minoa.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Event {

    public enum Eventstype {MATRIMONIO, BANCHETTO}
    public enum MealType {MATTINA, SERA}
    public enum EventLocation {MINOA, COLORADO}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private Eventstype eventstype;
    private int diners;
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private MealType mealType;
    @Enumerated(EnumType.STRING)
    private EventLocation eventLocation;

    @ManyToMany
    @JoinTable(
            name = "work_shift", // Nome della tabella di join
            joinColumns = @JoinColumn(name = "event_id"), // Colonna che fa riferimento a Event
            inverseJoinColumns = @JoinColumn(name = "waiter_id") // Colonna che fa riferimento a Waiter
    )
    private List<Waiter> waiters;
}