/**
 * 
 */
package com.ragentek.ypush.service.util;

/**
 * @author zixing.he
 *
 */
public enum ReturnCode {
	 DO_SUCCESS {
	        @Override
	        public String toString() {
	            return "1000";
	        }
	    
    },
    DO_ERROR {
        @Override
        public String toString() {
            return "1001";
        }
    
    },
    SERVER_ERROR {
        @Override
        public String toString() {
            return "1002";
        }
    },
    NETWORK_NOT_VALID {
        @Override
        public String toString() {
            return "1003";
        }
    };
   
    

}
