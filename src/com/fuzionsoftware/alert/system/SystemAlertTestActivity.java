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
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.fuzionsoftware.googleimages.GoogleImageSearch;
import com.fuzionsoftware.imagecaching.UrlImageViewHelper;

public class SystemAlertTestActivity extends Activity {

	private GoogleImageAdapter mAdapter;
	private GridView mGridView;
    private Button mSearchButton;
    private EditText mSearchText;
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
    
    final OnItemClickListener mImageClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
			//Pull back the appropriate bitmap and set that in the service
			ImageView iv = (ImageView) arg1;
			mService.setOverlayImage(iv.getDrawable());
		}
   };    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        mGridView = (GridView) findViewById(R.id.gridview);
        mAdapter = new GoogleImageAdapter(this);
        mGridView.setOnItemClickListener(mImageClickListener);
        mGridView.setAdapter(mAdapter);
        mSearchButton = (Button)findViewById(R.id.search);
        mSearchText = (EditText)findViewById(R.id.search_text);
        mSearchButton.setOnClickListener(new SearchClickListener());
        mSearchText.setOnFocusChangeListener(new SearchTextFocusListener());
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	unbindService(mServiceConnection);
    }
    
    public void hideKeyboard(){
        //hide keyboard :
    	//mSearchText.setInputType(InputType.TYPE_NULL);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
    }
    
    public void showKeyboard(){
    	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    
    @Override
    public void onResume() {
    	super.onPause();
        mSearchButton = (Button)findViewById(R.id.search);
        mSearchText = (EditText)findViewById(R.id.search_text);
    	startService(new Intent(this, OverlayService.class));
    	bindService(new Intent(this, OverlayService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }
    
    private class SearchTextFocusListener implements OnFocusChangeListener
    {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus)
				showKeyboard();
			else
				hideKeyboard();	
		}
    	
    }
    
    private class SearchClickListener implements OnClickListener
    {

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
                                         String searchString = mSearchText.getText().toString();    
                                         hideKeyboard();
                                         int count = 50;
                                        for (String url: GoogleImageSearch.searchGoogleImages(searchString, count)) {
                                            mAdapter.add(url);
                                        }
                                    }
                                });

                        }
                        catch (final Exception ex) {
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
        
    }
    
    private class GoogleImageAdapter extends ArrayAdapter<String> {

        public GoogleImageAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null)
            {
                convertView = imageView = new ImageView(SystemAlertTestActivity.this);
	            imageView.setLayoutParams(new GridView.LayoutParams(150, 150));
	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	            imageView.setPadding(8, 8, 8, 8);
            }
            else
            	imageView = (ImageView)convertView;
                     
            while (imageView.getDrawable() == null)
            {
              UrlImageViewHelper.setUrlDrawable(imageView, getItem(position));
        	  remove(getItem(position));  
            }
            return imageView;
        }
    }

    }


