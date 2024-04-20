package jack.Minoa.Entity;

import jack.Minoa.Entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Waiter {

    public enum BelongingGroup {MALE, FEMALE , SECONDARY, EXTRA}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String surname;
    @Enumerated(EnumType.STRING)
    private BelongingGroup belongingGroup;
    private Long telephoneNumber;
    private String email;
    private int positionOrder;
    private boolean isLast;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToMany(mappedBy = "waiters")
    private List<Event> events; // relazione inversa
}