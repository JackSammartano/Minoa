package jack.Minoa;

import jack.Minoa.Entity.User;
import jack.Minoa.Entity.Waiter;
import jack.Minoa.Request.UpdateUserRequest;
import jack.Minoa.Request.WaiterRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class WaiterService {

    private final WaiterRepository waiterRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public WaiterService(WaiterRepository waiterRepository, UserService userService, UserRepository userRepository, UserService userService1, PasswordEncoder passwordEncoder) {
        this.waiterRepository = waiterRepository;
        this.userService = userService1;
        this.passwordEncoder = passwordEncoder;
    }

    public void createWaiter (WaiterRequest waiterRequest){
        User user = User.builder()
                .username(waiterRequest.getUsername())
                .password(passwordEncoder.encode(waiterRequest.getPassword()))
                .accessLevel(waiterRequest.getAccessLevel())
                .build();
        Waiter waiter = Waiter.builder()
                .name(waiterRequest.getName())
                .surname(waiterRequest.getSurname())
                .belongingGroup(waiterRequest.getBelongingGroup())
                .telephoneNumber(waiterRequest.getTelephoneNumber())
                .email(waiterRequest.getEmail())
                .positionOrder(waiterRequest.getPositionOrder())
                .isLast(waiterRequest.isLast())
                .build();
        waiter.setUser(user);
    }

    public Waiter readWaiter (Long id){
        return waiterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Waiter not found with id " + id));
    }

    public void deleteWaiter(Long id){
        waiterRepository.delete(readWaiter(id));
    }

    @Transactional
    public Waiter updateWaiter(Long id, WaiterRequest waiterRequest){
        Waiter waiter = readWaiter(id);
        waiter.setName(waiterRequest.getName());
        waiter.setSurname(waiterRequest.getSurname());
        waiter.setBelongingGroup(waiterRequest.getBelongingGroup());
        waiter.setTelephoneNumber(waiterRequest.getTelephoneNumber());
        waiter.setEmail(waiterRequest.getEmail());
        waiter.setPositionOrder(waiterRequest.getPositionOrder());
        waiter.setLast(waiterRequest.isLast());

        UpdateUserRequest updateUserRequest = UpdateUserRequest.builder()
                .username(waiterRequest.getUsername())
                .password(waiterRequest.getPassword())
                .accessLevel(waiterRequest.getAccessLevel())
                .build();

        waiter.setUser(userService.updateUser(waiter.getUser().getId(), updateUserRequest));
        return waiterRepository.save(waiter);
    }
}
