package com.ragentek.ypush.service;

import java.util.Map;

interface IYPushService {
	
		String registerAppAndUser(String appId,String userId);
		
		String unRegisterAppAndUser(String appId,String userId);
		
		String strategyConfig(in Map strategyMap);
		
}
