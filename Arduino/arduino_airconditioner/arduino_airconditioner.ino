#include <ESP8266WiFiGratuitous.h>
#include <WiFiServerSecure.h>
#include <WiFiClientSecure.h>
#include <ArduinoWiFiServer.h>
#include <WiFiClientSecureBearSSL.h>
#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>
#include <WiFiUdp.h>
#include <ESP8266WiFiType.h>
#include <CertStoreBearSSL.h>
#include <ESP8266WiFiAP.h>
#include <WiFiClient.h>
#include <BearSSLHelpers.h>
#include <WiFiServer.h>
#include <ESP8266WiFiScan.h>
#include <WiFiServerSecureBearSSL.h>
#include <ESP8266WiFiGeneric.h>
#include <ESP8266WiFiSTA.h>

#include <ESP8266Firebase.h>

#include <Servo.h>

#define FIREBASE_HOST ""
#define FIREBASE_AUTH ""
#define WIFI_SSID ""
#define WIFI_PASSWORD ""

#define servoPin
Servo myServo;

boolean ac = false;

void setup() {
  Serial.begin(9600);
  myServo.attach(servoPin);
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

void loop() {
  float humidity = Firebase.getFloat("Humidity");
  boolean rain_state = Firebase.getBool("Rain");
  delay(500);
  Serial.println("Humidity= "+humidity);

  if(humidity > 123 && rain_state && !(ac)) {
    myServo.write(180);
    delay(100);
    myServo.write(0);
  } else {
    myServo.write(180);
    delay(100);
    myServo.write(0);
  }

}
