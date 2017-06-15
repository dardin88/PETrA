package it.unisa.petra.core.exceptions;

/**
 * @author dardin88
 */
public class AppNameCannotBeExtractedException extends Exception {

    public AppNameCannotBeExtractedException() {
        super("error: app name cannot be extract!");
        System.out.println("error: app name cannot be extract!");
    }
}
