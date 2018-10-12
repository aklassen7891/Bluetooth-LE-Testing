package android.kaviles.bletutorial;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gjiazhe.panoramaimageview.GyroscopeObserver;
import com.gjiazhe.panoramaimageview.PanoramaImageView;

import java.util.ArrayList;
import java.util.HashMap;

import io.rmiri.buttonloading.ButtonLoading;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int BTLE_SERVICES = 2;
    public static final int REQUEST_LOCATION = 3;
    public static final int REQUEST_FINE_LOCATION = 4;

    private HashMap<String, BTLE_Device> mBTDevicesHashMap;
    private ArrayList<BTLE_Device> mBTDevicesArrayList;
    private ListAdapter_BTLE_Devices adapter;
    private ListView listView;

    //private Button btn_Scan;

    private ButtonLoading btn_Scan;

    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private Scanner_BTLE mBTLeScanner;

    private ImageView ble_scan_img;
    private TextView headline,subline;
    private ScrollView scrollView;

    private PulsatorLayout pulsator;
    private GyroscopeObserver gyroscopeObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_zwei);

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toast(getApplicationContext(), "BLE not supported");
            finish();
        }

        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getApplicationContext());
        mBTLeScanner = new Scanner_BTLE(this, 5000, -100);

        mBTDevicesHashMap = new HashMap<>();
        mBTDevicesArrayList = new ArrayList<>();

        adapter = new ListAdapter_BTLE_Devices(this, R.layout.btle_device_list_item, mBTDevicesArrayList);

        listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        btn_Scan = (ButtonLoading) findViewById(R.id.btn_scan);
        ((ScrollView) findViewById(R.id.scrollView)).addView(listView);


        pulsator = (PulsatorLayout)findViewById(R.id.pulsator);
        pulsator.start();

        gyroscopeObserver = new GyroscopeObserver();
        // Set the maximum radian the device should rotate to show image's bounds.
        // It should be set between 0 and π/2.
        // The default value is π/9.
        gyroscopeObserver.setMaxRotateRadian(Math.PI/2);

        PanoramaImageView panoramaImageView = (PanoramaImageView) findViewById(R.id.panorama_image_view);
        // Set GyroscopeObserver for PanoramaImageView.
        panoramaImageView.setGyroscopeObserver(gyroscopeObserver);


        ble_scan_img = (ImageView) findViewById(R.id.ble_search);
        headline = (TextView)findViewById(R.id.headline);
        subline = (TextView)findViewById(R.id.subline);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        btn_Scan.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {


                pulsator.setCount(5);
                pulsator.setDuration(1050);

                Utils.toast(getApplicationContext(), "Scan Button Pressed");

                if (!mBTLeScanner.isScanning()) {


                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Permission is not granted
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            // Show an explanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.
                        } else {
                            // No explanation needed; request the permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_LOCATION);

                            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }
                    } else {
                        // Permission has already been granted
                    }

                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Permission is not granted
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                Manifest.permission.ACCESS_FINE_LOCATION)) {
                            // Show an explanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.
                        } else {
                            // No explanation needed; request the permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_FINE_LOCATION);

                            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }
                    } else {
                        // Permission has already been granted
                    }
                    startScan();
                }
                else {
                    stopScan();
                }

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onResume() {
        super.onResume();
        gyroscopeObserver.register(this);
//        registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        gyroscopeObserver.unregister();
//        unregisterReceiver(mBTStateUpdateReceiver);
        stopScan();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mBTStateUpdateReceiver);
        stopScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
//                Utils.toast(getApplicationContext(), "Thank you for turning on Bluetooth");
            }
            else if (resultCode == RESULT_CANCELED) {
                Utils.toast(getApplicationContext(), "Please turn on Bluetooth");
            }
        }
        else if (requestCode == BTLE_SERVICES) {
            // Do something
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Context context = view.getContext();

//        Utils.toast(context, "List Item clicked");

        // do something with the text views and start the next activity.

        stopScan();

        String name = mBTDevicesArrayList.get(position).getName();
        String address = mBTDevicesArrayList.get(position).getAddress();

        Intent intent = new Intent(this, Activity_BTLE_Services.class);
        intent.putExtra(Activity_BTLE_Services.EXTRA_NAME, name);
        intent.putExtra(Activity_BTLE_Services.EXTRA_ADDRESS, address);
        startActivityForResult(intent, BTLE_SERVICES);
    }

    /*
    @Override
    public void onClick(View v) {

        switch (v.getId()) {



                break;
            default:
                break;
        }

    }*/

    public void addDevice(BluetoothDevice device, int rssi) {

        String address = device.getAddress();
        if (!mBTDevicesHashMap.containsKey(address)) {
            BTLE_Device btleDevice = new BTLE_Device(device);
            btleDevice.setRSSI(rssi);

            if(device.getAddress().equals("F8:27:FB:87:D1:90")){
                mBTDevicesHashMap.put(address, btleDevice);
                mBTDevicesArrayList.add(btleDevice);
            }
        }
        else {
            mBTDevicesHashMap.get(address).setRSSI(rssi);
        }

        adapter.notifyDataSetChanged();
    }

    public void startScan(){
        btn_Scan.setText("Wird gesucht...");

        mBTDevicesArrayList.clear();
        mBTDevicesHashMap.clear();
        ble_scan_img.setVisibility(View.VISIBLE);
        headline.setVisibility(View.VISIBLE);
        subline.setText("Suche...");
        subline.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.INVISIBLE);

        mBTLeScanner.start();
    }

    public void stopScan() {
        btn_Scan.setText("Nochmal versuchen!");

        ble_scan_img.setVisibility(View.INVISIBLE);
        headline.setVisibility(View.INVISIBLE);
        subline.setVisibility(View.INVISIBLE);
        scrollView.setVisibility(View.VISIBLE);

        btn_Scan.setProgress(false);

        pulsator.stop();
        pulsator.setVisibility(View.INVISIBLE);

        mBTLeScanner.stop();
    }
}
