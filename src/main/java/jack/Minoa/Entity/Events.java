package jack.Minoa.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Events {

    public enum Eventstype {MATRIMONIO, BANCHETTO}
    public enum MealType {MATTINA, SERA}
    public enum EventLocation {MATTINA, SERA}
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
}