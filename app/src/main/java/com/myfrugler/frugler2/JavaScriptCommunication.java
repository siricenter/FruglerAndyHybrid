package com.myfrugler.frugler2;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;


import com.android.vending.billing.IInAppBillingService;

import org.json.JSONObject;

import quickconnect.family.json.JSONException;
import quickconnect.family.json.JSONParser;
import quickconnect.family.json.JSONUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Created by lee on 10/21/15.
 */
public class JavaScriptCommunication extends HybridActivity{
    Activity theActivity;
    WebView containingWebView;

    IInAppBillingService mService;
    ServiceConnection mServiceConn;

    String purchaseError = "false";
    String email = "";
    String ePass = "";

    String google = "https://www.google.com/";
    String theURL = "http://ec2-54-152-204-90.compute-1.amazonaws.com/app/";
    String nativeURL = "file:///android_asset/index.html";

    String subID = "com.myfrugler.frugler.monthly";
//    String subID = "android.test.purchased";

    public JavaScriptCommunication() {
        // Intentionally left blank
    }

    public JavaScriptCommunication(Activity theActivity, WebView containingWebView) {
        this.theActivity = theActivity;
        this.containingWebView = containingWebView;

        changeURL(nativeURL);

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }
        };

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");

        //TODO: Possible fix found @ http://stackoverflow.com/questions/24480069/google-in-app-billing-illegalargumentexception-service-intent-must-be-explicit
        serviceIntent.setPackage("com.android.vending");

        theActivity.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        //TODO: I think we need to unbind this at some point. See "onDestroy" @ http://developer.android.com/google/play/billing/billing_integrate.html

    }

    /**
     *
     * @param aMessageAsJSON - the data and an indicator as to what Java code is to be executed.
     *                       There is an optional key, 'callback', that has as its value a
     *                       JavaScript function.
     */
    @JavascriptInterface
    public void postMessage(String aMessageAsJSON){
        if(aMessageAsJSON != null) {
            try {
                HashMap<String, Object> message = (HashMap) JSONUtilities.parse(aMessageAsJSON);
                /*
                 * Evaluate the request to determine what should be done and the data to use to perform the request.
                 * Then do what was requested here.
                 */
                String command = (String)message.get("cmd");
                /*
                 * A simple if-else series of statements is used here. For scalability reasons
                 * you should consider using a verson of the Application Controller Pattern.
                 * A version of it is available at https://github.com/yenrab/qcnative.
                 * Use the Java/Android version by including the source files in your project.
                 */
                Serializable response = null;
                if(command.equals("increment")){
                    long count = (long)message.get("count");
                    count++;
                    HashMap<String,Object> dataMap = new HashMap<>();
                    dataMap.put("count",count);
                    response = dataMap;
                }else if (command.equals("requestMonthlyPurchase")){
                    // Handle user info stuff here
                    Log.d("TestButton", "Hello");
                    email = (String)message.get("email");
                    ePass = (String)message.get("ePass");
                    Log.d("DeBug - TestButton", email);
                    Log.d("DeBug - TestButton", ePass);
                    HashMap<String,Object> dataMap = new HashMap<>();
                    dataMap.put("user_email", email);
                    dataMap.put("ePass", ePass);
                    response = dataMap;

//                    HashMap<String, Object> user = (HashMap)message.get("userinfo");
////                    Log.d("DeBug - TestButton", (String) user.get("name"));
//                    Log.d("DeBug - TestButton", (String)user.get("email"));
//                    Log.d("DeBug - TestButton", (String) user.get("pass"));

                    try {
                        purchaseSub();
                    } catch (Exception e) {
                        System.out.println("ERROR: " + e);
                    }
//                    response = 0;
                } else if (command.equals("onload")){
                    Log.d("DeBug - TestLoad", "ONLOAD");

                    HashMap<String,Object> dataMap = new HashMap<>();
                    dataMap.put("purchaseError", purchaseError);
                    dataMap.put("user_email", email);
                    response = dataMap;
                } else if (command.equals("errorMsg")) {
                    String display = (String)message.get("msg");
                    System.out.println("ERROR MESSAGE: " + display);
                    Toast.makeText(theActivity, display, Toast.LENGTH_SHORT).show();
                } else if(command.equals("load_page")){
                    final String theURL = (String)message.get("url");
                    changeURL(theURL);
                } else if(command.equals("log")) {
                    String display = (String)message.get("string");
                    System.out.println("JS: " + display);
                }

                String asyncCallback = (String) message.get("callbackFunc");
                //after performing the request call sendResponse.
                sendResponse(response, asyncCallback);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void changeURL(final String theURL) {
        System.out.println("Debug - Changing url to " + theURL);
        if(theURL != null && theURL.trim().length() != 0){
            final WebView theWebView = this.containingWebView;
            this.containingWebView.post(new Runnable() {
                @Override
                public void run() {
                    theWebView.loadUrl(theURL);
                }
            });

        }
    }

    /**
     *
     * @param aResponse - the data for the response
     * @param callback - a JavaScript callback function (declared or anonymous)
     * @throws Exception - If there is a callback function, but the required 'responseAsJSON' paramter is missing, this Exception is thrown.
     */
    public void sendResponse(Serializable aResponse, String callback) throws JSONException {
        if(aResponse != null && callback != null) {
            //Turn the data to send back to the JavaScript into a string.
            String responseAsJSON = JSONUtilities.stringify(aResponse);
            //turn the callback into something that can be executed imediately in JavaScript.
            final String changedCallback = "(".concat(callback).concat("('").concat(responseAsJSON).concat("'))");
            //make sure to execute all methods of WebViews on the UI thread.
            this.theActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //execute the JavaScript callback
                    JavaScriptCommunication.this.containingWebView.evaluateJavascript(changedCallback, null);
                }
            });
        }
    }

    // Payment Methods

    public void displayPurchase() {
        Log.d("DeBug - TestPurchase", "Purchased");
        onDestroy();
    }

    public void purchaseSub() {
        ArrayList skuList = new ArrayList();
        skuList.add(subID);
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
        Bundle skuDetails;
        try {
            System.out.println("First: " + skuList.get(0));
            System.out.println("Mine: " + querySkus);
            System.out.println("getPackageName: " + theActivity.getPackageName());

            skuDetails = mService.getSkuDetails(3, theActivity.getPackageName(), "subs", querySkus);
            System.out.println("skuDetails: " + skuDetails);
            int response = skuDetails.getInt("RESPONSE_CODE");

            System.out.println("response: " + response);
            // Could contact google servers. We are ready to make a purchase.
            if (response == 0) {
                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                System.out.println("responseList: " + responseList);

                for (String thisResponse : responseList) {
                    JSONObject object = new JSONObject(thisResponse);
                    String sku = object.getString("productId");
                    String price = object.getString("price");

                    System.out.println("sku = subID" + sku.equals(subID));

                    // check that the purchase equals "com.myfrugler.frugler.monthly"
                    if (sku.equals(subID)) {
                        purchaseError = "false";

                        System.out.println("DeBug - Product sku:   " + sku);
                        System.out.println("DeBug - Product Price: " + price);

                        // if successful adds the purchase to the user
                        Bundle buyIntentBundle = mService.getBuyIntent(3,
                                theActivity.getPackageName(), sku, "subs",
                                "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");

                        if (buyIntentBundle.getInt("RESPONSE_CODE") == 0){

                            System.out.println("buyIntentBundle: BILLING_RESPONSE_RESULT_OK (" +
                                    buyIntentBundle.get("RESPONSE_CODE") + ")");

                            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                            System.out.println("pendingIntent: " + pendingIntent);

//                                // TODO: check that this is getting called... should load www.google.com
//                                theActivity.startIntentSenderForResult(pendingIntent.getIntentSender(), 1001,
//                                        new Intent(), Integer.valueOf(0),
//                                        Integer.valueOf(0), Integer.valueOf(0));

//                                // TODO: check that this is getting called... should load our aws login screen
//                                startIntentSenderForResult(pendingIntent.getIntentSender(), 1001,
//                                        new Intent(), Integer.valueOf(0),
//                                        Integer.valueOf(0), Integer.valueOf(0));

                                // TODO: check that this is getting called... should load our aws frugles page after login
                                autoLogin();

                        } else {
                            // ERROR making a purchase
                            throw new Exception("ERROR: Purchase failed");
                        }

                    }
                }
            } else {
                // ERROR could not make a cunnection to google servers
                System.out.println("DeBug - ERROR: Cannot connect to google servers.");
                purchaseError = "true";
                changeURL(nativeURL);
            }
        } catch (Exception e) {
            // Something else failed somewhere
            Log.e("DeBug - ERROR", "unexpected exception", e);
            e.printStackTrace();
            purchaseError = "true";
            changeURL(nativeURL);
        }
    }

    /**
     * autoLogin() -
     *      This reads the current un-consumed products owned by the user
     *      for the "com.myfrugler.frugler.montly" item specifically and
     *      then directs them to the right screen based on the purchase
     *      state (i.e., purchased, canceled, or refunded.)
     */
    public void autoLogin() {
        try {
            System.out.println("Debug - AutoLogin Method");
            // Make the call to play store
            Bundle ownedItems = mService.getPurchases(3, theActivity.getPackageName(), "subs", null);

            System.out.println("ownedItems: " + ownedItems);
            int ownedResponse = ownedItems.getInt("RESPONSE_CODE");
            System.out.println("ownedResponse: " + ownedResponse);

            // We were able to connect to google servers.
            if (ownedResponse == 0) {
                // get the purchased items in an arraylist
                ArrayList ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                // get the data for each owned item (there should be only one owned item in theory if a purchase was made)
                ArrayList purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                // get the signatures of purchases from this app
                ArrayList signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
                // Currently not needed, see table 6 of -> http://developer.android.com/google/play/billing/billing_reference.html#getPurchases
                String continuationToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                System.out.println("continuationToken: " + continuationToken);
                System.out.println("purchaseDataList.size: " + purchaseDataList.size());

                // If there was no product purchased throw error
                if (purchaseDataList.size() > 0) {

                    // check each purchase for
                    for (int i = 0; i < purchaseDataList.size(); ++i) {

                        // Debug - Check values
                        System.out.println("getPurchases() - \"INAPP_PURCHASE_ITEM_LIST\" return " + (ownedSkus != null ? ownedSkus.get(i) : "null"));
                        System.out.println("getPurchases() - \"INAPP_PURCHASE_DATA_LIST\" return " + purchaseDataList.toString());
                        System.out.println("getPurchases() - \"INAPP_DATA_SIGNATURE\" return " + (signatureList != null ? signatureList.toString() : "null"));
                        System.out.println("getPurchases() - \"INAPP_CONTINUATION_TOKEN\" return " + (continuationToken != null ? continuationToken : "null"));

                        // Check that the ownedSku is not null
                        String itemSub = (String) (ownedSkus != null ? ownedSkus.get(i) : null);
                        System.out.println("this: " + Objects.equals(itemSub, subID));

                        // Check if play store product matches our product
                        if (Objects.equals(itemSub, subID)) {
                            String purchaseData = purchaseDataList.get(i).toString();
                            JSONObject purchaseStateOBJ = new JSONObject(purchaseData);
//                            boolean purchaseState = purchaseStateOBJ.getBoolean("autoRenewing");
//                            if (purchaseState) {
//                                // Change url to our url
//                                System.out.println("Debug - user subscription status is '" + purchaseState + "'");
//                                purchaseError = "false";
//                                changeURL(theURL + "?email=\'" + email + "\'&password=\'" + ePass + "\'");
//                            } else {
//                                // Stay at current Registration url
//                                throw new Exception("Error: user subscription status is '" + purchaseState + "'");
//                            }

                            int purchaseState = purchaseStateOBJ.getInt("purchaseState");
                                System.out.println("Debug - Purchase State: " + purchaseState);
                                if (purchaseState == 0) {
                                    // Change url to our url
                                    // TODO: fix the call to our site
                                    purchaseError = "false";
                                    changeURL(theURL + "?email=\'" + email + "\'&password=\'" + ePass + "\'");
                                } else if (purchaseState == 1) {
                                    // Canceled
                                    purchaseError = "true";
                                    // Stay at current Registration url
                                    changeURL(nativeURL);
                                } else {
                                    // Refunded
                                    purchaseError = "true";
                                    // Stay at current Registration url
                                    changeURL(nativeURL);
                            }
                        }
                    }
                } else {
                    throw new Exception("Error: No products purchased");
                }
            } else {
                System.out.println("Debug - Cannot connect to Play Store");
                // Stay at current Registration url
                purchaseError = "true";
                changeURL(nativeURL);
            }
        } catch (Exception e) {
            System.out.println("Debug - Failed to Connect, exception caught");
            e.printStackTrace();
            // Stay at current Registration url
            purchaseError = "true";
            changeURL(nativeURL);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString(subID);

                    System.out.print("You subscribed to " + sku + "!");

                    // After purchase change the url to our url
                    purchaseError = "false";
                    changeURL(theURL);
                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Sub purchase failed. :(");
                // Stay at current Registration url
                purchaseError = "true";
                changeURL(nativeURL);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

}

