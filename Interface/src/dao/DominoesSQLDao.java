package dao;

import domain.Configuration;
import domain.Dominoes;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import arch.Cell;
import arch.IMatrix2D;
import arch.Matrix2D;
import arch.Matrix2DFactory;

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

    	}
    	
    	return null;
    }
    
    private IMatrix2D loadCommitFile(String row, String col) throws Exception {
    	
    	String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		// Get all commits
		sql = "SELECT TC.HashCode, TC.Date FROM TCOMMIT TC, TREPOSITORY TR "
				+ "WHERE TC.RepoId = TR.id AND TR.Name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
		
		sql = sql.concat("ORDER BY TC.Date;");
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			descriptor.AddRowDesc(rs.getString("HashCode"));
		}
		
		// Get all elements
		sql = "SELECT Distinct(TF.NewName) FROM TFILE TF, TCOMMIT TC, TREPOSITORY TR " +
				"WHERE TF.CommitId = TC.id AND TC.RepoId = TR.id AND TF.NewName NOT LIKE 'null' " +					
				"AND TR.name = '" + repository_name + "' ";
				
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
		
		sql = sql.concat("ORDER BY TC.Date, TF.NewName;");					
					
		rs = smt.executeQuery(sql);
			
		while (rs.next())
			descriptor.AddColDesc(rs.getString("NewName"));
		
		System.out.println("Commit x File Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
						
		// Build Matrix
		//IMatrix2D mat = new Matrix2D(descriptor);
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(Configuration.processingUnit, descriptor);

		sql = "SELECT TC.HashCode, TF.NewName FROM TFILE TF, TCOMMIT TC, TREPOSITORY TR " +
				"WHERE TF.CommitId = TC.id AND TC.RepoId = TR.id AND TF.NewName NOT LIKE 'null' " +
				"AND TR.name = '" + repository_name + "' ";
		
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
		
		sql = sql.concat("ORDER BY TC.Date, TF.NewName;");
					
		rs = smt.executeQuery(sql);
					
		
		// temp matrix
		ArrayList<Cell> cells = new ArrayList<Cell>();
		
		while (rs.next()){				
			Cell c = new Cell();
			c.row = descriptor.getRowElementIndex(rs.getString("HashCode"));
			c.col = descriptor.getColElementIndex(rs.getString("NewName"));
			c.value = 1;
			cells.add(c);
		}
		
		mat.setData(cells);
		
		
		rs.close();
		smt.close();
				
		return mat;
    }
    
    private IMatrix2D loadDeveloperCommit(String row, String col) throws Exception{
    	
    	String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(
				row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		// Get all commits
		sql = "SELECT TC.HashCode, TC.Date FROM TCOMMIT TC, TREPOSITORY TR "
				+ "WHERE TC.RepoId = TR.id AND TR.Name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
		
		sql = sql.concat("ORDER BY TC.Date;");
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			descriptor.AddColDesc(rs.getString("HashCode"));
		}
		
		// Get all users
		sql = "SELECT Distinct(TU.name) FROM TUser TU, TCOMMIT TC, TREPOSITORY TR " +
				"WHERE TC.userID = TU.id AND TC.RepoId = TR.id AND TR.name = '" + repository_name + "' " +
				"ORDER BY TC.Date, TU.name;";
					
		rs = smt.executeQuery(sql);
		while (rs.next())
		{
			descriptor.AddRowDesc(rs.getString("name"));
		}
		System.out.println("Developer x Commit Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		// Build Matrix
		//Matrix2D mat = new Matrix2D(descriptor);
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(Configuration.processingUnit, descriptor);
		
		// Get all commits
		sql = "SELECT TU.name, TC.HashCode FROM TUser TU, TCOMMIT TC, TREPOSITORY TR " +
				"WHERE TC.userID = TU.id AND TC.RepoId = TR.id AND TR.name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("ORDER BY TC.Date, TU.name;");
		
		rs = smt.executeQuery(sql);
		
		ArrayList<Cell> cells = new ArrayList<Cell>();
		
		while (rs.next()){	
			Cell c = new Cell();
			c.row = descriptor.getRowElementIndex(rs.getString("name"));
			c.col = descriptor.getColElementIndex(rs.getString("HashCode"));
			c.value = 1;
			cells.add(c);
			
		}
		rs.close();
		smt.close();
		
		mat.setData(cells);
		
		
		return mat;
	}
    
    private IMatrix2D loadPackageFile(String row, String col) throws Exception {
    	String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		// Get all commits
		sql = "SELECT DISTINCT(PackageName) FROM TFILE TF, TCOMMIT TC, TREPOSITORY TR "
				+ "WHERE TF.NewName NOT LIKE 'null' AND TF.PackageName NOT LIKE 'null' AND "
				+ "TF.CommitId = TC.id AND TC.RepoId = TR.id AND TR.Name = '" + repository_name + "' ";
								
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
		
		sql = sql.concat("ORDER BY TC.Date;");
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			descriptor.AddRowDesc(rs.getString("PackageName"));
		}
		
		// Get all elements
		sql = "SELECT DISTINCT(NewName) FROM TFILE TF, TCOMMIT TC, TREPOSITORY TR "
				+ "WHERE TF.CommitId = TC.id AND TC.RepoId = TR.id AND TF.NewName NOT LIKE 'null' AND " 
				+ "TR.Name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
		
		sql = sql.concat("ORDER BY TC.Date;");				
					
		rs = smt.executeQuery(sql);
			
		while (rs.next())
			descriptor.AddColDesc(rs.getString("NewName"));
						
		System.out.println("Package x File Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(Configuration.processingUnit, descriptor);

		sql = "SELECT TC.HashCode, TF.NewName, TF.PackageName FROM TFILE TF, TCOMMIT TC, TREPOSITORY TR " +
				"WHERE TF.CommitId = TC.id AND TC.RepoId = TR.id AND TF.NewName NOT LIKE 'null' AND " + 
				"TF.PackageName NOT LIKE 'null' AND TR.name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
		
		sql = sql.concat("ORDER BY TC.Date, TF.NewName;");
					
		rs = smt.executeQuery(sql);
					
		
		// temp matrix
		ArrayList<Cell> cells = new ArrayList<Cell>();
		
		while (rs.next()){				
			Cell c = new Cell();
			c.row = descriptor.getRowElementIndex(rs.getString("PackageName"));
			c.col = descriptor.getColElementIndex(rs.getString("NewName"));
			c.value = 1;
			cells.add(c);
		}
		
		mat.setData(cells);
		
		rs.close();
		smt.close();
				
		
		
		return mat;
    }

    private IMatrix2D loadFileClass(String row, String col) throws Exception {
    	String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;
				
		sql = "SELECT DISTINCT TFL.NewName AS FileName " + 
				"FROM TFILE TFL, TCOMMIT TC, TREPOSITORY TR " +
			"WHERE TFL.CommitID = TC.id " + 
				"AND TFL.NewName NOT LIKE 'null' AND TC.RepoId = TR.id " +						
				"AND TR.name = '" + repository_name + "' ";
				
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("ORDER BY TC.Date, TFL.NewName;");
		
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			descriptor.AddRowDesc(rs.getString("FileName"));
		}
		
		sql = "SELECT DISTINCT TFL.NewName AS FileName, TCL.name as ClassName " + 
				"FROM TCLASS TCL, TFILE TFL, TCOMMIT TC, TREPOSITORY TR " +
			"WHERE TCL.fileid = TFL.id AND TFL.CommitID = TC.id " + 
				"AND TFL.NewName NOT LIKE 'null' AND TC.RepoId = TR.id " +						
				"AND TR.name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("ORDER BY TC.Date, TFL.NewName, TCL.name;");
		
		
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{			
			descriptor.AddColDesc(rs.getString("FileName") + "$" +
					rs.getString("ClassName"));
		}
		
		System.out.println("File x Class Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(Configuration.processingUnit, descriptor);
		
		
		sql = "SELECT TFL.NewName AS FileName, TCL.name as ClassName " + 
				"FROM TCLASS TCL, TFILE TFL, TCOMMIT TC, TREPOSITORY TR " +
			"WHERE TCL.fileId = TFL.id AND TFL.CommitID = TC.id " + 
				"AND TFL.NewName NOT LIKE 'null' AND TC.RepoId = TR.id " +						
				"AND TR.name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("ORDER BY TC.Date, TFL.NewName, TCL.Name;");
	
		rs = smt.executeQuery(sql);
					
		
		// temp matrix
		ArrayList<Cell> cells = new ArrayList<Cell>();
		
		while (rs.next()){				
			Cell c = new Cell();
			c.row = descriptor.getRowElementIndex(rs.getString("FileName"));
			c.col = descriptor.getColElementIndex(rs.getString("FileName") + "$" +
						rs.getString("ClassName"));
			c.value = 1;
			cells.add(c);
		}
		
		mat.setData(cells);
		
		rs.close();
		smt.close();
		
		
				
		return mat;
    }
    
    private IMatrix2D loadClassMethod(String row, String col) throws Exception {
    	String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		sql = "SELECT DISTINCT TFL.NewName, TCL.name as ClassName " + 
				"FROM TCLASS TCL, TFILE TFL, TCOMMIT TC, TREPOSITORY TR " +
			"WHERE TCL.fileid = TFL.id AND TFL.CommitID = TC.id " + 
				"AND TFL.NewName NOT LIKE 'null' AND TC.RepoId = TR.id " +						
				"AND TR.name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("GROUP BY TFL.NewName, TCL.name ");
		sql = sql.concat("ORDER BY TC.date, TFL.NewName, TCL.name;");
		
		
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			descriptor.AddRowDesc(rs.getString("NewName") + "$" + 
					rs.getString("ClassName"));
		}
		
		sql = "SELECT DISTINCT TFL.NewName, TCL.name as ClassName, TF.name as FuncName " + 
				"FROM TFunction TF, TCLASS TCL, TFILE TFL, TCOMMIT TC, TREPOSITORY TR " +
			"WHERE TF.classid = TCL.id AND TCL.fileid = TFL.id AND TFL.CommitID = TC.id " + 
				"AND TFL.NewName NOT LIKE 'null' AND TC.RepoId = TR.id " +						
				"AND TR.name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("GROUP BY TFL.NewName, TCL.name, TF.name ");
		sql = sql.concat("ORDER BY TC.date, TFL.NewName, TCL.name, TF.name;");
		
		
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{			
			descriptor.AddColDesc(rs.getString("NewName") + "$" + 
					rs.getString("ClassName") + "$" + rs.getString("FuncName"));
		}
		
		System.out.println("Class x Method Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(Configuration.processingUnit, descriptor);
		
		
		sql = "SELECT TFL.NewName, TCL.name as ClassName, TF.name as FuncName " + 
				"FROM TFunction TF, TCLASS TCL, TFILE TFL, TCOMMIT TC, TREPOSITORY TR " +
			"WHERE TF.classid = TCL.id AND TCL.fileid = TFL.id AND TFL.CommitID = TC.id " + 
				"AND TFL.NewName NOT LIKE 'null' AND TC.RepoId = TR.id " +						
				"AND TR.name = '" + repository_name + "' ";
			
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
				
		sql = sql.concat("GROUP BY TFL.NewName, TCL.name, TF.name ");
		sql = sql.concat("ORDER BY TC.date, TFL.NewName, TCL.name, TF.name;");
	
		rs = smt.executeQuery(sql);
					
		
		// temp matrix
		ArrayList<Cell> cells = new ArrayList<Cell>();
		
		while (rs.next()){				
			Cell c = new Cell();
			c.row = descriptor.getRowElementIndex(rs.getString("NewName") + "$" + 
						rs.getString("ClassName"));
			c.col = descriptor.getColElementIndex(rs.getString("NewName") + "$" + 
						rs.getString("ClassName") + "$" + rs.getString("FuncName"));
			c.value = 1;
			
			cells.add(c);
		}
		
		mat.setData(cells);
		
		rs.close();
		smt.close();
					
		
		
		
		return mat;
    }
    
    private IMatrix2D loadBugCommit(String row, String col) throws Exception {
    	String sql;
		arch.MatrixDescriptor descriptor = new arch.MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		sql = "SELECT DISTINCT TB.id FROM TBUG TB, TCOMMIT TC, TREPOSITORY TR " +
			"WHERE TB.commitid = TC.id AND TC.RepoId = TR.id " +						
				"AND TR.name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("ORDER BY TC.date, TB.id;");
		
		
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			descriptor.AddRowDesc(rs.getString("id"));
		}
		
		sql = "SELECT DISTINCT TC.Hashcode FROM TCOMMIT TC, TREPOSITORY TR " +
			"WHERE TC.RepoId = TR.id AND TR.name = '" + repository_name + "' ";
		
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("ORDER BY TC.date;");
		
		
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{			
			descriptor.AddColDesc(rs.getString("Hashcode"));
		}
		
		
		System.out.println("Bug x Commit Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(Configuration.processingUnit, descriptor);
		
		
		sql = "SELECT TB.id, TC.hashcode FROM TBUG TB, TCOMMIT TC, TREPOSITORY TR " +
				"WHERE TB.commitid = TC.id AND TC.RepoId = TR.id " +						
					"AND TR.name = '" + repository_name + "' ";
			
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");

		sql = sql.concat("ORDER BY TC.date, TB.id;");
			
		if (beginDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(beginDate) + "' "); 
		if (endDate != null) sql = sql.concat("AND TC.date <= '" + sdf.format(endDate) + "' ");
				
		sql = sql.concat("ORDER BY TC.date, TB.id;");
	
		rs = smt.executeQuery(sql);
					
		
		// temp matrix
		ArrayList<Cell> cells = new ArrayList<Cell>();
		
		while (rs.next()){				
			Cell c = new Cell();
			c.row = descriptor.getRowElementIndex(rs.getString("id"));
			c.col = descriptor.getColElementIndex(rs.getString("Hashcode"));
			c.value = 1;
			
			cells.add(c);
		}
		
		mat.setData(cells);
		
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
