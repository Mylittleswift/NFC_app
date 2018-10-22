package io.github.mylittleswift.android_nfc_sdk;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * NFC tags are based on a number of independently developed technologies and
 * offer a wide range of capabilities. The TagTechnology implementations provide access to
 * these different technologies and capabilities. Some sub-classes map to technology
 * specification (for example NfcA, IsoDep, others map to pseudo-technologies or
 * capabilities (for example Ndef, NdefFormatable).
 *
 * It is mandatory for Android NFC devices to provide the following TagTechnology implementations.
 * NfcA (also known as ISO 14443-3A)
 * NfcB (also known as ISO 14443-3B)
 * NfcF (also known as JIS 6319-4)
 * NfcV (also known as ISO 15693)
 * IsoDep
 * Ndef on NFC Forum Type 1, Type 2, Type 3 or Type 4 compliant tags
 *
 * It is optional for Android NFC devices to provide the following TagTechnology implementations.
 * If it is not provided, the Android device will never enumerate that class via getTechList().
 * MifareClassic
 * MifareUltralight
 * NfcBarcode
 * NdefFormatable must only be enumerated on tags for which this Android device is capable of
 * formatting. Proprietary knowledge is often required to format a tag to make it NDEF compatible.
 *
 * TagTechnology implementations provide methods that fall into two classes:
 * cached getters and I/O operations.
 */


public class TagWriter extends Activity {
    private String mPackageName = "com.android.mms";
    private String mUri = "https://...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (mPackageName == null)
            return;

        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        writeNFCTag(detectedTag);

        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{createUriRecord(mUri)});
        boolean result = writeTag(ndefMessage, detectedTag);
        if (result) {
            Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "写入失败", Toast.LENGTH_SHORT).show();
        }
    }


    public void writeNFCTag(Tag tag) {
        if (tag == null) {
            return;
        }
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{NdefRecord
                .createApplicationRecord(mPackageName)});

        int size = ndefMessage.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return;
                }
                if (ndef.getMaxSize() < size) {
                    return;
                }
                ndef.writeNdefMessage(ndefMessage);
                Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    format.connect();
                    format.format(ndefMessage);
                    Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "写入失败", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
        }
    }



    public static NdefRecord createUriRecord(String uriStr) {
        byte prefix = 0;
        for (Byte b : URI_PREFIX_MAP.keySet()) {
            String prefixStr = URI_PREFIX_MAP.get(b).toLowerCase();
            if ("".equals(prefixStr))
                continue;
            if (uriStr.toLowerCase().startsWith(prefixStr)) {
                prefix = b;
                uriStr = uriStr.substring(prefixStr.length());
                break;
            }
        }
        byte[] data = new byte[1 + uriStr.length()];
        data[0] = prefix;
        System.arraycopy(uriStr.getBytes(), 0, data, 1, uriStr.length());
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0], data);
        return record;
    }



    public static NdefRecord createTextRecord(String text) {
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = Charset.forName("UTF-8");
        byte[] textBytes = text.getBytes(utfEncoding);
        int utfBit = 0;
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return ndefRecord;
    }




    public static boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    return false;
                }
                ndef.writeNdefMessage(message);
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }



    private static final Map<Byte, String> URI_PREFIX_MAP = new HashMap<Byte, String>();

    static {
        URI_PREFIX_MAP.put((byte) 0x00, "");
        URI_PREFIX_MAP.put((byte) 0x01, "http://www.");
        URI_PREFIX_MAP.put((byte) 0x02, "https://www.");
        URI_PREFIX_MAP.put((byte) 0x03, "http://");
        URI_PREFIX_MAP.put((byte) 0x04, "https://");
        URI_PREFIX_MAP.put((byte) 0x05, "tel:");
        URI_PREFIX_MAP.put((byte) 0x06, "mailto:");
        URI_PREFIX_MAP.put((byte) 0x07, "ftp://anonymous:anonymous@");
        URI_PREFIX_MAP.put((byte) 0x08, "ftp://ftp.");
        URI_PREFIX_MAP.put((byte) 0x09, "ftps://");
        URI_PREFIX_MAP.put((byte) 0x0A, "sftp://");
        URI_PREFIX_MAP.put((byte) 0x0B, "smb://");
        URI_PREFIX_MAP.put((byte) 0x0C, "nfs://");
        URI_PREFIX_MAP.put((byte) 0x0D, "ftp://");
        URI_PREFIX_MAP.put((byte) 0x0E, "dav://");
        URI_PREFIX_MAP.put((byte) 0x0F, "news:");
        URI_PREFIX_MAP.put((byte) 0x10, "telnet://");
        URI_PREFIX_MAP.put((byte) 0x11, "imap:");
        URI_PREFIX_MAP.put((byte) 0x12, "rtsp://");
        URI_PREFIX_MAP.put((byte) 0x13, "urn:");
        URI_PREFIX_MAP.put((byte) 0x14, "pop:");
        URI_PREFIX_MAP.put((byte) 0x15, "sip:");
        URI_PREFIX_MAP.put((byte) 0x16, "sips:");
        URI_PREFIX_MAP.put((byte) 0x17, "tftp:");
        URI_PREFIX_MAP.put((byte) 0x18, "btspp://");
        URI_PREFIX_MAP.put((byte) 0x19, "btl2cap://");
        URI_PREFIX_MAP.put((byte) 0x1A, "btgoep://");
        URI_PREFIX_MAP.put((byte) 0x1B, "tcpobex://");
        URI_PREFIX_MAP.put((byte) 0x1C, "irdaobex://");
        URI_PREFIX_MAP.put((byte) 0x1D, "file://");
        URI_PREFIX_MAP.put((byte) 0x1E, "urn:epc:id:");
        URI_PREFIX_MAP.put((byte) 0x1F, "urn:epc:tag:");
        URI_PREFIX_MAP.put((byte) 0x20, "urn:epc:pat:");
        URI_PREFIX_MAP.put((byte) 0x21, "urn:epc:raw:");
        URI_PREFIX_MAP.put((byte) 0x22, "urn:epc:");
        URI_PREFIX_MAP.put((byte) 0x23, "urn:nfc:");
    }
}
