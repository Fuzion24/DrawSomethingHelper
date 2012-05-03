package com.fuzionsoftware.alert.system;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.net.UrlQuerySanitizer.ValueSanitizer;
import android.os.Bundle;
import android.os.IBinder;

public class SystemAlertTestActivity extends Activity {

	private OverlayService.LocalBinder mService;
	private ServiceConnection mServiceConnection = new ServiceConnection() {	
		@Override
		public void onServiceConnected(ComponentName name, IBinder srv) {
			mService = (OverlayService.LocalBinder) srv;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
    };
    
    public void searchGoogleImages(String searchParameters)
    {
		try {
			
			Uri.Builder uri = new Uri.Builder()
				.scheme("https").authority("ajax.googleapis.com")
				.appendEncodedPath("ajax/services/search/images")
				.appendQueryParameter("v", "1.0")
				.appendQueryParameter("q", searchParameters);

			URLConnection connection = new URL(uri.toString()).openConnection();
			String line = new java.util.Scanner(connection.getInputStream()).useDelimiter("\\A").next();
			JSONObject json = new JSONObject(line);
			//now have some fun with the results...
			
			json.get("test");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	unbindService(mServiceConnection);
    }
    
    @Override
    public void onResume() {
    	super.onPause();
    	startService(new Intent(this, OverlayService.class));
    	bindService(new Intent(this, OverlayService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    	//mService.setOverlayImage();
    	searchGoogleImages("fuzzy monkey");
    }
    
    }


