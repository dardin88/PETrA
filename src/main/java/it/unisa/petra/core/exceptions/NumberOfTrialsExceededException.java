package it.unisa.petra.core.exceptions;

/**
 * @author dardin88
 */
public class NumberOfTrialsExceededException extends Exception {

    public NumberOfTrialsExceededException() {
        super("error: too many trials performed!");
        System.out.println("error: too many trials performed!");
    }
}
