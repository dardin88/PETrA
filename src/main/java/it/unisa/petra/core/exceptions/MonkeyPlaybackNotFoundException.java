package it.unisa.petra.core.exceptions;

/**
 * @author dardin88
 */
public class MonkeyPlaybackNotFoundException extends Exception {

    public MonkeyPlaybackNotFoundException() {
        super("error: cannot find monkeyplayer!");
        System.out.println("error: cannot find monkeyplayer!");
    }
}
