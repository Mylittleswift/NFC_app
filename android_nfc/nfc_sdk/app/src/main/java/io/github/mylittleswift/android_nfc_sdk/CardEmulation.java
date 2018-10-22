package io.github.mylittleswift.android_nfc_sdk;

/**
 * Android 4.4 introduces an additional method of card emulation that
 * does not involve a secure element, called host-based card emulation.
 * This allows any Android application to emulate a card and talk directly to the NFC reader.
 *
 * When an NFC card is emulated using host-based card emulation, the data is routed to
 * the host CPU on which Android applications are running directly, instead of routing
 * the NFC protocol frames to a secure element.
 *
 *Android 4.4 comes with a convenience Service class that can be used as a basis for
 *implementing a HCE service: the HostApduService class.
 *
 */

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class CardEmulation extends HostApduService {

    private HashMap<String, String> cmdMap = null;
    private boolean initialized = false;

    @Override
    public byte[]  processCommandApdu(byte[] apdu, Bundle extras) {
        // is initialized?
        if (!initialized) {
            parseJSONFile();
            initialized = true;
        }

        // get apdu bytes from cmdMap
        byte[] bytes = cmdMap.get(apdu.toString()).getBytes();
        if (bytes != null) {
            return bytes;
        }
        else { // command not supported by json file
            return new byte[]{0x68,0x00}; // => "The request function is not supported by the card."
        }
    }

    @Override
    public void onDeactivated(int reason) {


    }

    public void parseJSONFile() {
        try
        {
            File jsonfile = new File(Environment.getExternalStorageDirectory(), "faketag.json");
            InputStream jsonReader = new FileInputStream(jsonfile);
            int size = jsonReader.available();

            byte[] buffer = new byte[size];
            jsonReader.read(buffer);
            jsonReader.close();
            String json = new String(buffer, "UTF-8");

            JSONArray jsonArray = new JSONArray(json); //jsonBuilder.toString());
            for (int index = 0; index < jsonArray.length(); index++) {
                //add values to map
                byte[] request = Base64.decode(jsonArray.getJSONObject(index).getString("request"), Base64.DEFAULT);
                byte[] response = Base64.decode(jsonArray.getJSONObject(index).getString("response"), Base64.DEFAULT);
                cmdMap.put(request.toString(), response.toString());
            }
        } catch (FileNotFoundException e) {
            Log.e("jsonFile", "file not found");
        } catch (IOException e) {
            Log.e("jsonFile", "ioerror");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}