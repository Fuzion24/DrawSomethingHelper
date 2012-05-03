package com.fuzionsoftware.alert.system;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SystemAlertTestActivity extends Activity {
    /** Called when the activity is first created. */
	private Handler mHandler = new Handler();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler.postDelayed(mUpdateTimeTask, 1000);
    	Context ctx = getApplicationContext();
        ctx.startService(new Intent(ctx, OverlayService.class));
    }
    
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            //AlertDialog d = new AlertDialog.Builder(SystemAlertTestActivity.this).setTitle("tanchulai").setMessage("bucuo de tanchulai").create();
            //d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            //d.show();     

        }
     };
    }


