package it.unisa.petra.process;

/**
 * @author dardin88
 */
public class NoDeviceFoundException extends Exception {

    NoDeviceFoundException() {
        super("error: no device/emulator found!");
        System.out.println("error: no device/emulator found!");
    }
}
