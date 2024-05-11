package jack.Minoa.Response;

import jack.Minoa.Entity.Waiter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkShiftResponse {

    private List<Waiter> workShift;
    private String Message;
}
