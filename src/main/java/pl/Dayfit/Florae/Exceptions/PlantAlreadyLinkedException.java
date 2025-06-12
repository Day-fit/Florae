package pl.Dayfit.Florae.Exceptions;

public class PlantAlreadyLinkedException extends RuntimeException {
    public PlantAlreadyLinkedException(String message)
    {
        super(message);
    }
}
