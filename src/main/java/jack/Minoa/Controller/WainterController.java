package jack.Minoa.Controller;

import jack.Minoa.Entity.Waiter;
import jack.Minoa.Request.WaiterRequest;
import jack.Minoa.Service.WaiterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/waiters")
public class WainterController {

    private final WaiterService waiterService;

    public WainterController(WaiterService waiterService) {
        this.waiterService = waiterService;
    }

    @PostMapping()
    public ResponseEntity<Waiter> createWaiter(@RequestBody WaiterRequest waiterRequest){
        Waiter waiter = waiterService.createWaiter(waiterRequest);
        return ResponseEntity.ok(waiter);
    }

    @PutMapping
    public ResponseEntity<List<Waiter>> setLatestWaiters(@RequestBody List<Waiter> waitersToSetLatest){
        return ResponseEntity.ok(waiterService.resetLatestWaiters(waitersToSetLatest));
    }
}
