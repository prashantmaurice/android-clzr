package com.clozerr.app.Utils;

/**
 * This is the master settings controller.
 * Make sure you have defined local_ip in strings_local
 */
public class Settings {
    enum RunMode{ PROD, BETAPROD,LOCAL }

    /**
     * This is the master settings controller,
     * set it the way you want and make sure you don't push this file with RunMode as RunMode.LOCAL
     */
    private final static RunMode runMode = RunMode.LOCAL;//this thing will override all the below settings //set this thing true for production

    //don't touch below things unless you know what you are doing
    private final static String productionIp = "http://api.clozerr.in/";
    private final static String betaProductionIp = "http://beta-api.clozerr.in/";
    private final static String localIp = "http://api.clozerr.in/";
    public final static String BASE_URL = (runMode== RunMode.PROD)?productionIp: ((runMode== RunMode.BETAPROD)?betaProductionIp: localIp);


    public final static boolean bypassLogin = !(runMode== RunMode.PROD);
    public final static boolean isDebugMode = !(runMode== RunMode.PROD);
    public final static boolean showDebugToasts = !(runMode== RunMode.PROD);

    //Other settings not related to Network
    public final static String SDCARD_FOLDER = "TinyStep";
    public final static String SDCARD_IMG_FOLDER = "TinyStep Images";

}

