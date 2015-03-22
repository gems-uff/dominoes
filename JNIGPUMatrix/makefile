cuda := /usr/local/cuda
ROOTDIR := $(cuda)/samples/common
LIBS := /Users/josericardo/Projects/Libs

all : MatrixProcessor.jnilib
	
com_josericardojunior_Native_MatrixProcessor.h : 
	javac src/com/josericardojunior/Native/java_to_c_info.java src/com/josericardojunior/Native/MatrixProcessor.java
	javah -verbose -jni -classpath .:./src com.josericardojunior.Native.MatrixProcessor
	
MatrixProcessor.o : com_josericardojunior_Native_MatrixProcessor.h
	#g++ -fPIC -c -I /usr/lib/jvm/java/include -I /usr/lib/jvm/java-1.7.0-openjdk.x86_64/include/linux \
	 #MatrixProcessor.c -o libMatrixProcessor.o -m64 
	g++ -I /System/Library/Frameworks/JavaVM.framework/Versions/A/Headers -I$(LIBS)/armadillo-4.650.3/Inst/include \
	 -I /usr/local/include/eigen3 -c MatrixProcessor.c -o libMatrixProcessor.o -m64 -fPIC -stdlib=libstdc++	
	 
kernel.o : 
	$(cuda)/bin/nvcc  -ccbin  /usr/bin/clang -Xcompiler -fpic -I$(cuda)/include -I$(ROOTDIR)/inc -L$(cuda)/lib -lm -lstdc++ -m64 -c -arch=sm_20 kernel.cu -o kernel.o 
		
MatrixProcessor.jnilib : MatrixProcessor.o kernel.o
	g++ -shared libMatrixProcessor.o \
	 -Wl,-rpath,$(LIBS)/armadillo-4.650.3/Inst/lib -Wl,-rpath,$(cuda)/lib  \
	 kernel.o -o libMatrixProcessor.jnilib -m64 -L $(cuda)/lib -lcudart -L$(LIBS)/armadillo-4.650.3/Inst/lib -larmadillo -stdlib=libstdc++	
	
clean : 
	-rm com_josericardojunior_Native_MatrixProcessor.h
	-rm libMatrixProcessor.jnilib
	-rm libMatrixProcessor.o
	-rm kernel.o