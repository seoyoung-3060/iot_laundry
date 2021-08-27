#include <ESP8266Firebase.h>
#include <ESP8266WiFi.h>
#include <Stepper.h>

#define FIREBASE_HOST ""
#define FIREBASE_AUTH ""
#define WIFI_SSID ""
#define WIFI_PASSWORD ""

const int stepsPerRevolution = 200; // 360도회전 신호
Stepper myStepper(stepsPerRevolution, 8,9,10,11);
boolean window_state = false; //false==닫힌상태
boolean rain = false;
boolean humidity = false; //습도 데이터받음 기준치보다 높은지낮은지만 간단하게!

String ssid=""; //wifi ID
String PASSWORD="";
String host = ""; //computer IP


void setup() {
  myStepper.setSpeed(60);
  Serial.begin(9600);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting");
  while(WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.print("Connected: ");
  Serial.println(WiFi.localIP());
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);

}

void anticlockwise() {
  for(int i=0; i<2; i++) {
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

void window(humidity) {
  if(humidity > 123 || !(rain_state)) {
    clockwise();
    delay(500);
  } else {
    anticlockwise();
    delay(500);
  }
}

void curtain(humidity) {
  
}

void loop() {
  float humidity = Firebase.getFloat("Humidity");
  boolean rain_state = Firebase.getBool("Rain");
  delay(500);
  Serial.println("Humidity= "+humidity);

  window(humidity);
  curtain(humidity);
  delay(1000);
  
}
