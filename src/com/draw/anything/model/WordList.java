package com.draw.anything.model;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fuzionsoftware.alert.system.R;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

public class WordList implements Parcelable {

	private static final String LINE_ENDING = "\r\n";

	private HashMap<String, Word> words;

	public WordList() {
		words = new HashMap<String, Word>();
	}

	public WordList(Word[] werds) {
		words = new HashMap<String, Word>();
		for (Word w : werds) {
			words.put(w.getKey(), w);
		}
	}

	public WordList(String csv) {
		words = new HashMap<String, Word>();
		String[] parse = csv.split(LINE_ENDING);
		for (String werd : parse) {
			try {
				final Word w = new Word(werd.trim());
				words.put(w.getKey(), w);
			} catch (IllegalArgumentException e) {
				// Don't add the word of course.
				// Nothing else to be done here.
			}
			
		}
	}

	// Copy another list in-place (leaving reference intact)
	public WordList mimic(WordList target) {
		words = new HashMap<String, Word>();
		final Word[] werds = target.toArray();
		for (Word w : werds) {
			words.put(w.getKey(), w);
		}
		return this;
	}

	public WordList clone() {
		return new WordList().mimic(this);
	}

	public void addWord(Word w) {
		words.put(w.getKey(), w);
	}

	public void removeWord(Word w) {
		words.remove(w.getKey());
	}

	public void removeWord(String w) {
		words.remove(w);
	}

	public void clearWords() {
		words = new HashMap<String, Word>();
	}

	public int size() {
		return words.size();
	}

	public Word[] toArray() {
		final Collection<Word> werds = words.values();
		final Word[] wherds = new Word[werds.size()];
		return (Word[]) words.values().toArray(wherds);
	}

	public String toString() {
		StringBuilder res = new StringBuilder();
		Iterator<Entry<String, Word>> it = words.entrySet().iterator();
		while (it.hasNext()) {
			final Word w = it.next().getValue();
			res.append(w.toString());
			res.append(LINE_ENDING);
		}
		return res.toString();
	}
	
	public String toString(int repeat) {
		StringBuilder res = new StringBuilder();
		Iterator<Entry<String, Word>> it = words.entrySet().iterator();
		while (it.hasNext()) {
			final Word w = it.next().getValue();
			for (int i = 0; i < repeat; i++) {
				res.append(w.toString(i % 2));
				res.append(LINE_ENDING);
			}
		}
		return res.toString();
	}

	/*
	 * Default word list lazy loaded from res/raw folder
	 * 
	 * Using some ghetto dependency injection where res only needs to be
	 * non-null the first time
	 */

	private static WordList default_word_list;

	public static WordList getDefaultWordList(Resources res) {
		if (default_word_list == null) {
			final InputStream is = res.openRawResource(R.raw.wordlist);
			String csv;
			// Stack Overflow is da shit
			// http://stackoverflow.com/questions/309424/in-java-how-do-i-read-convert-an-inputstream-to-a-string
			try {
				csv = new java.util.Scanner(is).useDelimiter("\\A").next();
			} catch (java.util.NoSuchElementException e) {
				// Won't happen here
				csv = "ERROR,0,0";
			}

			default_word_list = new WordList(csv);
		}

		return default_word_list.clone();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		final Word[] werds = this.toArray();
		dest.writeParcelableArray(werds, flags);
	}

	public WordList(Parcel in) {
		this((Word[]) in.readParcelableArray(Word.class.getClassLoader()));
	}

}
