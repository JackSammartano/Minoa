package jack.Minoa;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
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

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}