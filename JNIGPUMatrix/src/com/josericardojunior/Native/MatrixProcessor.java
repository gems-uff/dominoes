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
	
	static {
		System.loadLibrary("MatrixProcessor");
	}
}
