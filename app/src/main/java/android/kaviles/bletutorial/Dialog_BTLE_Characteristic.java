package android.kaviles.bletutorial;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Kelvin on 5/9/16.
 */
public class Dialog_BTLE_Characteristic extends DialogFragment implements DialogInterface.OnClickListener {

    private String title;
    private Service_BTLE_GATT service;
    private BluetoothGattCharacteristic characteristic;

    private BluetoothGatt mBluetoothGatt;


    private BluetoothGattCallback callB;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_btle_characteristic, null))
                .setNegativeButton("Cancel", this).setPositiveButton("Send", this);
        builder.setTitle(title);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        // Find a way to check which button as pressed cancel or ok
//            Utils.toast(activity.getApplicationContext(), "Button " + Integer.toString(which) + " Pressed");

        EditText edit = (EditText) ((AlertDialog) dialog).findViewById(R.id.et_submit);

        //System.out.println("HIIIER Characteristic Value : " + characteristic.);


        switch (which) {
            case -2:
                // cancel button pressed
                String ts = "F601A201FF100100";
                //characteristic.setValue(Integer.toHexString(Integer.parseInt(edit.getText().toString())));
                /*try {
                    characteristic.setValue(ts.getBytes("UTF-8"));
                    service.writeCharacteristic(characteristic);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }*/

                //characteristic.setValue(URLEncoder.encode(edit.getText().toString(),"utf-8"));
                //service.writeCharacteristic(characteristic);
                break;
            case -1:
                // okay button pressed
                if (service != null) {

                    String t = "0xF601A201FF1001FF";
                    /*characteristic.setValue(Integer.toHexString(Integer.parseInt(edit.getText().toString())));
                    try {
                        characteristic.setValue(t.getBytes("UTF-8"));
                        service.writeCharacteristic(characteristic);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    String tt = edit.getText().toString();

                    byte[] strBytes = tt.getBytes();
                    byte[] bytes = characteristic.getValue();

                    if (bytes == null) {

                        System.out.println("Cannot get Values from mWriteCharacteristic.");
                        dismiss();// equivalent action
                    }

                    if (bytes.length <= strBytes.length) {
                        for(int i = 0; i < bytes.length; i++) {
                            bytes[i] = strBytes[i];
                        }
                    } else {
                        for (int i = 0; i < strBytes.length; i++) {
                            bytes[i] = strBytes[i];
                        }
                    }*/

                    characteristic.setValue(parseHex(edit.getText().toString()));

                    service.writeCharacteristic(characteristic);

                }
                break;
            default:
                break;
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setService(Service_BTLE_GATT service) {
        this.service = service;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
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
