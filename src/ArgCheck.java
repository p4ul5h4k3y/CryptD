//Written by Paul Schakel
//This class provides functions and classes useful for checking arguments in the main java class


public class ArgCheck {
    public ArgCheck(String[] newArgList) {
        argList = newArgList;
    }

    public String[] argList;

    public boolean checkIfPresent(String toCheck) {
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

    public BoolAndPos checkIfEncrypt(String[] toCheck) {
        for (int i = 0; i < toCheck.length; i++) {
            String arg = toCheck[i];
            if (arg.equals("-d")) {
                return new BoolAndPos(false, i);
            }
            else if (arg.equals("-e")) {
                return new BoolAndPos(true, 0);
            }
        }
        return null;
    }

    public boolean checkIfHelp(String[] toCheck) {
        for (int i = 0; i < toCheck.length; i++) {
            String arg = toCheck[i];
            if (arg.equals("-h")) {
                return true;
            }
        }
        return false;
    }

    public BoolAndPos checkIfPath(String[] toCheck) {
        for (int i = 0; i < toCheck.length; i++) {
            String arg = toCheck[i];
            if (arg.equals("-f")) {
                return new BoolAndPos(true, i);
            }
        }
        return new BoolAndPos(false, 0);
    }

    public static void printUsage() {
        System.out.println("Usage: encrypt [OPTION]... [ARGS]... \n" +
                "      -d [path-to-encrypted-text]       set mode to decrypt\n" +
                "      -e                                set mode to encrypt\n" +
                "      -g                                generate keypair for encryption\n" +
                "      -f [path-to-file]                 set path for saving message to (default is current-dir/date_time) when encrypting\n" +
                "                                            when decrypting it saves the decrypted message to the file\n" +
                "      -h                                display this info and exit");
    }
}