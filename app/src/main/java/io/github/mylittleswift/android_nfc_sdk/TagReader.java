package io.github.mylittleswift.android_nfc_sdk;

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


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcBarcode;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;



public class TagReader extends Activity {
    private TextView mNfcText;
    private String mTagText;
    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNfcText = (TextView) findViewById(R.id.tagReader);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(detectedTag);
        mTagText = ndef.getType() + "\nmaxsize:" + ndef.getMaxSize() + "bytes\n\n";
        readNfcTag(intent);
        mNfcText.setText(mTagText);


        //read MifareUltralight tag
        String[] techList = detectedTag.getTechList();
        boolean haveMifareUltralight = false;
        for (String tech : techList) {
            if (tech.indexOf("MifareUltralight") >= 0) {
                haveMifareUltralight = true;
                break;
            }
        }
        if (!haveMifareUltralight) {
            Toast.makeText(this, "MifareUltralight Not Supported", Toast.LENGTH_SHORT).show();
            return;
        }
        String data = readMUTag(detectedTag);
        if (data != null)
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();


        String barcode = readNfcBarcode(detectedTag);
        if (barcode != null) {
            readNfcBarcode(detectedTag);

        }

    }



    private void readNfcTag(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msgs[] = null;
            int contentSize = 0;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    contentSize += msgs[i].toByteArray().length;
                }
            }
            try {
                if (msgs != null) {
                    NdefRecord record = msgs[0].getRecords()[0];
                    String textRecord = parseTextRecord(record);
                    mTagText += textRecord + "\n\ntext\n" + contentSize + " bytes";
                }
                if (msgs != null) {
                    NdefRecord ndefRecord = msgs[0].getRecords()[0];
                    Uri uri = parse(ndefRecord);
                    mTagText += uri.toString() + "\n\nUri\n" + contentSize + " bytes";
                }
            } catch (Exception e) {
            }
        }
    }


    //read MifareUltralight tag
    public String readMUTag(Tag mutag) {
        MifareUltralight ultralight = MifareUltralight.get(mutag);
        try {
            ultralight.connect();
            byte[] data = ultralight.readPages(4);
            return new String(data, Charset.forName("GB2312"));
        } catch (Exception e) {
        } finally {
            try {
                ultralight.close();
            } catch (Exception e) {
            }
        }
        return null;
    }


    private String readNfcBarcode(Tag tag) {
        byte[] result = null;
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(mActivity);
        if (adapter != null && adapter.isEnabled()) {
            NfcBarcode barcode = NfcBarcode.get(tag);
            if (barcode != null && barcode.getType() == NfcBarcode.TYPE_KOVIO) {
                result = tag.getId();
            }
        }
        return null;
    }





    private static String parseTextRecord(NdefRecord ndefRecord) {

        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }

        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }
        try {

            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";

            int languageCodeLength = payload[0] & 0x3f;

            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

            String textRecord = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return textRecord;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }



    private static Uri parse(NdefRecord record) {
        short tnf = record.getTnf();
        if (tnf == NdefRecord.TNF_WELL_KNOWN) {
            return parseWellKnown(record);
        } else if (tnf == NdefRecord.TNF_ABSOLUTE_URI) {
            return parseAbsolute(record);
        }
        throw new IllegalArgumentException("Unknown TNF " + tnf);
    }


    private static Uri parseAbsolute(NdefRecord ndefRecord) {
        byte[] payload = ndefRecord.getPayload();
        Uri uri = Uri.parse(new String(payload, Charset.forName("UTF-8")));
        return uri;
    }


    private static Uri parseWellKnown(NdefRecord ndefRecord) {
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_URI))
            return null;

        byte[] payload = ndefRecord.getPayload();
        String prefix = URI_PREFIX_MAP.get(payload[0]);
        byte[] prefixBytes = prefix.getBytes(Charset.forName("UTF-8"));
        byte[] fullUri = new byte[prefixBytes.length + payload.length - 1];
        System.arraycopy(prefixBytes, 0, fullUri, 0, prefixBytes.length);
        System.arraycopy(payload, 1, fullUri, prefixBytes.length, payload.length - 1);
        Uri uri = Uri.parse(new String(fullUri, Charset.forName("UTF-8")));
        return uri;
    }


    private static final Map<Byte, String> URI_PREFIX_MAP = new HashMap<>();

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




    //kovio nfc barcode decoder
    final private static char[] hexArray = "0123456789abcdef".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    private static Uri decodeUri(byte[] tagId) throws IOException {
        final byte URI_PREFIX_HTTP_WWW = (byte) 0x01; // "http://www."
        final byte URI_PREFIX_HTTPS_WWW = (byte) 0x02; // "https://www."
        final byte URI_PREFIX_HTTP = (byte) 0x03; // "http://"
        final byte URI_PREFIX_HTTPS = (byte) 0x04; // "https://"

        // All tags of NfcBarcode technology and Kovio type have lengths of a multiple of 16 bytes
        if (tagId.length >= 4
                && (tagId[1] == URI_PREFIX_HTTP_WWW || tagId[1] == URI_PREFIX_HTTPS_WWW
                || tagId[1] == URI_PREFIX_HTTP || tagId[1] == URI_PREFIX_HTTPS)) {
            // Workaround for MediaTek bug
            if (tagId.length == 32) {
                boolean trim = true;
                for (int i = 16; i < 32; i++) {
                    if (tagId[i] != 0x00) {
                        trim = false;
                        break;
                    }
                }
                if (trim) {
                    byte[] newTagId = new byte[16];
                    System.arraycopy(tagId, 0, newTagId, 0, 16);
                    tagId = newTagId;
                }
            }


            // Look for optional URI terminator (0xfe), used to indicate the end of a URI prior to
            // the end of the full NfcBarcode payload. No terminator means that the URI occupies the
            // entire length of the payload field. Exclude checking the CRC in the final two bytes
            // of the NfcBarcode tagId.
            int end = 2;
            for (; end < tagId.length - 2; end++) {
                if (tagId[end] == (byte) 0xfe) {
                    break;
                }
            }
            byte[] payload = new byte[end - 2]; // Skip two bytes (manufacturer ID, uri prefix code)
            System.arraycopy(tagId, 2, payload, 0, payload.length);

            String uriPrefix = null;
            if (tagId[1] == URI_PREFIX_HTTP_WWW) {
                uriPrefix = "http://www.";
            } else if (tagId[1] == URI_PREFIX_HTTPS_WWW) {
                uriPrefix = "https://www.";
            } else if (tagId[1] == URI_PREFIX_HTTP) {
                uriPrefix = "http://";
            } else if (tagId[1] == URI_PREFIX_HTTPS) {
                uriPrefix = "https://";
            }

            try {
                return Uri.parse(new URL(uriPrefix + new String(payload, "US-ASCII")).toString());
            } catch (UnsupportedEncodingException ex) {
                throw new MalformedURLException(ex.getMessage());
            }
        } else {
            throw new MalformedURLException("Tag doesn't contain the uri");
        }
    }

}






