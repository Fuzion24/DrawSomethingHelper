package com.main.view;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.draw.anything.DrawSomethingElseActivity;
import com.fuzionsoftware.alert.system.R;
import com.fuzionsoftware.alert.system.SystemAlertTestActivity;

public class ActivityManager extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    intent = new Intent().setClass(this, SystemAlertTestActivity.class);
	    
	    spec = tabHost.newTabSpec("helper")
	                  .setContent(intent)
	                  .setIndicator("Helper");
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, DrawSomethingElseActivity.class);
	    spec = tabHost.newTabSpec("hijacker")
	    			  .setIndicator("Hijacker")
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}
}
