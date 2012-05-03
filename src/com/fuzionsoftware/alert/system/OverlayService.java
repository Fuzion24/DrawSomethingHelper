package com.fuzionsoftware.alert.system;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

public class OverlayService extends Service implements SensorEventListener{
  	private SensorManager sensorMgr;
    private long lastUpdate = -1;
    private float x, y, z;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 800;
	
    private View mView;
    private boolean mOverlayEnabled = false;
    
	@Override
	public IBinder onBind(Intent intent) {
		Toast.makeText(this, "Service binded...", Toast.LENGTH_LONG).show();
		return null;
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
		 sensorMgr=(SensorManager) getSystemService(SENSOR_SERVICE); 
		 sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
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
		img.setAlpha(70);
		iv.setImageDrawable(img);
		return iv;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		 if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){ 
			 	long curTime = System.currentTimeMillis(); // only allow one update every 500ms. 
			 	if ((curTime - lastUpdate) > 100) { 
			 		long diffTime = (curTime - lastUpdate); 
			 		lastUpdate = curTime; x = event.values[0]; 
			 		y = event.values[1]; z = event.values[2]; 
			 		float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000; 
			 		if (speed > SHAKE_THRESHOLD) { 
			 			// yes, this is a shake action! Do something about it! 
			 			//Toast.makeText(this, "Shake it like a polaroid picture...", Toast.LENGTH_LONG).show();
			 			toggleOverlay();
			 		} 
			 		last_x = x; 
			 		last_y = y; 
			 		last_z = z; 
			 	} 
			 } 		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	
}
