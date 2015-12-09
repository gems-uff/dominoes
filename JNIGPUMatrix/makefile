#CUDA := /usr/local/cuda
#LIBS := /Users/josericardo/Documents/Projects/Libs
JAVAJNI := /System/Library/Frameworks/JavaVM.framework/Versions/A/Headers

UNAME := $(shell uname -s)
LIBNAME := 

ifeq ($(UNAME),Linux)
   LIBNAME := libMatrixProcessor.so
endif
	
ifeq ($(UNAME),Darwin) 
   LIBNAME := libMatrixProcessor.jnilib
endif

ifndef CUDA
   $(error CUDA is not set)
endif

ifndef LIBS
   $(error LIBS is not set)
endif

ifndef JAVAJNI
   $(error JAVAJNI is not set)
endif

all : MatrixProcessor.jnilib
	
com_josericardojunior_Native_MatrixProcessor.h : 
	javac src/com/josericardojunior/Native/java_to_c_info.java src/com/josericardojunior/Native/MatrixProcessor.java
	javah -verbose -jni -classpath .:./src com.josericardojunior.Native.MatrixProcessor
	
MatrixProcessor.o : com_josericardojunior_Native_MatrixProcessor.h

   ifeq ($(UNAME),Linux)
	g++ -fPIC -c -I /usr/lib/jvm/java/include -I /usr/lib/jvm/java-1.7.0-openjdk.x86_64/include/linux \
	   MatrixProcessor.c -o libMatrixProcessor.o -m64 
   endif
   
   ifeq ($(UNAME),Darwin)
	   g++ -I $(JAVAJNI) -I $(LIBS)/armadillo-4.650.3/Inst/include -I \
	      $(LIBS)/eigen/inst/include/eigen3 -c MatrixProcessor.c -o libMatrixProcessor.o -m64 -fPIC 
   endif
	 
kernel.o : 
	$(CUDA)/bin/nvcc  -ccbin  /usr/bin/clang -Xcompiler -fpic -I$(cuda)/include -I$(CUDA)/samples/common/inc -L$(cuda)/lib -lm -m64 -c -arch=sm_20 kernel.cu -o kernel.o 
		
MatrixProcessor.jnilib : MatrixProcessor.o kernel.o
	g++ -shared libMatrixProcessor.o \
	 -Wl,-rpath,$(LIBS)/armadillo-4.650.3/Inst/lib -Wl,-rpath,$(CUDA)/lib  \
	 kernel.o -o $(LIBNAME) -m64 -L $(CUDA)/lib -lcudart -L$(LIBS)/armadillo-4.650.3/Inst/lib -larmadillo 	
	
clean : 
	-rm com_josericardojunior_Native_MatrixProcessor.h
	-rm $(LIBNAME)
	-rm libMatrixProcessor.o
	-rm kernel.o