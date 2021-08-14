#include <SoftwareSerial.h> //wifi
#include <DHT.h>
#include <DHT_U.h>

#define DHTPIN
#define DHTTYPE
String ssid=""; //wifi ID
String PASSWORD="";
String host = ""; //computer IP


DHT dht(DHTPIN, DHTTYPE);
SoftwareSerial ms(2,3);//TX,RX

void connectWifi() {
  String cmd = "AT+CWMODE=1";
  ms.println(cmd);
  cmd="AT+CWJAP=\""+ssid+"\",\""+PASSWORD+"\"";
  ms.println(cmd);
  delay(2000);
  if(ms.find("OK")) {
    Serial.println("Wifi Connected!");
  } else {
    Serial.println("Connect Timeout"+ms);
  }
}

void httpclient(String input) {
  String connect_server_cmd = "AI+CIPSTART=,\"TCP\",""+host+"\",8787";
  delay(500);
  ms.println(connect_server_cmd);
  String url = input;
  String httpCmd = "GET /process.php?temp="+url+"HTTP/1.0\r\n\r\n";
  String cmd = "AT+CIPSEND=,"+httpCmd.length();
  ms.println(cmd);
  ms.println(httpCmd);
  if(ms.find(">")){
    Serial.print(">");
  } else{
    ms.println("AT+CIPCLOSE");
    Serial.pritnln("Connect Timeout");
    delay(1000);
    return;
  }
  delay(500);

  ms.println(cmd);
  Serial.println(cmd);
  delay(100);
  if(Serial.find("ERROR")) return;
  mySerial.println("AT+CIPCLOSE");
  delay(100);
}

void setup() {
  Serial.begin(9600);
  dht.begin();
  ms.begin(9600);
  connectWifi();
}

void loop() {
  delay(2000);
  float humidity = dht.readHumidity();
  float temperature = dht.readTemperature();

  //값읽기 오류확인
  if (isnan(humidity) || isnan(temperature)) {
    Serial.println("Failed to read");
    return;
  }
  //Serial에서 값확인하려고 출력
  Serial.print(humidity);
  Serial.print(temperature);

  String output = String(temperature)+"&humidity="+String(humidity);
  httpclient(output);
  delay(1000);

  if(ms.available()) {
    Serial.write(ms.read());
  }
}
