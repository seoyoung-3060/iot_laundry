package com.example.iot_laundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {
    //현재시간&날짜 가져오기
    long now; //ll
    Date Date;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //dateFormat바꿈
    //date와 time String으로 가져오기
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH00");     //HHmm이었던거 HH00으로 바꿈
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    TextView dateTextView;
    TextView weatherTextView;

    private GPSTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    //excel x,y
    private String x="", y="", address = "";
    private String date = "", time = "", total_date="";

    private String TAG = "MainActivitylog";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateTextView = (TextView)findViewById(R.id.textViewTime);

        weatherTextView = (TextView)findViewById(R.id.textViewWeather);

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
        }

        final TextView textViewLocation = (TextView)findViewById(R.id.textViewLocation);
        Button buttonShowLocation = (Button) findViewById(R.id.reset_button);

        buttonShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 시간 및 날짜 얻어오기
                now = System.currentTimeMillis(); //현재 시간 가져오기
                Date = new Date(now);             //Date 생성하기

                total_date = mFormat.format(Date);
                dateTextView.setText(total_date);

                time = timeFormat.format(Date);
                date = dateFormat.format(Date);

                Log.i("total_date",total_date);
                Log.i("time",time);
                Log.i("date",date);

                // GPS 위치 얻어오기
                gpsTracker = new GPSTracker(MainActivity.this);
                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                address = getCurrentAddress(latitude, longitude);
                Log.i("address",address);

                String[] local = address.split(" "); //local[0]==대한민국 local[1]==부산광역시 local[2]==금정구 local[3]==장전동
                String localName = local[2];//'구'이름 불러옴

                readExcel(localName); //행정시 이름으로 격자값 구하기
                Toast.makeText(MainActivity.this, localName, Toast.LENGTH_LONG).show();

                Log.i("localname",localName);

                String weather = "";
                WeatherData weatherData = new WeatherData();
                try {
                    weather = weatherData.lookUpWeather(date, time, x, y);

                    Log.d(TAG, weather);
                    Log.d(TAG, "널 ㅋ");
                } catch (JSONException e) {
                    Log.i("WEATHER_JSONERROR", e.getMessage());
                } catch (IOException e) {
                    Log.i("WEATHER_IOERROR",e.getMessage());
                }
                Log.i("현재날씨",weather);

                weatherTextView.setText(weather);
                textViewLocation.setText(address);

            }

        });


    }

    private void readExcel(String localName) {
        try {
            InputStream inputStream = getBaseContext().getResources().getAssets().open("local_name.xls");
            Workbook workbook = Workbook.getWorkbook(inputStream); //여기 오류나는듯

            if (workbook != null) {
                Sheet sheet = workbook.getSheet(0); // 시트 불러오기

                if(sheet != null) {
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
            } else Log.d(TAG, "우ㅓ크북 널");
        } catch (IOException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        } catch (BiffException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        }
        Log.i("격자값", "x = " + x + "  y = " + y);
    }


    private String getTime(){
        now = System.currentTimeMillis();
        Date = new Date(now);
        return mFormat.format(Date);
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

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    void checkRunTimePermission(){
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)

            // 3.  위치 값을 가져올 수 있음

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    public String getCurrentAddress( double latitude, double longitude) {
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
        return address.getAddressLine(0).toString()+"\n";
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}

/** END OF CODE **/
//switch (v.getId()){
//        case R.id.reset_button:

