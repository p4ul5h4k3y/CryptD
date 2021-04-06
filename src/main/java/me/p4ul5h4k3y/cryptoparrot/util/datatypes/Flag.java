package me.p4ul5h4k3y.cryptoparrot.util.datatypes;

//Written by p4ul5h4k3y
//This class provides a custom datatype which can store: a flag String, a boolean which denotes if the flag takes an argument,
//a className String which shows which class the flag corresponds to, and a String[] which contains the required arguments for the class.
//This class is used to store the different option flags for running the project from the command-line.

public class Flag {
    public Flag(String newFlag, Boolean newTakesArg, String newClassName, String[] newClassArguments) {
        flag = newFlag;
        takesArg = newTakesArg;
        className = newClassName;
        classArguments = newClassArguments;
    }

    public String flag;
    public Boolean takesArg;
    public String className;
    public String[] classArguments;
}
