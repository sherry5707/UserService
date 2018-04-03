package com.ragentek.ypush.service.util;

import android.os.Environment;

public class YPushConfig {
	  /**
	   * socket Telecom ip
	   */
	  public static final String Telecom_dst_address = "push1.qingcheng.com"; //"140.207.13.238 ";
	  
	  /**
	   * socket Unicom ip
	   */
	  public static final String Unicom_dst_address = "push2.qingcheng.com"; //"140.207.13.238 ";
	  
	  /**
	   * socket port
	   * modify by xiaolin.he 20150929 start . 
	   * if (ipAddress exists in upload map) dst_port = 5555
	   * else if (ipAddress does not exist) dst_port = 5557
	   * modify by xiaolin.he 20150929 end .
	   */
	  public static final int dst_port = 5557;//8080; //5555;

	  /**
	   * socket Unicom ip
	   */
	  public static final String Test_dst_address = "push3.qingcheng.com"; //"140.207.13.238 ";
	  
	  /**
	   * socket port
	   */
	  public static final int Test_dst_port = 5556;
	  
	  /**
	   * ��������ͷ���� 8λ
	   */
	  public static final int head_len = 8;
	  
	  /**
	   * ���չ㲥intent������
	   */
	  public static final String broadcast_service_name = "com.ragentek.ypush.service.broadcast";
	  
	  /**
	   * ����intent������
	   */
	  public static final String broadcast_sdk_name = "com.ragentek.ypush.sdk.broadcast";
	  
	  /**
	   * packageName
	   */
	  public static final String package_name = "package:";
	  
	  /**
	   * service ����
	   */
	  public static final String service_name = "com.ragentek.ypush.service";
	  
	  /**
	   * sdk����
	   */
	  public static final String ypush_sdk_name ="com.example.testapp130129";//"com.ragentek.ypush.sdk";
	  
	  /**
	   * ���������
	   */
	  public static final String heart_beat_packet = "1";
	  
	  /**
	   * ע��͸���
	   */
	  public static final String  register_update= "2";

	  /**
	   * ����
	   */
	  public static final String command = "4";
	  
	  /**
	   * ��Ϣ
	   */
	  public static final String msg_type = "5";
	  
	  /**
	   * notify push msg send complete
	   */
	  public static final String notify_send_complete_type = "6";
	  
	  /**
	   * receive msg time out
	   */
	  public static final int receive_msg_time_out = 3*60*1000; //3 minutes
	  
	  
	  /**
	   * ��ʱʱ��
	   */
	  public static final int time_out = 120*1000;
	  
	  /**
	   * ��ʱʱ��
	   */
	  public static final int reconnect_interval = 300*1000;
	  
	  /**
	   * �������
	   */
	  public static final int heart_interval_time = 30*1000;
	  
	  /**
	   * DownLoad dir
	   */
	  public static final String updateClientDir = "app/download/";
	  
	  /**
	   * PushClient app name
	   */
	  //public static final String pushClientName = "YPush_service.apk";
	  
	  /**
	   * DownLoad dir
	   */
	  public static final String downloadDir = "app/download/";
	  
	  //public static final String downloadName = "test.apk";
	  
	  /**
	   * Database Name
	   */
	  public static final String DATABASE_NAME = "YPushService.db";

	  /**
	   * Downloaded apk dir
	   */
	  public static final String APK_DOWNLOAD_DIR = Environment.getExternalStorageDirectory().getPath() + "/UserService/apks/";
	  
//	  public static final String HTTP_HEAD = "http://test.kindui.com/api";//Test
	  public static final String HTTP_HEAD = "http://api.kindui.com/api";//"http://210.51.45.23:8080/api";//gavin: change ip address to net address. checked with chenwen

	  public static final String API_APP_DETAIL_URL = "/push/app/detail";
	  
	  public static final String API_USER_FEEDBACK_URL = "/push/addUserFeedbackMsg";
	  
	  //add by zhengguang.yang@20160919 start for push user behavior
	  public static final String VIEWMSG = "VIEWMSG";
	  public static final String SDOWNLOAD = "SDOWNLOAD";
	  public static final String FDOWNLOAD = "FDOWNLOAD";
	  public static final String INSTALLEDV = "INSTALLEDV";
	  public static final String INSTALLEDNV = "INSTALLEDNV";
	  //add by zhengguang.yang end
}
