package searchengine.services.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FaultResponse {
    private final boolean result;
    private final String error;
}
