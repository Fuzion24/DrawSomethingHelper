package com.draw.anything.server;

import java.util.Properties;

import com.nanohttpd.NanoHTTPD;
import com.nanohttpd.NanoHTTPD.Response;

public interface IHttpHandler {
	public Response serve(NanoHTTPD server, String uri,String method, Properties header, Properties params, Properties files);
}
