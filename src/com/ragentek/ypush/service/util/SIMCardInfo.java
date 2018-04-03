package com.ragentek.ypush.service.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * class name��SIMCardInfo<BR>
 * class description����ȡSim����Ϣ<BR>
 * PS�� �����ڼ������Ȩ�� <BR>
 * 
 */
public class SIMCardInfo {
	
	private static final String TAG = "YPushService.SIMCardInfo";
	
	/**
	 * TelephonyManager�ṩ�豸�ϻ�ȡͨѶ������Ϣ����ڡ� Ӧ�ó������ʹ������෽��ȷ���ĵ��ŷ����̺͹�� �Լ�ĳЩ���͵��û�������Ϣ��
	 * Ӧ�ó���Ҳ����ע��һ�����������绰��״̬�ı仯������Ҫֱ��ʵ�������
	 * ʹ��Context.getSystemService(Context.TELEPHONY_SERVICE)����ȡ������ʵ��
	 */
	private TelephonyManager telephonyManager;
	/**
	 * ����ƶ��û�ʶ����
	 */
	private String IMSI;
	/**
	 * �豸��
	 */
	private String IMEI;
	/**
	 * sim��id
	 */
	private String SIMID;
	/**
	 * �ֻ����
	 */
	private String Line1Number;

	public String getLine1Number() {
		Line1Number = telephonyManager.getLine1Number();
		return Line1Number;
	}

	public String getSIMID() {
		SIMID = telephonyManager.getSimSerialNumber();
		return SIMID;
	}

	public String getIMEI() {
		// change by xiaolin.he 2015-6-29 21:33:19 start
		// use the slot 1 as default device id for its uniqueness , in JL610
		IMEI = telephonyManager.getDeviceId(1);
		// add by xiaolin.he 2015-6-15 21:44:10 start 
		// if imei of slot 0 is null , try slot 1
		// note : imei is decimal number which length is 15 , and meid is hexadecimal number which length is 14
//		if (android.text.TextUtils.isEmpty(IMEI)) {
//			try {
//				IMEI = telephonyManager.getDeviceId(1);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		// add by xiaolin.he 2015-6-15 21:44:10 end
		// change by xiaolin.he 2015-6-29 21:33:19 end
		return IMEI;
	}

	public String getIMSI() {
		IMSI=  telephonyManager.getSubscriberId();
		return IMSI;
	}

	public SIMCardInfo(Context context) {
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
	}

	/**
	 * Role:��ȡ��ǰ���õĵ绰����
	 */
	public String getNativePhoneNumber() {
		String NativePhoneNumber=null;
		NativePhoneNumber=telephonyManager.getLine1Number();
		return NativePhoneNumber;
	}

	/**
	 * Role:Telecom service providers��ȡ�ֻ��������Ϣ <BR>
	 * ��Ҫ����Ȩ��<uses-permission android:name="android.permission.READ_PHONE_STATE"/> <BR>
	 */
	public String getProvidersName() {
		String ProvidersName = null;
		// 返回唯一的用户ID;就是这张卡的编号神马的
		IMSI = telephonyManager.getSubscriberId();
		// IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
		Log.v(TAG, "getProvidersName: IMSI :"+ IMSI);

		// add by xiaolin.he 2015-6-16 14:28:26 start
		// china mobile 00 02 07 ; china tietong 20 sometimes in high-speed rail
		if (IMSI.startsWith("46000") || IMSI.startsWith("46002") || IMSI.startsWith("46007")) {
			ProvidersName = "中国移动";
		} else if (IMSI.startsWith("46001") || IMSI.startsWith("46009")) {
			ProvidersName = "中国联通";
		} else if (IMSI.startsWith("46003") || IMSI.startsWith("46011") || IMSI.startsWith("46012") || IMSI.startsWith("46013")) {
			ProvidersName = "中国电信";
		}
		// add by xiaolin.he 2015-6-16 14:28:26 end
		return ProvidersName;
	}
	
	/**
	 * Role:Telecom service providers获取手机服务商信息 <BR>
	 * 需要加入权限<uses-permission android:name="android.permission.READ_PHONE_STATE"/> <BR>
	 */
	public String getProvidersType() {
		String ProvidersType = null;
		// 返回唯一的用户ID;就是这张卡的编号神马的
		IMSI = telephonyManager.getSubscriberId();
		// IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
		Log.v(TAG, "getProvidersType: IMSI :"+ IMSI);
		
		if (IMSI != null) {
			if (IMSI.startsWith("46000") || IMSI.startsWith("46002") || IMSI.startsWith("46007")) {
				ProvidersType = "1";
			} else if (IMSI.startsWith("46001") || IMSI.startsWith("46009")) {
				ProvidersType = "2";
			} else if (IMSI.startsWith("46003") || IMSI.startsWith("46011") || IMSI.startsWith("46012") || IMSI.startsWith("46013")) {
				ProvidersType = "3";
			}
		}
		
		return ProvidersType;
	}
}