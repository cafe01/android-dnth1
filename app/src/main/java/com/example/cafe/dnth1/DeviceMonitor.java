package com.example.cafe.dnth1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import android.text.method.ScrollingMovementMethod;

public class DeviceMonitor extends AppCompatActivity {

    private static BluetoothAdapter ba;
    private static TextView tvDataLog;
    private static TextView tvIdealTemp;
    private static TextView tvIdealHumi;
    private static TextView tvTemp;
    private static TextView tvHumi;
    private static TextView tvDaytime;
    private static TextView tvAc;
    private static TextView tvDeumidifier;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static Handler mHandler;

    private void toast(String message){
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        tvDataLog = (TextView) findViewById(R.id.device_messages);
        tvTemp = (TextView) findViewById(R.id.tv_temperature);
        tvHumi = (TextView) findViewById(R.id.tv_humidity);
        tvIdealTemp = (TextView) findViewById(R.id.tv_ideal_temperature);
        tvIdealHumi = (TextView) findViewById(R.id.tv_ideal_humidity);
        tvDaytime = (TextView) findViewById(R.id.tv_daytime);
        tvAc = (TextView) findViewById(R.id.tv_status_ac);
        tvDeumidifier = (TextView) findViewById(R.id.tv_status_dehumidifier);

        Intent intent = getIntent();
        String address = intent.getStringExtra(MainActivity.DEVICE_ADDRESS);
        tvDataLog.setMovementMethod(new ScrollingMovementMethod());

        // handler
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                String data = (String) message.obj;
                tvDataLog.append(data + "\n");
                updateStats(data);
            }
        };

        // connect to bluetooth
        ba = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = ba.getBondedDevices();
        BluetoothDevice device = null;

        // Loop through paired devices
        for (BluetoothDevice d : pairedDevices) {
            if (d.getAddress().equals(address)) {
                device = d;
                break;
            }
        }

        if (device != null) {
            toast("Connecting to " + device.getName() + " (" + device.getAddress() + ")");
            ConnectThread ct = new ConnectThread(device);
            ct.start();
        }
        else {
            toast("Could not find device.");
        }


    }

    private void updateStats(String message) {

        String data[] = message.split(":");

        if (data[0].equals("DATA")) {

            tvDaytime.setText(data[1]);
            tvIdealTemp.setText(data[2] + "");
            tvIdealHumi.setText(data[3] + "%");
            tvTemp.setText(data[4] + "º");
            tvHumi.setText(data[5] + "%");

            tvAc.setText(data[6].equals("1") ? "AC: ON" : "AC: OFF");
            tvDeumidifier.setText(data[7].equals("1") ? "DH: ON" : "DH: OFF");
        }
    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private InputStream mmInStream;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                send("Create socket error:" + e.getMessage());
            }
            mmSocket = tmp;
            try {
                mmInStream = mmSocket.getInputStream();
            } catch (IOException e) { }
        }

        private void send(String message) {
            mHandler.obtainMessage(1, 1, 1, message).sendToTarget();
        }

        public void run() {
            // Cancel discovery because it will slow down the connection

            ba.cancelDiscovery();

            send("Connecting...");
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                send("Connected!");
            } catch (IOException connectException) {

                send("Unable to connect: " + connectException.getMessage());

                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            send("Reading:");

            // read
            byte[] buffer = new byte[1];  // buffer store for the stream
//            int bytes; // bytes returned from read()
            String data = "";

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
//                    bytes = mmInStream.read();
                    mmInStream.read(buffer);

                    // end of data
                    if (buffer[0] == 0x0A) {

                        send(data);
                        data = "";

                    } else {

                        data = data.concat(new String(buffer));
                    }


                } catch (IOException e) {
                    break;
                }
            }
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
