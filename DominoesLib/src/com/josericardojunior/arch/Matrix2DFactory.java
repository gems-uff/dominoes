package com.josericardojunior.arch;

public class Matrix2DFactory {

	/**
     * This function is called to define the type of access to processing unit
     * @param type String with access type
     * @param parameter of the class builder
     * @return Access type
	 * @throws Exception 
     */
    public static IMatrix2D getMatrix2D(String type, MatrixDescriptor _matrixDescriptor) throws Exception{
        if (type.toUpperCase().equals("GPU")) {
            return new Matrix2D(_matrixDescriptor);
        }else if (type.toUpperCase().equals("CPU")) {
            return new Matrix2DJava(_matrixDescriptor);
        }
        return null;
    }
}
