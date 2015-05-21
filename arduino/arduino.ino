#define ledPin 13
#define smokePin 0
//1 second
#define writeInterval 1000

char val;
char charBuf[50];
long lastWrite;

void setup() {
  pinMode(ledPin, OUTPUT);
  Serial.begin(9600); // Default connection rate for my BT module
  pinMode(smokePin, INPUT);
  lastWrite = millis();
}

void loop() {
  if( Serial.available() )       // if data is available to read
  {
    val = Serial.read();         // read it and store it in 'val'
    //Serial.println(val);  
  }
  //kirim hasil baca sensor ke bluetooth
  int val = analogRead(smokePin);
  (intToString(val)).toCharArray(charBuf,50);
  
  if (readyToWrite()){
    Serial.write(charBuf);
    Serial.flush();
  }
  
}


bool readyToWrite(){
  //check if overflow
  if (millis() < lastWrite){
    lastWrite = millis();
    return false;    
  } else if (millis() - lastWrite  > writeInterval){
    lastWrite = millis();
    return true;
  } else {
    return false;
  }
}


//nambahin newline diakhir
String intToString(int val){
  String result = "";
  String appended = "";
  if (val == 0){
    return "0\n";
  } else {
    result += val;
    result += "\n";
//    while (val > 0){
//      
////      int mod = val % 10;
////      if (mod == 0){
////        appended = "0";
////      } else if (mod == 1){
////        appended = "1";
////      } else if (mod == 2){
////        appended = "2";
////      } else if (mod == 3){
////        appended = "3";
////      } else if (mod == 4){
////        appended = "4";
////      } else if (mod == 5){
////        appended = "5";
////      } else if (mod == 6){
////        appended = "6";
////      } else if (mod == 7){
////        appended = "7";
////      } else if (mod == 8){
////        appended = "8";
////      } else {
////        appended = "9";
////      }
//      result = result.concat(appended,result);
//    } //end while
    return result;
  }
}
