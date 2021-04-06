package me.p4ul5h4k3y.cryptoparrot;

//Written by p4ul5h4k3y
//This class takes a Hashmap of arguments and Booleans which identify whether or not there should be a string after the argument.
//The constructor loops through the array of strings to check and creates another hashmap which identifies which arguments are present
//in the array of strings. The returnCheckedArgs() method returns the Hashmap with the checked arguments.

import me.p4ul5h4k3y.cryptoparrot.util.datatypes.BoolAndFilename;
import me.p4ul5h4k3y.cryptoparrot.util.datatypes.BoolAndPos;
import me.p4ul5h4k3y.cryptoparrot.util.datatypes.Flag;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ArgCheck {

    public HashMap<String, BoolAndPos> checkedArgs = new HashMap<String, BoolAndPos>();
    HashMap<String, Flag> argMap = new HashMap<String, Flag>()
    {{
        put("decrypt", new Flag("-d", true, "me.p4ul5h4k3y.cryptoparrot.encryption.Decrypt", new String[]{"-d", "-p", "sessionKey"}));
        put("text", new Flag("-t", false, "me.p4ul5h4k3y.cryptoparrot.encryption.TextCrypt", new String[]{"-t", "-p", "sessionKey"}));
        put("generate", new Flag("-g", false, "me.p4ul5h4k3y.cryptoparrot.CreateKeypair", new String[]{"none"}));
        put("path", new Flag("-p", true, "none", new String[]{"none"}));
        put("help", new Flag("-h", false, "none", new String[]{"none"}));
        put("file", new Flag("-f", true, "me.p4ul5h4k3y.cryptoparrot.encryption.FileCrypt", new String[]{"-f", "-p", "sessionKey"}));
        put("export", new Flag("-x", true, "me.p4ul5h4k3y.cryptoparrot.contacts.Export", new String[]{"-x", "--name"}));
        put("name", new Flag("--name", true, "none", new String[]{"none"}));
        put("import", new Flag("-i", true, "me.p4ul5h4k3y.cryptoparrot.contacts.Import", new String[]{"-i", "--name"}));
    }};
    public ArrayList<String> argList = new ArrayList<String>();


    public ArgCheck(String[] args) {
        for (Map.Entry<String, Flag> arg : argMap.entrySet()) {
            argList.add(arg.getValue().flag);
        }

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

        getCheckedArgs(args);
        createRequiredObjects(args);
    }

    public void createRequiredObjects(String[] args) {
        HashMap<String, Object> argumentInformation = new HashMap<>();
        argumentInformation.put("sessionKey", CryptoParrot.genSessionKey());

        for (Map.Entry<String, BoolAndPos> checkedArg : checkedArgs.entrySet()) {       //create dictionary of arguments to be passed to objects
            String currentKey = checkedArg.getKey();
            BoolAndPos currentVal = checkedArg.getValue();
            if (currentVal.pos != -1) {
                argumentInformation.put(currentKey, new BoolAndFilename(currentVal.bool, args[currentVal.pos]));
            } else {
                argumentInformation.put(currentKey, new BoolAndFilename(currentVal.bool, "NONE"));
            }
        }

        for (Map.Entry<String, Flag> arg : argMap.entrySet()) {
            for (Map.Entry<String, BoolAndPos> checkedArg : checkedArgs.entrySet()) {
                Flag currentArgVal = arg.getValue();
                if (currentArgVal.flag.equals(checkedArg.getKey()) && !currentArgVal.className.equals("none") && checkedArg.getValue().bool) {
                    if (!currentArgVal.classArguments[0].equals("none")) {
                        HashMap<String, Object> requiredArgs = new HashMap<String, Object>();
                        for (String classArg : currentArgVal.classArguments) {
                            requiredArgs.put(classArg, argumentInformation.get(classArg));
                        }
                        /*try {
                            Class newObject = Class.forName(currentArgVal.className);
                            Constructor con = newObject.getConstructor(HashMap.class);
                            objectToCreate = (Default) con.newInstance(requiredArgs);
                        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        */}
                    } else {
                        /*try {
                            objectToCreate = (Default) Class.forName(currentArgVal.className).newInstance();
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                */}
            }
        }
    }

    public void getCheckedArgs(String[] args) {
        for (Map.Entry<String, Flag> validArg : argMap.entrySet()) {
            Flag currentVal = validArg.getValue();
            String currentFlag = currentVal.flag;
            Boolean currentTakesArg = currentVal.takesArg;
            checkedArgs.put(currentFlag, new BoolAndPos(false, -1));

            for (int i = 0; i <args.length; i++) {
                if (currentFlag.equals(args[i])) {
                    if (currentTakesArg) {
                        if (args.length < 2) {
                            System.out.println("E: No path to file specified");
                            System.exit(1);
                        }
                        checkedArgs.put(currentFlag, new BoolAndPos(true, i + 1));
                    } else {
                        checkedArgs.put(currentFlag, new BoolAndPos(true, -1));
                    }
                }
            }
        }
    }

    public boolean checkIfPresent(String toCheck) {     //checks if an argument (toCheck) is present in an array of valid arguments (argList)
        int i = 0;

        while  (i < argList.size()) {
            String arg = argList.get(i);
            if (toCheck.equals(arg)) {
                return true;
            }
            i++;
        }

        return false;
    }

    public static void printUsage() {
        System.out.println("Usage: encrypt [OPTION] [ARGS] \n" +
                "      -d [path-to-encrypted-data]       decrypt any data\n" +
                "      -t                                encrypt text\n" +
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