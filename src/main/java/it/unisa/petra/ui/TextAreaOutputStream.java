package it.unisa.petra.ui;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that writes its output to a javax.swing.JTextArea control.
 *
 * @author Ranganath Kini
 * @see javax.swing.JTextArea
 */
class TextAreaOutputStream extends OutputStream {

    private final JTextArea textControl;

    /**
     * Creates a new instance of TextAreaOutputStream which writes to the
     * specified instance of javax.swing.JTextArea control.
     *
     * @param control A reference to the javax.swing.JTextArea control to which
     *                the output must be redirected to.
     */
    TextAreaOutputStream(JTextArea control) {
        textControl = control;
    }

    /**
     * Writes the specified byte as a character to the javax.swing.JTextArea.
     *
     * @param b The byte to be written as character to the JTextArea.
     */
    @Override
    public void write(int b) throws IOException {
        // append the data as characters to the JTextArea control
        textControl.append(String.valueOf((char) b));
        textControl.update(textControl.getGraphics());
    }
}
