package com.josericardojunior.gitdataminer;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.print.attribute.standard.MediaSize.Other;

import com.josericardojunior.gitdataminer.Analyzer.Grain;
import com.josericardojunior.gitdataminer.Analyzer.InfoType;
import com.josericardojunior.gitdataminer.Database.CubeOptions;
import com.josericardojunior.gitdataminer.Definitions.Algorithm;

public class TestCubeMatrix {
	
	public static List<String> ExtractFileNames(Matrix2D _mat){
		
		List<String> fileNames = new ArrayList<String>();
		MatrixDescriptor _matDesc = _mat.getMatrixDescriptor();
		
		String actualFile = "";
		for (int col = 0; col < _matDesc.getNumCols(); col++){
			String colDesc = _matDesc.getColumnAt(col);
			String file = colDesc.substring(0, colDesc.indexOf('$'));
			
			if (!actualFile.equals(file)){
				actualFile = file;
				fileNames.add(actualFile);
			}
		}
		
		return fileNames;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String projName = "derby";
		String repoPath = "/home/josericardo/Projects/dominoes/" 
				+ projName + "/";
		
		Database.Open();
		try {
			System.out.println("------ Cube Time Experiment --------");
			
			/*System.out.println("Extracting ED cube at file level");
			Matrix3D cubeED = Database.ExtractDevArtifactSupportCube(Grain.FILE, projName, null, CubeOptions.Max_Commit_Month, 100);
			cubeED = StandardScore.Process(cubeED);
			
			// Flatten the cube
			System.out.println("Flattening ED Cube");
			Matrix2D cubeEDfirst = cubeED.getLayer(Database.CUBE_DEPTH_PREFIX + 0);
			MatrixDescriptor cubeEDfirstDesc = cubeEDfirst.getMatrixDescriptor();
			MatrixDescriptor cubeEDflattenDesc = new MatrixDescriptor(InfoType.USER, InfoType.ARTIFACT);
						
			for (int i = 0; i < cubeEDfirstDesc.getNumRows(); i++)
				cubeEDflattenDesc.AddRowDesc(cubeEDfirstDesc.getRowAt(i));
						
			for (int i = 0; i < cubeED.getNumLayers(); i++)
				cubeEDflattenDesc.AddColDesc(Database.CUBE_DEPTH_PREFIX + i);
						
			Matrix2D cubeEDflatten = new Matrix2D(cubeEDflattenDesc);
						
									
			Set<Entry<String, Matrix2D>> cubeEDLayers = cubeED.getIterator();
			for (Entry<String, Matrix2D> entry : cubeEDLayers ){
				Matrix2D _mat = entry.getValue();
				MatrixDescriptor _matDesc = _mat.getMatrixDescriptor();
							
				for (int row = 0; row < _matDesc.getNumRows(); row++ ){
					float count = _mat.RowCount(row, 0);
								
					cubeEDflatten.SetElement(_matDesc.getRowAt(row), entry.getKey(), count);
				}				
			}
			
			cubeEDflatten.ExportCSV("MaxByMonthEDile.txt"); 
						*/
						
		// ************************************************ // ************************************************* //	
			
			System.out.println("Extracting EBD cube at method level");						
			Matrix3D cubeEBD_Method = Database.ExtractDevArtifactSupportCube(Grain.METHOD, projName, null, CubeOptions.Max_Commit_Month, 10, 1);
			cubeEBD_Method = StandardScore.Process(cubeEBD_Method, Algorithm.CPU);
						
			// Compute the EBD at file grain considering EBD at method grain
			// First of all, get all file names
			Matrix2D _cubeEBD_MethodFirstLayer = cubeEBD_Method.getLayer(Database.CUBE_DEPTH_PREFIX + 0);
			MatrixDescriptor _cubeEBD_MethodFirstLayerDesc = _cubeEBD_MethodFirstLayer.getMatrixDescriptor();
			System.out.println("Extracting File Names to compute EBD at file level using EBD from method level");
			List<String> fileNames = ExtractFileNames(_cubeEBD_MethodFirstLayer);
			
			// Create the common descriptor containing the files and developers
			MatrixDescriptor commonEBDLayerDesc = new MatrixDescriptor(InfoType.USER, InfoType.ARTIFACT);
			for (int i = 0; i < fileNames.size(); i++)
				commonEBDLayerDesc.AddColDesc(fileNames.get(i));
			
			for (int i = 0; i < _cubeEBD_MethodFirstLayerDesc.getNumRows(); i++)
				commonEBDLayerDesc.AddRowDesc(_cubeEBD_MethodFirstLayerDesc.getRowAt(i));
			
			
			
			System.out.println("Processing cube EBD at file level");
			Matrix3D cubeEBDFileFromMethod = new Matrix3D();
			Set<Entry<String, Matrix2D>> _cubeEBD_MethodLayers = cubeEBD_Method.getIterator();
			for (Entry<String, Matrix2D> entry :_cubeEBD_MethodLayers){
				Matrix2D _mat = entry.getValue();
				MatrixDescriptor _matDesc = _mat.getMatrixDescriptor();
				
				Matrix2D _layer = new Matrix2D(commonEBDLayerDesc);
				
				for (int idxFile = 0; idxFile < fileNames.size(); idxFile++){
					String _fileName = fileNames.get(idxFile);
					
					// Find index stard and end on matrices
					int idxFileStard = TestExpertiseBreadth.GetIndexStart(_fileName, _mat);
					
					if (idxFileStard >= 0){
						int idxFileEnd = TestExpertiseBreadth.GetIndexEnd(_fileName, _mat, idxFileStard);
						
						for (int row = 0; row < _matDesc.getNumRows(); row++){
							int sum = 0;
							
							for (int offset = idxFileStard; offset < idxFileEnd; offset++){
								float value = _mat.GetElement(row, offset);
								
								
								if (value > 0) sum++;
							}
							
							_layer.SetElement(
									_matDesc.getRowAt(row), 
									_fileName, (float)sum);
						}
						
					}
					
				}
				
				cubeEBDFileFromMethod.AddLayer(entry.getKey(), _layer);						
			}
			
						
			
			// Apply the z-score in the new EBD cube from methods
			//cubeEBDFileFromMethod = StandardScore.Process(cubeEBDFileFromMethod, Algorithm.CPU);
			
			
			// Flatten the cube
			System.out.println("Flattening EDB cube at file level from method level");
			Matrix2D first = cubeEBDFileFromMethod.getLayer(Database.CUBE_DEPTH_PREFIX + 0);
			MatrixDescriptor firstDesc = first.getMatrixDescriptor();
			MatrixDescriptor flattenDesc = new MatrixDescriptor(InfoType.USER, InfoType.ARTIFACT);
			
			for (int i = 0; i < firstDesc.getNumRows(); i++)
				flattenDesc.AddRowDesc(firstDesc.getRowAt(i));
			
			for (int i = 0; i < cubeEBDFileFromMethod.getNumLayers(); i++)
				flattenDesc.AddColDesc(Database.CUBE_DEPTH_PREFIX + i);
			
			Matrix2D flatten = new Matrix2D(flattenDesc);
			
						
			Set<Entry<String, Matrix2D>> cubeEBDFileFromMethodLayers = cubeEBDFileFromMethod.getIterator();
			for (Entry<String, Matrix2D> entry : cubeEBDFileFromMethodLayers){
				Matrix2D _mat = entry.getValue();
				MatrixDescriptor _matDesc = _mat.getMatrixDescriptor();
				
				for (int row = 0; row < _matDesc.getNumRows(); row++ ){
					float count = _mat.RowCount(row, 0);
					//float count = _mat.SumRow(row);
					
					flatten.SetElement(_matDesc.getRowAt(row), entry.getKey(), _mat.GetElement(row, 0));
				}				
			}
			flatten.ExportCSV("MaxMonthEDBFileFromMethods_specificFile.txt");
			
			
			
			System.out.println("Finish");
						
			// Find a 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
