package com.draw.anything.server;

import java.util.HashMap;
import java.util.Properties;
import com.nanohttpd.NanoHTTPD;
import com.nanohttpd.NanoHTTPD.Response;

public class FileHandler implements IHttpHandler {

	private HashMap<String, IHttpHandler> filemap;
	private IHttpHandler defaulthandler;
	
	public FileHandler(IHttpHandler defaulthandler) {
		this.filemap = new HashMap<String, IHttpHandler>();
		this.defaulthandler = defaulthandler;
	}
	
	public void registerFile(String dir, IHttpHandler handler) {
		filemap.put("/"+dir, handler);
	}
	
	public void unregisterFile(String dir) {
		filemap.remove("/"+dir);
	}
	
	public IHttpHandler getDefaultHandler() {
		return defaulthandler;
	}
	
	public IHttpHandler getHandlerForFile(String dir) {
		return filemap.get("/"+dir);
	}
	
	@Override
	public Response serve(NanoHTTPD server, String uri,String method, Properties header, Properties params, Properties files) {
		final IHttpHandler next = filemap.get(uri);
		if (next != null) {
			return next.serve(server, uri, method, header, params, files);
		}
		
		return defaulthandler.serve(server, uri, method, header, params, files);
	}
}
