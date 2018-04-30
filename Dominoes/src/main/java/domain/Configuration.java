package domain;

import java.util.Date;

import com.josericardojunior.Native.MatrixProcessor;

public class Configuration {
	
	public static final String GPU_DEVICE = "GPU";
	public static final String CPU_DEVICE = "CPU";
	
    // to read file
    public static boolean fullscreen = false;
    public static boolean autoSave = false;
    public static boolean visibilityHistoric = true;
    public static boolean visibilityType = true;
    public static boolean resizable = true;
    public static boolean automaticCheck = false;
    public static boolean resizableTimeOnFullScreen = false;
    
    public static double width = 800.0f;
    public static double height = 600.0f;
    public static double listWidth = 130.0f;
    
    public static String accessMode = "SQL";
    public static String processingUnit =  MatrixProcessor.isGPUEnabled() ? GPU_DEVICE : CPU_DEVICE;
    public static String database = "db/gitdataminer.sqlite";
    public static String projName = "";
    
	public static Date beginDate = null;
    public static Date endDate = null; 
    
    public static int gpuDevice = 0;
    
    public static final int amount = 13;

    // not save/read file 
    public static double fullscreenWidth = Configuration.width;
    public static double fullscreenHeight = Configuration.height;
    
    public static boolean visibilityTimePane = true;
}
