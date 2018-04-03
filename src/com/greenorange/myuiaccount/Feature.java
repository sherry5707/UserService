package com.greenorange.myuiaccount;

/**
 * Created by JasWorkSpace on 15/10/30.
 */
public class Feature {

    public final static boolean isSecureUserData(){
        return true;
    }
    public final static boolean isOnlyOneAccount(){
        return true;
    }

    ///////////////////////////////////////////////////////////////
    // for control debug mode
    public final static boolean isDebugMode(){return true;}
    // for support unbind mode. for its debug mode.
    public final static boolean isSupportUnbind(){return true && isDebugMode();}
    // for message
    public final static boolean isSupportAuth(){return false && !isDebugMode();}

}
