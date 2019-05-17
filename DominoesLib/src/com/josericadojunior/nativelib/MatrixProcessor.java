package com.josericadojunior.nativelib;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class MatrixProcessor {
	
	public interface Cuda extends Library {
		Cuda INSTANCE = (Cuda)
				Native.load("cudart.10.0", Cuda.class);
	}
	
	public interface Sz extends Library {
		Sz INSTANCE = (Sz)
				Native.load("sz.2", Sz.class);
	}
	
	public interface ArPack extends Library {
		ArPack INSTANCE = (ArPack)
				Native.load("arpack.2", ArPack.class);
	}
	
	public interface Armadillo extends Library {
		
		//ArPack arpack = ArPack.INSTANCE;
		
		Armadillo INSTANCE = (Armadillo)
				Native.load("armadillo.9.10.5", Armadillo.class);
	}
	
	public interface GPUMatProc extends Library {
		

		
		//CLibray HDF5 = (CLibray)
		//		Native.load("hdf5.103", CLibray.class);
		



		//Armadillo armadillo = Armadillo.INSTANCE;
		
		GPUMatProc INSTANCE = (GPUMatProc) 
				Native.load("MatrixProcessor", GPUMatProc.class);
		
		//GPUMatProc INSTANCE = (GPUMatProc) 
		//				Native.load("SomadorJNA", GPUMatProc.class);
		
		boolean g_IsDeviceEnabled();
		//public int soma(int num1, int num2);
		//public void mul();
	}
}
