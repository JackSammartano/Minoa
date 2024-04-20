package jack.Minoa.Request;

import jack.Minoa.Entity.Event;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest {

    private String name;
    @Enumerated(EnumType.STRING)
    private Event.Eventstype eventstype;
    private int diners;
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private Event.MealType mealType;
    @Enumerated(EnumType.STRING)
    private Event.EventLocation eventLocation;
}
