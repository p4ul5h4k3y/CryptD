package me.p4ul5h4k3y.cryptoparrot.datatypes;

//Written by p4ul5h4k3y
//This class provides a custom datatype which can store: a boolean and an integer (usually denoting an index in an array)

public class BoolAndPos {
    public BoolAndPos(boolean newBool, int newPos) {
        bool = newBool;
        pos = newPos;
    }
    public boolean bool;
    public int pos;
}