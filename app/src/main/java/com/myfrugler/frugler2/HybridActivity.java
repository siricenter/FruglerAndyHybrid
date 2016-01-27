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
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONObject;

import java.util.ArrayList;

import quickconnect.family.json.JSONException;

public class HybridActivity extends AppCompatActivity {

    WebView theWebView;

    public static String PACKAGE_NAME;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hybrid);
        theWebView = (WebView) findViewById(R.id.theWebView);
        theWebView.addJavascriptInterface(new JavaScriptCommunication(this, theWebView), "native");
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
}
