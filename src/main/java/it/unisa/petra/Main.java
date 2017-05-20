package it.unisa.petra;

import it.unisa.petra.batch.Terminal;
import it.unisa.petra.ui.MainUI;

/**
 * @author dardin88
 */
public class Main {

    public static void main(String[] args) {
        switch (args.length) {
            case 1:
                if (args[0].equals("--desktop")) {
                    MainUI.run();
                } else {
                    System.out.println("\nName:");
                    System.out.println("\tPETrA - Power Estimation Tool for Android\n");
                    System.out.println("Launching PETrA gui:");
                    System.out.println("\tjava -jar PETrA.jar --desktop\n");
                    System.out.println("Launching PETrA as batch:");
                    System.out.println("\tjava -jar PETrA.jar --batch config_file_location\n");
                }
                break;
            case 2:
                if (args[0].equals("--batch") && (!args[1].isEmpty())) {
                    Terminal.run(args[1]);
                } else {
                    System.out.println("\nName:");
                    System.out.println("\tPETrA - Power Estimation Tool for Android\n");
                    System.out.println("Launching PETrA gui:");
                    System.out.println("\tjava -jar PETrA.jar --desktop\n");
                    System.out.println("Launching PETrA as batch:");
                    System.out.println("\tjava -jar PETrA.jar --batch config_file_location\n");
                }
                break;
            default:
                System.out.println("\nName:");
                System.out.println("\tPETrA - Power Estimation Tool for Android\n");
                System.out.println("Launching PETrA gui:");
                System.out.println("\tjava -jar PETrA.jar --desktop\n");
                System.out.println("Launching PETrA as batch:");
                System.out.println("\tjava -jar PETrA.jar --batch config_file_location\n");
        }
    }
}
