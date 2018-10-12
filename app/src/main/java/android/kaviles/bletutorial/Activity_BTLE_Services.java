package android.kaviles.bletutorial;

import android.Manifest;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class Activity_BTLE_Services extends AppCompatActivity implements ExpandableListView.OnChildClickListener {
    private final static String TAG = Activity_BTLE_Services.class.getSimpleName();

    public static final String EXTRA_NAME = "android.aviles.bletutorial.Activity_BTLE_Services.NAME";
    public static final String EXTRA_ADDRESS = "android.aviles.bletutorial.Activity_BTLE_Services.ADDRESS";

    private ListAdapter_BTLE_Services expandableListAdapter;
    private ExpandableListView expandableListView;

    private ArrayList<BluetoothGattService> services_ArrayList;
    private HashMap<String, BluetoothGattCharacteristic> characteristics_HashMap;
    private HashMap<String, ArrayList<BluetoothGattCharacteristic>> characteristics_HashMapList;


    private Intent mBTLE_Service_Intent;
    private Service_BTLE_GATT mBTLE_Service;
    private boolean mBTLE_Service_Bound;
    private BroadcastReceiver_BTLE_GATT mGattUpdateReceiver;

    private String name;
    private String address;

    private Croller slider;

    private ImageView licht,licht_aus,licht_an;

    static final int MY_PERMISSIONS_REQUEST = 2;

    private ServiceConnection mBTLE_ServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Service_BTLE_GATT.BTLeServiceBinder binder = (Service_BTLE_GATT.BTLeServiceBinder) service;
            mBTLE_Service = binder.getService();
            mBTLE_Service_Bound = true;

            if (!mBTLE_Service.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            mBTLE_Service.connect(address);

            // Automatically connects to the device upon successful start-up initialization.
//            mBTLeService.connect(mBTLeDeviceAddress);

//            mBluetoothGatt = mBTLeService.getmBluetoothGatt();
//            mGattUpdateReceiver.setBluetoothGatt(mBluetoothGatt);
//            mGattUpdateReceiver.setBTLeService(mBTLeService);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBTLE_Service = null;
            mBTLE_Service_Bound = false;

//            mBluetoothGatt = null;
//            mGattUpdateReceiver.setBluetoothGatt(null);
//            mGattUpdateReceiver.setBTLeService(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btle_services2);

        Intent intent = getIntent();
        name = intent.getStringExtra(Activity_BTLE_Services.EXTRA_NAME);
        address = intent.getStringExtra(Activity_BTLE_Services.EXTRA_ADDRESS);

        services_ArrayList = new ArrayList<>();
        characteristics_HashMap = new HashMap<>();
        characteristics_HashMapList = new HashMap<>();

        expandableListAdapter = new ListAdapter_BTLE_Services(
                this, services_ArrayList, characteristics_HashMapList);

        expandableListView = (ExpandableListView) findViewById(R.id.lv_expandable);
        //expandableListView.setAdapter(expandableListAdapter);
        //expandableListView.setOnChildClickListener(this);

        //((TextView) findViewById(R.id.tv_name)).setText(name + " Services");
        //((TextView) findViewById(R.id.tv_address)).setText(address);


        //licht = (ImageView)findViewById(R.id.licht);
        licht_an = (ImageView)findViewById(R.id.licht_an);
        licht_aus = (ImageView)findViewById(R.id.licht_aus);

        slider = (Croller) findViewById(R.id.slider);
        slider.setLabel("Dimmer");


        licht_aus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothGattCharacteristic characteristic = characteristics_HashMapList.get(services_ArrayList.get(2).getUuid().toString()).get(0);
                characteristic.setValue(parseHex("F601A201FF100100"));
                mBTLE_Service.writeCharacteristic(characteristic);
                //licht.setImageResource(R.drawable.ic_status_light_off);


                //F601a201FD1001xx
            }
        });


        licht_an.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BluetoothGattCharacteristic characteristic = characteristics_HashMapList.get(services_ArrayList.get(2).getUuid().toString()).get(0);
                characteristic.setValue(parseHex("F601A201FF1001FF"));
                mBTLE_Service.writeCharacteristic(characteristic);
                //licht.setImageResource(R.drawable.ic_status_light_on);
            }
        });


        slider.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(Croller croller, int i) {
                progress = i;

            }

            @Override
            public void onStartTrackingTouch(Croller croller) {

            }

            @Override
            public void onStopTrackingTouch(Croller croller) {

                BluetoothGattCharacteristic characteristic = characteristics_HashMapList.get(services_ArrayList.get(2).getUuid().toString()).get(0);
                characteristic.setValue(parseHex("F601a201FD1001"+ Integer.toHexString((int)progress)));
                System.out.println("Das was angefügt wird " + Integer.toHexString(progress));
                mBTLE_Service.writeCharacteristic(characteristic);
            }

        });



    }



    @Override
    protected void onStart() {
        super.onStart();

        mGattUpdateReceiver = new BroadcastReceiver_BTLE_GATT(this);
        registerReceiver(mGattUpdateReceiver, Utils.makeGattUpdateIntentFilter());

        mBTLE_Service_Intent = new Intent(this, Service_BTLE_GATT.class);
        bindService(mBTLE_Service_Intent, mBTLE_ServiceConnection, Context.BIND_AUTO_CREATE);
        startService(mBTLE_Service_Intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mBTLE_ServiceConnection);
        mBTLE_Service_Intent = null;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

        BluetoothGattCharacteristic characteristic = characteristics_HashMapList.get(
                services_ArrayList.get(groupPosition).getUuid().toString())
                .get(childPosition);

        if (Utils.hasWriteProperty(characteristic.getProperties()) != 0) {
            String uuid = characteristic.getUuid().toString();

            Dialog_BTLE_Characteristic dialog_btle_characteristic = new Dialog_BTLE_Characteristic();

            dialog_btle_characteristic.setTitle(uuid);
            dialog_btle_characteristic.setService(mBTLE_Service);
            dialog_btle_characteristic.setCharacteristic(characteristic);

            dialog_btle_characteristic.show(getFragmentManager(), "Dialog_BTLE_Characteristic");
        } else if (Utils.hasReadProperty(characteristic.getProperties()) != 0) {
            if (mBTLE_Service != null) {
                mBTLE_Service.readCharacteristic(characteristic);
            }
        } else if (Utils.hasNotifyProperty(characteristic.getProperties()) != 0) {
            if (mBTLE_Service != null) {
                mBTLE_Service.setCharacteristicNotification(characteristic, true);
            }
        }

        return false;
    }

    public void updateServices() {

        if (mBTLE_Service != null) {

            services_ArrayList.clear();
            characteristics_HashMap.clear();
            characteristics_HashMapList.clear();

            List<BluetoothGattService> servicesList = mBTLE_Service.getSupportedGattServices();

            for (BluetoothGattService service : servicesList) {

                services_ArrayList.add(service);

                List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> newCharacteristicsList = new ArrayList<>();

                for (BluetoothGattCharacteristic characteristic: characteristicsList) {

                        characteristics_HashMap.put(characteristic.getUuid().toString(), characteristic);
                        newCharacteristicsList.add(characteristic);

                }

                characteristics_HashMapList.put(service.getUuid().toString(), newCharacteristicsList);

                /*if(service.getUuid().toString().equals("9769f147-f77a-43ae-8c35-09f0c5245308")){

                }*/
            }

            if (servicesList != null && servicesList.size() > 0) {
                expandableListAdapter.notifyDataSetChanged();
            }
        }
    }

    public void updateCharacteristic() {
        expandableListAdapter.notifyDataSetChanged();
    }



    private byte[] parseHex(String hexString) {
        hexString = hexString.replaceAll("\\s", "").toUpperCase();
        String filtered = new String();
        for(int i = 0; i != hexString.length(); ++i) {
            if (hexVal(hexString.charAt(i)) != -1)
                filtered += hexString.charAt(i);
        }

        if (filtered.length() % 2 != 0) {
            char last = filtered.charAt(filtered.length() - 1);
            filtered = filtered.substring(0, filtered.length() - 1) + '0' + last;
        }

        return hexStringToByteArray(filtered);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private int hexVal(char ch) {
        return Character.digit(ch, 16);
    }


}
