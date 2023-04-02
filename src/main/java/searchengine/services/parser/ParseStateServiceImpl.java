package searchengine.services.parser;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class ParseStateServiceImpl implements ParseStateService {
    private boolean state;

    public boolean isStopped() {
        return state;
    }

    @Override
    public void setState(boolean state) {
        this.state = state;
    }
}
