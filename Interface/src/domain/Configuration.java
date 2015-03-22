package domain;

public class Configuration {
    // to read file
    public static boolean fullscreen = false;
    public static boolean autoSave = false;
    public static boolean visibilityHistoric = true;
    public static boolean visibilityType = true;
    public static boolean resizable = false;
    public static boolean automaticCheck = false;
    public static boolean resizableTimeOnFullScreen = false;
    
    public static double width = 800.0f;
    public static double height = 600.0f;
    public static double listWidth = 130.0f;
    
    public static String accessMode = "SQL";
    public static String processingUnit = "GPU";
    public static String database = "";
    
	public static String beginDate = "2013-11-01 00:00:00";
    public static String endDate = "2014-01-31 00:00:00";    
    
    public static final int amount = 15;

    // not save/read file 
    public static double fullscreenWidth = Configuration.width;
    public static double fullscreenHeight = Configuration.height;
    
    public static boolean visibilityTimePane = true;
}
