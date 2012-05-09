package com.fuzionsoftware.alert.system;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

public class OverlayService extends Service implements SensorEventListener{
  	private SensorManager mSensorMgr;
    private long mLastUpdate = -1;
    private long mLastChanged = -1;
    private float mX, mY, mZ;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 800;
	
	private final IBinder mBinder = new LocalBinder();
	
    private Drawable mOverlayDrawable;
    /* 0 means fully transparent, and 255 means fully opaque*/
    private int mOpacity = 70;
    private static View mOverlayView;
    private boolean mOverlayEnabled = false;
    
    
    //Notification Stuff
	private Notification mDisableServiceNotification;
    private NotificationManager mNotificationManager;
    private int SIMPLE_NOTIFICATION_ID = 0x1337;
    
	@Override
	public IBinder onBind(Intent intent) {
		//Toast.makeText(this, "Service binded...", Toast.LENGTH_LONG).show();
		return mBinder;
	}
	
	@Override
	public void onCreate() {
	    super.onCreate();
	    //Toast.makeText(this, "Service created...", Toast.LENGTH_LONG).show();
	    enableMotion();
		if(mOverlayView == null)
			createOrUpdateImageView();
		
		mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
		enableOverlayNotification();
	}
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		disableOverlayNotification(intent);
	}
	
	private void enableMotion()
	{
		 // start motion detection 
		 mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE); 
		 mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private void disableMotion()
	{
		mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE); 
		mSensorMgr.unregisterListener(this);
	}
	
	private void setOverlayEnabled(boolean enabled)
	{
		WindowManager mWindowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
		if(enabled)
		{
			WindowManager.LayoutParams mWmlp = new WindowManager.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			mWmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
			mWmlp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			mWmlp.format = PixelFormat.TRANSPARENT;
			try{ mWindowManager.addView(mOverlayView, mWmlp); }catch(IllegalStateException e){/* View already attached to the window manager*/}	
			mOverlayEnabled = true;
			//Toast.makeText(this, "Setting view on ", Toast.LENGTH_SHORT).show();
		}else{
			try{ mWindowManager.removeView(mOverlayView); }
			catch(IllegalArgumentException e){
				/* View not attached to the window manager*/
				System.out.println("");
				}			
			mOverlayEnabled = false;
			//Toast.makeText(this, "Setting view off...", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void enableOverlayNotification()
	{
		Context context = getApplicationContext();
		Resources resources = context.getResources();
		CharSequence contentTicker = resources.getText(R.string.disable_overlay_notification_ticker);
		CharSequence contentTitle = resources.getText(R.string.disable_overlay_notification_title);
		CharSequence contentText = resources.getText(R.string.disable_overlay_notification_text);
		
		Intent notifyIntent = new Intent(context,OverlayService.class);
		notifyIntent.putExtra("DisableService", true);
		PendingIntent intent = 	PendingIntent.getService(context, 0, notifyIntent, 0);

		mDisableServiceNotification = new Notification(R.drawable.drawsomething_helper,contentTicker,System.currentTimeMillis());
		mDisableServiceNotification.setLatestEventInfo(context,contentTitle,contentText, intent);
		mNotificationManager.notify(SIMPLE_NOTIFICATION_ID, mDisableServiceNotification);
	}
	
	private void disableOverlayNotification(Intent intent)
	{
		if(intent == null)
			return;
		
		Bundle bundle = intent.getExtras();

		if(bundle != null && bundle.getBoolean("DisableService"))
		{				
			mNotificationManager.cancel(SIMPLE_NOTIFICATION_ID);
			disableMotion();
			setOverlayEnabled(false);			
			stopSelf();
		}
		
	}
	
	public void createOrUpdateImageView()
	{
		if(mOverlayDrawable == null)
			mOverlayDrawable = this.getApplicationInfo().loadIcon(this.getPackageManager());
		
		if(mOverlayView == null)
		{
			LayoutInflater vi = (LayoutInflater) getSystemService(Service.LAYOUT_INFLATER_SERVICE);
			mOverlayView = vi.inflate(R.layout.overlay_layout, null);
			ImageView shakeToRemoveImageView = (ImageView) mOverlayView.findViewById(R.id.shake_to_remove);
			Drawable shakeToRemoveDrawable = getApplicationContext().getResources().getDrawable(R.drawable.shake_to_remove);
			shakeToRemoveDrawable.setAlpha(mOpacity);
			shakeToRemoveImageView.setImageDrawable(shakeToRemoveDrawable);
		}
		
		ImageView overlayImageView = (ImageView) mOverlayView.findViewById(R.id.overlay_image);
		mOverlayDrawable.setAlpha(mOpacity);
		overlayImageView.setImageDrawable(mOverlayDrawable);
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		 if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){ 
			 	long curTime = System.currentTimeMillis(); // only allow one update every 100ms. 
			 	if ((curTime - mLastUpdate) > 100) { 
			 		long diffTime = (curTime - mLastUpdate); 
			 		mLastUpdate = curTime; mX = event.values[0]; 
			 		mY = event.values[1]; mZ = event.values[2]; 
			 		float speed = Math.abs(mX+mY+mZ - last_x - last_y - last_z) / diffTime * 10000; 
			 		if (speed > SHAKE_THRESHOLD) { 
			 			//Make sure that the last update was more than a second ago
			 			if((curTime - mLastChanged) > 1000) 
			 			{
			 				mLastChanged = System.currentTimeMillis();
			 				setOverlayEnabled(!mOverlayEnabled);
			 				mOverlayEnabled = !mOverlayEnabled;
			 			}
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
		
		public void setOverlayImage(Drawable drawable)
		{
			mOverlayDrawable = drawable;
			createOrUpdateImageView();
		}
	}
	
	
}
