package com.draw.anything.server;

import java.util.HashMap;
import java.util.Properties;

import com.nanohttpd.NanoHTTPD;
import com.nanohttpd.NanoHTTPD.Response;

public class HostHandler implements IHttpHandler {

	private HashMap<String, IHttpHandler> hostmap;
	private IHttpHandler defaulthandler;
	
	public HostHandler(IHttpHandler defaulthandler) {
		this.hostmap = new HashMap<String, IHttpHandler>();
		this.defaulthandler = defaulthandler;
	}
	
	public void registerHost(String host, IHttpHandler handler) {
		hostmap.put(host, handler);
	}
	
	public void unregisterHost(String host) {
		hostmap.remove(host);
	}
	
	public IHttpHandler getDefaultHandler() {
		return defaulthandler;
	}
	
	public IHttpHandler getHandlerForHost(String host) {
		return hostmap.get(host);
	}
	
	@Override
	public Response serve(NanoHTTPD server, String uri,String method, Properties header, Properties params, Properties files) {
		String host = header.getProperty("Host");
		if (host != null) {
			final IHttpHandler handler = hostmap.get(host);
			if (handler != null) {
				header.remove("Host");
				return handler.serve(server, uri, method, header, params, files);
			}
		}
		return defaulthandler.serve(server, uri, method, header, params, files);
	}
}
