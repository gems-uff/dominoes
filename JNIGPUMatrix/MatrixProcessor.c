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

struct MatrixInfo {
	int rows;
	int cols;
	float *data;
};

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










JNIEXPORT jlong JNICALL Java_com_josericardojunior_Native_MatrixProcessor_createMatrixData
  (JNIEnv *env, jclass obj, jint rows, jint cols){

	sp_fmat *mat = new sp_fmat(rows, cols);

	return (long) mat;
}

JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_setData
  (JNIEnv *env, jclass obj, jlong mat, jfloatArray data){

//	fprintf(stderr, "Chegou\n");

	sp_fmat *_mat = (sp_fmat*) mat;
	fprintf(stderr, "rows: %d, cols: %d\n", _mat->n_rows, _mat->n_cols);
	jfloat* _data = env->GetFloatArrayElements(data, NULL);

	int count = 0;
	for (int i = 0; i < _mat->n_rows; i++){
		for (int j = 0; j < _mat->n_cols; j++){

			float d = _data[i * _mat->n_cols + j];

			if (fabsf(d) > 0.00000001f){
				(*_mat)(i, j) = _data[i * _mat->n_cols + j];
				count++;
			}
		}
	}

	//uvec indices = find((*_mat), 0);
	//indices.print();

	fprintf(stderr, "count: %d\n", count);


	env->ReleaseFloatArrayElements(data, _data, 0);
	env->DeleteLocalRef(data);

	//fprintf(stderr, "saiu\n");
}

JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_setRowData
  (JNIEnv *env, jclass obj, jlong mat, jfloatArray dataRow, jint row){

	MatrixInfo *_mat = (MatrixInfo*) mat;

	jfloat* _dataRow = env->GetFloatArrayElements(dataRow, NULL);

	memcpy(&_mat->data[_mat->cols * row], _dataRow, sizeof(float) * _mat->cols);


	env->ReleaseFloatArrayElements(dataRow, _dataRow, 0);
	env->DeleteLocalRef(dataRow);
}


JNIEXPORT jfloatArray JNICALL Java_com_josericardojunior_Native_MatrixProcessor_getRow
  (JNIEnv *env, jclass obj, jlong pointer, jint row){

	sp_fmat* _matrix = (sp_fmat*) pointer;

	//fprintf(stderr, "row\n");
	//fprintf(stderr, "cols: %d\n", _matrix->n_cols);
	jfloatArray jres = env->NewFloatArray(_matrix->n_cols);
	//fprintf(stderr, "float array created!\n");

	float *res = new float[_matrix->n_cols];

	for (int i = 0; i < _matrix->n_cols; i++){
		res[i] = _matrix->at(row, i);
	}
	env->SetFloatArrayRegion(jres, 0, _matrix->n_cols, res);
	//fprintf(stderr, "float array set!\n");
	//fprintf(stderr, "row end\n");

	delete res;
	return jres;
}

JNIEXPORT jfloatArray JNICALL Java_com_josericardojunior_Native_MatrixProcessor_getData
  (JNIEnv *env, jclass obj, jlong pointer){

	MatrixInfo* _matrix = (MatrixInfo*) pointer;

	jfloatArray jres = env->NewFloatArray(_matrix->rows * _matrix->cols);
	env->SetFloatArrayRegion(jres, 0, _matrix->rows * _matrix->cols, _matrix->data);

	return jres;
}


JNIEXPORT jboolean JNICALL Java_com_josericardojunior_Native_MatrixProcessor_deleteMatrixData
  (JNIEnv *env, jclass obj, jlong pointer){

	sp_fmat* _matrix = (sp_fmat*) pointer;

	delete _matrix;

	fprintf(stderr, "Matrix deleted!\n");

	return true;
}


JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_multiply
  (JNIEnv *env, jclass obj, jlong mat1, jlong mat2, jlong result, jboolean useGPU){

	//fprintf(stderr, "mul\n");

	sp_fmat* _matrix1 = (sp_fmat*) mat1;
	sp_fmat* _matrix2 = (sp_fmat*) mat2;
	sp_fmat* _matResult = (sp_fmat*) result;

	//fprintf(stderr, "mul new float\n");

	if (useGPU){
			//g_MatMul(_matrix1->data, _matrix2->data, _matResult->data,
				//	_matrix1->rows, _matrix1->cols, _matrix2->cols);
	} else {

		(*_matResult) = (*_matrix1) * (*_matrix2);
		/*for (int y = 0; y < _matrix1->rows; y++){

			for (int x = 0; x < _matrix2->cols; x++){

				float sum = 0;

				for (int k = 0; k < _matrix1->cols; k++){
					sum += _matrix1->data[y*_matrix1->cols + k] *
						_matrix2->data[k*_matrix2->cols+x];
				}

				_matResult->data[y * _matrix2->cols + x] = sum;
			}
		}*/
	}

	fprintf(stderr, "non-zeros: %d\n", _matResult->n_nonzero);

}


JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_transpose
  (JNIEnv *env, jclass obj, jlong pointerMat, jlong pointerRes){

	sp_fmat* _matrix = (sp_fmat*) pointerMat;
	sp_fmat* _res = (sp_fmat*) pointerRes;

	(*_res) = _matrix->t();
}

