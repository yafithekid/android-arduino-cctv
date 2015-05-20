package com.yafi.smokecctv;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class CCTVActivity extends Activity {
    public static final String DEBUG_TAG = "CCTVActivity";
    public static final int COLOR_RED = 0xffff4500;
    public static final int COLOR_GREEN = 0xff00ff00;
    BluetoothAdapter mBtAdapter;
    BluetoothSocket mBtSocket;
    Handler btInputHandler;
    TextView smokeText;
    EditText exposureLimitText;
    EditText serverIpText;
    ImageView photoResultImageView;

    final int HANDLER_STATE = 0;
    private StringBuilder recDataString = new StringBuilder();

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //koneksi untuk baca/tulis socket
    private ConnectionThread mConnectionThread;

    //camera object
    private Camera camera;
    private int cameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cctv);

        //asumsi dah ada kamera
        cameraId = findBackFacingCameraId();

        //buat output hasil
        smokeText = (TextView) findViewById(R.id.smokeText);
        //this.photoResultImageView = (ImageView) findViewById(R.id.imageView);

        //input alamat server
        serverIpText = (EditText) findViewById(R.id.serverIPText);

        exposureLimitText = (EditText) findViewById(R.id.limitExposureText);
        //bikin handler untuk menerima data dari connection thread
        btInputHandler = new Handler(){
            public void handleMessage(Message msg){
                if (msg.what == HANDLER_STATE){
                    //Log.d(CCTVActivity.DEBUG_TAG,"Handler berjalan");
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("\n"); //baca sampe \n
                    //Log.d(CCTVActivity.DEBUG_TAG,"Output: " + readMessage);
                    if (endOfLineIndex > 0) { //ketemu new line
                        String printedData = recDataString.substring(0, endOfLineIndex);
                        //Log.d(CCTVActivity.DEBUG_TAG,"Output Data: " + printedData);
                        //set the output
                        smokeText.setText(printedData);

                        //cek kalo dia lebih dari exposure, ganti warna
                        int value = Integer.parseInt(printedData);
                        int limit = Integer.parseInt(exposureLimitText.getText().toString());
                        if (value > limit){
                            smokeText.setTextColor(COLOR_RED);
                        } else {
                            smokeText.setTextColor(COLOR_GREEN);
                        }
                        //hapus data newline
                        recDataString.delete(0,endOfLineIndex + 1);
                    }
                }
            }
        };

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

        getCameraInstance();
    }

    @Override
    public void onPause(){
        super.onPause();
        try {
            mBtSocket.close();
            if (camera != null){
                camera.release();
                camera = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //utilites
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    private int findBackFacingCameraId(){
        int cameraId = -1;
        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; ++i){
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i,cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private void getCameraInstance(){
        camera = null;
        try {
            this.camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            camera.cancelAutoFocus();
            camera.setPreviewCallback(null);
            camera.startPreview();
            camera.setParameters(parameters);
        } catch (Exception e) {
            Log.d(CCTVActivity.DEBUG_TAG,e.getMessage());
        }
    }

    private void doTakePicture(){
        Log.d(DEBUG_TAG,"Taking picture");
        String resultImagePath = new String();
        camera.takePicture(null, null, new PhotoHandler(this.getApplicationContext(),resultImagePath,getServerUri()));
    }

    public String getServerUri(){
        StringBuilder serverIP = new StringBuilder();
        serverIP.append("http://").append(serverIpText.getText().toString()).append("/tes-android/upload.php");
        return serverIP.toString();
    }

    public void onClickButtonSendMessage(View view){
//        TextView textView = (TextView) findViewById(R.id.sendText);
//        mConnectionThread.write(textView.getText().toString());
//        Toast.makeText(this,"Data sent",Toast.LENGTH_SHORT).show();
    }

    public void onClickButtonCapture(View view){
        doTakePicture();
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
            byte[] buffer = new byte[256];
            int nbytes;
            while (true){
                try {
                    nbytes = mmInputStream.read(buffer);
                    String readMessage = new String(buffer,0,nbytes);
                    btInputHandler.obtainMessage(HANDLER_STATE,readMessage).sendToTarget();
                } catch (IOException ioe){
                    break;
                }
            }
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
