package com.draw.anything.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Word implements Parcelable {
	
	private String word;
	private int coins;
	private int unknown;
	
	public Word(String word, int coins, int unknown) throws IllegalArgumentException {
		// Word must only contain a-z and be 1-9 chars long
		word = word.toLowerCase();
		if (word.matches("[a-z]+") && word.length() >= 1 && word.length() <= 9) {
			this.word = word.toLowerCase();
		} else {
			throw new IllegalArgumentException();
		}
		
		// Coins must be in the range of 1-3
		if (coins < 1) {
			coins = 1;
		} else if (coins > 3) {
			coins = 3;
		}
		this.coins = coins;
		
		// Looks like this is either 0 or 1
		unknown = unknown == 0 ? 0 : 1;
		this.unknown = unknown;
	}
	
	// This should probably go away once "unknown" is figured out
	public Word(String word, int coins) throws IllegalArgumentException {
		// Just randomly pick a value for unknown
		//this(word, coins, (int) Math.floor(Math.random() * 2));
		this(word, coins, 1);
	}
	
	public Word(String csv) throws IllegalArgumentException {
		// Java will only let you call this first
		this(csv.split(","));
	}
	
	private Word(String[] java_workaround) {
		this(java_workaround[0], Integer.parseInt(java_workaround[1]), Integer.parseInt(java_workaround[2]));
	}
	
	public String getWord() {
		return word;
	}
	
	public int getCoins() {
		return coins;
	}
	
	public int getUnknown() {
		return unknown;
	}
	
	public String getKey() {
		return getWord();
	}
	
	public String toString(int unkn) {
		unkn = unkn == 0 ? 0 : 1;
		return word + "," + coins + "," + unkn;
		
	}
	
	public String toString() {
		return toString(this.unknown);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(word);
		dest.writeInt(coins);
		dest.writeInt(unknown);
	}
	
	public Word(Parcel in) {
		this.word = in.readString();
		this.coins = in.readInt();
		this.unknown = in.readInt();
	}
}
