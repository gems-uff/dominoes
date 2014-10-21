package dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.scene.input.KeyCode;
import org.apache.commons.lang.time.StopWatch;
import javax.xml.ws.handler.MessageContext;
import control.Controller;
import arch.Cell;
import arch.IMatrix2D;
import arch.Matrix2DFactory;
import domain.Configuration;
import domain.Dominoes;

public class DominoesSQLDao implements DominoesDao{
	
	
	public static String repository_name = "derby";
	public static String databaseName = "db/gitdataminer.sqlite";
	private static Date beginDate = null;
	private static Date endDate = null;
	private static Connection conn = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	
	public static final int Developer_Commit = 1;
	public static final int Commit_File = 2;
	public static final int Package_File = 3;
	public static final int File_Class = 4;
	public static final int Class_Method = 5;
	public static final int Bug_Commit = 6;
	public static final int Commit_Method = 8;
	
	public static final int Amount_Tiles = 6;
	
	public enum Group {
		Month,
		Day
	}
	
	
	public static void openDatabase() throws ClassNotFoundException, SQLException{
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + databaseName);
	}

    @Override
    public ArrayList<Dominoes> loadAllMatrices() throws IOException, SQLException, Exception {
    	
    	ArrayList<Dominoes> _dominoesList = new ArrayList<Dominoes>();
    	
    	if (conn != null){
			Statement smt = conn.createStatement();
			ResultSet rs = null;
			
			String sql = "SELECT * FROM TMATDESC;";
			
			rs = smt.executeQuery(sql);
			
			while (rs.next()){
				int _id = rs.getInt("mat_id");
				String _row_name = rs.getString("row_name");
				String _col_name = rs.getString("column_name");
				String _row_ab = rs.getString("row_abbreviate");
				String _col_ab = rs.getString("column_abbreviate");
				
				//Matrix2D _mat = loadMatrixFromDatabase(_id, _row_name, _col_name);
				
				IMatrix2D _mat = loadMatrixFromDatabase(_id, _row_name, _col_name);
				
				if (_mat != null){
					Dominoes _dom = new Dominoes(_row_ab, _col_ab, _mat);
					_dominoesList.add(_dom);
				}
			}
							
			rs.close();
			smt.close();
		}
    	
    	return _dominoesList;
    }

    private IMatrix2D loadMatrixFromDatabase(int id, String row_name, String col_name) throws Exception{

    	
    	switch (id){
    	case Developer_Commit:
    		return loadDeveloperCommit(row_name, col_name);
    		
    	case Commit_File:
    		return loadCommitFile(row_name, col_name);
    		
    	case Package_File:
    		return loadPackageFile(row_name, col_name);
    		
    	case File_Class:
    		return loadFileClass(row_name, col_name);
    		
    	case Class_Method:
    		return loadClassMethod(row_name, col_name);
    		
    	case Bug_Commit:
    		return loadBugCommit(row_name, col_name);
    		
    	case Commit_Method:
    		return loadCommitMethod(row_name, col_name);

    	}
    	
    	return null;
    }
    
    private IMatrix2D loadCommitFile(String row, String col) throws Exception {
    	String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		StopWatch stopWatch = new StopWatch();
		System.out.println("*Loading Commit x File");
		
		stopWatch.reset();
		stopWatch.start();

		
		sql = "SELECT TC.HashCode, TF.NewName FROM TCOMMIT TC, TREPOSITORY TR " + 
				"LEFT JOIN TFILE AS TF ON TF.CommitId = TC.id " + 
				"WHERE TR.name = '" + repository_name + "' ";
		
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
		
		sql = sql.concat("ORDER BY TC.Date, TF.NewName;");
		rs = smt.executeQuery(sql);
		
		stopWatch.stop();
		System.out.println("**SQL (ms): " + stopWatch.getTime());
		Controller.indexTileSelected = DominoesSQLDao.Commit_File;
		Controller.printPrompt("Commit x File Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
						
		
		stopWatch.reset();
		stopWatch.start();

		ArrayList<Cell> cells = new ArrayList<Cell>();
		String oldRow = "";
		String oldCol = "";
		
		while (rs.next()){	
			String hashCode = rs.getString("hashcode");
			String newName = rs.getString("NewName");
						
			if (!oldRow.equals(hashCode)){
				
				if (!descriptor.hasRow(hashCode))
					descriptor.AddRowDesc(hashCode);
				
				oldRow = hashCode;
			}
			
			if (newName == null || newName.equals("null")) 
				continue;
			
			if (!oldCol.equals(newName)){
			
				if (!descriptor.hasCol(newName))
					descriptor.AddColDesc(newName);
				
				oldCol = newName;
			}
			
			Cell c = new Cell();
			c.row = descriptor.getRowElementIndex(hashCode);
			c.col = descriptor.getColElementIndex(newName);
			c.value = 1;
			cells.add(c);
		}
		
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(Configuration.processingUnit, descriptor);
		mat.setData(cells);
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
				
		return mat;
    }
     
    private IMatrix2D loadDeveloperCommit(String row, String col) throws Exception{
		String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;

		StopWatch stopWatch = new StopWatch();
		System.out.println("*Loading Developer x Commit");

		stopWatch.reset();
		stopWatch.start();

		
		// Get all commits
		sql = "SELECT TU.name, TC.HashCode FROM TUser TU, TCOMMIT TC, TREPOSITORY TR " +
				"WHERE TC.userID = TU.id AND TC.RepoId = TR.id AND TR.name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("ORDER BY TC.Date, TU.name;");
		rs = smt.executeQuery(sql);
				
		stopWatch.stop();
		System.out.println("**SQL (ms): " + stopWatch.getTime());

		stopWatch.reset();
		stopWatch.start();

		ArrayList<Cell> cells = new ArrayList<Cell>();
		String oldRow = "";
		String oldCol = "";

		while (rs.next()) {
			String hashCode = rs.getString("hashcode");
			String name = rs.getString("name");

			if (!oldRow.equals(name)) {

				if (!descriptor.hasRow(name))
					descriptor.AddRowDesc(name);

				oldRow = name;
			}

			if (!oldCol.equals(hashCode)) {

				if (!descriptor.hasCol(hashCode))
					descriptor.AddColDesc(hashCode);

				oldCol = hashCode;
			}

			Cell c = new Cell();
			c.row = descriptor.getRowElementIndex(name);
			c.col = descriptor.getColElementIndex(hashCode);
			c.value = 1;

			cells.add(c);
		}
		
		Controller.indexTileSelected = DominoesSQLDao.Developer_Commit;
		Controller.printPrompt("Developer x Commit Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());

		
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(Configuration.processingUnit, descriptor);
		mat.setData(cells);
		
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
		
		return mat;
	}
    
    private IMatrix2D loadPackageFile(String row, String col) throws Exception {
    	String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;

		StopWatch stopWatch = new StopWatch();
		System.out.println("*Loading Package x File");

		stopWatch.reset();
		stopWatch.start();

		
		sql = "SELECT TC.HashCode, TF.NewName, TF.PackageName FROM TCOMMIT TC, TREPOSITORY TR, TFILE TF " + 
				"WHERE TF.CommitId = TC.id AND TR.name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
		
		sql = sql.concat("ORDER BY TC.Date, TF.PackageName, TF.NewName;");
		rs = smt.executeQuery(sql);
					
		stopWatch.stop();
		System.out.println("**SQL (ms): " + stopWatch.getTime());

		stopWatch.reset();
		stopWatch.start();

		ArrayList<Cell> cells = new ArrayList<Cell>();
		String oldRow = "";
		String oldCol = "";

		while (rs.next()) {
			String packageName = rs.getString("PackageName");
			String newName = rs.getString("NewName");
			
			if (packageName == null)
				continue;

			if (!oldRow.equals(packageName)) {

				if (!descriptor.hasRow(packageName))
					descriptor.AddRowDesc(packageName);

				oldRow = packageName;
			}
			
			if (newName == null || newName.equals("null")){
				continue;
			}

			if (!oldCol.equals(newName)) {

				if (!descriptor.hasCol(newName))
					descriptor.AddColDesc(newName);

				oldCol = newName;
			}

			Cell c = new Cell();
			c.row = descriptor.getRowElementIndex(packageName);
			c.col = descriptor.getColElementIndex(newName);
			c.value = 1;

			cells.add(c);
		}
		
				
		Controller.indexTileSelected = DominoesSQLDao.Package_File;
		Controller.printPrompt("Package x File Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());


		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(Configuration.processingUnit, descriptor);
		mat.setData(cells);
		
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
		
		return mat;
    }

    private IMatrix2D loadFileClass(String row, String col) throws Exception {
    	String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;

		StopWatch stopWatch = new StopWatch();
		System.out.println("*Loading File x Class");

		stopWatch.start();
	
		sql = "SELECT TC.HashCode, TF.NewName, TF.PackageName, TCL.name FROM TCOMMIT TC, TREPOSITORY TR, TFILE TF " +
				"LEFT JOIN TCLASS AS TCL ON TCL.fileid = TF.id " +
				"WHERE TF.CommitId = TC.id AND TR.name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("ORDER BY TC.Date, TF.PackageName, TF.NewName, TCL.Name;");
		rs = smt.executeQuery(sql);
				
		stopWatch.stop();
		System.out.println("**SQL (ms): " + stopWatch.getTime());

		stopWatch.reset();
		stopWatch.start();

		ArrayList<Cell> cells = new ArrayList<Cell>();
		String oldRow = "";
		String oldCol = "";

		while (rs.next()) {
			String fileName = rs.getString("NewName");
			String className = rs.getString("name");
			
			if (fileName == null || fileName.equals("null")){
				continue;
			}

			if (!oldRow.equals(fileName)) {

				if (!descriptor.hasRow(fileName))
					descriptor.AddRowDesc(fileName);

				oldRow = fileName;
			}
			
			if (className == null || className.equals("null")){
				continue;
			}

			if (!oldCol.equals(fileName + "$" + className)) {

				if (!descriptor.hasCol(fileName + "$" + className))
					descriptor.AddColDesc(fileName + "$" + className);

				oldCol = fileName + "$" + className;
			}

			Cell c = new Cell();
			c.row = descriptor.getRowElementIndex(fileName);
			c.col = descriptor.getColElementIndex(fileName + "$" + className);
			c.value = 1;

			cells.add(c);
		}
		
		Controller.indexTileSelected = DominoesSQLDao.File_Class;
		Controller.printPrompt("File x Class Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(Configuration.processingUnit, descriptor);
		mat.setData(cells);
		
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
		
		return mat;
    }
    
    private IMatrix2D loadClassMethod(String row, String col) throws Exception {
    	String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;

		StopWatch stopWatch = new StopWatch();
		System.out.println("*Loading Class x Method");

		stopWatch.reset();
		stopWatch.start();
		
		sql = "SELECT TC.HashCode, TF.NewName, TF.PackageName, TCL.name AS ClassName, " + 
				"TM.name as FuncName FROM TCOMMIT TC, TREPOSITORY TR, TFILE TF, TCLASS TCL " +
				"LEFT JOIN TFUNCTION AS TM ON TM.classid = TCL.id " + 
				"WHERE TF.CommitId = TC.id AND TCL.fileid = TF.id " + 
				"AND TF.NewName NOT LIKE 'null' AND TC.repoid = TR.id AND TR.name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("ORDER BY TC.Date, TF.PackageName, TF.NewName, ClassName, FuncName;");
		rs = smt.executeQuery(sql);
		
		
		stopWatch.stop();
		System.out.println("**SQL (ms): " + stopWatch.getTime());

		stopWatch.reset();
		stopWatch.start();

		ArrayList<Cell> cells = new ArrayList<Cell>();
		String oldRow = "";
		String oldCol = "";
	
		while (rs.next()) {
			String fileName = rs.getString("NewName");
			String className = rs.getString("ClassName");
			String methodName = rs.getString("FuncName");
				
			if (className == null || className.equals("null")){
				continue;
			}
			
			if (!oldRow.equals(fileName + "$" + className)) {

				if (!descriptor.hasRow(fileName + "$" + className))
					descriptor.AddRowDesc(fileName + "$" + className);

				oldRow = fileName + "$" + className;
			}
			
			if (methodName == null || methodName.equals("null")){
				continue;
			}
			

			if (!oldCol.equals(className + "$" + methodName)) {

				if (!descriptor.hasCol(className + "$" + methodName))
					descriptor.AddColDesc(className + "$" + methodName);

				oldCol = className + "$" + methodName;
			}

			Cell c = new Cell();
			c.row = descriptor.getRowElementIndex(fileName + "$" + className);
			c.col = descriptor.getColElementIndex(className + "$" + methodName);
			c.value = 1;

			cells.add(c);
		}
		
		Controller.indexTileSelected = DominoesSQLDao.Class_Method;
		Controller.printPrompt("Class x Method Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(Configuration.processingUnit, descriptor);
		mat.setData(cells);
		
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
		
		return mat;
    }
    
    private IMatrix2D loadCommitMethod(String row, String col) throws Exception {
		String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;

		StopWatch stopWatch = new StopWatch();
		System.out.println("*Loading Commit x Method");

		stopWatch.reset();
		stopWatch.start();
		
		sql = "SELECT TC.HashCode, TF.NewName, TF.PackageName, TCL.name AS ClassName, " + 
				"TM.name as FuncName FROM TCOMMIT TC, TREPOSITORY TR " +
				"LEFT JOIN TFILE AS TF ON TF.commitid = TC.id " +
				"LEFT JOIN TCLASS AS TCL ON TCL.fileid = TF.id " +
				"LEFT JOIN TFUNCTION AS TM ON TM.classid = TCL.id " + 
				"WHERE TR.name = '" + repository_name + "' "; 



		if (beginDate != null)
			sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' ");
		if (endDate != null)
			sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("ORDER BY TC.date, TF.PackageName, TF.NewName, ClassName, FuncName;");

		rs = smt.executeQuery(sql);
		stopWatch.stop();
		System.out.println("**SQL (ms): " + stopWatch.getTime());

		stopWatch.reset();
		stopWatch.start();

		ArrayList<Cell> cells = new ArrayList<Cell>();
		String oldRow = "";
		String oldCol = "";

		while (rs.next()) {
			String hashCode = rs.getString("hashcode");
			String className = rs.getString("ClassName"); 
			String newName = rs.getString("FuncName");
			

			if (!oldRow.equals(hashCode)) {

				if (!descriptor.hasRow(hashCode))
					descriptor.AddRowDesc(hashCode);

				oldRow = hashCode;
			}
			
			if (newName == null || newName.equals("null")){
				continue;
			}

			if (!oldCol.equals(className + "$" + newName)) {

				if (!descriptor.hasCol(className + "$" + newName))
					descriptor.AddColDesc(className + "$" + newName);

				oldCol = className + "$" + newName;
			}

			Cell c = new Cell();
			c.row = descriptor.getRowElementIndex(hashCode);
			c.col = descriptor.getColElementIndex(className + "$" + newName);
			c.value = 1;

			cells.add(c);
		}
		
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(Configuration.processingUnit, descriptor);
		mat.setData(cells);
		
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
		
		return mat;
    }
    
    private IMatrix2D loadBugCommit(String row, String col) throws Exception {
    	String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;

		StopWatch stopWatch = new StopWatch();
		System.out.println("*Loading Bug x Commit");

		stopWatch.reset();
		stopWatch.start();
		
		sql = "SELECT TB.id, TC.hashcode FROM TCOMMIT TC, TREPOSITORY TR " +
				"LEFT JOIN TBUG AS TB ON TB.commitid = TC.id " +
				"WHERE TC.RepoId = TR.id AND TR.name = '" + repository_name + "' ";
				
						
		

		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("ORDER BY TC.date, TB.id;");
			
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
				
		sql = sql.concat("ORDER BY TC.date, TB.id;");
		rs = smt.executeQuery(sql);
				
		stopWatch.stop();
		System.out.println("**SQL (ms): " + stopWatch.getTime());

		stopWatch.reset();
		stopWatch.start();

		ArrayList<Cell> cells = new ArrayList<Cell>();
		String oldRow = "";
		String oldCol = "";

		while (rs.next()) {
			String id = rs.getString("id");
			String hashCode = rs.getString("hashCode");

			if (id != null && !id.equals("null")) {
				if (!oldRow.equals(id)) {
	
					if (!descriptor.hasRow(id))
						descriptor.AddRowDesc(id);
	
					oldRow = id;
				}
			}

			if (!oldCol.equals(hashCode)) {

				if (!descriptor.hasCol(hashCode))
					descriptor.AddColDesc(hashCode);

				oldCol = hashCode;
			}

			
			if (id != null && !id.equals("null")){
				Cell c = new Cell();
				c.row = descriptor.getRowElementIndex(id);
				c.col = descriptor.getColElementIndex(hashCode);
				c.value = 1;
	
				cells.add(c);
			}
		}
		
		Controller.indexTileSelected = DominoesSQLDao.Bug_Commit;
		Controller.printPrompt("Bug x Commit Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols()); 
		
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(Configuration.processingUnit, descriptor);
		mat.setData(cells);
		
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
		
		return mat;
    }
    
    
	@Override
    public Dominoes loadMatrix(Dominoes dominoes) throws IOException {
        return null;
    }

    @Override
    public boolean removeMatrix(Dominoes dominoes) throws IOException {
        return true;
    }

    @Override
    public boolean saveMatrix(Dominoes dominoes) throws IOException {
        return true;
    }

	public static Date getBeginDate() {
		return beginDate;
	}

	public static void setBeginDate(Date beginDate) {
		DominoesSQLDao.beginDate = beginDate;
	}

	public static Date getEndDate() {
		return endDate;
	}

	public static void setEndDate(Date endDate) {
		DominoesSQLDao.endDate = endDate;
	}
	
	public static Map<String, Integer> getNumCommits(Group group) throws SQLException{		
		String sql = "";
		Statement smt = conn.createStatement();
		ResultSet rs;
		Map<String, Integer> results = new LinkedHashMap<>();
		
		if (group == Group.Month){
			sql = "SELECT strftime('%m/%Y', Date) as Period, count(*) as Total FROM TCOMMIT TC, TREPOSITORY TR " + 
					"WHERE TC.RepoId = TR.id AND TR.name = '" + repository_name + "' ";
		}
		else if (group == Group.Day){
			sql = "SELECT strftime('%d/%m/%Y', Date) as Period, count(*) as Total FROM TCOMMIT TC, TREPOSITORY TR " + 
					"WHERE TC.RepoId = TR.id AND TR.name = '" + repository_name + "' ";
		}
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
		
		if (group == Group.Month){
			sql = sql.concat("GROUP BY strftime('%m/%Y', date) ");
		} else if (group == Group.Day){
			sql = sql.concat("GROUP BY strftime('%d/%m/%Y', date) ");
		}
		
		sql = sql.concat("ORDER BY TC.date;");
		
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			results.put(rs.getString("Period"), rs.getInt("Total"));
			System.out.println(rs.getString("Period") + " - " + rs.getInt("Total"));
		}
		
		
		rs.close();
		smt.close();
							
		return results;
	}
    
	public static Map<String, Integer> getNumBugs(Group group) throws SQLException{		
		String sql = "";
		Statement smt = conn.createStatement();
		ResultSet rs;
		Map<String, Integer> results = new LinkedHashMap<>();
		
		if (group == Group.Month){
			sql = "SELECT strftime('%m/%Y', Date) as Period, count(*) as Total FROM TCOMMIT TC, TBUG TB, TREPOSITORY TR " + 
					"WHERE TB.commitId = TC.id AND TC.RepoId = TR.id AND TR.name = '" + repository_name + "' ";
		}
		else if (group == Group.Day){
			sql = "SELECT strftime('%d/%m/%Y', Date) as Period, count(*) as Total FROM TCOMMIT TC, TBUG TB, TREPOSITORY TR " + 
					"WHERE TB.commitId = TC.id AND TC.RepoId = TR.id AND TR.name = '" + repository_name + "' ";
		}
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
		
		if (group == Group.Month){
			sql = sql.concat("GROUP BY strftime('%m/%Y', date) ");
		} else if (group == Group.Day){
			sql = sql.concat("GROUP BY strftime('%d/%m/%Y', date) ");
		}
		
		sql = sql.concat("ORDER BY TC.date;");
		
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			results.put(rs.getString("Period"), rs.getInt("Total"));
			System.out.println(rs.getString("Period") + " - " + rs.getInt("Total"));
		}
		
		
		rs.close();
		smt.close();
					
		
		
		return results;
	}
	
}
