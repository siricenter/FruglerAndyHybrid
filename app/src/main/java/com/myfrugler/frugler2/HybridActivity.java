package com.myfrugler.frugler2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONObject;

import java.util.ArrayList;

import quickconnect.family.json.JSONException;

public class HybridActivity extends AppCompatActivity {

    WebView theWebView;

    IInAppBillingService mService;
    String inAppID = "com.myfrugler.fruger.monthly";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hybrid);
        theWebView = (WebView) findViewById(R.id.theWebView);
        theWebView.addJavascriptInterface(new JavaScriptCommunication(this, theWebView), "native");
        theWebView.getSettings().setJavaScriptEnabled(true);
        theWebView.loadUrl("file:///android_asset/index.html");


        ServiceConnection mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }
        };

        Log.d("DeBug - TestLoad", "ONLOAD");
        bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"),
                mServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hybrid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void purchaseSub() {
        Log.d("Test", "Test1");
        ArrayList skuList = new ArrayList();
        Log.d("Test", "Test2");
        skuList.add(inAppID);
        Log.d("Test", "Test3");
        Bundle querySkus = new Bundle();
        Log.d("Test", "Test4");
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
        Log.d("Test", "Test5");
        Bundle skuDetails;
        Log.d("Test", "Test6");
        try {
            skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
            int response = skuDetails.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                for (String thisResponse : responseList) {
                    JSONObject object = new JSONObject(thisResponse);
                    String sku = object.getString("productId");
                    String price = object.getString("price");
                    if (sku.equals(inAppID)) {
                        Log.d("DeBug - Product", "Price" + sku);
                        Log.d("DeBug - Product", "Price" + price);
                    }
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("DeBug- ERROR", "unexpected exception", e);
        }
    }
}
