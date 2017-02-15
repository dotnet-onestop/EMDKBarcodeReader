import android.util.Log;

import com.google.zxing.client.result.EmailDoCoMoResultParser;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerInfo;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by GLH8 on 12/12/2016.
 */

public class EmdkBarcodeReaderPlugin extends CordovaPlugin implements EMDKManager.EMDKListener, Scanner.DataListener, Scanner.StatusListener, BarcodeManager.ScannerConnectionListener {
    private static String TAG = "BarcodeModule";
    private static EmdkBarcodeReaderPlugin EmdkBarcodeReaderPlugin;
    boolean isIdle;
    /*Emdk variables start*/
    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    private List<ScannerInfo> deviceList = null;
    private boolean hasEmdkStarted;

    private static final String STOP_READER = "StopReader";
    private static final String INITIATE_EMDK = "intiateEmdk";
    private static final String START_SCANNER = "StartScanner";
    private static final String REMOVE_BARCODE_LISTENER = "removeBarcodeListener";
    private static final String SET_CONFIG = "SetConfig";
    private static final String STOP_SCANNER = "StopScanner";
    private CallbackContext mCallbackContext;
    CallbackContext callbackContextForSecondtime;

    public EmdkBarcodeReaderPlugin() {

    }

    /**
     * Initializes the EMDK manager , onOpened method is called as a callback function where the EMDK manager object
     * is obtained
     */
    private void initializeEMDK() {
        try {
            if (!hasEmdkStarted) {
                EMDKResults results = EMDKManager.getEMDKManager(this.cordova.getActivity(), this);
                hasEmdkStarted = true;
                if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
                    Log.d(TAG, "Status: " + "EMDKManager object request failed!");
                }
            }
        } catch (Exception e) {
            Log.v(EmdkBarcodeReaderPlugin.getClass().getName().toString(), e.getMessage());
            mCallbackContext.error("Emdk Manager not initialized");
        }
    }

    /**
     * Emdk manager and the barcode manager are uninitialized and the scanner resources are
     * released. This allows other applications to access the scanner hardware.
     */
    void unbindEmdk() {
        // De-initialize scanner
        deInitScanner();
        // Remove connection listener
        if (barcodeManager != null) {
            barcodeManager.removeConnectionListener(this);
            barcodeManager = null;
        }
        // Release all the resources
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
        hasEmdkStarted = false;
    }

    /***
     * It deinitializes the scanner
     */
    private void deInitScanner() {
        if (scanner != null) {
            try {
                scanner.cancelRead();
                scanner.disable();
            } catch (ScannerException e) {
                Log.e(TAG, "Status: " + e.getMessage());
            }
            scanner.removeDataListener(this);
            scanner.removeStatusListener(this);
            try {
                scanner.release();
            } catch (ScannerException e) {
                Log.e(TAG, "Status: " + e.getMessage());
            }
            scanner = null;
        }
    }


    /***
     * This method is called when the "exec" function call is made in the javascript layer
     * Provision has been made to control the scanner characteristics  if need be.
     */
    @Override
    public boolean execute(String action, JSONArray rawArgs, CallbackContext callbackContext) throws JSONException {
        Log.v(TAG, "execute: " + action);
        if (action.equals(INITIATE_EMDK)) {
            mCallbackContext = callbackContext;
            initializeEMDK();
        } else if (action.equals(START_SCANNER)) {
            callbackContextForSecondtime = callbackContext;
            startScanner();
        } else if (action.equals(SET_CONFIG)) {
            //setDecoders();
        } else if (action.equals(STOP_SCANNER)) {
            // stopScanner();
        } else {
            Log.e(TAG, "execute: invalid action '" + action + "'");
            return false;
        }

        return true;
    }

    /***
     * Stops the scanner momentarily, it has not been used but can be exposed if need be.
     */
    private void stopScanner() {
        stopScan();
    }

    private void stopScan() {
        if (scanner != null) {
            try {
                // Cancel the pending read.
                scanner.cancelRead();
                isIdle = false;
            } catch (ScannerException e) {
                Log.e(TAG, "Status: " + e.getMessage());
            }
        }
    }

    /***
     * Starts the scanner . It has to be called only after Scanner object is initialized.
     */
    private void startScanner() {
        startScan();
    }

    private void startScan() {

        if (scanner != null) {
            try {
                // Submit a new read.
                setDecoders();
                isIdle = true;
                scanner.read();
            } catch (ScannerException e) {
                Log.e(TAG, "Scanner Exception Status: " + e.getMessage());
            }
        }
    }

    /***
     * Can be used to handle the resources just as in activity call backs.
     */

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        try {
            if (!hasEmdkStarted) {
                execute(INITIATE_EMDK, new JSONArray(), mCallbackContext);
            }
        } catch (Exception ex) {
            Log.e(EmdkBarcodeReaderPlugin.getClass().getName().toString(), ex.getMessage());
        }

    }

    @Override
    public void onPause(boolean multitasking) {

        super.onPause(multitasking);
        unbindEmdk();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }


    /***
     * Call back function when EMDK manager is started.
     */
    @Override
    public void onOpened(EMDKManager emdkManager) {
        if (emdkManager != null) {
            this.emdkManager = emdkManager;
            barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
            if (enumerateScannerDevices()) {
                if (initScanner()) {
                    mCallbackContext.success();
                } else {
                    mCallbackContext.error("Scanner not initialized");
                }
            } else {
                mCallbackContext.error("No Scanning device found");
            }
        } else {
            mCallbackContext.error("Emdk Manager Not initialized");
        }

    }


    /***
     * this can be used to set the required characteristics if needed
     */
    private void setDecoders() {

        if (scanner != null) {
            try {
                ScannerConfig config = scanner.getConfig();
                //config.readerParams.readerSpecific.cameraSpecific.illuminationMode = ScannerConfig.IlluminationMode.OFF;
                //config.readerParams.readerSpecific.imagerSpecific.illuminationBrightness =3;
                //config.readerParams.readerSpecific.laserSpecific.powerMode = ScannerConfig.PowerMode.OPTIMIZED;

                // Set Supplemental Mode as Auto
                config.decoderParams.upcEanParams.supplementalMode = ScannerConfig.SupplementalMode.AUTO;

                scanner.setConfig(config);
            } catch (ScannerException e) {
                Log.e(TAG, "Status: " + e.getMessage());
            }
        }
    }

    /***
     * Initializes the scanner
     */
    private boolean initScanner() {
        if (scanner == null) {
            if ((deviceList != null) && (deviceList.size() != 0)) {
                scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
            } else {
                Log.e(TAG, "Status: " + "Failed to get the specified scanner device! Please close and restart the application.");
                return false;
            }
            if (scanner != null) {
                scanner.addDataListener(this);
                scanner.addStatusListener(this);
                try {
                    scanner.enable();
                } catch (ScannerException e) {
                    Log.e(TAG, "Status: " + e.getMessage());

                }
            } else {
                Log.e(TAG, "Status: " + "Failed to initialize the scanner device.");
                return false;
            }
        }
        return true;
    }

    private boolean enumerateScannerDevices() {
        if (barcodeManager != null) {
            deviceList = barcodeManager.getSupportedDevicesInfo();
            if ((deviceList == null) || (deviceList.size() == 0)) {
                Log.e(TAG, "Status: " + "Failed to get the list of supported scanner devices! Please close and restart the application.");
                return false;
            }
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void onClosed() {

    }

    /***
     * Call back method when data scanning is success.
     *
     * @param scanDataCollection
     */
    @Override
    public void onData(ScanDataCollection scanDataCollection) {

        String dataString = "";
        if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
            ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
            for (ScanDataCollection.ScanData data : scanData) {
                ScanDataCollection.LabelType labelType = data.getLabelType();
                dataString = data.getData();
                Log.e(TAG, "Barcode Scanned" + dataString);
            }
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, dataString);
            pluginResult.setKeepCallback(true);
            callbackContextForSecondtime.sendPluginResult(pluginResult);
            mCallbackContext.success();

        }
    }


    @Override
    public void onStatus(StatusData statusData) {
        StatusData.ScannerStates state = statusData.getState();
        String statusString = "";
        switch (state) {
            case IDLE:
                statusString = statusData.getFriendlyName() + " is enabled and idle...";
                if (isIdle) {
                    try {
                        // An attempt to use the scanner continuously and rapidly (with a delay < 100 ms between scans)
                        // may cause the scanner to pause momentarily before resuming the scanning.
                        // Hence add some delay (>= 100ms) before submitting the next read.
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException exception) {
                            Log.e(TAG, Log.getStackTraceString(exception));
                        }
                        if (!scanner.isReadPending()) {
                            setDecoders();
                            scanner.read();
                        }
                    } catch (Exception e) {
                        statusString = e.getMessage();
                    }
                }
                break;
            case WAITING:
                statusString = "Scanner is waiting for trigger press...";
                break;
            case SCANNING:
                statusString = "Scanning...";
                break;
            case DISABLED:
                statusString = statusData.getFriendlyName() + " is disabled.";
                break;
            case ERROR:
                statusString = "An error has occurred.";
                break;
            default:
                break;
        }
        Log.d(TAG, "Scanner Status : " + statusString);

    }

    @Override
    public void onConnectionChange(ScannerInfo scannerInfo, BarcodeManager.ConnectionState connectionState) {

    }


}
