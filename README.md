#EMDK Barcode Reader Plugin for Android#

This is a barcode reader plugin which uses Zebra's EMDK sdk.

##Supported platforms##
Android


##Adding the plugin##
There are two ways in which this plugin can be installed into the android project.<br />


###Adding the plugin through the command line

1.Navigate to the folder where the root config.xml is present.  
2.Open a command prompt window  
3.Run the command  ***` cordova plugin add https://github.dev.global.tesco.org/31StoreStock/Newman_App_plugins.git`***  
   

###Adding through config.xml


In order to add the plugin to the Android project , in the root config.xml file add the following line .

```
<plugin name="EmdkBarcodeReaderPlugin" source="git" spec="https://github.dev.global.tesco.org/31StoreStock/Newman_App_plugins.git"/>

```

Open a comnmand prompt in the same folder where the root config.xml is present and type 

  ***`cordova plugin update. `***

Rebuild the android project. The emdk Plugin would be accessable to the project for usage.


##Usage##

In the www folder, the javascript file EmdkBarcodeReaderPlugin.js acts as a bridge between javascript layer and the native layer. It exposes three methods to access the EMDK plugin.

***initiateEMdk***

***startScanner*** 

***stopScanner***


**initiateEMdk** : This method initializes the EMDK Manager , it has to be the first call made before accessing the scanner . 

 ***`EmdkBarcodeReaderPlugin.initiateEMdk("Success call back function", "failure callback function");`***
 
 Example: </br>
```
var successFunc = function(){
 
   console.log("Scanner Initialization is successful");
   
   }
 
 ```

  ```
var failureFunc = function(String failureMessage){ 
 
console.log("Failure in initalization "+failureMessage"); 

}
 ```


 
  ***`EmdkBarcodeReaderPlugin.initiateEMdk(successFunc,failureFunc);`***  <br/>
  
 We get the control in the ***`successFunc`*** if the operation is successful or in ***`failureFunc`*** if it is a failure. Necessary actions can be performed depending on the result.
  
  
  
  **startScanner**: This method has to be only called after initiating the EMDK manager or else it won't be effective. In the same way as mentioned in the above example this call results in obtaining the Scanner object through which allows the user to scan and read the data. The success callback sent for the startScanner method will provide the user with the scanned data in the string format for every successful read.</br>
   Example: </br>

```
 var barCodeReadSuccessFunc = function(String barcodeValue){
 
 console.log("The Barcode is: " + barcodeValue) };
 
 ```
 
 ```
var failureFunc = function(String failureMessage){
 
 console.error("Scan failed: " + failureMessage);  
 }
 ```
  ***`EmdkBarcodeReaderPlugin.startScanner(barCodeReadSuccessFunc,failureFunc);`***  <br/>
  
  
  **stopScanner**: This is an optional method which can be used to prevent the scanning in particular pages if need be.
  
  Example: </br>
  
 ```
var stoppedSuccess = function(){
 
  console.error("Success in stopping the scanner "); 
  }
```
   
   

```
 var failureFunc = function(String failureMessage){ 
 
   console.error("Stopping scanner failed : " + failureMessage); 
   
   }
   
```

 
  ***`EmdkBarcodeReaderPlugin.stopScanner(stoppedSuccess,failureFunc);`***  <br/>


















