#include <ESP8266WiFi.h>
#include <WiFiClient.h>

// Defina suas credenciais Wi-Fi
const char* ssid = "ArmitageV27";      // Substitua pelo seu SSID
const char* password = "Th30d0r0@"; // Substitua pela sua senha Wi-Fi

// Configurações do ThingSpeak
const char* api_key = "GUEWSAL0OXTESUIV"; // Substitua pela sua chave API do ThingSpeak
const char* server = "api.thingspeak.com";

// Pino do sensor MQ-7
#define MQ7_PIN A0

WiFiClient client;

void setup() {
  Serial.begin(9600);

  // Conecta ao Wi-Fi
  Serial.print("Conectando ao Wi-Fi...");
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  
  Serial.println("\nConectado ao Wi-Fi");
}

void loop() {
  // Lê o valor do sensor MQ-7
  float sensorValue = analogRead(MQ7_PIN);
  float voltage = sensorValue * (3.3 / 1024.0);
  
  Serial.print("Valor do sensor: ");
  Serial.println(sensorValue);
  Serial.print("Tensão: ");
  Serial.println(voltage);

  // Verifica se está conectado ao Wi-Fi
  if (WiFi.status() == WL_CONNECTED) {
    enviarThingSpeak(sensorValue);
  } else {
    Serial.println("Conexão Wi-Fi perdida");
  }

  // Intervalo entre envios (15 segundos é o mínimo para contas grátis no ThingSpeak)
  delay(15000);
}

void enviarThingSpeak(int valorSensor) {
  if (client.connect(server, 80)) {
    // Monta a URL com a API key e os dados a serem enviados
    String postStr = "api_key=" + String(api_key) + "&field1=" + String(valorSensor);

    // Envia a requisição HTTP para o ThingSpeak
    client.print("POST /update HTTP/1.1\n");
    client.print("Host: " + String(server) + "\n");
    client.print("Connection: close\n");
    client.print("Content-Type: application/x-www-form-urlencoded\n");
    client.print("Content-Length: " + String(postStr.length()) + "\n\n");
    client.print(postStr);

    Serial.println("Dados enviados para ThingSpeak");

    // Aguarda a resposta do servidor
    delay(500);
    client.stop();
  } else {
    Serial.println("Falha ao conectar ao ThingSpeak");
  }
}
