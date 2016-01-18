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

    String subID = "com.myfrugler.frugler.monthly";
//    String subID = "android.test.purchased";

    public JavaScriptCommunication(Activity theActivity, WebView containingWebView) {
        this.theActivity = theActivity;
        this.containingWebView = containingWebView;

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
        theActivity.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
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
                    HashMap<String, Object> user = (HashMap)message.get("userinfo");
                    Log.d("DeBug - TestButton", (String) user.get("name"));
                    Log.d("DeBug - TestButton", (String)user.get("email"));
                    Log.d("DeBug - TestButton", (String) user.get("pass"));

                    try {
                        purchaseSub();
                    } catch (Exception e) {
                        System.out.println("ERROR: " + e);
                    }

                } else if(command.equals("onload")){
                    Log.d("DeBug - TestLoad", "ONLOAD");

                    try {
                        HashMap<String,String> dataMap = new HashMap<>();
                        Bundle ownedItems = mService.getPurchases(3, theActivity.getPackageName(), "subs", null);
                        System.out.println("ownedItems: " + ownedItems);
                        int ownedResponse = ownedItems.getInt("RESPONSE_CODE");
                        System.out.println("ownedResponse: " + ownedResponse);
                        if (ownedResponse == 0) {
                            ArrayList ownedSkus =
                                    ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                            ArrayList purchaseDataList =
                                    ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                            ArrayList signatureList =
                                    ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
                            String continuationToken =
                                    ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                            System.out.println("continuationToken: " + continuationToken);
                            System.out.println("purchaseDataList.size: " + purchaseDataList.size());
                            for (int i = 0; i < purchaseDataList.size(); ++i) {
                                System.out.println("getPurchases() - \"INAPP_PURCHASE_ITEM_LIST\" return " + (ownedSkus != null ? ownedSkus.get(i) : "null"));
                                System.out.println("getPurchases() - \"INAPP_PURCHASE_DATA_LIST\" return " + purchaseDataList.toString());
                                System.out.println("getPurchases() - \"INAPP_DATA_SIGNATURE\" return " + (signatureList != null ? signatureList.toString() : "null"));
                                System.out.println("getPurchases() - \"INAPP_CONTINUATION_TOKEN\" return " + (continuationToken != null ? continuationToken : "null"));
                                String itemSub = (String) (ownedSkus != null ? ownedSkus.get(i) : null);
                                System.out.println("this: " + Objects.equals(itemSub, subID));
                                if (Objects.equals(itemSub, subID)) {
//                                    continuationToken = "apples";
                                    response = (continuationToken != null ? continuationToken : null);
                                    System.out.print("response: " + response);
                                    dataMap.put("token", response != null ? response.toString() : null);
                                    response = dataMap;
                                }
//                                dataMap.put("token", "Apples!!!");
//                                response = dataMap;
                            }
                        } else {
                            dataMap.put("token", null);
                            response = dataMap;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (command.equals("errorMsg")) {
                    String display = (String)message.get("msg");
                    System.out.println("ERROR MESSAGE: " + display);
                    Toast.makeText(theActivity, display, Toast.LENGTH_SHORT).show();
                } else if(command.equals("load_page")){
                    final String theURL = (String)message.get("url");
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

                String asyncCallback = (String) message.get("callbackFunc");
                //after performing the request call sendResponse.
                sendResponse(response, asyncCallback);

            } catch (Exception e) {
                e.printStackTrace();
            }
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
            if (response == 0) {
                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                System.out.println("responseList: " + responseList);

                for (String thisResponse : responseList) {
                    JSONObject object = new JSONObject(thisResponse);
                    String sku = object.getString("productId");
                    String price = object.getString("price");

                    System.out.println("sku = subID" + sku.equals(subID));

                    if (sku.equals(subID)) {
                        System.out.println("DeBug - Product sku:   " + sku);
                        System.out.println("DeBug - Product Price: " + price);

                        Bundle buyIntentBundle = mService.getBuyIntent(3,
                                theActivity.getPackageName(), sku, "subs",
                                "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");

                        System.out.println("buyIntentBundle: BILLING_RESPONSE_RESULT_OK (" +
                                buyIntentBundle.get("RESPONSE_CODE") + ")");

                        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                        System.out.println("pendingIntent: " + pendingIntent);

                        theActivity.startIntentSenderForResult(pendingIntent.getIntentSender(), 1001,
                                new Intent(), Integer.valueOf(0),
                                Integer.valueOf(0), Integer.valueOf(0));
                    }
                }
            }

        } catch (Exception e) {
            Log.e("DeBug- ERROR", "unexpected exception", e);
            e.printStackTrace();
        }
    }

    public void login() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString(subID);

                    System.out.print("You have bought the " + sku + ".");
                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}

