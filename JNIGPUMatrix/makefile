CUDA := /usr/local/cuda
EIGEN_INC := /usr/local/include
JAVAJNI := /System/Library/Frameworks/JavaVM.framework/Versions/A/Headers
LIBS := .

UNAME := $(shell uname -s)
LIBNAME := 

ifeq ($(UNAME),Linux)
   OUT_DIR := resources/lib/linux_64
   LIBNAME := libMatrixProcessor.so
endif
	
ifeq ($(UNAME),Darwin) 
   OUT_DIR := resources/lib/osx_64
   LIBNAME := libMatrixProcessor.jnilib
endif

MKDIR_P = mkdir -p
   
.PHONY: directories all clean

directories: $(OUT_DIR)

$(OUT_DIR):
	@echo "Teste"
	@echo $(OUT_DIR)
	$(MKDIR_P) $(OUT_DIR)
	


ifndef CUDA
   $(error CUDA is not set)
endif

ifndef EIGEN_INC
   $(error EIGEN_INC is not set)
endif

ifndef JAVAJNI
   $(error JAVAJNI is not set)
endif


all : directories | MatrixProcessor.jnilib
	
com_josericardojunior_Native_MatrixProcessor.h : 
	javac src/com/josericardojunior/Native/java_to_c_info.java src/com/josericardojunior/Native/MatrixProcessor.java 
	javah -verbose -jni -classpath .:./src com.josericardojunior.Native.MatrixProcessor
	
MatrixProcessor.o : com_josericardojunior_Native_MatrixProcessor.h

   ifeq ($(UNAME),Linux)
	g++ -fPIC -c -I $(JAVAJNI) -I $(JAVAJNI)/linux -I $(LIBS)/armadillo/include -I $(EIGEN_INC)/eigen/include/eigen3 \
	    -c MatrixProcessor.c -o libMatrixProcessor.o -m64 
   endif

   ifeq ($(UNAME),Darwin)
	   g++ -I $(JAVAJNI) -I $(LIBS)/armadillo/include -I $(EIGEN_INC)/eigen3 \
	      -c MatrixProcessor.c -o libMatrixProcessor.o -m64 -fPIC 
   endif
	 
kernel.o : 
   ifeq ($(UNAME),Linux)
	$(CUDA)/bin/nvcc -Xcompiler -fpic -I$(cuda)/include -I$(CUDA)/samples/common/inc -L$(cuda)/lib -lm -m64 -c -arch=sm_30 kernel.cu -o kernel.o 
   endif

   ifeq ($(UNAME),Darwin)
	$(CUDA)/bin/nvcc --cudart=static -Xcompiler -static -ccbin  /usr/bin/clang -Xcompiler -fpic -I$(cuda)/include -I$(CUDA)/samples/common/inc -L$(cuda)/lib -lm -m64 -c -arch=sm_30 kernel.cu -o kernel.o 
   endif
		
MatrixProcessor.jnilib : MatrixProcessor.o kernel.o
   ifeq ($(UNAME),Darwin)
	g++ -shared libMatrixProcessor.o \
	 -Wl,-rpath,$(LIBS)/armadillo/lib -Wl,-rpath,$(CUDA)/lib \
	 kernel.o -o $(OUT_DIR)/$(LIBNAME) -m64 -L $(CUDA)/lib -lcudart_static -L$(LIBS)/armadillo/lib  	
   endif

   ifeq ($(UNAME),Linux)
	g++ -shared libMatrixProcessor.o \
	 -Wl,-rpath,$(LIBS)/armadillo/lib -Wl,-rpath,$(CUDA)/lib64  \
	 kernel.o -o $(OUT_DIR)/$(LIBNAME) -m64 -L $(CUDA)/lib64 -lcudart -L$(LIBS)/armadillo/lib 	
   endif


	
clean : 
	-rm com_josericardojunior_Native_MatrixProcessor.h
	-rm $(OUT_DIR)/$(LIBNAME)
	-rm libMatrixProcessor.o
	-rm kernel.o
