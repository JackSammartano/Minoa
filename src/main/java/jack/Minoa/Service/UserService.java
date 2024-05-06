package jack.Minoa.Service;

import jack.Minoa.Entity.User;
import jack.Minoa.Request.RegisterRequest;
import jack.Minoa.Request.UpdateUserRequest;
import jack.Minoa.Repository.UserRepository;
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

    public void createUser (RegisterRequest registerRequest){
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .accessLevel(registerRequest.getAccessLevel())
                .build();
        userRepository.save(user);
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

    @Transactional
    public void deleteUser(Long id){

        userRepository.delete(readUser(id));
    }

    public User readUser(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }
}