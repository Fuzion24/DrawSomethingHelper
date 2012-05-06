package com.draw.anything.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

import com.nanohttpd.NanoHTTPD;

public class BaseTamperingServer extends NanoHTTPD {
	
	private static final String DRAWSOMETHING_HOST = "static.iminlikewithyou.com";
	private static final int INTERNAL_LISTENING_PORT = 31337;
	
	private IHttpHandler passthrough;
	
	public BaseTamperingServer(int port, IHttpHandler passthrough) throws IOException {
		super(port, null);
		this.passthrough = passthrough;
		setupServerTraffic();
	}
	
	@Override
	public void stop() {
		super.stop();
		tearDownServerTraffic();
	}
	
	public Response serve(String uri,String method, Properties header, Properties params, Properties files) {
		return passthrough.serve(this, uri, method, header, params, files);
	}
	
	public IHttpHandler getHandler() {
		return passthrough;
	}
	
	public static void setupServerTraffic()
	{
			String[] commands = new String[4];
			
			//Append /etc/hosts to redirect DrawSomething traffic to localhost
			commands[0] = "echo \"127.0.0.1 " + DRAWSOMETHING_HOST + "\" >> /etc/hosts";
			
			//DroidWall binary may be more stable for iptables -- just as a future note 
			//http://code.google.com/p/droidwall/downloads/detail?name=droidwall-v1_5_7.apk
			commands[1] = "iptables -t nat -I OUTPUT --src 0/0 --dst 127.0.0.1 -p tcp --dport 80 -j REDIRECT --to-ports " + INTERNAL_LISTENING_PORT;
			
			executeAsRoot(commands);
	}

	public static void tearDownServerTraffic()
	{
		String[] commands = new String[6];
		//Delete all lines from /etc/hosts that have the have DRAWSOMETHING_HOST contents in them and write result(s) back to /etc/hosts
		commands[0] = "sed '/" + DRAWSOMETHING_HOST +"/d' /etc/hosts > /etc/hosts";
		
		//Remove iptable rule -- It needs to be deleted as many times as it was added, though
		commands[1] = "iptables -t nat -D OUTPUT --src 0/0 --dst 127.0.0.1 -p tcp --dport 80 -j REDIRECT --to-ports " + INTERNAL_LISTENING_PORT;
		commands[5] = commands[4] = commands[3] = commands[2] = commands[1];
		 
				
		executeAsRoot(commands);
	}
		
	public static void executeAsRoot(String [] commands)
	{	
		try{
			//Get a root shell
			Process proc = Runtime.getRuntime().exec("su");
			
			//There is a race condition somehow, so lose the race.
			Thread.sleep(100);
			
			BufferedReader stdErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedWriter stdIn = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
			
			//Remount System partition as read/write to be able to write to /etc/hosts
			stdIn.write("mount -orw,remount /system\n");
			stdIn.flush();
			if(stdErr.ready() || stdOut.ready())
			{
				if(stdErr.ready())
					System.out.println(stdErr.readLine());
				if(stdOut.ready())
					System.out.println(stdOut.readLine());
			}
			
			for(String cmd : commands)
			{
				if(cmd == null)
					continue;
				stdIn.write(cmd + "\n");
				stdIn.flush();
				if(stdErr.ready() || stdOut.ready())
				{
					if(stdErr.ready())
						System.out.println(stdErr.readLine());
					if(stdOut.ready())
						System.out.println(stdOut.readLine());
				}
			}
			
			//Remount System partition as read only
			stdIn.write("mount -oro,remount /system\n");
			stdIn.write("exit\n");
			stdIn.flush();
			//Throws an exception
			//proc.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
