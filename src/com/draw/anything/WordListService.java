package com.draw.anything;

import java.io.IOException;

import com.draw.anything.model.Word;
import com.draw.anything.model.WordList;
import com.draw.anything.server.BaseTamperingServer;
import com.draw.anything.server.DeleteMeFactory;
import com.draw.anything.server.WordListHandler;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class WordListService extends Service {
	
	private final IBinder binder = new LocalBinder();
	
	private BaseTamperingServer httpServer;
	private WordList            wordlist;
	
	private WordListHandler.GetWordListCallback gwl = new WordListHandler.GetWordListCallback() {
		@Override
		protected WordList getWordList() {
			return wordlist;
		}
	};
	
	private boolean startServer() {
		if (httpServer != null) {
			return true;
		}
		
		try {
			httpServer = DeleteMeFactory.MakeServer(this, gwl);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean stopServer() {
		if (httpServer == null) {
			return true;
		}
		
		try {
			httpServer.stop();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			httpServer = null;
		}
	}
	
	@Override
	public void onCreate() {
		//wordlist = WordList.getDefaultWordList(this.getResources());
		wordlist = new WordList();
		setMinimumDefault();
		super.onCreate();
	}
	
	@Override
	public void onStart(Intent intent, int id) {
		super.onStart(intent, id);
	}
	
	@Override
	public void onDestroy() {
		stopServer();
		super.onDestroy();
	}
	private void setMinimumDefault()
	{
		wordlist.addWord(new Word("one",1,1));
		wordlist.addWord(new Word("onefive",1,0));
		wordlist.addWord(new Word("two",2,0));
		wordlist.addWord(new Word("twofive",2,1));
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	class LocalBinder extends Binder {
		
		private OnChangeCallback cb;
		
		public void registerCallback(OnChangeCallback cb) {
			this.cb = cb;
			notifyChange();
		}
		
		public void unregisterCallback() {
			this.cb = null;
		}
		
		public void setWordList(WordList target) {
			wordlist.mimic(target);
			notifyChange();
		}
		
		public void addWord(Word w) {
			wordlist.addWord(w);
			notifyChange();
		}
		
		public void delWord(Word w) {
			wordlist.removeWord(w);
			notifyChange();
		}
		
		public void delWord(String w) {
			wordlist.removeWord(w);
			notifyChange();
		}
		
		public void clearWords() {
			wordlist.clearWords();
			setMinimumDefault();
			notifyChange();
		}
		
		public void reloadDefault() {
			wordlist.mimic(WordList.getDefaultWordList(WordListService.this.getResources()));
			notifyChange();
		}
		
		private void notifyChange() {
			if (cb != null) {
				cb.onChange(wordlist.clone());
			}
		}
		
		// HTTP Server commands
		
		public void startServer() {
			final boolean success = WordListService.this.startServer();
			cb.onServerStatus(true, success);
		}
		
		public void stopServer() {
			final boolean success = WordListService.this.stopServer();
			cb.onServerStatus(false, success);
		}
		
		public void toggleServer() {
			if (serverStarted()) {
				stopServer();
			} else {
				startServer();
			}
		}
		
		public boolean serverStarted() {
			return httpServer != null;
		}
		
		protected abstract class OnChangeCallback {
			protected abstract void onChange(WordList wordlist);
			protected abstract void onServerStatus(boolean start, boolean success);
		}
	}
}
