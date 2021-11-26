package com.example.iot_laundry.Utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MyServer {
    //수민핫스팟
    public static String moistAddress = "192.168.120.90";

    public static String buttonAddress = "192.168.120.100";

    public static String curtainAddress = "192.168.120.110"; //172.20.10.110
    public static String windowAddress = "192.168.120.110";

    public static void setServerAddress(String serverAddress) {
        moistAddress = serverAddress + "90";
        buttonAddress = serverAddress + "100";
        curtainAddress = serverAddress + "110";
        windowAddress = serverAddress + "110";
    }

    //승민님
//    public static String moistAddress = "170.30.1.90";
//
//    public static String buttonAddress = "170.30.1.100";
//
//    public static String curtainAddress = "170.30.1.110"; //172.20.10.110
//    public static String windowAddress = "170.30.1.110";
}
