package me.p4ul5h4k3y.cryptoparrot.datatypes;

//Written by p4ul5h4k3y
//This class provides a custom datatype which can store: a boolean, and a String (which usually denotes a path to a file)

public class BoolAndFilename{
    public BoolAndFilename(boolean newBool, String newFilename) {
        bool = newBool;
        filename = newFilename;
    }
    public boolean bool;
    public String filename;
}