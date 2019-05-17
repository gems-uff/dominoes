package com.josericardojunior.Native;

import java.lang.annotation.Native;
import java.util.Map;

import org.omg.CORBA.Environment;

//import com.josericardojunior.Native.JNILoadLib.Architecture;

public class MatrixProcessor {
	

	public native static float[] CPUMatMult(float m1[], 
			float m2[], int rows1, int cols1, int cols2);
	
	//public native static float[] BLASMatMult(float m1[], 
	//		float m2[], int rows1, int cols1, int cols2);
	
	public native static float[] GPUMatMult(float m1[], 
			float m2[], int rows1, int cols1, int cols2);	
	
	public native static float[] GPUStandardScore(float mat[], 
			int rows, int cols, int depth, float meanSD[]);
	
	public native static float[] CPUStandardScore(float mat[], 
			int rows, int cols, int depth, float meanSD[]);
	
	public native static float[] GPUMeanSD(float mat[], 
			int rows, int cols, int depth, boolean considerZeros);
	
	public native static float[] CPUMeanSD(float mat[], 
			int rows, int cols, int depth, boolean considerZeros);
	
	
	public native static void resetGPU(int deviceToUse);
	public native static long createMatrixData(int rows, int cols);
	public native static void setData(long pointer, int rows[], int cols[], float values[]);
	//public native static void setRowData(long pointer, float[] rowData, int row);
	//public native static float[] getRow(long pointer, int row);
	public native static java_to_c_info [] getNonZeroData(long pointer);
	public native static boolean deleteMatrixData(long pointer);
	public native static float getMin(long pointer);
	public native static float getMax(long pointer);
	public native static void meanSD(long pointer, long result, boolean useGPU);
	public native static void standard_deviation(long pointer, long result, boolean useGPU);
	public native static void standard_score(long pointer, long result, boolean useGPU);
	
	
	public native static void multiply(long m1, long m2, long result, boolean useGPU);
	public native static void transpose(long m1, long result);
	public native static void reduceRow(long m1, long result, boolean useGPU);
	public native static void confidence(long m1, long result, boolean useGPU);
	public native static boolean isGPUEnabled();
	
	
	
	static {		
		//JNILoadLib.loadLibrary("armadillo.8");
		//JNILoadLib.loadLibrary("cudart");
		//JNILoadLib.loadLibrary("MatrixProcessor.jnilib");
		//JNILoadLib.loadCUDART();
		//JNILoadLib.loadLibrary("MatrixProcessor");
		//JNILoadLib.loadLibrary("armadillo.8", "lib");
		//JNILoadLib.loadLibrary("cudart.9.0", "lib");
		//JNILoadLib.loadLibrary("MatrixProcessor", "JNI");
		//Map<String, String> env = System.getenv();
		//for (Map.Entry<String, String> entry : env.entrySet()) {
		//	System.out.println("Key: " + entry.getKey() + " value: " + entry.getValue());
		//}
		//System.out.println(System.getProperty("java.library.path"));
		//System.loadLibrary("sz.2");
		//System.loadLibrary("hdf5.103");
		//System.loadLibrary("armadillo.9");
		//System.loadLibrary("cudart");
		System.loadLibrary("MatrixProcessor");
	}
}
