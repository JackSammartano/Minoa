package jack.Minoa;

import jack.Minoa.Entity.User;
import jack.Minoa.Request.UpdateUserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @RequestBody UpdateUserRequest updateUserRequest){
        User user = userService.updateUser(id, updateUserRequest);
        return ResponseEntity.ok(user);
    }
}