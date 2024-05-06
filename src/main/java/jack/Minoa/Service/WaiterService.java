package jack.Minoa.Service;

import jack.Minoa.Entity.User;
import jack.Minoa.Entity.Waiter;
import jack.Minoa.Request.UpdateUserRequest;
import jack.Minoa.Request.WaiterRequest;
import jack.Minoa.Repository.UserRepository;
import jack.Minoa.Repository.WaiterRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class  WaiterService {

    private final WaiterRepository waiterRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public WaiterService(WaiterRepository waiterRepository, UserService userService, UserRepository userRepository, UserService userService1, PasswordEncoder passwordEncoder) {
        this.waiterRepository = waiterRepository;
        this.userService = userService1;
        this.passwordEncoder = passwordEncoder;
    }

        public Waiter createWaiter (WaiterRequest waiterRequest){
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
                .latest(waiterRequest.isLatest())
                .build();
        waiter.setUser(user);
        return waiterRepository.save(waiter);
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
        waiter.setLatest(waiterRequest.isLatest());

        UpdateUserRequest updateUserRequest = UpdateUserRequest.builder()
                .username(waiterRequest.getUsername())
                .password(waiterRequest.getPassword())
                .accessLevel(waiterRequest.getAccessLevel())
                .build();

        waiter.setUser(userService.updateUser(waiter.getUser().getId(), updateUserRequest));
        return waiterRepository.save(waiter);
    }

    public List<Waiter> resetLatestWaiters(List<Waiter> newestLatestWaiter){
        List<Waiter> previousLatestWaiters = waiterRepository.findAllByLatest(true);
        for(Waiter w : previousLatestWaiters){
            w.setLatest(false);
            waiterRepository.save(w);
        }
        for(Waiter w : newestLatestWaiter){
            Waiter u = readWaiter(w.getId());
            u.setLatest(true);
            waiterRepository.save(u);
        }
        return waiterRepository.findAllByLatest(true);
    }

    public List<Waiter> getAllWaitersByBelongingGroup(Waiter.BelongingGroup belongingGroup) {
        return waiterRepository.findByBelongingGroup(belongingGroup);
    }

    public List<Waiter> setLatestWaiter(List<Waiter> waiterToSetLatest){
        List<Waiter> result = new ArrayList<>();
        for(Waiter w : waiterToSetLatest){
            Waiter waiter = readWaiter(w.getId());
            waiter.setLatest(true);
            waiterRepository.save(waiter);
            result.add(waiter);
        }
        return result;
    }
}
