package com.ragentek.ypush.service.util;

import java.util.Date;
import java.util.List;

import com.ragentek.ypush.service.db.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @version
 */
public class CoordinateHelper {
	
	private static final String TAG = "YPushService.CoordinateHelper";
	
	public CoordinateHelper(Context context, Handler handler) {
		this.mContext = context;
		this.myHandler = handler;
		
		setLocationParam();
	}
	
	double lat;
	double lng;
	
	private Context mContext;
	
	Handler myHandler;
	
	private final static int UPDATE_LOCATION_TO_DB = 1;
	
	public String getLatitude() {
		return String.valueOf(lat);
	}

	public String getLongitude() {
		return String.valueOf(lng);
	}
	
	/*public void getWiFiLocation(){
		//��ȡwifi�������
		WifiManager mainWifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		//�ж�wifi�Ƿ���
	    if (mainWifi.isWifiEnabled())
	    {
	            //���ͽ�����ɨ�����󣬷���true�ɹ�������ʧ��
	            mainWifi.startScan();
	            //����һ���߳�ִ�еڶ����еĴ���
	    }
	}*/
	
	private void setLocationParam() {
		
		try {
			LocationManager locationManager =(LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			
			long startTime = new Date().getTime();
			
		    String provider = LocationManager.GPS_PROVIDER;
		    Location location = locationManager.getLastKnownLocation(provider);
		    Log.v(TAG, "AGPS location=" + location);
			
		    //zhaoshh, we will not use the gps to get the location
		    /*
		    if(location == null){
		    	provider = LocationManager.GPS_PROVIDER;
		    	location = locationManager2.getLastKnownLocation(provider);
		    	Log.v("CoordinateHelper gps location","location=" + location);
		    }
		    */	    
			
		    Log.v(TAG, "the best provider is :" + provider);
		    updateWithNewLocation(location);
		    locationManager.requestLocationUpdates(provider, 2000, 10000, mLocationListener);
		    
		    boolean agpsEnableFlag = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		    Log.v(TAG, "agpsEnableFlag:" + agpsEnableFlag);
		    boolean gpsEnableFlag = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		    Log.v(TAG, "gpsEnableFlag:" + gpsEnableFlag);
		    
		    
		    long endTime = new Date().getTime();
			long interval = endTime - startTime;
			Log.d(TAG, "AGPS get location: startTime=" + startTime + " ,endTime=" + endTime + " ,interval=" + interval);
			
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			
		}
		
	}

	private final LocationListener mLocationListener = new LocationListener() {
	    public void onLocationChanged(Location location) {
	    	Log.v(TAG, "mLocationListener location =" + location.getLatitude()+"---"+location.getLongitude());
	        updateWithNewLocation(location);
	    }
	
	    public void onProviderDisabled(String provider) {
	        updateWithNewLocation(null);
	    }
	
	    public void onProviderEnabled(String provider) {
	    }
	
	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    }
	};
	
	
	private void setLocationParam2() {
		try {
			String serviceName = Context.LOCATION_SERVICE;
			LocationManager locationManager = (LocationManager) mContext
					.getSystemService(serviceName);

		/*	Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(true);
			criteria.setPowerRequirement(Criteria.ACCURACY_LOW);

			String provider = locationManager.getBestProvider(criteria, true);
			Location location = locationManager.getLastKnownLocation(provider);*/
			Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(location!=null){
				Log.v(TAG, location.getLatitude()+"---"+location.getLongitude());
			}else{
				location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			
			//loctionManager.requestLocationUpdates(provider, 2000, 10, locationListener);
			
			if(location!=null){
				Log.v(TAG, location.getLatitude()+"---"+location.getLongitude());
			}
			
			updateWithNewLocation(location);
			// ���ü��������Զ����µ���Сʱ��Ϊ���1�룬��Сλ�Ʊ仯����5��
			//locationManager.requestLocationUpdates(provider, 5*60*1000, 500,locationListener);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

	}
	
	/*private final LocationListener locationListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}

		public void onProviderDisabled(String provider) {
			updateWithNewLocation(null);
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	};*/

	private void updateWithNewLocation(Location location) {
		if (location != null) {
			lat = location.getLatitude();
			lng = location.getLongitude();
		} else {
			lat = 0;
			lng = 0;
		}
		
		if (location != null) {
			StringBuffer lalo = new StringBuffer();
    		lalo.append(getLongitude()).append(",");
    		lalo.append(getLatitude());
    		String longitudeLatitude = "";
    		longitudeLatitude = lalo.toString();
    		Log.v(TAG, "notify service: longitudeLatitude=" + longitudeLatitude);
    		
    		//zhaoshh for test city name
    		//getAddressInfo(location);
    		
			Message message = myHandler.obtainMessage();
			message.what = UPDATE_LOCATION_TO_DB;
			message.obj = longitudeLatitude;
			
			myHandler.sendMessage(message);
		} 
	}
	
	
	private void getAddressInfo(Location location) {
	    String cityName = "";
	    String placename = "";
	    
	    if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            
            Geocoder geocoder = new Geocoder(mContext); 
            List<Address> places = null;
            
            try {
            	long startTime = new Date().getTime();
                places = geocoder.getFromLocation(lat, lng, 3);
                long endTime = new Date().getTime();
                long interval = endTime - startTime;
                Log.d(TAG, "get address: startTime=" + startTime + " ,endTime=" + endTime + " ,interval=" + interval);
                Log.d(TAG, "places.size()=" + places.size() + " ,places=" + places);
            } catch (Exception e) {  
            	Log.d(TAG, "get address have exception");
                e.printStackTrace();
            }       
            
            if (places != null && places.size() > 0) {
            	cityName = ((Address)places.get(0)).getLocality();
                  
                placename = "���:"+((Address) places.get(0)).getAddressLine(0) + ","
                                + "С��:" + ((Address) places.get(0)).getAddressLine(1) + "," 
                                + "���:" + ((Address) places.get(0)).getAddressLine(2);
                Log.d(TAG, "cityName=" + cityName + " ,placename=" + placename);
            }

	    } else {
    	    Log.v(TAG,"location is null!!!");
    	    
	    }
	}
	
}