package com.josericardojunior.arch;

import java.util.ArrayList;
import java.util.List;

import com.josericardojunior.Native.MatrixProcessor;

public class Session {

	private static boolean sessionStarted = false;
	private static List<Matrix2D> matrices;
	private static int gpuMemoryUsed = 0;
	
	public static boolean isSessionStarted(){
		return sessionStarted;
	}
	
	public static float getMemUsed(){
		return gpuMemoryUsed;
	}
	
	public static void startSession(int deviceToUse){
		sessionStarted = true;
		MatrixProcessor.resetGPU(deviceToUse);
		matrices = new ArrayList<Matrix2D>();
	}
	
	public static void closeSection(){
		for (int i = 0; i < matrices.size(); i++){
			IMatrix2D mat = matrices.get(i);
			gpuMemoryUsed -= mat.getMemUsed();
			mat.finalize();
		}
			
	}
	
	public static void debugInfo(){
		System.out.println("Memory used: " + getMemUsed() + " KB");
	}
	
	public static void register2DMatrix(Matrix2D mat){
		gpuMemoryUsed += mat.getMemUsed();
		matrices.add(mat);
	}
	
}
