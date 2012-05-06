package com.draw.anything.server;

import java.util.Properties;

import com.nanohttpd.NanoHTTPD;
import com.nanohttpd.NanoHTTPD.Response;

public class Always404Handler implements IHttpHandler {

	@Override
	public Response serve(NanoHTTPD server, String uri, String method,
			Properties header, Properties params, Properties files) {
		// TODO Auto-generated method stub
		return server.new Response( NanoHTTPD.HTTP_NOTFOUND, "text/plain", "404" );
	}

}
