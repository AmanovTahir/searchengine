package searchengine.services.parser;

public interface ParseStateService {
    boolean isStopped();

    void setState(boolean state);
}
