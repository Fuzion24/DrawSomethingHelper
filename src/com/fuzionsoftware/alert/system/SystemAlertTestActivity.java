package com.fuzionsoftware.alert.system;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.fuzionsoftware.googleimages.GoogleImageSearch;
import com.fuzionsoftware.googleimages.UrlImageViewHelper;

public class SystemAlertTestActivity extends Activity {

	private MyAdapter mAdapter;
	private ListView mListView;
	private OverlayService.LocalBinder mService;
	private ServiceConnection mServiceConnection = new ServiceConnection() {	
		@Override
		public void onServiceConnected(ComponentName name, IBinder srv) {
			mService = (OverlayService.LocalBinder) srv;
			if(mService != null)
				mService= null;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
    };
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final Button search = (Button)findViewById(R.id.search);
        final EditText searchText = (EditText)findViewById(R.id.search_text);
        
        mListView = (ListView)findViewById(R.id.results);
        mAdapter = new MyAdapter(this);
        mListView.setAdapter(mAdapter);
        search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // background the search call!
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            // clear existing results
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.clear();
                                }
                            });
                            
                                // add the results to the adapter
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                         String searchString = searchText.getText().toString();                                         
                                         int count = 20;
                                        for (String url: GoogleImageSearch.searchGoogleImages(searchString, count)) {
                                            mAdapter.add(url);
                                        }
                                    }
                                });

                        }
                        catch (final Exception ex) {
                            // explodey error, lets toast it
                            runOnUiThread(new Runnable() {
                               @Override
                                public void run() {
                                   Toast.makeText(SystemAlertTestActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
                                } 
                            });
                        }
                    }
                };
                thread.start();
            }
        });
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
    }
    
    
    private class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView iv;
            if (convertView == null)
                convertView = iv = new ImageView(SystemAlertTestActivity.this);
            else
                iv = (ImageView)convertView;
            
            // yep, that's it. it handles the downloading and showing an interstitial image automagically.
            UrlImageViewHelper.setUrlDrawable(iv, getItem(position));
            
            return iv;
        }
    }

    
    
    }


