package it.unisa.petra.ui;

/**
 *
 * @author dardin88
 */
public class NoDeviceFoundException extends Exception {

    public NoDeviceFoundException() {
        super("error: no device/emulator found!");
        System.out.println("error: no device/emulator found!");
    }
}
