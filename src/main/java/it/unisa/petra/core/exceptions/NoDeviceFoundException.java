package it.unisa.petra.core.exceptions;

/**
 * @author dardin88
 */
public class NoDeviceFoundException extends Exception {

    public NoDeviceFoundException() {
        super("error: no device/emulator found!");
        System.out.println("error: no device/emulator found!");
    }
}
