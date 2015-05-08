package com.yafi.smokecctv;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class CCTVActivity extends Activity {
    BluetoothAdapter mBtAdapter;
    BluetoothSocket mBtSocket;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //koneksi untuk baca/tulis socket
    private ConnectionThread mConnectionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cctv);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Intent currentIntent = getIntent();
        String targetDevAddr = currentIntent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);
        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        //connect to the device
        BluetoothDevice btTargetDev = mBtAdapter.getRemoteDevice(targetDevAddr);

        try {
            mBtSocket = createBluetoothSocket(btTargetDev);
        } catch (IOException e){
            Toast.makeText(getBaseContext(),"Socket bluetooth connection failed", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        try {
            mBtSocket.connect();
        } catch (IOException e) {
            try {
                mBtSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        mConnectionThread = new ConnectionThread(mBtSocket);
        mConnectionThread.start();
    }

    @Override
    public void onPause(){
        super.onPause();
        try {
            mBtSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    public void onClickButtonSendMessage(View view){
        TextView textView = (TextView) findViewById(R.id.sendText);
        mConnectionThread.write(textView.getText().toString());
        Toast.makeText(this,"Data sent",Toast.LENGTH_SHORT).show();
    }

    private class ConnectionThread extends Thread{
        private final InputStream mmInputStream;
        private final OutputStream mmOutputStream;

        public ConnectionThread(BluetoothSocket btSocket){
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = btSocket.getInputStream(); tmpOut = btSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInputStream = tmpIn; mmOutputStream = tmpOut;

        }

        public void run(){
            //harusnya ngebaca tapi ntar dlu dah
        }

        public void write(String input){
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutputStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}
