package jack.Minoa;

import jack.Minoa.Entity.User;
import jack.Minoa.Request.UpdateUserRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public User updateUser(Long id, UpdateUserRequest updateUserRequest) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(updateUserRequest.getUsername());
                    if (updateUserRequest.getPassword() != null && !updateUserRequest.getPassword().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
                    }
                    user.setAccessLevel(updateUserRequest.getAccessLevel());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }
}