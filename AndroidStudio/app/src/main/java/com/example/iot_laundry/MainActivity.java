package com.example.iot_laundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
    AppCompatImageButton button_setting;
    private GPSTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkRunTimePermission();

        buttonStart = findViewById(R.id.buttonStart);
        button_setting = findViewById(R.id.button_setting);

        MyServer myServer = new MyServer(getApplicationContext());

//        this.registerReceiver(wifiEventReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyFirebase.startRef.setValue(true); //????????? ?????? ??????
                Intent intent = new Intent(getApplicationContext(), DryingActivity.class);
                //?????? ?????? ??????????????? ??????????????? ???

                startActivity(intent);
                String activityState = "start";
//                DryingActivity.HttpRequestTask httpRequestTask = new DryingActivity.HttpRequestTask();
                DryingActivity.HttpRequestTask requestTask = new DryingActivity.HttpRequestTask(myServer.getMoistAddress());
                requestTask.execute(activityState);
            }
        });
        button_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerSettingDialog bottomSheet = new ServerSettingDialog();
                bottomSheet.show(getSupportFragmentManager(), "addDiary");
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
                        buttonStart.setText("?????? ???");
                        buttonStart.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.navigation_color_4));
                        buttonStart.setEnabled(true);

                    } else {
                        buttonStart.setText("??????");
                        buttonStart.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.progress_blue));

                    }
                }
            }
        });

    }

    void checkRunTimePermission() {
        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)

            // 3.  ?????? ?????? ????????? ??? ??????

        } else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.
            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Toast.makeText(MainActivity.this, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }
}
/** END OF CODE **/

//    // wifi ????????? ???????????? ???????????? ???????????? ??? ?????? ??? ??????
//    private final BroadcastReceiver wifiEventReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context_, Intent intent) {
//            String action = intent.getAction();
//            if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
//                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
//                if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
//                    Toast.makeText(context_, "WIFI ?????? ???????????????.", Toast.LENGTH_SHORT).show();
//                    buttonStart.setEnabled(true);
//
//                } else {
//                    Toast.makeText(context_, "WIFI ?????? ????????? ?????? ????????????.", Toast.LENGTH_SHORT).show();
//                    buttonStart.setEnabled(false);
//                }
//            }
//        }
//    };


//final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                    builder.setTitle("???????????? ????????? ????????????");
//                            builder.setMessage("??????????????? ???????????? ?????? ???????????? ???????????? ??? ?????????!");
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
