package pl.Dayfit.Florae.Exceptions;

/**
 * Describes a situation when FloraLink device is not connected to a sever
 */
public class DeviceOfflineException extends RuntimeException {
    public DeviceOfflineException(String message) {
        super(message);
    }
}
