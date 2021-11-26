package com.example.iot_laundry.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static android.content.Context.MODE_PRIVATE;

public class MyServer {
    Context context;
    //수민핫스팟
    public static String moistAddress = "192.168.120.90";

    public static String buttonAddress = "192.168.120.100";

    public static String curtainAddress = "192.168.120.110"; //172.20.10.110
    public static String windowAddress = "192.168.120.110";

    public MyServer(Context context) {
        this.context = context;
        SharedPreferences sharedPreferences= context.getSharedPreferences("test", MODE_PRIVATE);    // test 이름의 기본모드 설정, 만약 test key값이 있다면 해당 값을 불러옴.
        String serverAddress = sharedPreferences.getString("serverAddress","192.168.120");

        moistAddress = serverAddress + ".90";
        buttonAddress = serverAddress + ".100";
        curtainAddress = serverAddress + ".110";
        windowAddress = serverAddress + ".110";
    }

    public String getMoistAddress() {
        return moistAddress;
    }

    public String getButtonAddress() {
        return buttonAddress;
    }

    public String getCurtainAddress() {
        return curtainAddress;
    }

    public String getWindowAddress() {
        return windowAddress;
    }

    public void setServerAddress(String serverAddress) {
//        this.context = ctx;
//        moistAddress = serverAddress + "90";
//        buttonAddress = serverAddress + "100";
//        curtainAddress = serverAddress + "110";
//        windowAddress = serverAddress + "110";

        SharedPreferences sharedPreferences= context.getSharedPreferences("test", MODE_PRIVATE);    // test 이름의 기본모드 설정
        SharedPreferences.Editor editor= sharedPreferences.edit(); //sharedPreferences를 제어할 editor를 선언
        editor.putString("serverAddress", serverAddress); // key,value 형식으로 저장
        editor.commit();    //최종 커밋. 커밋을 해야 저장이 된다.
    }


    //승민님
//    public static String moistAddress = "170.30.1.90";
//
//    public static String buttonAddress = "170.30.1.100";
//
//    public static String curtainAddress = "170.30.1.110"; //172.20.10.110
//    public static String windowAddress = "170.30.1.110";
}
