package com.uc3m.Speckle_BLE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


import static android.content.Context.BLUETOOTH_SERVICE;
import static com.uc3m.Speckle_BLE.HR_Profile.BATTERY_UUID;
import static com.uc3m.Speckle_BLE.HR_Profile.CHARACTERISTIC_LEVEL_UUID;
//import static HR_Profile.COMMAND_START;

import static com.uc3m.Speckle_BLE.HR_Profile.UART_UUID;
import static com.uc3m.Speckle_BLE.HR_Profile.RX_UUID;
import static com.uc3m.Speckle_BLE.HR_Profile.TX_UUID;
import static com.uc3m.Speckle_BLE.HR_Profile.CLIENT_UUID;

public class GattClient {

    private static final String TAG = GattClient.class.getSimpleName();

    public interface OnHeartRateReadListener {
        void onHeartRateRead(String value);
        void onBatteryRead(int value);
        void onConnected(boolean success);
    }


    private Context mContext;
    private Handler mHandler;
    private OnHeartRateReadListener mListener;
    private String mDeviceAddress;
    private boolean mConnected = false;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;
    private BluetoothGattCharacteristic battery;

    private List<BluetoothGattCharacteristic> characteristics = new ArrayList<BluetoothGattCharacteristic>();


    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT client. Attempting to start service discovery");

                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT client");
                mListener.onConnected(false);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                super.onServicesDiscovered(gatt, status);

                mConnected = true;

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.w(TAG, "Service discovery completed!");
                }
                else {
                    Log.w(TAG, "Service discovery failed with status: " + status);
                }
                // Save reference to each characteristic.
                tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
                rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
                battery = gatt.getService(BATTERY_UUID).getCharacteristic(CHARACTERISTIC_LEVEL_UUID);

                // we add the notification characteristics in order to enable them one by one
                characteristics.add(rx);
                characteristics.add(battery);

                // enable notifications
                subscribeToCharacteristics(gatt);

                mListener.onConnected(mConnected);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            readCharacteristic(characteristic);

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            readCharacteristic(characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            characteristics.remove(0);
            subscribeToCharacteristics(gatt);
        }

        private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
            if (CHARACTERISTIC_LEVEL_UUID.equals(characteristic.getUuid())) {
                mListener.onBatteryRead(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0));
            }
            else if (RX_UUID.equals(characteristic.getUuid())) {
                mListener.onHeartRateRead(characteristic.getStringValue(0));
            }

        }

    };

    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    startClient();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stopClient();
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
    };

    public void onCreate(Context context, String deviceAddress, OnHeartRateReadListener listener) throws RuntimeException {
        mContext = context;
        mListener = listener;
        mDeviceAddress = deviceAddress;
        mHandler = new Handler(mContext.getMainLooper());

        mBluetoothManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (!checkBluetoothSupport(mBluetoothAdapter)) {
            throw new RuntimeException("GATT client requires Bluetooth support");
        }

        // Register for system Bluetooth events
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mBluetoothReceiver, filter);
        if (!mBluetoothAdapter.isEnabled()) {
            Log.w(TAG, "Bluetooth is currently disabled... enabling");
            mBluetoothAdapter.enable();
        } else {
            Log.i(TAG, "Bluetooth enabled... starting client");
            startClient();
        }
    }

    public void onDestroy() {
        mListener = null;
        mConnected = false;
        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        if (bluetoothAdapter.isEnabled()) stopClient();
        mContext.unregisterReceiver(mBluetoothReceiver);
    }

    private void subscribeToCharacteristics(BluetoothGatt gatt) {
        if(characteristics.size() == 0) return;

        BluetoothGattCharacteristic characteristic = characteristics.get(0);
        gatt.setCharacteristicNotification(characteristic, true);
        //characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_UUID);
        if(descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        } else {
            //mConnected = false;
        }
    }

    // Handler for click on the START or STOP button.
    public void sendClick(int COMMAND) {
        String message = null;
        if (COMMAND == 10) {
            message = "10";
        } else if (COMMAND == 20) {
            message = "20";
        }

        if (tx == null || message == null || message.isEmpty()) {
            // Do nothing if there is no device or message to send.
            return;
        }
        // Update TX characteristic value.  Note the setValue overload that takes a byte array must be used.
        tx.setValue(message.getBytes(Charset.forName("UTF-8")));
        if (mBluetoothGatt.writeCharacteristic(tx)) {
            Log.w(TAG,"Sent: " + message);
        } else {
            Log.e(TAG,"Couldn't write TX characteristic!");
        }
    }

    private boolean checkBluetoothSupport(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null) {
            Log.w(TAG, "Bluetooth is not supported");
            return false;
        }

        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.w(TAG, "Bluetooth LE is not supported");
            return false;
        }

        return true;
    }

    private void startClient() {
        final BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
        // Connect to BLE device from mHandler
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt = bluetoothDevice.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
            }
        });

        if (mBluetoothGatt == null) {
            Log.w(TAG, "Unable to create GATT client");
            return;
        }
    }

    private void stopClient() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter = null;
        }
    }
}