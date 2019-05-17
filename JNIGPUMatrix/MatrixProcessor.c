/*
 * JGPUMatrix.c
 *
 *  Created on: Sep 23, 2013
 *      Author: josericardo
 */
#include <jni.h>
#include "com_josericardojunior_Native_MatrixProcessor.h"
#include <stdio.h>
#include <Eigen/SparseCore>
//#include "armadillo"
#include <vector>

typedef Eigen::SparseMatrix<float,Eigen::RowMajor> SpMatf;
typedef Eigen::Triplet<float> Tf;

struct NonZeroInfo {
	int row;
	int col;
	float value;
};

struct MatrixInfo {
	int rows;
	int cols;
	float *data;
};


extern "C" {
	void g_MatMul(int n_rowsA, int n_colsA, int n_colsB, int nzA, int nzB,
	    	int *rowsA, int *colsA, float *valuesA,
	    	int *rowsB, int *colsB, float *valuesB,
	    	int **row_res, int **cols_res, float **values_res,
	    	int& res_nz);

	void g_StandardDeviation(float* mat, int rows, int cols, 
		float* meanSD, float* result);

	void g_MeanSD(int rows, int cols, int depth, float *h_data,
			float *result, bool considerZeros);

	void g_Confidence(float* values, float* diagonal, int elements, float* result);
	
	void g_ResetAndSetGPUDevice(int gpuDevice);
	
	bool g_IsDeviceEnabled();
}



//using namespace arma;

/*JNIEXPORT jfloatArray JNICALL Java_com_josericardojunior_Native_MatrixProcessor_GPUMatMult
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
}*/

JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_resetGPU
  (JNIEnv *env, jclass obj, jint gpuDevice)
{
	g_ResetAndSetGPUDevice(gpuDevice);
}  
  

/*JNIEXPORT jfloatArray JNICALL Java_com_josericardojunior_Native_MatrixProcessor_BLASMatMult
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
}*/


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


JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_meanSD
  (JNIEnv *env, jclass obj, jlong data, jlong result, jboolean useGPU) {

	if (useGPU){

	}

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






SpMatf *createMatrix(int rows, int cols){
	SpMatf *mat = new SpMatf(rows, cols);

	return mat;
}

void deleteMatrix(SpMatf *matrix){
	delete matrix;
	matrix = NULL;

	//fprintf(stderr, "Matrix deleted!\n");
}

void setNonZeroData(SpMatf *mat, int *rows, int *cols, float *values, int size){
	std::vector<Tf> tripletList;

	for (int i = 0; i < size; i++){
		tripletList.push_back(Tf(rows[i], cols[i], values[i]));
	}

	mat->setFromTriplets(tripletList.begin(), tripletList.end());
}

void calculateMean(SpMatf *matrix, SpMatf *result, bool useGPU){

	float sumRows[matrix->cols()];
	memset(sumRows, 0, sizeof(sumRows));

	if (useGPU){

	} else {
		for (int i = 0; i < matrix->outerSize(); ++i){
			for (SpMatf::InnerIterator it((*matrix), i); it; ++it){
				sumRows[it.col()] += it.value();
			}
		}
	}

	std::vector<Tf> tripletList;

	for (int i = 0; i < matrix->cols(); i++){
		tripletList.push_back(Tf(0, i, sumRows[i] / (float) matrix->rows()));
	}

	result->setFromTriplets(tripletList.begin(), tripletList.end());
}

void calculateSD(SpMatf *matrix, SpMatf *mean, SpMatf *result, bool useGPU){
	float variance[mean->cols()];
	float matrixToArray[matrix->rows() * matrix->cols()];
	memset(matrixToArray, 0, sizeof(matrixToArray));
	memset(variance, 0, sizeof(variance));

	int matrixRows = matrix->rows();
	int matrixCols = matrix->cols();

	for (int i = 0; i < matrix->outerSize(); i++){
		for (SpMatf::InnerIterator it((*matrix), i); it; ++it){
			matrixToArray[it.row() * matrixCols + it.col()] = it.value();
		}
	}

	for (int j = 0; j < matrixCols; j++){
		float colMean = mean->coeff(0, j);

		for (int i = 0; i < matrixRows; i++){
			float deviate = matrixToArray[i * matrixCols + j] - colMean;
			variance[j] += deviate * deviate;
		}
	}

	std::vector<Tf> tripletList;

	for (int i = 0; i < result->cols(); i++){
		tripletList.push_back(Tf(0, i, sqrtf(variance[i] / (float) matrixRows)));
	}

	result->setFromTriplets(tripletList.begin(), tripletList.end());
}

void standardScore(SpMatf *matrix, SpMatf *mean, SpMatf *sd, SpMatf *result, bool useGPU){

	float matrixToArray[matrix->rows() * matrix->cols()];
	memset(matrixToArray, 0, sizeof(matrixToArray));

	int matrixRows = matrix->rows();
	int matrixCols = matrix->cols();

	for (int i = 0; i < matrix->outerSize(); i++){
		for (SpMatf::InnerIterator it((*matrix), i); it; ++it){
			matrixToArray[it.row() * matrixCols + it.col()] = it.value();
		}
	}


	float epsilon = std::numeric_limits<float>::epsilon();

	std::vector<Tf> tripletList;

	for (int j = 0; j < matrixCols; j++){
		float colMean = mean->coeff(0, j);
		float colSD = sd->coeff(0, j);

		for (int i = 0; i < matrixRows; i++){
			float value = matrixToArray[i * matrixCols + j];
			value = (value - colMean) / colSD;

			if (fabs(value) > epsilon){
				tripletList.push_back(Tf(i, j, value));
			}
		}
	}

	result->setFromTriplets(tripletList.begin(), tripletList.end());
}

void matrixMult(SpMatf *mat1, SpMatf *mat2, SpMatf *result, bool useGPU){

	//fprintf(stderr, "mul new float\n");

	if (useGPU){
		int nonZeros_1 = mat1->nonZeros();
		int nonZeros_2 = mat2->nonZeros();

		//fprintf(stderr, "nz1: %d\n", nonZeros_1);
		//fprintf(stderr, "nz2: %d\n", nonZeros_2);

		int *rows_1 = (int*) malloc(sizeof(int) * nonZeros_1);
		int *cols_1 = (int*) malloc(sizeof(int) * nonZeros_1);
		int *rows_2 = (int*) malloc(sizeof(int) * nonZeros_2);
		int *cols_2 = (int*) malloc(sizeof(int) * nonZeros_2);
		float *values_1 = (float*) malloc(sizeof(float) * nonZeros_1);
		float *values_2 = (float*) malloc(sizeof(float) * nonZeros_2);

		//fprintf(stderr, "malloc ok\n");

		int k = 0;
		for (int i = 0; i < mat1->outerSize(); ++i){
			for (SpMatf::InnerIterator it((*mat1), i); it; ++it){
				rows_1[k] = it.row();
				cols_1[k] = it.col();
				values_1[k] = it.value();
				k++;
			}
		}

		//fprintf(stderr, "set ok\n");

		k = 0;
		for (int i = 0; i < mat2->outerSize(); ++i){
			for (SpMatf::InnerIterator it((*mat2), i); it; ++it){
				rows_2[k] = it.row();
				cols_2[k] = it.col();
				values_2[k] = it.value();
				k++;
			}
		}
		//fprintf(stderr, "set2 ok\n");

		int *res_rows, *res_cols, res_nz;
		float *res_data;

		//fprintf(stderr, "before gpu ok\n");
		g_MatMul(mat1->rows(), mat1->cols(), mat2->cols(), nonZeros_1, nonZeros_2,
				rows_1, cols_1, values_1, rows_2, cols_2, values_2, &res_rows, &res_cols, &res_data, res_nz);
		//fprintf(stderr, "after gpu ok\n");

		//fprintf(stderr, "nz1\n");


		setNonZeroData(result, res_rows, res_cols, res_data, res_nz);

		//fprintf(stderr, "nz2\n");
		free(res_rows);
		free(res_cols);
		free(res_data);
		free(rows_1);
		free(cols_1);
		free(rows_2);
		free(cols_2);
		free(values_1);
		free(values_2);
	} else {
		(*result) = (*mat1) * (*mat2);
	}
}

void calculateReducedRows(SpMatf *matrix, SpMatf *result, bool useGPU){

	//if (useGPU){
		/*int nonZeros_1 = mat1->nonZeros();
		int nonZeros_2 = mat2->nonZeros();

		int rows_1[nonZeros_1], cols_1[nonZeros_1], rows_2[nonZeros_2], cols_2[nonZeros_2];
		float values_1[nonZeros_1], values_2[nonZeros_2];

		int k = 0;
		for (int i = 0; i < mat1->outerSize(); ++i){
			for (SpMatf::InnerIterator it((*mat1), i); it; ++it){
				rows_1[k] = it.row();
				cols_1[k] = it.col();
				values_1[k] = it.value();
				k++;
			}
		}

		k = 0;
		for (int i = 0; i < mat2->outerSize(); ++i){
			for (SpMatf::InnerIterator it((*mat2), i); it; ++it){
				rows_2[k] = it.row();
				cols_2[k] = it.col();
				values_2[k] = it.value();
				k++;
			}
		}


		int *res_rows, *res_cols, res_nz;
		float *res_data;

		g_MatMul(mat1->rows(), mat1->cols(), mat2->cols(), nonZeros_1, nonZeros_2,
				rows_1, cols_1, values_1, rows_2, cols_2, values_2, &res_rows, &res_cols, &res_data, res_nz);

		fprintf(stderr, "nz1\n");;


		setNonZeroData(result, res_rows, res_cols, res_data, res_nz);

		fprintf(stderr, "nz2\n");
		free(res_rows);
		free(res_cols);
		free(res_data);*/
//	} else {
	{
		float matrixToArray[matrix->cols()];
		memset(matrixToArray, 0, sizeof(matrixToArray));

		for (int i = 0; i < matrix->outerSize(); i++){
			for (SpMatf::InnerIterator it((*matrix), i); it; ++it){
				matrixToArray[it.col()] += it.value();
			}
		}

		float epsilon = std::numeric_limits<float>::epsilon();

		std::vector<Tf> tripletList;

		for (int j = 0; j <matrix->cols(); j++){

			if (fabs(matrixToArray[j]) > epsilon){
					tripletList.push_back(Tf(0, j, matrixToArray[j]));
			}
		}

		result->setFromTriplets(tripletList.begin(), tripletList.end());
	}
}

JNIEXPORT jlong JNICALL Java_com_josericardojunior_Native_MatrixProcessor_createMatrixData
  (JNIEnv *env, jclass obj, jint rows, jint cols){

	SpMatf *mat = createMatrix(rows, cols);
	
	return (long) mat;
}


void calculateConfidence(SpMatf *mat1, SpMatf *result, bool useGPU){

	if (useGPU){
		int nonZeros = mat1->nonZeros();

		//fprintf(stderr, "nz1: %d\n", nonZeros);

		int *rows = (int*) malloc(sizeof(int) * nonZeros);
		int *cols = (int*) malloc(sizeof(int) * nonZeros);
		float *values = (float*) malloc(sizeof(float) * nonZeros);
		float *diagonal = (float*) malloc(sizeof(float) * nonZeros);
		float *res_data = (float*) malloc(sizeof(float) * nonZeros);

		//fprintf(stderr, "malloc ok\n");

		int k = 0;
		for (int i = 0; i < mat1->outerSize(); ++i){
			for (SpMatf::InnerIterator it((*mat1), i); it; ++it){
				rows[k] = it.row();
				cols[k] = it.col();
				values[k] = it.value();
				diagonal[k] = mat1->coeffRef(rows[k], rows[k]);
				k++;
			}
		}

		//fprintf(stderr, "before gpu ok\n");
		g_Confidence(values, diagonal, nonZeros, res_data);
		//fprintf(stderr, "after gpu ok\n");

		setNonZeroData(result, rows, cols, res_data, nonZeros);

		//fprintf(stderr, "nz2\n");
		free(rows);
		free(cols);
		free(res_data);
		free(values);
		free(diagonal);
	} else {
		int nonZeros = mat1->nonZeros();

		//fprintf(stderr, "nz0\n");

		int *rows = (int*) malloc(sizeof(int) * nonZeros);
		int *cols = (int*) malloc(sizeof(int) * nonZeros);
		float *values = (float*) malloc(sizeof(float) * nonZeros);
		float *res_data = (float*) malloc(sizeof(float) * nonZeros);
		memset(res_data, 0, sizeof(float) * nonZeros);

		//fprintf(stderr, "nz1\n");

		int k = 0;
		for (int i = 0; i < mat1->outerSize(); ++i){
			for (SpMatf::InnerIterator it((*mat1), i); it; ++it){
				rows[k] = it.row();
				cols[k] = it.col();

				float diagonal = mat1->coeffRef(rows[k], rows[k]);

				if (diagonal > 0)
					res_data[k] = it.value() / diagonal;

				k++;
			}
		}

		setNonZeroData(result, rows, cols, res_data, nonZeros);

		//fprintf(stderr, "nz2\n");
		free(rows);
		free(cols);
		free(res_data);
	}
}


JNIEXPORT jobjectArray JNICALL Java_com_josericardojunior_Native_MatrixProcessor_getNonZeroData
  (JNIEnv *env, jclass obj, jlong pointer){

	SpMatf *mat = (SpMatf*) pointer;

	std::vector<NonZeroInfo> nonZeros;

	//fprintf(stderr, "pointer: %d\n", mat);

	//fprintf(stderr, "rows: %d - cols: %d - NZ: %d\n", mat->rows(), mat->cols(), mat->nonZeros());


	for (int i = 0; i < mat->outerSize(); ++i){
		for (SpMatf::InnerIterator it((*mat), i); it; ++it){
			NonZeroInfo nz;
			nz.row = it.row();
			nz.col = it.col();
			nz.value = it.value();
			//fprintf(stderr, "row: %d - col: %d - value: %f\n", nz.row, nz.col, nz.value);
			nonZeros.push_back(nz);
		}
	}

	//fprintf(stderr, "Num nonzeros: %d\n", nonZeros.size());


	jclass java_to_c_info_class = env->FindClass("com/josericardojunior/Native/java_to_c_info");
	jmethodID defConstructor = env->GetMethodID(java_to_c_info_class, "<init>", "()V");
	jfieldID jav_to_c_info_row = env->GetFieldID(java_to_c_info_class, "row", "I");
	jfieldID jav_to_c_info_col = env->GetFieldID(java_to_c_info_class, "col", "I");
	jfieldID jav_to_c_info_value = env->GetFieldID(java_to_c_info_class, "value", "F");
	// find the class constructor


	/*fprintf(stderr, "class: %d!\n", jav_to_c_info_row);
	fprintf(stderr, "row: %d!\n", jav_to_c_info_row);
	fprintf(stderr, "col: %d!\n", jav_to_c_info_col);
	fprintf(stderr, "value: %d!\n", jav_to_c_info_value);
	fprintf(stderr, "constructor: %d!\n", defConstructor);*/


	jobjectArray jNonZeroArray = env->NewObjectArray(nonZeros.size(),
			java_to_c_info_class, NULL);

	//fprintf(stderr, "Created object array!\n");

	for (int i = 0; i < nonZeros.size(); i++){

		//fprintf(stderr, "Creating obj...\n");
		jobject obj = env->NewObject(java_to_c_info_class,
				defConstructor);

		//fprintf(stderr, "Object: %d!\n", obj);

		env->SetIntField(obj, jav_to_c_info_row, nonZeros[i].row);
		env->SetIntField(obj, jav_to_c_info_col, nonZeros[i].col);
		env->SetFloatField(obj, jav_to_c_info_value, nonZeros[i].value);

		env->SetObjectArrayElement(jNonZeroArray, i, obj);
	}

	return jNonZeroArray;
}

JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_setData
  (JNIEnv *env, jclass obj, jlong pointer, jintArray rows, jintArray cols, jfloatArray values){

	SpMatf *mat = (SpMatf*) pointer;

	jsize nzSize = env->GetArrayLength(rows);
	jint* _rowsData = env->GetIntArrayElements(rows, NULL);
	jint* _colsData = env->GetIntArrayElements(cols, NULL);
	jfloat* _valuesData = env->GetFloatArrayElements(values, NULL);

	setNonZeroData(mat, _rowsData, _colsData, _valuesData, nzSize);

	env->ReleaseIntArrayElements(rows, _rowsData, 0);
	env->ReleaseIntArrayElements(cols, _colsData, 0);
	env->ReleaseFloatArrayElements(values, _valuesData, 0);
	env->DeleteLocalRef(rows);
	env->DeleteLocalRef(cols);
	env->DeleteLocalRef(values);
}

JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_setRowData
  (JNIEnv *env, jclass obj, jlong mat, jfloatArray dataRow, jint row){

	MatrixInfo *_mat = (MatrixInfo*) mat;

	jfloat* _dataRow = env->GetFloatArrayElements(dataRow, NULL);

	memcpy(&_mat->data[_mat->cols * row], _dataRow, sizeof(float) * _mat->cols);


	env->ReleaseFloatArrayElements(dataRow, _dataRow, 0);
	env->DeleteLocalRef(dataRow);
}


/*JNIEXPORT jfloatArray JNICALL Java_com_josericardojunior_Native_MatrixProcessor_getRow
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
}*/

JNIEXPORT jfloatArray JNICALL Java_com_josericardojunior_Native_MatrixProcessor_getData
  (JNIEnv *env, jclass obj, jlong pointer){

	MatrixInfo* _matrix = (MatrixInfo*) pointer;

	jfloatArray jres = env->NewFloatArray(_matrix->rows * _matrix->cols);
	env->SetFloatArrayRegion(jres, 0, _matrix->rows * _matrix->cols, _matrix->data);

	return jres;
}


JNIEXPORT jboolean JNICALL Java_com_josericardojunior_Native_MatrixProcessor_deleteMatrixData
  (JNIEnv *env, jclass obj, jlong pointer){

	SpMatf* _matrix = (SpMatf*) pointer;

	deleteMatrix(_matrix);

	return true;
}


JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_multiply
  (JNIEnv *env, jclass obj, jlong mat1, jlong mat2, jlong result, jboolean useGPU){

	//fprintf(stderr, "mul\n");

	SpMatf* _matrix1 = (SpMatf*) mat1;
	SpMatf* _matrix2 = (SpMatf*) mat2;
	SpMatf* _matResult = (SpMatf*) result;

	matrixMult(_matrix1, _matrix2, _matResult, useGPU);
}


JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_transpose
  (JNIEnv *env, jclass obj, jlong pointerMat, jlong pointerRes){

	SpMatf* _matrix = (SpMatf*) pointerMat;
	SpMatf* _res = (SpMatf*) pointerRes;

	(*_res) = _matrix->transpose();
}

JNIEXPORT jfloat JNICALL Java_com_josericardojunior_Native_MatrixProcessor_getMin
  (JNIEnv *env, jclass obj, jlong pointer){

	SpMatf* _matrix = (SpMatf*) pointer;

	float min = 0;

	for (int i = 0; i < _matrix->outerSize(); i++){
		for (SpMatf::InnerIterator it((*_matrix), i); it; ++it){

			if (it.value() < min)
				min = it.value();
		}
	}

	return min;
}


JNIEXPORT jfloat JNICALL Java_com_josericardojunior_Native_MatrixProcessor_getMax
  (JNIEnv *env, jclass obj, jlong pointer){
	SpMatf* _matrix = (SpMatf*) pointer;

	float max = 0;

	for (int i = 0; i < _matrix->outerSize(); i++){
		for (SpMatf::InnerIterator it((*_matrix), i); it; ++it){

			if (it.value() > max)
				max = it.value();
		}
	}

	return max;
}

JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_mean
  (JNIEnv *env, jclass obj, jlong pointer, jlong result, jboolean useGPU){

	SpMatf* _matrix = (SpMatf*) pointer;
	SpMatf* _res = (SpMatf*) result;

	calculateMean(_matrix, _res, useGPU);
}


JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_standard_1deviation
  (JNIEnv *env, jclass obj, jlong pointer, jlong result, jboolean useGPU){

	SpMatf* _matrix = (SpMatf*) pointer;
	SpMatf* _res = (SpMatf*) result;

	SpMatf* _mean = createMatrix(1, _matrix->cols());
	calculateMean(_matrix, _mean, useGPU);

	calculateSD(_matrix, _mean, _res, useGPU);

	deleteMatrix(_mean);
}

JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_reduceRow
  (JNIEnv *env, jclass obj, jlong pointer, jlong result, jboolean useGPU){

	SpMatf* _matrix = (SpMatf*) pointer;
	SpMatf* _res = (SpMatf*) result;

	calculateReducedRows(_matrix, _res, useGPU);
}
//201447617980

JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_standard_1score
  (JNIEnv *env, jclass obj, jlong pointer, jlong result, jboolean useGPU){


}

JNIEXPORT void JNICALL Java_com_josericardojunior_Native_MatrixProcessor_confidence
  (JNIEnv *env, jclass obj, jlong pointer, jlong result, jboolean useGPU){

	SpMatf* _matrix = (SpMatf*) pointer;
	SpMatf* _res = (SpMatf*) result;

	calculateConfidence(_matrix, _res, useGPU);
}

JNIEXPORT jboolean JNICALL Java_com_josericardojunior_Native_MatrixProcessor_isGPUEnabled
  (JNIEnv *env, jclass obj){

	return g_IsDeviceEnabled();
}
