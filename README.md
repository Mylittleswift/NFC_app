# NFC Applications on iOS and Android
<img src="http://www.themobilespot.co.uk/wp-content/uploads/2014/12/itkt-blog-b-5-2-2013.jpg" width="512">

## What's NFC?

Near Field Communication (NFC) is a set of short-range wireless technologies, typically requiring a distance of 4cm or less to initiate a connection. NFC allows you to share small payloads of data between an NFC tag and an Android-powered device, or between two Android-powered devices.

Android-powered devices with NFC simultaneously support three main modes of operation:

1. Reader/writer mode, allowing the NFC device to read and/or write passive NFC tags and stickers.
2. P2P mode, allowing the NFC device to exchange data with other NFC peers; this operation mode is used by Android Beam.
3. Card emulation mode, allowing the NFC device itself to act as an NFC card. The emulated NFC card can then be accessed by an external NFC reader, such as an NFC point-of-sale terminal. <br>

https://developer.android.com/guide/topics/connectivity/nfc/index.html  <br>
https://en.wikipedia.org/wiki/Near_field_communication  <br>


## This SDK can do:

1. Tag reader/writer
(Support NfcA, NfcB, NfcF, NfcV, IsoDep, Ndef on NFC Forum Type 1, Type 2, Type 3 or Type 4 compliant tags, MifareUltralight, NfcBarcode. For MIFARE Classic tags, this would be useful: [MIFARE Classic Tool] (https://github.com/ikarus23/MifareClassicTool))
2. P2P beam
3. Card emulation

Compatible with Android 4.4 (API 19) and above

## References

[Android NFC API](https://developer.android.com/reference/android/nfc/package-summary.html)  <br>
[iOS NFC API](https://developer.apple.com/documentation/corenfc/building_an_nfc_tag_reader_app?language=objc) <br>
[NFC Forum Specification](http://nfc-forum.org/our-work/specifications-and-application-documents/specifications/)  <br>


## License

    The MIT License (MIT)

    Copyright (c) 2015 LinkMob.cc

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
