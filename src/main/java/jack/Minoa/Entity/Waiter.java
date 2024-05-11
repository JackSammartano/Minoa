package jack.Minoa.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private boolean latest;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    @ManyToMany(mappedBy = "waiters")
    @JsonIgnore
    private List<Event> events; // relazione inversa
}