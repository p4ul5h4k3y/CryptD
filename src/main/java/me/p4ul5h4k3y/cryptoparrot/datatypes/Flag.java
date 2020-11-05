package me.p4ul5h4k3y.cryptoparrot.datatypes;

//Written by p4ul5h4k3y
//This class provides a custom datatype which can store: a name String, a flag String, and a boolean which denotes if the flag takes an argument.
//This class is used to store the different option flags for running the project from the command-line.

public class Flag {
    public Flag(String newName, String newFlag, Boolean newTakesArg) {
        name = newName;
        flag = newFlag;
        takesArg = newTakesArg;
    }

    public String name;
    public String flag;
    public Boolean takesArg;
}
