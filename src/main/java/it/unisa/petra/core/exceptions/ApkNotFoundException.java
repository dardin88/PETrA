package it.unisa.petra.core.exceptions;

/**
 * @author dardin88
 */
public class ApkNotFoundException extends Exception {

    public ApkNotFoundException() {
        super("error: apk not found!");
        System.out.println("error: apk not found!");
    }
}
