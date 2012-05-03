package com.fuzionsoftware.alert.system;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

public class OverlayService extends Service implements SensorEventListener{
  	private SensorManager mSensorMgr;
    private long mLastUpdate = -1;
    private float mX, mY, mZ;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 800;
	
	private final IBinder mBinder = new LocalBinder();
	
    private Bitmap mOverlayImage;
    private int mOpacity = 70;
    private View mView;
    private boolean mOverlayEnabled = false;
    
	@Override
	public IBinder onBind(Intent intent) {
		Toast.makeText(this, "Service binded...", Toast.LENGTH_LONG).show();
		return mBinder;
	}
	
	@Override
	public void onCreate() {
	    super.onCreate();
	    Toast.makeText(this, "Service created...", Toast.LENGTH_LONG).show();
	    enableMotion();
	    mView = setBitmapOverlay();
	}
	
	private void enableMotion()
	{
		 // start motion detection 
		 mSensorMgr=(SensorManager) getSystemService(SENSOR_SERVICE); 
		 mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private void toggleOverlay()
	{
		WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		if(!mOverlayEnabled)
		{
			WindowManager.LayoutParams mWmlp = new WindowManager.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			mWmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
			mWmlp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			mWmlp.format = PixelFormat.TRANSPARENT;
			mWindowManager.addView(mView, mWmlp);
			mOverlayEnabled = true;
			Toast.makeText(this, "Setting view on ", Toast.LENGTH_SHORT).show();
		}else{
			mWindowManager.removeView(mView);
			mOverlayEnabled = false;
			Toast.makeText(this, "Setting view off...", Toast.LENGTH_SHORT).show();
		}
	}
	
	public View setBitmapOverlay()
	{
		ImageView iv = new ImageView(this);
		Drawable img = this.getApplicationInfo().loadIcon(this.getPackageManager());
		img.setAlpha(mOpacity);
		iv.setImageDrawable(img);
		return iv;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		 if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){ 
			 	long curTime = System.currentTimeMillis(); // only allow one update every 500ms. 
			 	if ((curTime - mLastUpdate) > 100) { 
			 		long diffTime = (curTime - mLastUpdate); 
			 		mLastUpdate = curTime; mX = event.values[0]; 
			 		mY = event.values[1]; mZ = event.values[2]; 
			 		float speed = Math.abs(mX+mY+mZ - last_x - last_y - last_z) / diffTime * 10000; 
			 		if (speed > SHAKE_THRESHOLD) { 
			 			// yes, this is a shake action! Do something about it! 
			 			//Toast.makeText(this, "Shake it like a polaroid picture...", Toast.LENGTH_LONG).show();
			 			toggleOverlay();
			 		} 
			 		last_x = mX; 
			 		last_y = mY; 
			 		last_z = mZ; 
			 	} 
			 } 		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	
	
	class LocalBinder extends Binder {
		
		public void setOverlayImage()
		{
			
		}
			
	}
	
	
}
