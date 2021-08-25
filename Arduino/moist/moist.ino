//https://www.javatpoint.com/iot-project-google-firebase-nodemcu
#include <Firebase.h>  
#include <FirebaseArduino.h>  
#include <FirebaseCloudMessaging.h>  
#include <FirebaseError.h>  
#include <FirebaseHttpClient.h>  
#include <FirebaseObject.h>  
  
// FirebaseDemo_ESP8266 is a sample that demo the different functions  
// of the FirebaseArduino API.  
  
#include <ESP8266WiFi.h>  
#include <FirebaseArduino.h>  
  
// Set these to run example.  
#define FIREBASE_HOST "iot-laundry02-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "qlNCOwAypMDI1bjWPus5Szvs32lTDu1EDkRqEqiy"

#define WIFI_SSID "SK_WiFiGIGA2B95"  
#define WIFI_PASSWORD "1603064717"

WiFiServer server(80); //추가

int MOISTPIN = A0;

void setup() {  
  Serial.begin(9600);  
  
  // connect to wifi.  
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);  
  Serial.print("connecting");  
  while (WiFi.status() != WL_CONNECTED) {  
    Serial.print(".");  
    delay(500);  
  }  
  Serial.println();  
  Serial.print("connected: ");  
  Serial.println(WiFi.localIP());

  //server
  server.begin();
  Serial.println("Server started");
  Serial.print("URL to connect: ");
  Serial.print("http://");
  Serial.print(WiFi.localIP());
  Serial.print("/");
    
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);  
}  
  
int n = 0;  
  
void loop() {  
  /** 클라이언트(앱) 처리. 연결 안됐으면 return; **/
  WiFiClient client = server.available();
  if(!client) {
    return;
  }
  Serial.println("앱으로부터 연결 완료, 건조 시스템 시작");
  while(!client.available()){
    delay(1);
  }
  String request = client.readStringUntil('\r');
  Serial.println(request);
  client.flush();

  if(request.indexOf("/start")>0) {
    /** 클라이언트(앱) 과 연결 됐다면, **/
    // soil sensor
    delay(2000);  //2초 단위, 테스트용
  //  delay(20000);   //2분 단위, 시현용
    int moist = analogRead(MOISTPIN);
  
    // 디버깅용, 시리얼 모니터에 출력
    Serial.print("moist value: ");
    Serial.println(moist); 
    
    // db에 값 적기 
    Firebase.setFloat("moist", moist);  
    // handle error  
    if (Firebase.failed()) {  
        Serial.print("setting /number failed:");  
        Serial.println(Firebase.error());    
        return;  
    }  
    delay(1000);  
      
//    //건조 완료되면 프로그램 종료  
//    if (Firebase.getFloat("moist") < 5) { 
//      Serial.println("건조가 완료되었습니다");   //디버깅용
//      return;                         
//    }
  }

}  
