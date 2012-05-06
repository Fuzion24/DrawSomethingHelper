package com.draw.anything.server;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Properties;

import android.content.Context;

import com.nanohttpd.NanoHTTPD;
import com.nanohttpd.NanoHTTPD.Response;

public class StaticFileHandler implements IHttpHandler {
	
	private final String file_cache;
	private final String content_type;
	
	public StaticFileHandler(Context ctx, int resid, String content_type) throws NoSuchElementException {
		this.content_type = content_type;
		final InputStream is = ctx.getResources().openRawResource(resid);
		// Stack Overflow is da shit
		// http://stackoverflow.com/questions/309424/in-java-how-do-i-read-convert-an-inputstream-to-a-string
		file_cache = new java.util.Scanner(is).useDelimiter("\\A").next();
	}

	@Override
	public Response serve(NanoHTTPD server, String uri, String method,
			Properties header, Properties params, Properties files) {
		// TODO Auto-generated method stub
		final Response resp = server.new Response( NanoHTTPD.HTTP_OK, content_type, file_cache );
		resp.addHeader("Content-Length", Integer.toString(file_cache.length()));
		return resp;
	}
}
