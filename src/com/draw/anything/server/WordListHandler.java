package com.draw.anything.server;

import java.util.Properties;

import com.draw.anything.model.WordList;
import com.nanohttpd.NanoHTTPD;
import com.nanohttpd.NanoHTTPD.Response;

public class WordListHandler implements IHttpHandler {
	
	private static final int TARGET_WORD_COUNT = 1500;
	
	private GetWordListCallback callback;
	
	public WordListHandler(GetWordListCallback cb) {
		this.callback = cb;
	}

	@Override
	public Response serve(NanoHTTPD server, String uri, String method,
			Properties header, Properties params, Properties files) {
		final WordList wordlist = callback.getWordList();
		final int numWords = wordlist.size();
		final String wordresp = numWords >= TARGET_WORD_COUNT ? wordlist.toString() : wordlist.toString((TARGET_WORD_COUNT / numWords) + 1);
		final Response resp = server.new Response( NanoHTTPD.HTTP_OK, "text/plain", wordresp );
		resp.addHeader("Content-Length", Integer.toString(wordresp.length()));
		return resp;
	}

	public static abstract class GetWordListCallback {
		protected abstract WordList getWordList();
	}
}
