package com.example.iot_laundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
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
import com.example.iot_laundry.firebase.MyFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivityLog";
    MaterialButton buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStart = findViewById(R.id.buttonStart);

//        this.registerReceiver(wifiEventReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyFirebase.startRef.setValue(true); //시스템 가동 중임
                Intent intent = new Intent(getApplicationContext(), DryingActivity.class);
                //시작 시의 수분센서값 넘겨주어야 함

                startActivity(intent);
                String activityState = "start";
//                DryingActivity.HttpRequestTask httpRequestTask = new DryingActivity.HttpRequestTask();
                DryingActivity.HttpRequestTask requestTask = new DryingActivity.HttpRequestTask(MyServer.moistAddress);
                requestTask.execute(activityState);
            }
        });

        MyFirebase.startMoistRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    long startMoist = (Long) task.getResult().getValue();
                    if (startMoist != 0) {
                        buttonStart.setText("진행 중");
                        buttonStart.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.navigation_color_4));
                        buttonStart.setEnabled(true);

                    } else {
                        buttonStart.setText("시작");
                        buttonStart.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.progress_blue));

                    }
                }
            }
        });

    }
}
/** END OF CODE **/

//    // wifi 연결된 상태에만 액티비티 전환시킬 수 있는 것 끄자
//    private final BroadcastReceiver wifiEventReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context_, Intent intent) {
//            String action = intent.getAction();
//            if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
//                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
//                if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
//                    Toast.makeText(context_, "WIFI 연결 상태입니다.", Toast.LENGTH_SHORT).show();
//                    buttonStart.setEnabled(true);
//
//                } else {
//                    Toast.makeText(context_, "WIFI 연결 상태를 확인 해주세요.", Toast.LENGTH_SHORT).show();
//                    buttonStart.setEnabled(false);
//                }
//            }
//        }
//    };


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
