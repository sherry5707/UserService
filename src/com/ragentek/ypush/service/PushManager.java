package com.ragentek.ypush.service;

import com.ragentek.ypush.service.socket.SocketClient;

import android.content.Context;
import android.content.Intent;

public class PushManager {
	private Context c;
	private SocketClient socketClient;	
	private Intent serviceIntent;
	public PushManager (Context context){
		this.c = context;
	}
	
	public void startService(){
		//socketClient = new SocketClient();
		serviceIntent = new Intent(c, YPushService.class);  
        c.startService(serviceIntent);  
	}
	public void stopService(){
		if(socketClient !=null){
			socketClient.close();
		}		
        c.stopService(serviceIntent);  
	}
}
