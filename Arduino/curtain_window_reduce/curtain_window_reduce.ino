///https://www.javatpoint.com/iot-project-google-firebase-nodemcu
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

#define WINDOW 0
#define CURTAIN 1
#define ANTICLOCK 0
#define CLOCK 1
//
//#define WIFI_SSID "KT_GiGA_2G_sumin"
//#define WIFI_PASSWORD "sumin78900"
//IPAddress ip(172, 30, 1, 110); // 사용할 IP 주소
//IPAddress gateway(172, 30, 1, 254); // 게이트웨이 주소
//IPAddress subnet(255, 255, 255, 0); // 서브넷 주소

//핫스팟
#define WIFI_SSID "winterz"
#define WIFI_PASSWORD "201105166"
IPAddress ip(192, 168, 120, 110); // 사용할 IP 주소
IPAddress gateway(192, 168, 120, 34); // 게이트웨이 주소/IPAddress subnet(255, 255, 255, 0); // 서브넷 주소
IPAddress subnet(255, 255, 255, 0); // 서브넷 주소

//핫스팟
//#define WIFI_SSID "iPhone"
//#define WIFI_PASSWORD "63113515"
//IPAddress ip(172, 20, 10, 110); // 사용할 IP 주소
//IPAddress gateway(172, 20, 10, 1); // 게이트웨이 주소
//IPAddress subnet(255, 255, 255, 240); // 서브넷 주소


WiFiServer server(80); //추가

//위모스 D1용 핀배열, window 핀
int IN1 = D8;                      // IN1핀을 8번에 배선합니다.
int IN2 = D9;                      // IN2핀을 9번에 배선합니다.
int IN3 = D10;                    // IN3핀을 10번에 배선합니다.
int IN4 = D11;                    // IN4핀을 11번에 배선합니다.

//위모스 D1용 핀배열, curtain 핀
int IN5 = D4;                      // IN1핀을 8번에 배선합니다.
int IN6 = D5;                      // IN2핀을 9번에 배선합니다.
int IN7 = D6;                    // IN3핀을 10번에 배선합니다.
int IN8 = D7;                    // IN4핀을 11번에 배선합니다.


int motorSpeed = 1200;     // 스텝모터의 속도를 정할 수 있습니다. 원본 1200, 800안됨 1000됨 
// 스텝을 카운트하여 얼마나 회전했는지 확인할 수 있습니다.
#define COUNT_WINDOW 512
#define COUNT_CURTAIN 1536

int lookup[8] = {B01000, B01100, B00100, B00110, B00010, B00011, B00001, B01001};
// 스텝모터를 제어할 방향의 코드를 미리 설정합니다.
boolean curtain_state = false;
boolean window_state = false;

void setup() {
  Serial.begin(9600);
  pinMode(IN1, OUTPUT);    // IN1을 출력핀으로 설정합니다.
  pinMode(IN2, OUTPUT);    // IN2을 출력핀으로 설정합니다.
  pinMode(IN3, OUTPUT);    // IN3을 출력핀으로 설정합니다.
  pinMode(IN4, OUTPUT);    // IN4을 출력핀으로 설정합니다.

  pinMode(IN5, OUTPUT);    // IN1을 출력핀으로 설정합니다.
  pinMode(IN6, OUTPUT);    // IN2을 출력핀으로 설정합니다.
  pinMode(IN7, OUTPUT);    // IN3을 출력핀으로 설정합니다.
  pinMode(IN8, OUTPUT);    // IN4을 출력핀으로 설정합니다.

  // connect to wifi.
  WiFi.config(ip, gateway, subnet);
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

void open(int flag) {
  int count = 0;
  int direction;
  Serial.println("open()호출");
  int countsperrev;
  if (flag == WINDOW) {
    window_state = true;
    countsperrev = COUNT_WINDOW;
//    Firebase.setBool("/window", true); //써지질 않음 
    direction = ANTICLOCK;
    
  } else if (flag == CURTAIN) {
    curtain_state = true;
    countsperrev = COUNT_CURTAIN;
//    Firebase.setBool("/curtain", curtain_state);
    direction = CLOCK;
  }

  while (count < countsperrev) {                 // count가 countsperrev 보다 작으면
    yield(); //Soft WDT reset 에러 없애기 위해 추가
    Serial.print("countsperrev, count: ");
    Serial.println((String) countsperrev + ", " + count);
//    anticlockwise(flag);
    rotate(flag, direction);
    count++;// anticlockwise()함수를 실행합니다.
  }
}

void close(int flag) {
  int count = 0;
  Serial.println("close()호출");
  int countsperrev, direction;
  if (flag == WINDOW) {
    window_state = false;
    countsperrev = COUNT_WINDOW;
//    Firebase.setBool("/window", false); //왜안되노...
    direction = CLOCK;
  } else if (flag == CURTAIN) {
    curtain_state = false;
    countsperrev = COUNT_CURTAIN;
//    Firebase.setBool("/curtain", false);
    direction = ANTICLOCK;
  }

  while (count < countsperrev) {                 // count가 countsperrev 보다 작으면
    yield(); //Soft WDT reset 에러 없애기 위해 추가
    Serial.print("countsperrev, count: ");
    Serial.println((String) countsperrev + ", " + count);
//    clockwise(flag);
    rotate(flag, direction);
    count++;// clockwise()함수를 실행합니다.
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

  if (request.indexOf("/curtOn") > 0) {
    open(CURTAIN);
  }
  else if (request.indexOf("/curtOff") > 0) {
    close(CURTAIN);
  }
  else if (request.indexOf("/winOn") > 0) {
    open(WINDOW);
  }
  else if (request.indexOf("/winOff") > 0) {
    close(WINDOW);
  }
}

void loop() {
  clientcall();
  while (true) {
    Serial.print("건조완료대기중 ");
    Serial.println(WiFi.localIP());
    clientcall();
    if (Firebase.getInt("moist") < 4 && Firebase.getInt("startMoist") > 4) {
      Serial.println((String) "moist: " + Firebase.getInt("moist"));
      Serial.println("건조완료");
      if (curtain_state)  close(CURTAIN);
      if (window_state)   close(WINDOW);
    }
  }
}

void rotate(int flag, int direction) {
  if (direction == CLOCK) {
      for (int i = 0; i < 8; i++) {                                    // 8번 반복합니다.
        setOutput(i, flag);                                                     //  setOutput() 함수에 i 값을 넣습니다 (0~7)
        delayMicroseconds(motorSpeed);                      // 모터 스피드만큼 지연합니다.
      }
  } else if (direction == ANTICLOCK) {
      for (int i = 7; i >= 0; i--) {                                    // 8번 반복합니다.
        setOutput(i, flag);                                                    // setOutput() 함수에 i 값을 넣습니다 (7~0)
        delayMicroseconds(motorSpeed);                      // 모터 스피트만큼 지연합니다.
      }
  }
}

//void anticlockwise(int flag) {
//  for (int i = 0; i < 8; i++) {                                    // 8번 반복합니다.
//    setOutput(i, flag);                                                     //  setOutput() 함수에 i 값을 넣습니다 (0~7)
//    delayMicroseconds(motorSpeed);                      // 모터 스피드만큼 지연합니다.
//  }
//}
//void clockwise(int flag) {
//  for (int i = 7; i >= 0; i--) {                                    // 8번 반복합니다.
//    setOutput(i, flag);                                                    // setOutput() 함수에 i 값을 넣습니다 (7~0)
//    delayMicroseconds(motorSpeed);                      // 모터 스피트만큼 지연합니다.
//  }
//}

void setOutput(int out, int flag) {
  if (flag == WINDOW) {
    digitalWrite(IN1, bitRead(lookup[out], 0));             // IN1에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
    digitalWrite(IN2, bitRead(lookup[out], 1));             // IN2에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
    digitalWrite(IN3, bitRead(lookup[out], 2));             // IN3에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
    digitalWrite(IN4, bitRead(lookup[out], 3));             // IN4에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
  } else if (flag == CURTAIN) {
    digitalWrite(IN5, bitRead(lookup[out], 0));             // IN1에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
    digitalWrite(IN6, bitRead(lookup[out], 1));             // IN2에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
    digitalWrite(IN7, bitRead(lookup[out], 2));             // IN3에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
    digitalWrite(IN8, bitRead(lookup[out], 3));             // IN4에 함수로부터 입력받은 out값을 넣어 모터를 제어합니다.
  }
}
