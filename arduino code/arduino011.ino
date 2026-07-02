#include <SoftwareSerial.h>
#include <Servo.h>

SoftwareSerial BT(10, 11);   // RX, TX
Servo doorServo;

// Servo positions
const int LOCK_POS = 90;
const int UNLOCK_POS = 0;

void setup() {

  Serial.begin(9600);
  BT.begin(9600);

  doorServo.attach(9);

  // Start locked
  doorServo.write(LOCK_POS);

  Serial.println("================================");
  Serial.println("Smart Server Room Lock Started");
  Serial.println("Bluetooth Ready");
  Serial.println("================================");
}

void loop() {

  if (BT.available()) {

    char cmd = BT.read();

    Serial.print("Received: ");
    Serial.println(cmd);

    switch (cmd) {

      case 'U':

        Serial.println("Unlock");

        doorServo.write(UNLOCK_POS);

        BT.println("UNLOCKED");

        break;

      case 'L':

        Serial.println("Lock");

        doorServo.write(LOCK_POS);

        BT.println("LOCKED");

        break;

      default:

        Serial.print("Unknown Command: ");
        Serial.println(cmd);

        break;
    }
  }
}