package com.fuzionsoftware.alert.system;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
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

import com.fuzionsoftware.googleimages.GoogleImageResponse;
import com.fuzionsoftware.googleimages.GoogleImageResponse.GoogleImageResult;
import com.fuzionsoftware.imagecaching.UrlImageViewHelper;
import com.google.gson.Gson;

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
			if(hasFocus) showKeyboard(); else hideKeyboard();	
		}
    	
    }
    
    private class SearchClickListener implements OnClickListener
    {
            @Override
            public void onClick(View v) {
            	hideKeyboard();
            	new GoogleImageSearch().execute(mSearchText.getText().toString());
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
            else{
            	imageView = (ImageView)convertView;
            }
            UrlImageViewHelper.setUrlDrawable(imageView, getItem(position));
            /* TODO: Clean up all null images; This code does that, but is jankity     
            while (true)
            {
              UrlImageViewHelper.setUrlDrawable(imageView, getItem(position));
              if(imageView.getDrawable() == null){
	        	  remove(getItem(position));
	        	  notifyDataSetChanged();
              }else{
            	  break;
              }
            }
            */
            return imageView;
        }
    }
    private class GoogleImageSearch extends AsyncTask<String,Void,ArrayList<String>> {

    	protected void onPreExecute (){
    		mAdapter.clear();
    	}
    	@Override
    	protected ArrayList<String> doInBackground(String... searchParameters) {
    		ArrayList<String> urls = new ArrayList<String>();
    		int start = 0;
    		int count = 50;
    		try {
    			while(start < count)
    			{
    				//https://developers.google.com/image-search/v1/jsondevguide
    				Uri.Builder uri = new Uri.Builder()
    					.scheme("https").authority("ajax.googleapis.com")
    					.appendEncodedPath("ajax/services/search/images")
    					.appendQueryParameter("v", "1.0")
    					.appendQueryParameter("imgsz", "large")
    					.appendQueryParameter("imgtype", "clipart")
    					/*
    					Specifies the search safety level, which may be one of the following:

    					safe=active enables the highest level of safe search filtering.
    					safe=moderate enables moderate safe search filtering (default).			
    					safe=off disables safe search filtering.
    					*/
    					.appendQueryParameter("safe", "off")
    					.appendQueryParameter("start", Integer.toString(start))
    					.appendQueryParameter("q", searchParameters[0]);
    	
    				URLConnection connection = new URL(uri.toString()).openConnection();
    				
    				//http://stackoverflow.com/questions/309424/in-java-how-do-i-read-convert-an-inputstream-to-a-string
    				String json_results = new java.util.Scanner(connection.getInputStream()).useDelimiter("\\A").next();
    				
    		        GoogleImageResponse gImages = new Gson().fromJson(json_results, GoogleImageResponse.class);
    		        
    		        for(GoogleImageResult result : gImages.responseData.results)
    		        	urls.add(result.url);
    		        
    		        start += gImages.responseData.results.length;
    			}
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		return urls;
    	}
    	
    	protected void onPostExecute (ArrayList<String> results){
    		for(String s: results)
    			mAdapter.add(s);
    		mAdapter.notifyDataSetChanged();
    	}
    }
    }


