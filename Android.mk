LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

#LOCAL_STATIC_JAVA_LIBRARIES := libhttpclient422 \

LOCAL_STATIC_JAVA_LIBRARIES := libgson222_userservice \
				libormliteandroid442_userservice \
				libhttpmime401_userservice \
				libormlitecore442_userservice \
				libprotobufjava230_userservice \
				libuniversalimageloader192_userservice \
				libandroidasynchttp149_userservice \
				libapachemime4j06_userservice \
				android-support-v4 \
				libhttpclient436_userservice \
				libokhttp260_userservice \
				libokio160_userservice \
							   org.apache.http.legacy

LOCAL_JAVA_LIBRARIES += mediatek-framework

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := UserService

LOCAL_CERTIFICATE   := platform
LOCAL_DEX_PREOPT := false

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libhttpclient422:libs/httpclient-4.2.2.jar \

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libgson222_userservice:libs/gson-2.2.2.jar \
					libhttpmime401_userservice:libs/httpmime-4.0.1.jar \
					libprotobufjava230_userservice:libs/protobuf-java-2.3.0.jar \
					libapachemime4j06_userservice:libs/apache-mime4j-0.6.jar \
					libhttpclient436_userservice:libs/httpclient-4.3.6.jar \
					libormliteandroid442_userservice:libs/ormlite-android-4.42.jar \
					libormlitecore442_userservice:libs/ormlite-core-4.42.jar \
					libandroidasynchttp149_userservice:libs/android-async-http-1.4.9.jar \
					libuniversalimageloader192_userservice:libs/universal-image-loader-1.9.2-SNAPSHOT-with-sources.jar \
					libokhttp260_userservice:libs/okhttp-2.6.0.jar \
					libokio160_userservice:libs/okio-1.6.0.jar \

include $(BUILD_MULTI_PREBUILT)

# Use the following include to make our test apk.
#include $(call all-makefiles-under,$(LOCAL_PATH))

