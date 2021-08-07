package com.example.iot_laundry;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class WeatherData {
    private String weather = "", rain = "", reh = "";

    public String lookUpWeather(String baseDate, String time, String nx, String ny) throws IOException, JSONException {
//        String baseDate = date;
        String baseTime = timeChange(time);
        String type = "json";

        String apiURL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0";
        String serviceKey = "QK0I%2Bb1xeGcqkwrmsq%2FnDohNQgrpwIviKakibnwqQBCK%2BrvVRYuG9%2Blkv1LTrWZEp3OdIvLiiEehxErd1t%2BGGQ%3D%3D";
        StringBuilder urlBuilder = new StringBuilder(apiURL);
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8")); //경도
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8")); //위도
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8")); /* 조회하고싶은 날짜*/
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8")); /* 조회하고싶은 시간 AM 02시부터 3시간 단위 */
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));    /* 타입 */

        URL url = new URL(urlBuilder.toString());
        Log.i("url주소", String.valueOf(url));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        //해석..
        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        String result = sb.toString();

        //respose 키로 데이터파싱
        JSONObject OBJ1 = new JSONObject(result);
        String response = OBJ1.getString("response");

        //response로 body찾기
        JSONObject OBJ2 = new JSONObject(response);
        String body = OBJ2.getString("body");

        //body로 items찾기
        JSONObject OBJ3 = new JSONObject(body);
        String items = OBJ3.getString("items");
        Log.i("ITEMS", items);

        //items로 itemlist받기
        JSONObject OBJ4 = new JSONObject(result);
        JSONArray jsonArray = OBJ4.getJSONArray("itemlist");

        for(int i=0; i<jsonArray.length(); i++) {
            OBJ4 = jsonArray.getJSONObject(i);
            String fcstValue = OBJ4.getString("fcstValue");
            String category = OBJ4.getString("category");

            if (category.equals("SKY")) {
                weather = "현재 날씨 : ";
                if (fcstValue.equals("1")) {
                    weather += "맑음";
                } else if (fcstValue.equals("2")) {
                    weather += "비";
                } else if (fcstValue.equals("3")) {
                    weather += "구름많음";
                } else if (fcstValue.equals("4")) {
                    weather += "흐림";
                }
            }

            if (category.equals("POP")) {
                rain = "강수확률 : " + fcstValue + "%";
            }

            if (category.equals("REH")) {
                reh = "습도 : " + fcstValue + "%";
            }

            Log.i("날씨", fcstValue);
            Log.i("카테고리", category);
            Log.i("현재날씨", weather+rain+reh);
        }

        return weather + rain + reh;

    }

    private String timeChange(String time) {
        /*기상청 데이터가 3시간마다 업데이트되므로 3시간 단위로 조회하지 않으면 정보없음으로 뜸
        0200 0500 ~ 2300으로 변환해주어야함
         */
        switch (time) {
            case "0200":
            case "0300":
            case "0400":
                time = "0200";
                break;
            case "0500":
            case "0600":
            case "0700":
                time = "0500";
                break;
            case "0800":
            case "0900":
            case "1000":
                time = "0800";
                break;
            case "1100":
            case "1200":
            case "1300":
                time = "1100";
                break;
            case "1400":
            case "1500":
            case "1600":
                time = "1400";
                break;
            case "1700":
            case "1800":
            case "1900":
                time = "1700";
                break;
            case "2000":
            case "2100":
            case "2200":
                time = "2000";
                break;
            default:
                time = "2300";

        }
        return time;
        }
    }

