package com.ragentek.ypush.service.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

import com.ragentek.ypush.service.util.YPushConfig;

/**
 * @author zixing.he
 *
 */
public class SocketClient {
	
	private static final String TAG = "YPushService.SocketClient";
	
	public Socket socket = null;
	public DataInputStream din = null;
	public DataOutputStream dout = null;
	
	public boolean scFlag = true;
	
    public SocketClient(String domain, boolean isBusiServer) {
		if(socket == null){
			try {
				Log.v(TAG, "SocketClient: isBusiServer=" + isBusiServer);
				if (isBusiServer) {
					//get ip address
					InetAddress addr = InetAddress.getByName("push1.qingcheng.com");
					byte[] ipAddress = new byte[4];
					//addr.getByAddress(ipAddress);
					ipAddress = addr.getAddress();
					String szAddr = String.format("%d.%d.%d.%d", (int)(ipAddress[0] & 0x0ff),  (int)(ipAddress[1] & 0x0ff),  (int)(ipAddress[2] & 0x0ff),  (int)(ipAddress[3] & 0x0ff));
					Log.v(TAG, " socket create szAddr:"+szAddr +" ,ipAddress1: "+ipAddress[0] +" ipAddress2: "+ipAddress[1]+" ipAddress3: "+ipAddress[2]+" ipAddress4: "+ipAddress[3]);
		    		    		
					//double Domain Name
					//socket  = new Socket(YPushConfig.dst_address, YPushConfig.dst_port);
			    		String dstName = YPushConfig.Telecom_dst_address;
			    		if ("0".equals(domain)) {
			    			Log.v(TAG, "default use Telecom service!");
			    			
			    			dstName = YPushConfig.Unicom_dst_address;			
			    		} else if ("1".equals(domain)) {
			    			Log.v(TAG, "use Telecom service!");
			    			
			    			dstName = YPushConfig.Telecom_dst_address;			
			    		} else if ("2".equals(domain)) {
			    			Log.v(TAG, "use Unicom service!");		
			    			
			    			dstName = YPushConfig.Unicom_dst_address;
			    		}
			    		
			    		Log.v(TAG, " socket create ip dstName:"+dstName +" port: "+YPushConfig.dst_port);
					socket  = new Socket(dstName, YPushConfig.dst_port);	
				} else {
					socket  = new Socket(YPushConfig.Test_dst_address, YPushConfig.Test_dst_port);

					Log.v(TAG, " socket create ip YPushConfig.Test_dst_address:"+YPushConfig.Test_dst_address +" port: "+YPushConfig.dst_port);
				}
				
				
				boolean bKeepAlive = socket.getKeepAlive();
				int sockTimeout = socket.getSoTimeout();
				//socket.setKeepAlive(false);
				Log.v(TAG, " socket success bKeepAlive=" + bKeepAlive + " sockTimeout=" + sockTimeout);
				din = new DataInputStream(socket.getInputStream());			
				dout = new DataOutputStream(socket.getOutputStream());
				Log.v(TAG, " socket success");
			} catch (UnknownHostException e) {
				scFlag = false;
				e.printStackTrace();
				Log.v(TAG, " UnknownHostException:"+e);
			} catch (IOException e) {
				scFlag = false;
				e.printStackTrace();
				Log.v(TAG, " IOException:"+e);
			}
		}
    }
    

  	public void close(){
  		try{  			
  			din.close();  			
  			dout.close();
  			socket.close();
  			socket=null;
  		}catch(Exception e){
  			e.printStackTrace();
  		}
  	}

  	
   /* public String sendMsg(String msg){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(msg);
            out.flush();
            return in.readLine();
        }catch(IOException e){
            e.printStackTrace();
        }
        return "";
    }
    */
  	
/*	 class SendThread extends Thread{   
	        private Socket socket;   
	        private String sendStr;
	        public SendThread(Socket socket,String sendStr) {   ;
	            this.socket = socket;  
	            this.sendStr = sendStr;
	        }   
	        @Override  
	        public void run() {   
	            while(true){   
	                try {              
	                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));   
	                    pw.write(sendStr);   
	                    pw.flush();   
	                } catch (Exception e) {   
	                    e.printStackTrace();   
	                }   
	            }   
	        }   
	       
	    } */  
	 
/*	class ReceiveThread extends Thread{   
        private Socket socket;   
        private String receiveStr;
        public ReceiveThread(Socket socket) {   
            this.socket = socket;   
        }   
  
        @Override  
        public void run() {   
            while(true){   
                try {                      
                    Reader reader = new InputStreamReader(socket.getInputStream());   
                    CharBuffer charBuffer = CharBuffer.allocate(8192);   
                    int index = -1;   
                    StringBuffer sb = new StringBuffer();
                    while((index=reader.read(charBuffer))!=-1){   
                        charBuffer.flip();   
                        System.out.println("client:"+charBuffer.toString());   
                        sb.append(charBuffer.toString());                        
                    }   
                    receiveStr = sb.toString();
                } catch (Exception e) {   
                    e.printStackTrace();   
                }   
            }   
        }   
    }  */ 
  	
}
