#include <SoftwareSerial.h>
#include <Stepper.h>
#define IN1 8
#define IN2 9
#define IN3 10
#define IN4 11
const int stepsPerRevolution = 200; // 모터의 1회전당 스텝 수에 맞게 조정
Stepper myStepper(stepsPerRevolution, 11,9,10,8);
boolean window_state = false; //false==닫힌상태
boolean rain = false;
boolean humidity = false; //습도 데이터받음 기준치보다 높은지낮은지만 간단하게!

String ssid=""; //wifi ID
String PASSWORD="";
String host = ""; //computer IP
SoftwareSerial mySerial(2,3);

void connectWifi() {
  String cmd = "AT+CWMODE=1";
  mySerial.println(cmd);
  cmd="AT+CWJAP=\""+ssid+"\",\""+PASSWORD+"\"";
  mySerial.println(cmd);
  delay(2000);
  if(mySerial.find("OK")) {
    Serial.println("Wifi Connected!");
  } else {
    Serial.println("Connect Timeout"+ms);
  }
}

void setup() {
  myStepper.setSpeed(60);
  Serial.begin(9600);
}

void anticlockwise() {
  for(int i=0; i<12; i++) {
    myStepper.step(stepsPerRevolution);
    Serial.println(i);
  }
}
void clockwise() {
  for(int i=1; i<12; i++) {
    myStepper.step(-stepsPerRevolution);
    Serial.println(i);
  }
}
void loop() {
  if(
}
