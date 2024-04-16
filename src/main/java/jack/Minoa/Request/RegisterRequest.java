package jack.Minoa.Request;

import jack.Minoa.Entity.User;
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
public class RegisterRequest {

    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private User.AccessLevel accessLevel;
}
