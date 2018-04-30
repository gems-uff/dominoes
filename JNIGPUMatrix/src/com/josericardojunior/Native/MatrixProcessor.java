package com.josericardojunior.Native;


public class MatrixProcessor {
	

	public native static float[] CPUMatMult(float m1[], 
			float m2[], int rows1, int cols1, int cols2);
	
	public native static float[] BLASMatMult(float m1[], 
			float m2[], int rows1, int cols1, int cols2);
	
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
	public native static void mean(long pointer, long result, boolean useGPU);
	public native static void standard_deviation(long pointer, long result, boolean useGPU);
	public native static void standard_score(long pointer, long result, boolean useGPU);
	
	
	public native static void multiply(long m1, long m2, long result, boolean useGPU);
	public native static void transpose(long m1, long result);
	public native static void reduceRow(long m1, long result, boolean useGPU);
	public native static void confidence(long m1, long result, boolean useGPU);
	public native static boolean isGPUEnabled();
	
	
	
	static {
		JNILoadLib.loadCUDART();
		JNILoadLib.loadLibrary("MatrixProcessor");
	}
}
