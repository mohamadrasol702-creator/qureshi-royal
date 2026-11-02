package com.qureshisahib.royal;

public class NodeBridge {
    static {
        System.loadLibrary("node");
        System.loadLibrary("native-lib");
    }
    public static native int startNodeWithArguments(String[] args);
}
