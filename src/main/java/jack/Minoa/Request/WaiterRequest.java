package jack.Minoa.Request;

import jack.Minoa.Entity.User;
import jack.Minoa.Entity.Waiter;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WaiterRequest {

    private String name;
    private String surname;
    @Enumerated(EnumType.STRING)
    private Waiter.BelongingGroup belongingGroup;
    private Long telephoneNumber;
    private String email;
    private int positionOrder;
    private boolean latest;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private User.AccessLevel accessLevel;
}