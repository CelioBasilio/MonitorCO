/*
Detect CO - Sistema Inteligente de Detecção de Monoxido de Carbono
Autor: TCC540-sala-004-Grupo001
*/

#include <Wire.h>
#include <WiFi.h>  // Use a biblioteca correta para ESP32
#include <WiFiClient.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <ESP32Servo.h> 

const char* ssid = "Andromeda";
const char* password = "Estutrampo";
const char* api_key = "HLU897LXCWR1JGOC";
const char* server = "api.thingspeak.com";

WiFiClient client;

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

const int MQ7_PIN = 25;
const int BUZZER_PIN = 26;
const int SERVO_PIN = 27;
const float VCC = 5.0;
const float RL = 10.0;
const float RO = 10.0;

Servo myServo;

// Função para ler a tensão do sensor
float readVoltage(int pin) {
  int adcValue = analogRead(pin);
  float voltage = adcValue * (3.3 / 1024.0);
  return voltage;
}

float calculateResistance(float voltage) {
  return ((VCC * 1000) / voltage - 1) * RL;
}

float calculatePPM(float resistance) {
  float ratio = resistance / RO;
  float ppm = pow((ratio / 27.0), -1.5);
  return ppm;
}

void setup() {
  Serial.begin(115200);
  analogSetAttenuation(ADC_11db);

  Serial.print("Conectando ao Wi-Fi...");
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }

  Serial.println("\nWi-Fi conectado!");

  pinMode(MQ7_PIN, INPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  myServo.attach(SERVO_PIN);
  myServo.write(0);

  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println(F("Falha ao inicializar o display OLED"));
    for (;;);
  }
  display.clearDisplay();
  display.setTextSize(2);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.print("Iniciando...");
  display.display();
  delay(2000);
}

void loop() {
  float voltage = readVoltage(MQ7_PIN);
  float resistance = calculateResistance(voltage);
  float ppm = calculatePPM(resistance);

  Serial.print("Tensão (mV): ");
  Serial.print(voltage);
  Serial.print(" | Resistência (kOhms): ");
  Serial.print(resistance);
  Serial.print(" | CO (ppm): ");
  Serial.println(ppm);

  display.clearDisplay();
  display.setCursor(0, 0);
  display.print("CO:");
  display.print(ppm);
  display.println(" ppm");

  display.setCursor(0, 40);
  if (ppm < 11) {
    display.print("Normal");
    digitalWrite(BUZZER_PIN, LOW);
    myServo.write(0);
  } else if (ppm >= 11 && ppm < 15) {
    display.print("Alerta!");
    digitalWrite(BUZZER_PIN, LOW);
    myServo.write(0);
  } else {
    display.print("PERIGO!");
    digitalWrite(BUZZER_PIN, HIGH);
    myServo.write(120);
  }

  display.display();
  delay(1000);

  if (WiFi.status() == WL_CONNECTED) {
    enviarThingSpeak(ppm);
  } else {
    Serial.println("Conexão Wi-Fi perdida");
  }
}

void enviarThingSpeak(float valorSensor) {
  if (client.connect(server, 80)) {
    String postStr = "api_key=" + String(api_key) + "&field1=" + String(valorSensor);

    client.print("POST /update HTTP/1.1\n");
    client.print("Host: " + String(server) + "\n");
    client.print("Connection: close\n");
    client.print("Content-Type: application/x-www-form-urlencoded\n");
    client.print("Content-Length: " + String(postStr.length()) + "\n\n");
    client.print(postStr);

    Serial.println("Dados enviados para ThingSpeak");
    delay(500);
    client.stop();
  } else {
    Serial.println("Falha ao conectar ao ThingSpeak");
  }
}
