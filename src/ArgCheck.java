//Written by Paul Schakel
//This class takes a Hashmap of arguments and Booleans which identify whether or not there should be a string after the argument.
//The constructor loops through the array of strings to check and creates another hashmap which identifies which arguments are present
//in the array of strings. The returnCheckedArgs() method returns the Hashmap with the checked arguments.


import datatypes.*;
import java.util.HashMap;
import java.util.Map;

public class ArgCheck {
    public String[] argList;
    public HashMap<String, BoolAndPos> checkedArgs = new HashMap<String, BoolAndPos>();

    public ArgCheck(HashMap<String, Boolean> argMap, String[] args) {
        argList = argMap.keySet().toArray(new String[0]);

        try {
            if (!checkIfPresent(args[0])) {     //makes sure that the first argument is a valid one
                System.out.println(args[1]);
                System.out.println("E: Argument invalid");
                ArgCheck.printUsage();
                System.exit(1);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {   //shows error when no arguments are specified and prints usage
            System.out.println("E: No arguments");
            ArgCheck.printUsage();
            System.exit(1);
        }

        for (Map.Entry validArg : argMap.entrySet()) {
            String currentKey = (String) validArg.getKey();
            Boolean currentVal = (Boolean) validArg.getValue();
            checkedArgs.put(currentKey, new BoolAndPos(false, -1));

            for (int i = 0; i <args.length; i++) {
                if (currentKey.equals(args[i])) {
                    if (currentVal) {
                        if (args.length < 2) {
                            System.out.println("E: No path to file specified");
                            System.exit(1);
                        }
                        checkedArgs.put(currentKey, new BoolAndPos(true, i + 1));
                    } else {
                        checkedArgs.put(currentKey, new BoolAndPos(true, -1));
                    }
                }
            }
        }
    }

    public HashMap<String, BoolAndPos> returnCheckedArgs() {
        return checkedArgs;
    }

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

    public static void printUsage() {
        System.out.println("Usage: encrypt [OPTION] [ARGS] \n" +
                "      -d [path-to-encrypted-data]       set mode to decrypt\n" +
                "      -e                                set mode to encrypt\n" +
                "      -i [path-to-public-key]           imports somebody else's public key from a specified file and prompts the user to name the new contact\n" +
                "           --name [nickname-for contact]      set the name for the new contact with a command\n" +
                "      -x [filename]                     exports your public key and saves it to a specified filename so you can share it with others\n" +
                "      -f [path-to-file]                 encrypt a specified file or directory\n" +
                "      -g                                generate keypair for encryption\n" +
                "      -p [path-to-file]                 set path for saving message to (default is current-dir/date_time) when encrypting\n" +
                "                                            when decrypting it saves the decrypted message to the file\n" +
                "      -h                                display this info and exit");
    }
}