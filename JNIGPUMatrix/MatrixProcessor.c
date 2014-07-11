/*
 * JGPUMatrix.c
 *
 *  Created on: Sep 23, 2013
 *      Author: josericardo
 */
#include <jni.h>
#include "com_josericardojunior_Native_MatrixProcessor.h"
#include <stdio.h>
#include "armadillo"

using namespace arma;

extern "C" {
	void g_MatMul(float* _mat1, float *_mat2, float *_res,
			int rows1, int cols, int cols2);

	void g_StandardDeviation(float* mat, int rows, int cols, 
		float* meanSD, float* result);

	void g_MeanSD(int rows, int cols, int depth, float *h_data,
			float *result, bool considerZeros);
}


JNIEXPORT jfloatArray JNICALL Java_com_josericardojunior_Native_MatrixProcessor_GPUMatMult
  (JNIEnv *env, jclass obj, jfloatArray m1, jfloatArray m2, jint rows1, jint cols1, jint cols2)
{
	jfloat* _m1Data= env->GetFloatArrayElements(m1, NULL);
	jfloat* _m2Data= env->GetFloatArrayElements(m2, NULL);

	float* result = new float[rows1 * cols2];

	g_MatMul(_m1Data, _m2Data, result, rows1, cols1, cols2);
	jfloatArray jres = env->NewFloatArray(rows1 * cols2);


	env->SetFloatArrayRegion(jres, 0, rows1 * cols2, (jfloat*)result);
	
	if (env->ExceptionOccurred()){
		fprintf(stderr,"Exception!\n");
	}
	
	env->ReleaseFloatArrayElements(m1, _m1Data, 0);
	env->ReleaseFloatArrayElements(m2, _m2Data, 0);
	env->DeleteLocalRef(m1);
	env->DeleteLocalRef(m2);
	return jres;
}

JNIEXPORT jfloatArray JNICALL Java_com_josericardojunior_Native_MatrixProcessor_BLASMatMult
  (JNIEnv *env, jclass obj, jfloatArray m1, jfloatArray m2, jint rows1, jint cols1, jint cols2)
{
 	jfloat* _m1Data= env->GetFloatArrayElements(m1, NULL);
	jfloat* _m2Data= env->GetFloatArrayElements(m2, NULL);
	
	fmat A(_m1Data, cols1, rows1, false);
	fmat B(_m2Data, cols2, cols1, false);
	fmat C = (A.t() * B.t()).t();

			
	jfloatArray jres = env->NewFloatArray(rows1 * cols2);

	env->SetFloatArrayRegion(jres, 0, rows1 * cols2, C.memptr());
	


	env->ReleaseFloatArrayElements(m1, _m1Data, 0);
	env->ReleaseFloatArrayElements(m2, _m2Data, 0);
	env->DeleteLocalRef(m1);
	env->DeleteLocalRef(m2);
	
	return jres;
}


JNIEXPORT jfloatArray JNICALL Java_com_josericardojunior_Native_MatrixProcessor_GPUStandardScore
  (JNIEnv *env, jclass obj, jfloatArray m, jint rows, jint cols, jint depth, jfloatArray meanSD)
{
	jfloat* _mData= env->GetFloatArrayElements(m, NULL);
	jfloat* _meanSDData= env->GetFloatArrayElements(meanSD, NULL);

	float* result = new float[rows * cols];

	g_StandardDeviation(_mData, rows, cols, 
		_meanSDData, result);

	jfloatArray jres = env->NewFloatArray(rows * cols);
	env->SetFloatArrayRegion(jres, 0, rows * cols, (jfloat*)result);

	if (env->ExceptionOccurred()){
		fprintf(stderr,"Exception!\n");
	}
	
	env->ReleaseFloatArrayElements(m, _mData, 0);
	env->ReleaseFloatArrayElements(meanSD, _meanSDData, 0);
	env->DeleteLocalRef(m);
	env->DeleteLocalRef(meanSD);
	return jres;	
}


JNIEXPORT jfloatArray JNICALL Java_com_josericardojunior_Native_MatrixProcessor_CPUMatMult
  (JNIEnv *env, jclass obj, jfloatArray m1, jfloatArray m2, jint rows1, jint cols1, jint cols2)
 {
 	jfloat* _m1Data= env->GetFloatArrayElements(m1, NULL);
	jfloat* _m2Data= env->GetFloatArrayElements(m2, NULL);

	float* result = new float[rows1 * cols2];
	
	for (int y = 0; y < rows1; y++){
		
		for (int x = 0; x < cols2; x++){
				
			float sum = 0;

			for (int k = 0; k < cols1; k++){
				sum += _m1Data[y*cols1+k] * 
					_m2Data[k*cols2+x];
			}
			
			result[y*cols2+x] = sum;
		}
	} 

			
	jfloatArray jres = env->NewFloatArray(rows1 * cols2);

	env->SetFloatArrayRegion(jres, 0, rows1 * cols2, result);
	


	env->ReleaseFloatArrayElements(m1, _m1Data, 0);
	env->ReleaseFloatArrayElements(m2, _m2Data, 0);
	env->DeleteLocalRef(m1);
	env->DeleteLocalRef(m2);
	
	delete result;
	return jres;
 }

JNIEXPORT jfloatArray JNICALL Java_com_josericardojunior_Native_MatrixProcessor_GPUMeanSD
  (JNIEnv *env, jclass obj, jfloatArray data, jint rows, jint cols, jint depth, jboolean considerZeros){

	jfloat* _data= env->GetFloatArrayElements(data, NULL);

	float* result = new float[cols * 2];

	g_MeanSD(rows, cols, depth, _data, result, considerZeros);

	jfloatArray jres = env->NewFloatArray(cols * 2);

	env->SetFloatArrayRegion(jres, 0, cols * 2, result);


	env->ReleaseFloatArrayElements(data, _data, 0);
	env->DeleteLocalRef(data);

	delete result;
	return jres;

}

JNIEXPORT jfloatArray JNICALL Java_com_josericardojunior_Native_MatrixProcessor_CPUMeanSD
  (JNIEnv *env, jclass obj, jfloatArray data, jint rows, jint cols, jint depth, jboolean considerZeros){

}



