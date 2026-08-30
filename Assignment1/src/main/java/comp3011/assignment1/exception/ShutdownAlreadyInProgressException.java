package comp3011.assignment1.exception;

public class ShutdownAlreadyInProgressException
        extends RuntimeException {

    public ShutdownAlreadyInProgressException() {
        super("Graceful shutdown is already in progress.");
    }
}