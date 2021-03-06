package com.example.iot_laundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.iot_laundry.Utils.MyServer;
import com.example.iot_laundry.firebase.MyFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DryingActivity extends AppCompatActivity implements View.OnClickListener, SettingDialog.BottomSheetListener {
    //현재시간&날짜 가져오기
    long now; //ll
    Date Date;

    TextView dateTextView, weatherTextView, rainTextView, humidityTextView, adviceTextView, textViewPercent, textViewLocation;
    ImageButton button_setting;

    private GPSTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    //excel x,y
    private String x = "", y = "", address = "";
    private String date = "", time = "", total_date = "";

    private String TAG = "DryingActivityLog";


    SwitchCompat switch_curtain, switch_window, switch_ac;

    ProgressBar progressBar;

    static long startMoist = 0;

    MyServer myServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drying);

//        showCurrentTime();

        initView();
        //토글버턴 상태설정이안됨..
//        toggleAC.setChecked(true);toggleAC.setSelected(true);
//        toggleWindow.setChecked(false);toggleWindow.setSelected(false);
//        toggleCurtain.setChecked(true);toggleCurtain.setSelected(true);

        restore(); //토글버튼 상태 복원을 원했으나 안됨 ㅡㅡ

        createNotificationChannel();
//        sendNotification();

        showWeather();

        MyFirebase.startMoistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                startMoist = (Long) dataSnapshot.getValue();
//                startMoist = (int) startMoistLong;
                if (startMoist > 4) {
                    progressBar.setMax((int) startMoist);
                }

                boolean bool;
                if (startMoist == 0) {
                    bool = true;
                } else bool = false;

                switch_ac.setChecked(false);
                switch_curtain.setChecked(false);
                switch_window.setChecked(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        MyFirebase.moistRef.addValueEventListener(new ValueEventListener() {
            long moist_old = 0, moist_new;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "moistRef onDataChange");
                moist_new = (Long) dataSnapshot.getValue();

                Log.d(TAG, "moist_new: " + moist_new);
                Log.d(TAG, "moist_old: " + moist_old);
                Log.d(TAG, "startMoist: " + startMoist);


//                if (startMoist != 0 && (startMoist > moist_new) && (moist_old > moist_new)) { 갑자기 값이 뛰는 경우가 있으므로 세번째껀 추가하지 말아야겠음
                if (startMoist != 0 && (startMoist > moist_new)) {
                    progressBar.setProgress((int) (startMoist - moist_new));
                    Log.d(TAG, String.valueOf(startMoist - moist_new));
                    double percent = Math.round(((double) (startMoist - moist_new) / (double) startMoist) * 100);
                    Log.d(TAG, "percent: " + percent);
                    Log.d(TAG, "percent2: " + (startMoist - moist_new) / startMoist);
                    textViewPercent.setText(String.valueOf(percent));
                }
                moist_old = moist_new;

                //건조완료 시
                if (startMoist > 4 && moist_new < 4) {
                    sendNotification();
                    Log.d(TAG, "건조완료, moist: " + moist_new);
                    startMoist = 0;
                    MyFirebase.startRef.setValue(false); //시스템 가동 중 아님
                    MyFirebase.startMoistRef.setValue(0);
                    textViewPercent.setText("100");
                    progressBar.setProgress((int) startMoist);

                    //건조 완료 시 http
                    HttpRequestTask requestTask2 = new HttpRequestTask(myServer.getCurtainAddress());
                    requestTask2.execute("curtOn");
                    HttpRequestTask requestTask3 = new HttpRequestTask(myServer.getWindowAddress());
                    requestTask3.execute("winOn");
                    HttpRequestTask requestTask1 = new HttpRequestTask(myServer.getButtonAddress());
                    requestTask1.execute("acOn");


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }
    }

    private void initView() {
        progressBar = findViewById(R.id.progressBar);
        switch_ac = findViewById(R.id.switch_ac);
        switch_window = findViewById(R.id.switch_window);
        switch_curtain = findViewById(R.id.switch_curtain);

        button_setting = findViewById(R.id.button_setting);

        dateTextView = (TextView) findViewById(R.id.textViewTime);
        weatherTextView = (TextView) findViewById(R.id.weather);
        rainTextView = (TextView) findViewById(R.id.rain);
        humidityTextView = (TextView) findViewById(R.id.humidity);
        adviceTextView = (TextView) findViewById(R.id.textViewAdvice);
        textViewPercent = (TextView) findViewById(R.id.textViewPercent);
        textViewLocation = findViewById(R.id.textViewLocation);

        switch_ac.setOnClickListener(this);
        switch_curtain.setOnClickListener(this);
        switch_window.setOnClickListener(this);

        button_setting.setOnClickListener(this);

        myServer = new MyServer(getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.switch_ac) {
            Log.d(TAG, "switch_ac버튼 클릭됨");
            boolean isChecked = switch_ac.isChecked();
            MyFirebase.acRef.setValue(isChecked);
            Log.d(TAG, String.valueOf(isChecked));

            String ac_status;
            if (isChecked) ac_status = "acOn";
            else ac_status = "acOff";

            Log.i(TAG, "ac: " + ac_status);
            HttpRequestTask requestTask = new HttpRequestTask(myServer.getButtonAddress());
            requestTask.execute(ac_status);
        } else if (id == R.id.switch_curtain) {
            boolean isChecked = switch_curtain.isChecked();
            MyFirebase.curtRef.setValue(isChecked);
            Log.d(TAG, "ㅋㅋㅋ");

            String curtain_status;
            if (isChecked) curtain_status = "curtOn";
            else curtain_status = "curtOff";

            HttpRequestTask requestTask = new HttpRequestTask(myServer.getCurtainAddress());
            requestTask.execute(curtain_status);
        } else if (id == R.id.switch_window) {
            boolean isChecked = switch_window.isChecked();
            MyFirebase.winRef.setValue(isChecked);

            String window_status;
            if (isChecked) window_status = "winOn";
            else window_status = "winOff";

            HttpRequestTask requestTask = new HttpRequestTask(myServer.getWindowAddress());
            requestTask.execute(window_status);
        } else if (id == R.id.button_setting) {
            SettingDialog bottomSheet = new SettingDialog();
            bottomSheet.show(getSupportFragmentManager(), "addDiary");
        }
    }

    private void restore() {
        MyFirebase.acRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase ac ", String.valueOf(task.getResult().getValue())); //값은 잘 불러와지는데
                    boolean acVal = (boolean) task.getResult().getValue();
                    switch_ac.setChecked(acVal);
                }
            }
        });
        MyFirebase.winRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase win", String.valueOf(task.getResult().getValue()));
                    boolean winVal = (boolean) task.getResult().getValue();
                    switch_window.setChecked(winVal);

                }
            }
        });
        MyFirebase.curtRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase curt", String.valueOf(task.getResult().getValue()));
                    boolean acVal = (boolean) task.getResult().getValue();
                    switch_curtain.setChecked(acVal);
                }
            }
        });
//        View.requestLayout();
    }

    private void showWeather() {
        // 현재 시간 및 날짜 얻어오기
        now = System.currentTimeMillis(); //현재 시간 가져오기
        Date = new Date(now);             //Date 생성하기

        // GPS 위치 얻어오기
        gpsTracker = new GPSTracker(DryingActivity.this);
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        address = getCurrentAddress(latitude, longitude);
        Log.i("address", address);

        String[] local = address.split(" "); //local[0]==대한민국 local[1]==부산광역시 local[2]==금정구 local[3]==장전동
        String localName = local[2];//'구'이름 불러옴
        String addressStr = "";
        for (int i = 1; i < local.length; i++) {
            addressStr += local[i] + " ";
        }

        readExcel(localName); //행정시 이름으로 격자값 구하기
        Toast.makeText(DryingActivity.this, localName, Toast.LENGTH_LONG).show();

        Log.i("localname", localName);

        String weather = "";
        WeatherData weatherData = new WeatherData();
        try {
            weather = weatherData.lookUpWeather(Date, x, y, DryingActivity.this);
            Log.d(TAG, weather);
        } catch (JSONException e) {
            Log.i("WEATHER_JSONERROR", e.getMessage());
        } catch (IOException e) {
            Log.i("WEATHER_IOERROR", e.getMessage());
        }
        Log.i("현재날씨", weather);

        textViewLocation.setText(addressStr);
    }

    private void readExcel(String localName) {
        try {
            InputStream inputStream = getBaseContext().getResources().getAssets().open("local_name.xls");
            Workbook workbook = Workbook.getWorkbook(inputStream);

            if (workbook != null) {
                Sheet sheet = workbook.getSheet(0); // 시트 불러오기
                if (sheet != null) {
                    int colTotal = sheet.getColumns(); //전체 칼럼
                    int rowIndexStart = 1;             //row 인덱스 시작
                    int rowTotal = sheet.getColumn(colTotal - 1).length;

                    for (int row = rowIndexStart; row < rowTotal; row++) {
                        String contents = sheet.getCell(0, row).getContents();
                        if (contents.contains(localName)) {
                            x = sheet.getCell(2, row).getContents();
                            y = sheet.getCell(3, row).getContents();
                            row = rowTotal;
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        } catch (BiffException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        }
        Log.i("격자값", "x = " + x + "  y = " + y);
    }

    // ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                //위치 값을 가져올 수 있음
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(DryingActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(DryingActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    void checkRunTimePermission() {
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(DryingActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(DryingActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)

            // 3.  위치 값을 가져올 수 있음

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(DryingActivity.this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(DryingActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(DryingActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(DryingActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    public String getCurrentAddress(double latitude, double longitude) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0);
//        return address.getAddressLine(0).toString()+"\n";
        return address.getAddressLine(0).toString();
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DryingActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
//        restore();
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    String channelId = "one-channel";
    String channelName = "My Channel One1";
    String channelDescription = "My Channel One Description";

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification() {
        Intent intent = new Intent(this, DryingCompleteActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("AnotherActivity", TrueOrFalse);
//        intent.putExtra("imageUri", imageUri);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);


//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "one-channel")
                .setSmallIcon(R.drawable.laundrydrying)
//                .setSmallIcon(IconCompat.createWithBitmap(image))
//                .setLargeIcon(image) //not work
//                .setContentTitle(messageBody)
                .setContentTitle("건조가 완료되었습니다")
                .setContentText("건조완료")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setStyle(new NotificationCompat.BigPictureStyle()
//                        .bigPicture(image))/*Notification with Image*/
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onButtonApply() {
        restore();
    }


    public static class HttpRequestTask extends AsyncTask<String, Void, String> {
        private String serverAddress;// = MyServer.serverAddress;
        private String TAG = "HttpRequestTask";

        public HttpRequestTask(String serverAdress) {
            this.serverAddress = serverAdress;
        }

        public HttpRequestTask() {
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground호출");
            Log.d(TAG, "serverAdress: " + serverAddress);

            String val = params[0];
            final String url = "http://" + serverAddress + "/" + val;
            Log.d(TAG, "url: " + url);

            //okHttp 라이브러리를 사용한다.
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                Log.d(TAG, "반응");
                Log.d(TAG, String.valueOf(response));
                Log.d(TAG, String.valueOf(response.body()));
                return null;
            } catch (IOException e) { //이부분호출됨
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }


    }

    // 액티비티 나갔다오면 변수 초기화 되므로..
    @Override
    protected void onResume() {
        super.onResume();
//        MyFirebase.startMoistRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DataSnapshot> task) {
//                    Log.d(TAG, "startMoist: " + String.valueOf(task.getResult().getValue()));
//
//                    //파이어베이스에 값이 적어진 뒤 한참 후에 메소드가 호출되지만 그래도 ㄱㅊ은듯
//                    startMoist = (Long) task.getResult().getValue();
////                    progressBar.setMax((int) startMoist);
//                }
//            });
    }
}


/** END OF CODE **/

//    private void showCurrentTime() {
//        dateTextView = (TextView)findViewById(R.id.textViewTime);
//        SimpleDateFormat mFormat = new SimpleDateFormat("M월 d일 H시 mm분"); //dateFormat바꿈
////        timeText =  findViewById(R.id.textView_time);
//        new Thread(new Runnable() { //실시간으로 시계출력하기 위한 thread
//            @Override
//            public void run() {
//
//                while (true) {
//                    try {
//                        runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//
//                                now = System.currentTimeMillis(); //현재 시간 가져오기
//                                Date = new Date(now);             //Date 생성하기
//
//                                String total_date = mFormat.format(Date);
//                                dateTextView.setText(total_date);
//
//
//                            }
//                        });
//
////                        Calendar calendar = Calendar.getInstance();
////                        dateTextView.setText("오전 " + calendar.get(Calendar.HOUR_OF_DAY) + "시 " + calendar.get(Calendar.MINUTE) + "분 " + calendar.get(Calendar.SECOND) + "초");
//
////                        now = System.currentTimeMillis(); //현재 시간 가져오기
////                        Date = new Date(now);             //Date 생성하기
////
////                        String total_date = mFormat.format(Date);
////                        dateTextView.setText(total_date);
////
//                        Thread.sleep(60000); //1분
//                    } catch (InterruptedException ex) {}
//                }
//            }
//        }).start(); // 실시간으로 시계 출력
//    }
//
//}


//switch (v.getId()){
//        case R.id.reset_button:

//안쓰길래
//private String getTime(){
//    now = System.currentTimeMillis();
//    Date = new Date(now);
//    return mFormat.format(Date);
//}

//온체크드체인지리스너
// ac_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
//@Override
//public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        String ac_status;
//        if (isChecked){
//        ac_status = "acOn";
//        Log.i("ac","acon");
//        } else {
//        ac_status = "acOff";
//        Log.i("ac","acoff");
//        }
//        HttpRequestTask requestTask = new HttpRequestTask();
//        requestTask.execute(ac_status);
//        }
//        });
//        curtain_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
//@Override
//public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        String curtain_status;
//        if (isChecked){
//        curtain_status = "cOn";
//        } else {
//        curtain_status = "cOff";
//        }
//        HttpRequestTask requestTask = new HttpRequestTask();
//        requestTask.execute(curtain_status);
//        }
//        });
//        window_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
//@Override
//public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        String window_status;
//        if (isChecked){
//        window_status = "winOn";
//        } else {
//        window_status = "winOff";
//        }
//        HttpRequestTask requestTask = new HttpRequestTask();
//        requestTask.execute(window_status);
//        }
//        });