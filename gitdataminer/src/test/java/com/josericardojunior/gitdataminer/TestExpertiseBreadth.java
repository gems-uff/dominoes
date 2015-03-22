package com.josericardojunior.gitdataminer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.josericardojunior.gitdataminer.Analyzer.Grain;
import com.josericardojunior.gitdataminer.Analyzer.InfoType;
import com.josericardojunior.gitdataminer.MatrixDescriptor.Equality;

public class TestExpertiseBreadth {
	
	public static int GetIndexStart(String name, Matrix2D matrix){
		
		MatrixDescriptor matDesc = matrix.getMatrixDescriptor();
		
		for (int col = 0; col < matDesc.getNumCols(); col++){
			String colDesc = matDesc.getColumnAt(col);
			String file = colDesc.substring(0, colDesc.indexOf('$'));
			
			if (name.equals(file))
				return col;
		}
		
		return -1;
		
	}
	
	public static int GetIndexEnd(String name, Matrix2D matrix, int indexStart){
		
		MatrixDescriptor matDesc = matrix.getMatrixDescriptor();
		
		int col = indexStart;
		for (; col < matDesc.getNumCols(); col++){
			String colDesc = matDesc.getColumnAt(col);
			String file = colDesc.substring(0, colDesc.indexOf('$'));
			
			if (!name.equals(file))
				return col;
		}
		
		if (col == matDesc.getNumCols()){
			return col -1;
		}

		return -1;
		
	}
	
	public static float Distance(float n1, float n2){
		return (float)Math.sqrt((double)(n2 - n1) * (n2 - n1));
	}
	

	public static void main(String[] args) {																																																																																																																																																																																																																																																																																																								
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		String projName = "derby";
		String repoPath = "//home/josericardo/Projects/" 
				+ projName + "/";
		
		
		// Extract CM
		Database.Open();
		try {
			
			// Calculate using File
			System.out.println("Matrix CA");
			Matrix2D CAF = Database.ExtractCommitArtifactMatrix(Grain.FILE, projName, null);
			//CAF.ExportCSV("CAF.txt");
			//CA.Debug();
			
			System.out.println("Matrix DC");
			Matrix2D DC = Database.ExtractDeveloperCommitMatrix(projName, null);
			//DC.Debug();
			
			//for (int i = 0; i < CAF.getMatrixDescriptor().getNumRows(); i++)
				//if (CAF.GetElement(i, 0) > 0)
					//System.out.println("1");
			
			System.out.println("Matrix DAF");
			Matrix2D DAF = DC.GPUMultiplication(CAF);
			//DAF.ExportCSV("DAF.txt");
			
			System.out.println("Matrix SSF");
			Matrix2D SSF = StandardScore.Process(DAF, false);
			
			//SSF.ExportCSV("SSF.txt");

			
			// Find EB for each file	
			MatrixDescriptor ssfDesc = SSF.getMatrixDescriptor();
			Map<String, String> EBFile = new HashMap<String, String>();
			for (int col = 0; col < ssfDesc.getNumCols(); col++){
				float max = -10000;
				String _dev = "";
							
				for (int row = 0; row < ssfDesc.getNumRows(); row++){
					float v = SSF.GetElement(row, col);
					if (v > max){
						max = v;
						_dev = ssfDesc.getRowAt(row);
					}
				}
							
				EBFile.put(ssfDesc.getColumnAt(col), _dev);
			}
			
			// Calculate using method
			System.out.println("Matrix CAM");
			Matrix2D CAM = Database.ExtractCommitArtifactMatrix(Grain.METHOD, projName, null);

			System.out.println("Matrix DAM");
			Matrix2D DAM = DC.GPUMultiplication(CAM);
			
			System.out.println("Matrix SSM");
			Matrix2D SSM = StandardScore.Process(DAM, false);
			//SSM.ExportCSV("SSM.txt");
						
			
			Matrix2D SSMCount = new Matrix2D(ssfDesc);
			List<String> filesWithoutModifications = new ArrayList<String>();
			
			// Generate a sum of counts				
			for (int col = 0; col < ssfDesc.getNumCols(); col++){
				
				// Get methods offset
				int _start = GetIndexStart(ssfDesc.getColumnAt(col), SSM);
				
				// Any method modification on this file
				if (_start < 0){
					filesWithoutModifications.add(ssfDesc.getColumnAt(col));
					continue;
				}
				int _end = GetIndexEnd(ssfDesc.getColumnAt(col), SSM, _start);
				
				for (int row = 0; row < ssfDesc.getNumRows(); row++){
					int sumCount = 0;
					
					for (int offset = _start; offset < _end; offset++){
						if (SSM.GetElement(row, offset) > 0) sumCount++;
					}
					
					SSMCount.SetElement(ssfDesc.getRowAt(row), 
							ssfDesc.getColumnAt(col), sumCount);
				}				
			}
			
			
			
			// Check for max discrepancy among EBD and BD between developers
			/*float max = -1000;
			String _fileDisc = "";
			String _otherDeveloper = "";
			for (Map.Entry<String, String> entry : EBFile.entrySet()){
				String _file = entry.getKey();
				String _dev = entry.getValue();
				
				// Retrieve the EBD for this developer and file
				float _devEBD = SSMCount.GetElement(ssfDesc.getRowElementIndex(_dev),
						ssfDesc.getColElementIndex(_file));
				
				// Check all others developers EBD for this file
				for (int row = 0; row < ssfDesc.getNumRows(); row++){
					float _otherDev = SSMCount.GetElement(row, 
							ssfDesc.getColElementIndex(_file));
					
					if (_otherDev > _devEBD){
						float _distance = Distance(_otherDev, _devEBD);
						if ( _distance > max){
							max = _distance;
							_fileDisc = _file;
							_otherDeveloper = ssfDesc.getRowAt(row);
						}
					}
				}
			}*/
			
			// Process the z-scove over SSM
			SSMCount = StandardScore.Process(SSMCount, false);
			
			// Get the frequencey of how many times a discrepancy occurs
			StringBuffer analysisResust = new StringBuffer();
			int totalDisc = 0;
			for (Map.Entry<String, String> entry : EBFile.entrySet()){
				String _file = entry.getKey();
				String _dev = entry.getValue();
				
				// Retrieve the EBD for this developer and file
				float _devEBD = SSMCount.GetElement(ssfDesc.getRowElementIndex(_dev),
						ssfDesc.getColElementIndex(_file));
				
				// Check all others developers EBD for this file
				for (int row = 0; row < ssfDesc.getNumRows(); row++){
					float _otherDev = SSMCount.GetElement(row, 
							ssfDesc.getColElementIndex(_file));
					
					String _otherDevName = ssfDesc.getRowAt(row);
					
					if (_dev.equals(_otherDevName)) continue;
					
					
					if (_otherDev > _devEBD){
						analysisResust.append(
								PrintDiscrepancy(_file, EBFile, SSF, ssfDesc.getRowAt(row), SSMCount, SSM));
						totalDisc++;
						break;
					}
				}
			}
			
						
			
			// Output the hight difference file among using SSF and SSM
			analysisResust.append("Total: " + ssfDesc.getNumCols() 
					+ " Disparities: " + totalDisc + " (" 
					+ (float) totalDisc / ssfDesc.getNumCols() + ")\n" );

			
			System.out.println(analysisResust.toString());
			
			
			File f = new File("TestExpertiseBreadthAnalysis2.txt");
			try {
				f.createNewFile();
				
				FileWriter fw = new FileWriter(f.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(analysisResust.toString());
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			// Print files without modifications
			System.out.println("\n---Files without modifications---");
			for (String file : filesWithoutModifications)
				System.out.println(file);

			
			
			// Generate a vector of SS based on files
			/*MatrixDescriptor expFileDesc = new MatrixDescriptor(InfoType.USER, InfoType.MEAN);
			MatrixDescriptor ssDesc = SS.getMatrixDescriptor();
			
			for (int i = 0; i < ssDesc.getNumRows(); i++){
				expFileDesc.AddRowDesc(ssDesc.getRowAt(i));
			}
			expFileDesc.AddColDesc("SUM");
			
			Matrix2D expFile = new Matrix2D(expFileDesc);
			
			for (int row = 0; row < ssDesc.getNumRows(); row++){
				
				float sum = 0;
				for (int col = 0; col < ssDesc.getNumCols(); col++){
					sum += SS.GetElement(row, col);
				}
				
				expFile.SetElement(ssDesc.getRowAt(row), "SUM", sum);
			}
			
			// Print
			for (int row = 0; row < expFileDesc.getNumRows(); row++){
				System.out.println(expFileDesc.getRowAt(row) + ": " + 
					expFile.GetElement(row, 0));
			}*/
			
			
			// Export whole system ED and EDB
			PrintWriter wholeSystemDeveloperED_EBD = new PrintWriter("wholeSystemED_EBDFiltered2.txt", "UTF-8");
			wholeSystemDeveloperED_EBD.println("Developer;ED;EBD");
			
			for (int row = 0; row < SSF.getMatrixDescriptor().getNumRows(); row++){
				String dev = SSF.getMatrixDescriptor().getRowAt(row);
				float dev_ED = SSF.RowCount(row, 0);
				
				// Get the EBD
				float dev_EBD = SSM.RowCount(SSM.getMatrixDescriptor().getRowElementIndex(dev), 0);
				
				wholeSystemDeveloperED_EBD.println(dev + ";" + dev_ED + ";" + dev_EBD);
			}			
			wholeSystemDeveloperED_EBD.close();
			
			System.out.println("Finish");
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static String PrintDiscrepancy(String _fileDisc, Map<String, String> fileDevED, 
			Matrix2D _zscoreFile, String otherDev, Matrix2D _zscoreFileFromMethod, Matrix2D _zscoreMethod){
		
		StringBuffer log = new StringBuffer();
		
		log.append("----------------- DISCREPANCY --------------------\n");
		log.append("File: " + _fileDisc + "\n");
		log.append("----Developer: " + fileDevED.get(_fileDisc) +
				" ED: " + _zscoreFile.GetElement(fileDevED.get(_fileDisc), _fileDisc) + 
				" EBD: " + _zscoreFileFromMethod.GetElement(fileDevED.get(_fileDisc), _fileDisc) + "\n");
		log.append("----Developer: " + otherDev + 
				" ED: " + _zscoreFile.GetElement(otherDev, _fileDisc) +
				" EBD: " + _zscoreFileFromMethod.GetElement(otherDev, _fileDisc) + "\n");
		
		
		// Print methods
		int _f_start = GetIndexStart(_fileDisc, _zscoreMethod);
		int _f_end = GetIndexEnd(_fileDisc, _zscoreMethod, _f_start);
		log.append("Total Methods: " + (_f_end - _f_start) + "\n" );
		
		return log.toString();
		
		/*MatrixDescriptor SSMDesc = SSM.getMatrixDescriptor();
		for (int _offset = _f_start; _offset < _f_end; _offset++){
			System.out.println("Method: " + SSMDesc.getColumnAt(_offset) + " -> " +
					" Developer: " + EBFile.get(_fileDisc) + ": " +
						SSM.GetElement(EBFile.get(_fileDisc), SSMDesc.getColumnAt(_offset)) + " | " +
					" Developer: " + _otherDeveloper + ": " +
						SSM.GetElement(_otherDeveloper, SSMDesc.getColumnAt(_offset)));
		}*/
		
		//EBD tem relacao com o numero de metodos modificados, e nao a quantidade de modificacao. Ou seja, quem mexe em mais metodos diferentes
		// em uma classe vence no EBD
	}

}
