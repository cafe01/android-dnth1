package com.example.cafe.dnth1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public final static String DEVICE_ADDRESS = "com.example.cafe.tutorial01.DEVICE_ADDRESS";

    private static BluetoothAdapter ba;

    private ArrayList deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ba = BluetoothAdapter.getDefaultAdapter();
        final TextView statusText = (TextView) findViewById(R.id.ba_status);
        statusText.setText("Initializing...");



        if (ba == null) {
            statusText.setText("No BT device found!");
            return;
        }

        if (ba.isEnabled()) {
            statusText.setVisibility(View.GONE);
        }
        else {
            statusText.setText("Please enable bluetooth");
            return;
        }


        // paired devices
        LinearLayout buttonList = (LinearLayout) findViewById(R.id.button_list);
        Set<BluetoothDevice> pairedDevices = ba.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (final BluetoothDevice device : pairedDevices) {

                Button btn = new Button(this);
                btn.setText(device.getName());
                buttonList.addView(btn);
                btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {

                        openDeviceMonitor(device.getAddress());
                    }
                });
            }
        }
    }


    /** Called when the user clicks the Send button */
    public void openDeviceMonitor(String address) {

        Intent intent = new Intent(this, DeviceMonitor.class);
        intent.putExtra(DEVICE_ADDRESS, address);
        startActivity(intent);
    }

}
