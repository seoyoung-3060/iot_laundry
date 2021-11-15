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

//#define WIFI_SSID "SK_WiFiGIGA2B95"
//#define WIFI_PASSWORD "1603064717"
//#define WIFI_SSID "winterz"
//#define WIFI_PASSWORD "201105166"
#define WIFI_SSID "KT_GiGA_2G_sumin"
#define WIFI_PASSWORD "sumin78900"

WiFiServer server(80); //추가

////stepmotor 02
//int IN1 = 4;                      // IN1핀을 8번에 배선합니다.
//int IN2 = 5;                      // IN2핀을 9번에 배선합니다.
//int IN3 = 6;                    // IN3핀을 10번에 배선합니다.
//int IN4 = 7;                    // IN4핀을 11번에 배선합니다.

//stepmotor 01
int IN5 = D8;                      // IN1핀을 8번에 배선합니다.
int IN6 = D9;                      // IN2핀을 9번에 배선합니다.
int IN7 = D10;                    // IN3핀을 10번에 배선합니다.
int IN8 = D11;                    // IN4핀을 11번에 배선합니다.

int motorSpeed = 1200;     // 스텝모터의 속도를 정할 수 있습니다.
// 스텝을 카운트하여 얼마나 회전했는지 확인할 수 있습니다.
int countsperrev = 512;     // 최대 카운트를 512로 설정합니다.
int lookup[8] = {B01000, B01100, B00100, B00110, B00010, B00011, B00001, B01001};
// 스텝모터를 제어할 방향의 코드를 미리 설정합니다.
boolean window_state = false;

void setup() {

  Serial.begin(9600);

  //  pinMode(IN1, OUTPUT);    // IN1을 출력핀으로 설정합니다.
  //  pinMode(IN2, OUTPUT);    // IN2을 출력핀으로 설정합니다.
  //  pinMode(IN3, OUTPUT);    // IN3을 출력핀으로 설정합니다.
  //  pinMode(IN4, OUTPUT);    // IN4을 출력핀으로 설정합니다.

  pinMode(IN5, OUTPUT);    // IN1을 출력핀으로 설정합니다.
  pinMode(IN6, OUTPUT);    // IN2을 출력핀으로 설정합니다.
  pinMode(IN7, OUTPUT);    // IN3을 출력핀으로 설정합니다.
  pinMode(IN8, OUTPUT);    // IN4을 출력핀으로 설정합니다.

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

void windowclose() {
  int count = 0;
    window_state = false;
  while (count < countsperrev ) {                 // count가 countsperrev 보다 작으면
    Serial.println(count);
//    Serial.println(countsperrev);
    clockwise();
    count++;// clockwise()함수를 실행합니다.
  }
}

void windowopen() {
  int count = 0;
  window_state = true;
  while (count < countsperrev) {
    Serial.println(count);
//    Serial.println(countsperrev);
    anticlockwise();
    count++;
  }
}
void clientcall() {
  /** 클라이언트(앱) 처리. 연결 안됐으면 return; **/
  WiFiClient client = server.available();
  if (!client) {
    return;
  }
  Serial.println("앱으로부터 연결 완료, 건조 시스템 시작");
  while (!client.available()) {
    delay(1);
  }
  String request = client.readStringUntil('\r');
  Serial.println(request);
  client.flush();

  if (request.indexOf("/winOn") > 0) {
    windowopen();
  }
  else if (request.indexOf("/winOff") > 0) {
    windowclose();
  }
}

void loop() {
  Serial.println("loop");

  clientcall();
  while (true) {
    Serial.println("건조완료대기중");
    clientcall();
    if (Firebase.getInt("moist") < 4) {
      Serial.println("건조완료");
      if (window_state) {
        windowclose();
      }
    }
  }

}

void anticlockwise()
{
  for (int i = 0; i < 8; i++)                                          // 8번 반복합니다.
  {
    setOutput(i);                                                     //  setOutput() 함수에 i 값을 넣습니다 (0~7)
    delayMicroseconds(motorSpeed);                      // 모터 스피드만큼 지연합니다.
    yield();
  }
}
void clockwise()
{
  for (int i = 7; i >= 0; i--)                                       // 8번 반복합니다.
  {
    setOutput(i);                                                    // setOutput() 함수에 i 값을 넣습니다 (7~0)
    delayMicroseconds(motorSpeed);                      // 모터 스피트만큼 지연합니다.
    yield();
  }
}
void anticlockwise2()
{
  int i = 0;
  while ( i < 8) {
    yield();
    setOutput(i);                                                     //  setOutput() 함수에 i 값을 넣습니다 (0~7)
    delayMicroseconds(motorSpeed);                      // 모터 스피드만큼 지연합니다.
    i++;
  }
}
void clockwise2()
{
  int i = 7;
  while (i >= 0) {
    yield();
    setOutput(i);                                                    // setOutput() 함수에 i 값을 넣습니다 (7~0)
    delayMicroseconds(motorSpeed);
    i--;
  }
}

void setOutput(int out)
{
  //  digitalWrite(IN1, bitRead(lookup[out], 0));             // IN1에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
  //  digitalWrite(IN2, bitRead(lookup[out], 1));             // IN2에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
  //  digitalWrite(IN3, bitRead(lookup[out], 2));             // IN3에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
  //  digitalWrite(IN4, bitRead(lookup[out], 3));             // IN4에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.

  digitalWrite(IN5, bitRead(lookup[out], 0));             // IN1에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
  digitalWrite(IN6, bitRead(lookup[out], 1));             // IN2에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
  digitalWrite(IN7, bitRead(lookup[out], 2));             // IN3에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
  digitalWrite(IN8, bitRead(lookup[out], 3));             // IN4에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.

}
