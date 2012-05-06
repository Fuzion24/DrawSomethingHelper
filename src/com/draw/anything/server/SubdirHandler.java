package com.draw.anything.server;

import java.util.HashMap;
import java.util.Properties;

import com.nanohttpd.NanoHTTPD;
import com.nanohttpd.NanoHTTPD.Response;

public class SubdirHandler implements IHttpHandler {

	private HashMap<String, IHttpHandler> dirmap;
	private IHttpHandler defaulthandler;
	
	public SubdirHandler(IHttpHandler defaulthandler) {
		this.dirmap = new HashMap<String, IHttpHandler>();
		this.defaulthandler = defaulthandler;
	}
	
	public void registerDir(String dir, IHttpHandler handler) {
		dirmap.put(dir, handler);
	}
	
	public void unregisterDir(String dir) {
		dirmap.remove(dir);
	}
	
	public IHttpHandler getDefaultHandler() {
		return defaulthandler;
	}
	
	public IHttpHandler getHandlerForDir(String dir) {
		return dirmap.get(dir);
	}
	
	@Override
	public Response serve(NanoHTTPD server, String uri,String method, Properties header, Properties params, Properties files) {
		final String[] uri_parts = uri.split("/");
		if (uri_parts.length > 2) {
			final String key = uri_parts[1];
			final IHttpHandler subhandler = dirmap.get(key);
			if (subhandler != null) {
				final String new_uri = shift_uri(uri_parts);
				return subhandler.serve(server, new_uri, method, header, params, files);
			}
		}
		return defaulthandler.serve(server, uri, method, header, params, files);
	}
	
	private String shift_uri(String[] old) {
		StringBuilder ret = new StringBuilder();
		for (int i = 2; i < old.length; i++) {
			ret.append('/');
			ret.append(old[i]);
		}
		return ret.toString();
	}
}
