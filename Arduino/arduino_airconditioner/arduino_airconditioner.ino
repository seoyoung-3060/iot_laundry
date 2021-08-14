#include <SoftwareSerial.h>
#include <Servo.h>
#define servoPin
Servo myServo;
SoftwareSerial mySerial(2,3); //TX,RX

String ssid=""; //wifi ID
String PASSWORD="";
String host = ""; //computer IP

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
  Serial.begin(9600);
  mySerial.begin(9600);
  myServo.attach(servoPin);
}

void loop() {
  int angle=0;
  if(mySerial.available()) {
    Serial.write(mySerial.read());
  }
  
}
