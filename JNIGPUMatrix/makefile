cuda := /usr/local/cuda
ROOTDIR := $(cuda)/samples/common


all : MatrixProcessor.jnilib
	
com_josericardojunior_Native_MatrixProcessor.h : 
	javac src/com/josericardojunior/Native/MatrixProcessor.java
	javah -verbose -jni -classpath .:./src com.josericardojunior.Native.MatrixProcessor
	
MatrixProcessor.o : com_josericardojunior_Native_MatrixProcessor.h
	#g++ -fPIC -c -I /usr/lib/jvm/java/include -I /usr/lib/jvm/java-1.7.0-openjdk.x86_64/include/linux \
	 #MatrixProcessor.c -o libMatrixProcessor.o -m64 
	g++ -I /System/Library/Frameworks/JavaVM.framework/Versions/A/Headers -I /Users/josericardo/Downloads/armadillo-4.200.0/inst/include \
	 -c MatrixProcessor.c -o libMatrixProcessor.o -m64 -fPIC
	 
kernel.o : 
	$(cuda)/bin/nvcc  -ccbin  /usr/bin/clang -Xcompiler -fpic -I$(cuda)/include -I$(ROOTDIR)/inc -m64 -c -arch=sm_20 kernel.cu -o kernel.o 
		
MatrixProcessor.jnilib : MatrixProcessor.o kernel.o
	g++ -shared libMatrixProcessor.o \
	 -Wl,-rpath,/Users/josericardo/Downloads/armadillo-4.200.0/inst/lib -Wl,-rpath,$(cuda)/lib  \
	 kernel.o -o libMatrixProcessor.jnilib -m64 -L $(cuda)/lib -lcudart -L /Users/josericardo/Downloads/armadillo-4.200.0/inst/lib -larmadillo
	
clean : 
	-rm com_josericardojunior_Native_MatrixProcessor.h
	-rm libMatrixProcessor.jnilib
	-rm libMatrixProcessor.o
	-rm kernel.o