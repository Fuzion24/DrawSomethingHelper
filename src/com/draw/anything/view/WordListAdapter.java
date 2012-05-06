package com.draw.anything.view;

import com.draw.anything.model.Word;
import com.draw.anything.model.WordList;
import com.fuzionsoftware.alert.system.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class WordListAdapter extends BaseAdapter {
	
	private Word[] mWordlist;
	
	//private Context mCTX = null;
	private LayoutInflater mInflater;
	
	public WordListAdapter(Context ctx) {
		super();
		this.mWordlist = new Word[0];
		//this.mCTX = ctx;
		this.mInflater = LayoutInflater.from(ctx);
	}
	
	public WordListAdapter(Context ctx, WordList wordlist) {
		this(ctx, wordlist.toArray());
	}
	
	public WordListAdapter(Context ctx, Word[] wordlist) {
		this(ctx);
		this.mWordlist = wordlist;
	}
	
	public void SetWordList(WordList wordlist) {
		SetWordList(wordlist.toArray());
	}
	
	public void SetWordList(Word[] wordlist) {
		this.mWordlist = wordlist;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mWordlist.length;
	}

	@Override
	public Object getItem(int pos) {
		return mWordlist[pos];
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View view, ViewGroup parent) {
		if (view == null) {
			view = mInflater.inflate(R.layout.word, null);
		}
		final Word w = mWordlist[pos];
		final TextView word = (TextView) view.findViewById(R.id.word);
		final TextView coins = (TextView) view.findViewById(R.id.coins);
		
		word.setText(w.getWord());
		coins.setText(String.valueOf(w.getCoins()));
		
		return view;
	}

}
