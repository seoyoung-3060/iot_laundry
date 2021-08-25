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

//서보모터 관련
#include <Servo.h>
Servo myservo;

int SERVOPIN = 11;
int angle = 0;

void setup() {
  Serial.begin(9600);
  myservo.attach(SERVOPIN);

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

}
boolean on = false;

void buttonControl() {
  on = !on;
  Serial.println(on); //버튼 on, off 유무 시리얼 모니터에 출력
  for (angle = 0; angle < 110; angle++)
  {
    myservo.write(angle);
    delay(15);
  }
  // now scan back from 180 to 0 degrees
  for (angle = 110; angle > 0; angle--)
  {
    myservo.write(angle);
    delay(15);
  }
}

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

        /** 클라이언트(앱) 과 연결 됐다면, **/
      if (Firebase.getFloat("moist") < 5) {
        Serial.println("건조가 완료되었습니다");   //디버깅용
        buttonControl(); //서보모터 제어 함수
        return;
      } 

    if(request.indexOf("/acOn")>0) {
      buttonControl();
      else if(request.indexOf("/acOff")>0) {
        buttonControl();
      }
      else {
        delay(2000); //테스트용, 2초 마다 파이어베이스의 moist값 읽음
        //    delay(500000); //시현용, 5분
      }
    }
    
  }
