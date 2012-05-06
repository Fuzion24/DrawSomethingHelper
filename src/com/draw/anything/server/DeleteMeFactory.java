package com.draw.anything.server;

import java.io.IOException;

import com.fuzionsoftware.alert.system.R;

import android.content.Context;

public class DeleteMeFactory {
	public static BaseTamperingServer MakeServer(Context ctx, WordListHandler.GetWordListCallback callback) throws IOException {
		final Always404Handler error = new Always404Handler();
		final WordListHandler wlh = new WordListHandler(callback);
		final StaticFileHandler prod_paid = new StaticFileHandler(ctx, R.raw.products_paid, "application/json");
		final StaticFileHandler version   = new StaticFileHandler(ctx, R.raw.version, "application/json");
		
		final FileHandler files = new FileHandler(error);
		files.registerFile("products_paid.json", prod_paid);
		files.registerFile("version.json", version);
		files.registerFile("wordlist.csv", wlh);
		
		final SubdirHandler ds = new SubdirHandler(error);
		ds.registerDir("drawsomething", files);
		
		final BaseTamperingServer bs = new BaseTamperingServer(31337, ds);
		return bs;
	}
}
