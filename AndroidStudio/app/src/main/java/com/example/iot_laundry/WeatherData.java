package com.example.iot_laundry;

import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class WeatherData {
    private String weather = "", rain = "", reh = "";
    private String TAG = "WeatherDatalog";

    public String lookUpWeather(String baseDate, String time, String nx, String ny, TextView weatherTextView, TextView rainTextView, TextView humidityTextView, TextView adviceTextView) throws IOException, JSONException {
//        String baseDate = date;
        String baseTime = timeChange(time);
        String type = "json";

        Log.i(TAG,"time: " + time);
        Log.i(TAG,"baseDate: " + baseDate);
        Log.i(TAG,"baseTime: " + baseTime);
        Log.i(TAG,"nx: " + nx);
        Log.i(TAG,"ny: " + ny);

        String apiURL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
        String serviceKey = "cU7%2F%2FBtN5Kb66G%2BYYW61iwedrB0vlroCHcTP5v60uNO2jHJRnu%2BwCDqz3ZKllb%2Buzgw1XJLa99yxDNMzuNxEkw%3D%3D";

        StringBuilder urlBuilder = new StringBuilder(apiURL);
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8")); //경도
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8")); //위도
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8")); /* 조회하고싶은 날짜*/
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8")); /* 조회하고싶은 시간 AM 02시부터 3시간 단위 */
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));    /* 타입 */

        final String[] result = new String[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlBuilder.toString());
                    Log.i("url주소", String.valueOf(url));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-type", "application/json");

                    Log.d(TAG, String.valueOf(conn.getResponseCode()));
                    BufferedReader bufferedReader;// = new BufferedReader(reader);
                    if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                        bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    } else {
                        bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    }

                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    Log.d(TAG, "결과2:"+ sb.toString());

                    bufferedReader.close();
                    conn.disconnect();

                    String result = sb.toString();

                    // response 키를 가지고 데이터를 파싱
                    JSONObject jsonObj_1 = new JSONObject(result);
                    String response = jsonObj_1.getString("response");

                    // response 로 부터 body 찾기
                    JSONObject jsonObj_2 = new JSONObject(response);
                    String body = jsonObj_2.getString("body");

                    // body 로 부터 items 찾기
                    JSONObject jsonObj_3 = new JSONObject(body);
                    String items = jsonObj_3.getString("items");
                    Log.i("ITEMS", items);

                    // items로 부터 itemlist 를 받기
                    JSONObject jsonObj_4 = new JSONObject(items);
                    JSONArray jsonArray = jsonObj_4.getJSONArray("item");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObj_4 = jsonArray.getJSONObject(i);
                        String fcstValue = jsonObj_4.getString("fcstValue");
                        String category = jsonObj_4.getString("category");

                        if (category.equals("SKY")) {
                            weather = "현재 날씨: ";
                            if (fcstValue.equals("1")) {
                                weather += "맑음 ";
                                adviceTextView.setText("오늘은 맑네요! 창문/커튼을 활짝열어볼까요!");
                            } else if (fcstValue.equals("2")) {
                                weather += "비 ";
                                adviceTextView.setText("오늘은 비가오네요! 창문/커튼을 닫고 에어컨켜는것을 추천드려요");
                            } else if (fcstValue.equals("3")) {
                                weather += "구름많음 ";
                                adviceTextView.setText("오늘은 구름이 많네요! 창문열어도 괜찮을것같아요");
                            } else if (fcstValue.equals("4")) {
                                weather += "흐림 ";
                                adviceTextView.setText("오늘은 흐리네요! 창문열어도 괜찮을것같아요");
                            }
                        }

                        if (category.equals("POP")) {
                            rain = fcstValue + "%";
                        }

                        if (category.equals("REH")) {
                            reh = fcstValue + "%";
                        }

                        Log.i("날씨", fcstValue);
                        Log.i("카테고리", category);
                        Log.i("현재날씨", weather+rain+reh);
                        weatherTextView.setText(weather);
                        rainTextView.setText(rain);
                        humidityTextView.setText(reh);
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

