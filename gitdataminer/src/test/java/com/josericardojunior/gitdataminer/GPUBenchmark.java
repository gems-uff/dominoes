package com.josericardojunior.gitdataminer;

import org.apache.commons.lang.time.StopWatch;

import com.josericardojunior.gitdataminer.Analyzer.InfoType;

public class GPUBenchmark {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int [] sizes = {1, 4};//1024, 2048, 4096, 8192, 16484, 32768};
		StopWatch timer = new StopWatch();
		
		for (int i = 0; i < sizes.length; i++){
			System.out.println("Matrix size: " + sizes[i] + "x" + sizes[i]);
			
			MatrixDescriptor desc = new MatrixDescriptor(InfoType.USER, InfoType.COMMIT);
					
			
			for (int k = 0; k < sizes[i]; k++){
				desc.AddColDesc(Integer.toString(k));
				desc.AddRowDesc(Integer.toString(k));
			}
			Matrix2D mat = new Matrix2D(desc);				

			Matrix2D res;
			try {
				timer.reset();
				timer.start();
				res = mat.CPUMultiplication(mat);				
				timer.stop();				
				System.out.println("CPU Time (ms): " + timer.getTime());	
				
				timer.reset();
				timer.start();
				res = mat.GPUMultiplication(mat);			
				timer.stop();				
				System.out.println("GPU Time (ms): " + timer.getTime());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
	
		}
		
		

	}

}
