package it.unisa.petra.ui;

import it.unisa.petra.core.Process;
import it.unisa.petra.core.ProcessOutput;
import it.unisa.petra.core.exceptions.ADBNotFoundException;
import it.unisa.petra.core.exceptions.NoDeviceFoundException;
import it.unisa.petra.core.exceptions.NumberOfTrialsExceededException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainUI extends JDialog {
    private JPanel contentPane;
    private JTextField appNameField;
    private JPanel appNameLabel;
    private JTextField apkLocationField;
    private JLabel apkLocationLabel;
    private JButton apkLocationButton;
    private JSlider interactionsSlider;
    private JLabel interactionsLabel;
    private JLabel timeBetweenInteractionsLabel;
    private JSlider timeBetweenInteractionsSlider;
    private JTextField scriptLocationField;
    private JButton scriptLocationButton;
    private JLabel scriptLocationLabel;
    private JSlider runsSlider;
    private JLabel runsLabel;
    private JLabel powerProfileFileLabel;
    private JTextField powerProfileFileField;
    private JButton powerProfileFileButton;
    private JTextArea statusArea;
    private JProgressBar progressBar;
    private JButton startEstimationButton;
    private JButton statisticsButton;
    private JScrollPane statusAreaScroll;

    MainUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("PETrA");
        setIconImage(new ImageIcon(getClass().getResource("/petra.png")).getImage());
        setResizable(false);
        setContentPane(contentPane);
        setModal(true);

        apkLocationButton.addActionListener(evt -> apkLocationButtonActionPerformed());
        powerProfileFileButton.addActionListener(evt -> powerProfileFileButtonActionPerformed());
        scriptLocationButton.addActionListener(evt -> scriptLocationButtonActionPerformed());
        startEstimationButton.addActionListener(evt -> startEstimationButtonActionPerformed());
        statisticsButton.addActionListener(evt -> statisticsButtonActionPerformed());
        statusAreaScroll.setViewportView(statusArea);

        PrintStream out = new PrintStream(new TextAreaOutputStream(statusArea));
        System.setOut(out);
    }

    public static void run() {
        try {
            OUTER:
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if (info.getName() != null) {
                    switch (info.getName()) {
                        case "GTK+":
                            javax.swing.UIManager.setLookAndFeel(info.getClassName());
                            break OUTER;
                        case "Windows":
                            javax.swing.UIManager.setLookAndFeel(info.getClassName());
                            break OUTER;
                        case "Nimbus":
                            javax.swing.UIManager.setLookAndFeel(info.getClassName());
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        MainUI dialog = new MainUI();
        dialog.pack();
        dialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - dialog.getWidth() / 2, (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - dialog.getHeight() / 2);
        dialog.setVisible(true);
        System.exit(0);
    }

    private void startEstimationButtonActionPerformed() {
        statusArea.setText(null);
        statisticsButton.setEnabled(false);

        String appName = this.appNameField.getText();
        String apkLocationPath = this.apkLocationField.getText();
        int runs = this.runsSlider.getValue();
        int interactions = interactionsSlider.getValue();
        int timeBetweenInteractions = timeBetweenInteractionsSlider.getValue();
        String scriptLocationPath = this.scriptLocationField.getText();
        String powerProfilePath = powerProfileFileField.getText();

        String outputLocationPath = new File(apkLocationPath).getParent() + File.separator + "test_data" + File.separator + appName;

        boolean valid = true;

        if (appName.isEmpty()) {
            System.out.println("App name missing.");
            valid = false;
        }

        if (apkLocationPath.isEmpty()) {
            System.out.println("Apk location missing.");
            valid = false;
        }

        if (interactions <= 0 && scriptLocationPath.isEmpty()) {
            System.out.println("You must perform at least one Monkey interaction or select a Monkeyrunner script.");
            valid = false;
        }

        if (runs <= 0) {
            System.out.println("You must execute at least one run.");
            valid = false;
        }

        if (powerProfilePath.isEmpty()) {
            System.out.println("Please select an Android Power Profile file.");
            valid = false;
        }

        if (!valid) {
            return;
        }

        progressBar.setValue(0);

        MainUI.Task task = new MainUI.Task(appName, apkLocationPath, interactions, timeBetweenInteractions,
                scriptLocationPath, runs, powerProfilePath, outputLocationPath);
        task.addPropertyChangeListener((PropertyChangeEvent evt1) -> {
            if ("progress".equals(evt1.getPropertyName())) {
                progressBar.setValue((Integer) evt1.getNewValue());
            }
        });
        task.execute();
    }

    private void apkLocationButtonActionPerformed() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Apk Files", "apk");
        chooser.setFileFilter(filter);
        int res = chooser.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String filename = f.getAbsolutePath();
            apkLocationField.setText(filename);
        }
    }

    private void powerProfileFileButtonActionPerformed() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String filename = f.getAbsolutePath();
            powerProfileFileField.setText(filename);
        }
    }

    private void statisticsButtonActionPerformed() {
        this.setVisible(false);
        String outputLocationPath = new File(apkLocationField.getText()).getParent() + File.separator + "test_data" + File.separator + appNameField.getText();
        new StatsUI(outputLocationPath).setVisible(true);
    }

    private void scriptLocationButtonActionPerformed() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String filename = f.getAbsolutePath();
            scriptLocationField.setText(filename);
        }
    }

    class Task extends SwingWorker<Void, Void> {

        private final String appName;
        private final String apkLocationPath;
        private final int interactions;
        private final int timeBetweenInteractions;
        private final String scriptLocationPath;
        private final int runs;
        private final String powerProfilePath;
        private final String outputLocationPath;

        Task(String appName, String apkLocationPath, int interactions, int timeBetweenInteractions, String scriptLocationPath,
             int runs, String powerProfilePath, String outputLocationPath) {
            this.appName = appName;
            this.apkLocationPath = apkLocationPath;
            this.interactions = interactions;
            this.timeBetweenInteractions = timeBetweenInteractions;
            this.scriptLocationPath = scriptLocationPath;
            this.runs = runs;
            this.powerProfilePath = powerProfilePath;
            this.outputLocationPath = outputLocationPath;
        }

        @Override
        public Void doInBackground() {
            try {
                appNameField.setEditable(false);
                apkLocationButton.setEnabled(false);
                powerProfileFileField.setEditable(false);
                runsSlider.setEnabled(false);
                interactionsSlider.setEnabled(false);
                timeBetweenInteractionsSlider.setEnabled(false);
                scriptLocationField.setEditable(false);
                scriptLocationButton.setEnabled(false);
                powerProfileFileButton.setEnabled(false);
                startEstimationButton.setEnabled(false);
                Process process = new Process();
                setProgress(0);
                int progress;
                int trials = 0;
                BufferedWriter seedsWriter = null;

                File appDataFolder = new File(outputLocationPath);

                appDataFolder.mkdirs();

                if (this.scriptLocationPath.isEmpty()) {
                    File seedsFile = new File(outputLocationPath + File.separator + "seeds");
                    seedsWriter = new BufferedWriter(new FileWriter(seedsFile, true));
                }
                process.installApp(apkLocationPath);

                int timeCapturing = (interactions * timeBetweenInteractions) / 1000;

                if (timeCapturing <= 0) {
                    timeCapturing = 100;
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainUI.class.getName()).log(Level.SEVERE, null, ex);
                }

                for (int run = 1; run <= runs; run++) {
                    if (trials == 10) {
                        throw new NumberOfTrialsExceededException();
                    }
                    try {
                        ProcessOutput output = process.playRun(run, appName, interactions, timeBetweenInteractions, timeCapturing,
                                scriptLocationPath, powerProfilePath, outputLocationPath);
                        if (seedsWriter != null) {
                            seedsWriter.append(String.valueOf(output.getSeed())).append("\n");
                        }
                        timeCapturing = output.getTimeCapturing();
                        progress = (100 * run / runs);
                        setProgress(progress);
                    } catch (InterruptedException | IOException ex) {
                        run--;
                        trials++;
                    }
                }
                appNameField.setEditable(true);
                apkLocationButton.setEnabled(true);
                powerProfileFileField.setEditable(true);
                runsSlider.setEnabled(true);
                interactionsSlider.setEnabled(true);
                timeBetweenInteractionsSlider.setEnabled(true);
                scriptLocationField.setEditable(true);
                scriptLocationButton.setEnabled(true);
                powerProfileFileButton.setEnabled(true);
                startEstimationButton.setEnabled(true);
                statisticsButton.setEnabled(true);
                process.uninstallApp(appName);
            } catch (NoDeviceFoundException | IOException | NumberOfTrialsExceededException | ADBNotFoundException ex) {
                appNameField.setEditable(true);
                apkLocationButton.setEnabled(true);
                powerProfileFileField.setEditable(true);
                runsSlider.setEnabled(true);
                interactionsSlider.setEnabled(true);
                timeBetweenInteractionsSlider.setEnabled(true);
                scriptLocationField.setEditable(true);
                scriptLocationButton.setEnabled(true);
                powerProfileFileButton.setEnabled(true);
                startEstimationButton.setEnabled(true);
                Logger.getLogger(MainUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
}
