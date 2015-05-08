int ledPin = 13; 

char val;
void setup() {
 pinMode(ledPin, OUTPUT);
 
 Serial.begin(9600); // Default connection rate for my BT module
}
 
void loop() {

 if( Serial.available() )       // if data is available to read
  {
    
    val = Serial.read();         // read it and store it in 'val'
    Serial.println(val);  
}
  if( val == 'h' )               // if 'H' was received
  {
    digitalWrite(ledPin, HIGH);  // turn ON the LED
  } else { 
    digitalWrite(ledPin, LOW);   // otherwise turn it OFF
  }
  delay(100); 
}
