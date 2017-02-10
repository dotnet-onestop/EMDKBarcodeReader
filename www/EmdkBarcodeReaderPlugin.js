
var exec = require('cordova/exec');
//function EmdkBarCodeReaderPlugin(){}
var EmdkBarcodeReaderPlugin = {
    /**
     * Initialize the EMDK Manager to obtain the EMdk manager  object
     */
    initiateEMdk:function (successCallback, errorCallback)
    {

        console.log("EmdkBarCodeReaderPlugin.js: initiateEMdk");
        exec(successCallback, errorCallback, "EmdkBarcodeReaderPlugin", "intiateEmdk", []);
    },
    /**
    * Starts the scanning process allowing the user to scan and read data
    */
    startScanner : function(barcodeReadCallback, barcodeFailureCallback)
    {
        console.log("EmdkBarCodeReaderPlugin.js: startScanner");
        exec(barcodeReadCallback, barcodeFailureCallback, "EmdkBarcodeReaderPlugin", "StartScanner", [])
    },
    /**
    * Stops the scanner if need be
    */
    stopScanner : function(barcodeReadCallback, barcodeFailureCallback)
    {
    console.log("EmdkBarCodeReaderPlugin.js: stopScanner");
    exec(barcodeReadCallback, barcodeFailureCallback, "EmdkBarcodeReaderPlugin", "StopScanner", [])
    }


}


//var emdkBarcodeReaderPlugin = new EmdkBarCodeReaderPlugin();
module.exports = EmdkBarcodeReaderPlugin;
