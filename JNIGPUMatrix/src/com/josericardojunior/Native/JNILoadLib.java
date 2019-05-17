package com.josericardojunior.Native;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class JNILoadLib {
	
	private static enum Processor {
		UNKNOWN, INTEL_32, INTEL_64, PPC, ARM, AARCH_64
	}
	
	public static enum Architecture {
		UNKNOWN, LINUX_32, LINUX_64, LINUX_ARM, LINUX_ARM64, WINDOWS_32, WINDOWS_64, OSX_32,
			OSX_64, OSX_PPC
	}
	private static Architecture architecture = Architecture.UNKNOWN;
	private static final String LIB_BIN = "resources/lib/";
	
	
	/**
	 * Determines what processor is in use.
	 *
	 * @return The processor in use.
	 */
	private static Processor getProcessor() {
		Processor processor = Processor.UNKNOWN;
		int bits;

		// Note that this is actually the architecture of the installed JVM.
		final String arch = System.getProperty("os.arch").toLowerCase();

		if (arch.contains("arm")) {
			processor = Processor.ARM;
		}
		else if (arch.contains("aarch64")) {
			processor = Processor.AARCH_64;
		}
		else if (arch.contains("ppc")) {
			processor = Processor.PPC;
		}
		else if (arch.contains("86") || arch.contains("amd")) {
			bits = 32;
			if (arch.contains("64")) {
				bits = 64;
			}
			processor = (32 == bits) ? Processor.INTEL_32 : Processor.INTEL_64;
		}
		System.out.println("processor is " + processor + " os.arch is " +
			System.getProperty("os.arch").toLowerCase());
		return processor;
	}
	
	/**
	 * Determines the underlying hardware platform and architecture.
	 *
	 * @return enumerated architecture value
	 */
	public static Architecture getArchitecture() {
		if (Architecture.UNKNOWN == architecture) {
			final Processor processor = getProcessor();
			if (Processor.UNKNOWN != processor) {
				final String name = System.getProperty("os.name").toLowerCase();
				if (name.contains("nix") || name.contains("nux")) {
					if (Processor.INTEL_32 == processor) {
						architecture = Architecture.LINUX_32;
					}
					else if (Processor.INTEL_64 == processor) {
						architecture = Architecture.LINUX_64;
					}
					else if (Processor.ARM == processor) {
						architecture = Architecture.LINUX_ARM;
					}
					else if (Processor.AARCH_64 == processor) {
						architecture = Architecture.LINUX_ARM64;
					}
				}
				else if (name.contains("win")) {
					if (Processor.INTEL_32 == processor) {
						architecture = Architecture.WINDOWS_32;
					}
					else if (Processor.INTEL_64 == processor) {
						architecture = Architecture.WINDOWS_64;
					}
				}
				else if (name.contains("mac")) {
					if (Processor.INTEL_32 == processor) {
						architecture = Architecture.OSX_32;
					}
					else if (Processor.INTEL_64 == processor) {
						architecture = Architecture.OSX_64;
					}
					else if (Processor.PPC == processor) {
						architecture = Architecture.OSX_PPC;
					}
				}
			}
		}
		System.out.println("Architecture is " + architecture + " os.name is " +
			System.getProperty("os.name").toLowerCase());
		return architecture;
	}
	
	public static void loadLibrary(String name, String type){
	
		String system_dir = "";
		String extension = "";
		String beginning = "";
		
		switch (getArchitecture()){
		case OSX_64:
			system_dir = "osx_64";
			extension = type == "JNI" ? "jnilib" : "dylib";
			beginning = "lib";
			break;
			
		case LINUX_64:
			system_dir = "linux_64";
			beginning = type == "JNI" ? "jnilib" : "lib";
			extension = "so";
			break;
		}
		
		String finalName = beginning + name + "." + extension;
		
		InputStream in = JNILoadLib.class.getResourceAsStream(LIB_BIN + system_dir + "/" + finalName);
		System.out.println(LIB_BIN + system_dir + "/" + finalName);
	       
		// always write to different location
	    File fileOut = new File(System.getProperty("java.io.tmpdir") + "/" + "tmp" + LIB_BIN +
	    		system_dir + "/" + finalName);
	    
	    System.out.println(System.getProperty("java.io.tmpdir") + "/" + "tmp" + LIB_BIN +
	    		system_dir + "/" + finalName);
	       
	    OutputStream out;
		
	    try {
	    	out = FileUtils.openOutputStream(fileOut);
		    IOUtils.copy(in, out);
		    in.close();
		    out.close();
		    System.load(fileOut.toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	public static void loadCUDART(){
		
		String system_dir = "";
		String extension = "";
		String beginning = "";
		String name = "";
		
		switch (getArchitecture()){
		case OSX_64:
			system_dir = "osx_64";
			beginning = "lib";
			extension = "dylib";
			name = "cudart";
			break;
			
		case LINUX_64:
			system_dir = "linux_64";
			beginning = "lib";
			extension = "so";
			name = "cudart";
			break;
		}
		
		String finalName = beginning + name + "." + extension;
		
		InputStream in = JNILoadLib.class.getResourceAsStream(LIB_BIN + system_dir + "/" + finalName);
	       
		// always write to different location
	    File fileOut = new File(System.getProperty("java.io.tmpdir") + "/" + "tmp" + LIB_BIN +
	    		system_dir + "/" + finalName);
	       
	    OutputStream out;
		
	    try {
	    	out = FileUtils.openOutputStream(fileOut);
		    IOUtils.copy(in, out);
		    in.close();
		    out.close();
		    System.load(fileOut.toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
