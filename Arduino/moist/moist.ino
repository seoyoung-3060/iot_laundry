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
//
////핫스팟
#define WIFI_SSID "winterz"
#define WIFI_PASSWORD "201105166"
IPAddress ip(192, 168, 120, 90); // 사용할 IP 주소
IPAddress gateway(192, 168, 120, 34); // 게이트웨이 주소
IPAddress subnet(255, 255, 255, 0); // 서브넷 주소

//핫스팟
//#define WIFI_SSID "iPhone"
//#define WIFI_PASSWORD "63113515"
//IPAddress ip(172, 20, 10, 90); // 사용할 IP 주소
//IPAddress gateway(172, 20, 10, 1); // 게이트웨이 주소
//IPAddress subnet(255, 255, 255, 240); // 서브넷 주소

int MOISTPIN = A0;

WiFiServer server(80); //추가

void setup() {
  Serial.begin(9600);

  // connect to wifi.
  WiFi.config(ip, gateway, subnet); // before or after Wifi.Begin(ssid, password);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("connecting");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println();
  Serial.print("connected: ");
  Serial.println(WiFi.localIP());

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);

  //server
  server.begin();
  Serial.println("Server started");
  Serial.print("URL to connect: ");
  Serial.print("http://");
  Serial.print(WiFi.localIP());
  Serial.print("/");
}

void loop() {
  Serial.println(WiFi.localIP());
  Serial.println(analogRead(MOISTPIN));

  /** 클라이언트(앱) 처리. 연결 안됐으면 return; **/
  Serial.println("요청 기다리는 중");
  WiFiClient client = server.available(); //클라이언트와 연결됐는지 확인
  if (!client) {
    return;
  }
  Serial.println("앱으로부터 연결 완료, 건조 시스템 시작");
  while (!client.available()) {  //클라이언트가 데이터를 보낼때까지 기다림
    delay(1);
  }
  String request = client.readStringUntil('\r');
  Serial.println(request);
  client.flush();
  /** **/

  if (request.indexOf("/start") > 0) {
    //  digitalWrite(ledPin, HIGH);
  }

  int startMoist = analogRead(MOISTPIN);
  Firebase.setInt("startMoist", startMoist);

  // 디버깅용, 시리얼 모니터에 출력
  Serial.print("startMoist value: ");
  Serial.println(startMoist);

  /** 클라이언트(앱) 과 연결된 후, **/
  while (true) {
    // soil sensor
    delay(2000);  //2초 단위, 테스트용
    //  delay(20000);   //2분 단위, 시현용

    int moist = analogRead(MOISTPIN);

    // 디버깅용, 시리얼 모니터에 출력
    Serial.print("moist value: ");
    Serial.println(moist);

    // db에 값 적기
    Firebase.setInt("moist", moist);
    // handle error
    if (Firebase.failed()) {
      Serial.print("setting /number failed:");
      Serial.println(Firebase.error());
      return;
    }
    delay(1000);

    //건조 완료되면 프로그램 종료
    if (Firebase.getFloat("moist") < 4) {
      Serial.println("건조가 완료되었습니다");   //디버깅용
      Firebase.setInt("startMoist", 0);
      return;
    }
  }

}
