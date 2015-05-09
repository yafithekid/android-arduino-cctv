package com.yafi.smokecctv;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Set;


public class MainActivity extends Activity {
    public static final String EXTRA_DEVICE_ADDRESS = "extra_device_address";

    ListView pairedListView;
    ArrayAdapter<String> mPairedDevicesArrayAdapter;
    BluetoothAdapter mBluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.pairedListView = (ListView) findViewById(R.id.pairedListView);

        //set device list item selected listener
        this.pairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Tag", "Clicked");
                String __temp = mPairedDevicesArrayAdapter.getItem((int) id);
                String deviceAddress = __temp.substring(__temp.length() - 17);

                Toast.makeText(getBaseContext(), deviceAddress, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(),CCTVActivity.class);
                intent.putExtra(MainActivity.EXTRA_DEVICE_ADDRESS,deviceAddress);
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onResume(){
        super.onResume();

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,R.layout.list_item);
        this.pairedListView.setAdapter(this.mPairedDevicesArrayAdapter);
        doRefreshDeviceList();
    }

    public void onClickButtonRefresh(View view){
        doRefreshDeviceList();
    }

    public void onClickButtonOn(View view){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent,0);
        mBluetoothAdapter.enable();
        Toast.makeText(this,"Bluetooth enabled",Toast.LENGTH_SHORT).show();
    }

    public void onClickButtonOff(View view){
        mBluetoothAdapter.disable();
        Toast.makeText(this,"Bluetooth disabled",Toast.LENGTH_SHORT).show();
    }


    private void doRefreshDeviceList(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        this.mPairedDevicesArrayAdapter.clear();
        for(BluetoothDevice bd: pairedDevices){
            this.mPairedDevicesArrayAdapter.add(bd.getName() + "\n" + bd.getAddress());
        }
        this.pairedListView.setAdapter(this.mPairedDevicesArrayAdapter);
    }
}
