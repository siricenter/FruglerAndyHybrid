package com.myfrugler.frugler2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class HybridActivity extends AppCompatActivity {

    WebView theWebView;

    public static String PACKAGE_NAME;

    JavaScriptCommunication jsc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hybrid);
        theWebView = (WebView) findViewById(R.id.theWebView);
        theWebView.addJavascriptInterface(jsc = new JavaScriptCommunication(this, theWebView), "native");
        theWebView.getSettings().setJavaScriptEnabled(true);
        theWebView.getSettings().setDomStorageEnabled(true);
//        theWebView.loadUrl("file:///android_asset/index.html");
//        theWebView.loadUrl("https://www.google.com/");
        PACKAGE_NAME = getPackageName();
        System.out.println("PACKAGE_NAME: " + PACKAGE_NAME);


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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: figure out how to put this into JavaScriptCommunication.java
        System.out.println("Debug - Check that HybridActivity onActivityResult is called");
//        .onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            System.out.println("resultCode aonActivityResult: " + resultCode);
            System.out.println("resultCode aonActivityResult: " + RESULT_OK);
            if (resultCode == RESULT_OK) {
                System.out.print("You subscribed to com.myfrugler.frugler2.monthly!");

                // After purchase change the url to our url
                jsc.setPurchaseError(false);
                System.out.println("Test purchaseError: " + jsc.getPurchaseError());
                jsc.changeURL(jsc.getUserURL());
            } else {
                System.out.println("Sub purchase failed. :(");
                // Stay at current Registration url
                jsc.setPurchaseError(true);
                System.out.println("Test purchaseError: " + jsc.getPurchaseError());
                jsc.changeURL(jsc.getNativeURL());
            }
        }

//
//        if (requestCode == 1001) {
//            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
//
//            if (resultCode == RESULT_OK) {
//                try {
//                    JSONObject jo = new JSONObject(purchaseData);
//                    String sku = jo.getString("com.myfrugler.frugler2.monthly");
//
//                    System.out.print("You subscribed to " + sku + "!");
//
//                    // After purchase change the url to our url
////                    changeURL(theURL);
//                    theWebView.loadUrl("https://www.google.com/");
//
//                } catch (org.json.JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                System.out.println("Sub purchase failed. :(");
//            }
//        }
//    }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (jsc.getmService() != null) {
            unbindService(jsc.getmServiceConn());
        }
    }
}
