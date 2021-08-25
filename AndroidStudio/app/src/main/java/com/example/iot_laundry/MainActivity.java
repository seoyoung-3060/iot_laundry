package com.example.iot_laundry;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.iot_laundry.Utils.MyServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    String HOST_NAME;
    String HOST_ADDRESS;
    private String TAG = "MainActivityLog";
    Button buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStart = findViewById(R.id.buttonStart);

        this.registerReceiver(wifiEventReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DryingActivity.class);
                //시작 시의 수분센서값 넘겨주어야 함

                startActivity(intent);

                String activityState = "start";
                String serverAddress = "";

                DryingActivity.HttpRequestTask requestTask = new DryingActivity.HttpRequestTask();
                requestTask.execute(activityState);
            }
        });

    }
    private String getHostIp() {
        Context context = getApplicationContext();
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }
    private final BroadcastReceiver wifiEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context_, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
                    Toast.makeText(context_, "WIFI 연결 상태입니다.", Toast.LENGTH_SHORT).show();
                    String ip = getHostIp();
                    MyServer myServer = new MyServer();
                    myServer.setServerAddress(ip);
                    buttonStart.setEnabled(true);

                } else {
                    Toast.makeText(context_, "WIFI 연결 상태를 확인 해주세요.", Toast.LENGTH_SHORT).show();
                    buttonStart.setEnabled(false);
                }
            }
        }
    };
}

/** END OF CODE **/
//final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                    builder.setTitle("와이파이 연결을 해주세요");
//                            builder.setMessage("와이파이가 연결돼야 건조 시스템을 이용하실 수 있어요!");
//                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//@Override
//public void onClick(DialogInterface dialog, int which) {
//        getHostIp();
//        }
//        });
//        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//@Override
//public void onDismiss(DialogInterface dialog) {
//        //finish();
//        //System.exit(0);
//        }
//        });
//        builder.show();
