package main.java;//Written by Paul Schakel
//This class provides methods and classes useful for checking arguments in the CryptoParrot class


import datatypes.*;
import main.java.datatypes.BoolAndPos;

public class ArgCheck {
    public ArgCheck(String[] newArgList) {
        argList = newArgList;
    }

    public String[] argList;

    public boolean checkIfPresent(String toCheck) {     //checks if an argument (toCheck) is present in an array of valid arguments (argList)
        int i = 0;

        while  (i < argList.length) {
            String arg = argList[i];
            if (toCheck.equals(arg)) {
                return true;
            }
            i++;
        }

        return false;
    }

    public BoolAndPos checkIfEncrypt(String[] toCheck) {        //checks if the user wants to decrypt a .crypt file
        for (int i = 0; i < toCheck.length; i++) {
            String arg = toCheck[i];
            if (arg.equals("-d")) {
                if (toCheck.length < 2) {
                    System.out.println("E: No path to file specified");
                    System.exit(1);
                }
                return new BoolAndPos(false, i);
            }
        }
        return new BoolAndPos(true, -1);
    }

    public boolean checkIfHelp(String[] toCheck) {
        for (String arg : toCheck) {
            if (arg.equals("-h")) {
                return true;
            }
        }
        return false;
    }

    public BoolAndPos checkIfPath(String[] toCheck) {
        for (int i = 0; i < toCheck.length; i++) {
            String arg = toCheck[i];
            if (arg.equals("-p")) {
                return new BoolAndPos(true, i);
            }
        }
        return new BoolAndPos(false, -1);
    }

    public BoolAndPos checkIfFile(String[] toCheck) {
        for (int i = 0; i < toCheck.length; i++) {
            String arg = toCheck[i];
            if (arg.equals("-f")) {
                return new BoolAndPos(true, i);
            }
        }
        return new BoolAndPos(false, -1);
    }

    public BoolAndPos checkIfImport(String[] toCheck) {
        for (int i = 0; i < toCheck.length; i++) {
            String arg = toCheck[i];
            if (arg.equals("-i")) {
                return new BoolAndPos(true, i);
            }
        }
        return new BoolAndPos(false, -1);
    }

    public BoolAndPos checkIfExport(String[] toCheck) {
        for (int i = 0; i < toCheck.length; i++) {
            String arg = toCheck[i];
            if (arg.equals("-x")) {
                return new BoolAndPos(true, i);
            }
        }
        return new BoolAndPos(false, -1);
    }

    public BoolAndPos checkIfName(String[] toCheck) {
        for (int i = 0; i < toCheck.length; i++) {
            String arg = toCheck[i];
            if (arg.equals("--name")) {
                return new BoolAndPos(true, i);
            }
        }
        return new BoolAndPos(false, -1);
    }

    public static void printUsage() {
        System.out.println("Usage: encrypt [OPTION] [ARGS] \n" +
                "      -d [path-to-encrypted-data]       set mode to decrypt\n" +
                "      -e                                set mode to encrypt\n" +
                "      -i [path-to-public-key]           imports somebody else's public key from a specified file and prompts the user to name the new contact" +
                "           --name [nickname-for contact]      set the name for the new contact with a command" +
                "      -x [filename]                     exports your public key and saves it to a specified filename so you can share it with others" +
                "      -f [path-to-file]                 encrypt a specified file or directory\n" +
                "      -g                                generate keypair for encryption\n" +
                "      -p [path-to-file]                 set path for saving message to (default is current-dir/date_time) when encrypting\n" +
                "                                            when decrypting it saves the decrypted message to the file\n" +
                "      -h                                display this info and exit");
    }
}