package com.draw.anything;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.draw.anything.model.Word;
import com.draw.anything.model.WordList;
import com.draw.anything.view.WordListAdapter;
import com.fuzionsoftware.alert.system.R;

public class DrawSomethingElseActivity extends Activity {
	
    private WordListAdapter wla;
    private ListView wordlist;
    private Button   startstop;
    
    private WordListService.LocalBinder service;
	private ServiceConnection sc = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder srv) {
			service = (WordListService.LocalBinder) srv;
			service.registerCallback(service.new OnChangeCallback() {
				@Override
				protected void onChange(WordList wordlist) {
					wla.SetWordList(wordlist);
				}

				@Override
				protected void onServerStatus(boolean start, boolean success) {
					/*final String started = start   ? "started" : "stopped";
					final String worked    = success ? "Successfully" : "Unsuccessfully";
					Toast.makeText(DrawSomethingElseActivity.this, worked + " " + started + " the server.", 3);*/
					
					updateStartButton(start);
				}
			});
			
			updateStartButton(service.serverStarted());
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			service = null;
			service.unregisterCallback();
		}
    	
    };
    
    private void addWord() {   	
    	final View     prompt = this.getLayoutInflater().inflate(R.layout.add_word, null);
    	final EditText word   = (EditText) prompt.findViewById(R.id.add_word_word);
    	final Spinner  coins  = (Spinner)  prompt.findViewById(R.id.add_word_coins);
    	coins.setSelection(2); //Set spinner to 3 coins by default
    	final AlertDialog.Builder alert = new AlertDialog.Builder(this).
			setTitle(R.string.add_word_title).
			setView(prompt).
			setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						final Word werd = new Word(word.getText().toString(), coins.getSelectedItemPosition() + 1);
						if (service != null) {
							service.addWord(werd);
						}
					} catch (IllegalArgumentException e) {
						dialog.dismiss();
						showWordErrorDialog();
					}
				}	
			}).
			setNegativeButton(R.string.cancel, null);
		alert.show();
    }
    
    private void showWordErrorDialog() {
    	final AlertDialog.Builder alert = new AlertDialog.Builder(this).
    		setTitle(R.string.error).
    		setMessage(R.string.word_validation_error).
    		setPositiveButton(R.string.ok, null);
    	alert.show();
    }
    
    private void updateStartButton(boolean started) {
    	final int newlabel = started ? R.string.stop_server : R.string.start_server;
    	startstop.setText(newlabel);
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.draw_something);
        
        wla = new WordListAdapter(this);
        wordlist = (ListView) this.findViewById(R.id.wordlist);
        wordlist.setAdapter(wla);
        
        startstop = (Button) this.findViewById(R.id.start_server);
        startstop.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if (service != null) {
					service.toggleServer();
				}
			}
        	
        });
        
        final Button addWord = (Button) this.findViewById(R.id.add_word);
        addWord.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				addWord();
			}
        	
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	this.getMenuInflater().inflate(R.menu.main_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.menu_clear:
    		if (service != null) {
    			service.clearWords();
    		}
    		return true;
    	case R.id.menu_quit:
    		if (service != null) {
    			service.stopServer();
    		}
    		stopService(new Intent(this, WordListService.class));
    		this.finish();
    		return true;
    	case R.id.menu_reload_default:
    		if (service != null) {
    			service.reloadDefault();
    		}
    		return true;
    	}
    	return false;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	getApplicationContext().startService(new Intent(getApplicationContext(), WordListService.class));
    	getApplicationContext().bindService(new Intent(getApplicationContext(), WordListService.class), sc, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	getApplicationContext().unbindService(sc);
    }
}