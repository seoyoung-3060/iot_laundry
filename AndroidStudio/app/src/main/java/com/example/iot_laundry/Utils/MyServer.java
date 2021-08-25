package com.example.iot_laundry.Utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MyServer {
    public static String serverAddress;// = "192.168.35.237";

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
    public static String getServerAddress() {
        return serverAddress;
    }
}
