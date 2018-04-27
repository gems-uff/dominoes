package com.josericardojunior.dao;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.scene.input.KeyCode;

import org.apache.commons.lang.time.StopWatch;

import javax.xml.ws.handler.MessageContext;

import com.josericardojunior.RepositoryImporter.ClassNode;
import com.josericardojunior.RepositoryImporter.CommitNode;
import com.josericardojunior.RepositoryImporter.FileNode;
import com.josericardojunior.RepositoryImporter.FunctionNode;
import com.josericardojunior.RepositoryImporter.RepositoryNode;
import com.josericardojunior.RepositoryImporter.UserNode;
import com.josericardojunior.arch.Cell;
import com.josericardojunior.arch.IMatrix2D;
import com.josericardojunior.arch.Matrix2DFactory;
import com.josericardojunior.arch.MatrixDescriptor;
import com.josericardojunior.domain.Dominoes;

public class DominoesSQLDao {
	
	
	//public static String repository_name = "derby";
	/*
		_begin:				2014-01-01 00:00:00
		_end:				2014-12-31 00:00:00
	 */
	//public static String databaseName = "db/gitdataminer_q0.sqlite";
	
	//_begin:				2013-01-01 00:00:00
	//_end:				2014-01-31 00:00:00
	//public static String databaseName = "db/gitdataminer.sqlite";
	private static Connection conn = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	
	public static final int Developer_Commit = 1;
	public static final int Commit_File = 7;
	public static final int File_Method = 8;
	public static final int Package_File = 3;
	public static final int File_Class = 4;
	public static final int Class_Method = 5;
	public static final int Bug_Commit = 6;
	public static final int Commit_Method = 2;
	
	public static final int Amount_Tiles = 8;
	
	public enum Group {
		Month,
		Day
	}
	
	
	public static void openDatabase(String _database) throws ClassNotFoundException, SQLException{
		
		// Check if db exist
		File db = new File(_database);
		boolean needsToReestructure = false;
		
		if (!db.exists()){
			new File(db.getParent()).mkdirs();
			needsToReestructure = true;
			
		}
		
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + _database);
		
		if (needsToReestructure)
			createDatabase();
	}
	
	private static void createDatabase(){
		
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();
			
			// Create TREPOSITORY
		    String sql = "CREATE TABLE TREPOSITORY " +
		    		"(id INTEGER PRIMARY KEY AUTOINCREMENT,Name STRING UNIQUE NOT NULL, " +
		    		"LastCommitId INTEGER REFERENCES TCOMMIT(id), RepoLocation String, BugSuffix String)";	  
		   stmt.executeUpdate(sql);
		   
		   // Create TBUG
		   sql = "CREATE TABLE TBUG(id STRING NOT NULL,CommitID STRING NOT NULL)";
		   stmt.executeUpdate(sql);
		   
		   // Create TCOMMIT
		   sql = "CREATE TABLE TCOMMIT " +
				   "(id INTEGER PRIMARY KEY AUTOINCREMENT,RepoId INTEGER REFERENCES TREPOSITORY(id) NOT NULL, " +
				   "UserID INTEGER REFERENCES TUSER(id) NOT NULL,HashCode STRING NOT NULL,Date STRING NOT NULL, " +
				   "Message STRING NOT NULL)";
		   stmt.executeUpdate(sql);
		   
		   // Create TFILE
		   sql = "CREATE TABLE TFILE " +
				   "(id INTEGER PRIMARY KEY AUTOINCREMENT,CommitId INTEGER REFERENCES TCOMMIT(id), " + 
				   "NewName STRING NOT NULL,OldName STRING NOT NULL,NewObjId STRING NOT NULL, " + 
				   "PackageName STRING NOT NULL,ChangeType STRING NOT NULL)";
		   stmt.executeUpdate(sql);
		   
		   // Create TCLASS
		   sql = "CREATE TABLE TCLASS " +
				   "(id INTEGER PRIMARY KEY AUTOINCREMENT,FileId INTEGER REFERENCES TFILE(id), " +
				   "Name STRING NOT NULL,LineStart INT NOT NULL,LineEnd INT NOT NULL,ChangeType STRING NOT NULL)";
		   stmt.executeUpdate(sql);
		   
		   // Create TFUNCTION
		   sql = "CREATE TABLE TFUNCTION " +
				   "(id INTEGER PRIMARY KEY AUTOINCREMENT,ClassId INTEGER REFERENCES TCLASS(id), " +
				   "Name STRING NOT NULL,LineStart INT NOT NULL,LineEnd INT NOT NULL,ChangeType STRING NOT NULL)";
		   stmt.executeUpdate(sql);
		   
		   // Create TUSER
		   sql = "CREATE TABLE TUSER(id INTEGER PRIMARY KEY AUTOINCREMENT,Name STRING NOT NULL)";
		   stmt.executeUpdate(sql);
		   
		   // Create TMATDESC
		   sql = "CREATE TABLE TMATDESC " +
				   "(mat_id INTEGER PRIMARY KEY  NOT NULL , column_name VARCHAR, " +
				   "row_name VARCHAR DEFAULT (null) , row_abbreviate VARCHAR, column_abbreviate VARCHAR)";
		   stmt.executeUpdate(sql);
		   
		   sql = "INSERT INTO TMATDESC (column_name, row_name, row_abbreviate, column_abbreviate) " +
				   "VALUES ('Commit', 'Developer', 'D', 'C'), ('Method', 'Commit', 'C', 'M'), " +
				   "('File', 'Package', 'P', 'F'), ('Class', 'File', 'F', 'Cl'), " +
				   "('Method', 'Class', 'Cl', 'M'), ('Commit', 'Issue', 'I', 'C'), " +
				   "('File', 'Commit', 'C', 'F')";
		   stmt.executeUpdate(sql);
		   
		   stmt.close();
		   
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void closeDatabase() throws ClassNotFoundException, SQLException{
		conn.close();
		conn = null;
	}
	
	public static Map<String, RepositoryNode> retrieveRepositores() throws SQLException{
		Map<String, RepositoryNode> repos = new HashMap<String, RepositoryNode>();
		
		String sql;
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		// Get all commits
		sql = "SELECT TR.Name, TR.LastCommitId, TR.RepoLocation, TR.BugSuffix, TC.Date FROM TREPOSITORY TR, TCOMMIT TC " + 
				"WHERE TC.repoId = TR.id AND TC.id = TR.LastCommitId";
		
		rs = smt.executeQuery(sql);
		
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		
		while (rs.next())
		{
			RepositoryNode rnode;
			try {
				rnode = new RepositoryNode(rs.getString("Name"), rs.getString("RepoLocation"),
						rs.getInt("LastCommitId"), rs.getString("BugSuffix"), sdf.parse(rs.getString("Date")) );
				repos.put(rnode.getName(), rnode);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		rs.close();
		smt.close();
		
		return repos;
	}
	
	public static void addRepository(RepositoryNode repo) throws SQLException{
		
		Statement smt;
		String sql;
		
		if (conn == null)
			throw new SQLException("Database not opened!");
		
		smt = conn.createStatement();

		
		
		if (repo.getBugPrefix().length() > 0){
			sql = "INSERT INTO TREPOSITORY(Name, LastCommitId, RepoLocation, BugSuffix) VALUES ('" + 
				repo.getName() + "', -1, '" + repo.getLocation() + "', '" + repo.getBugPrefix() + "')";
		}
		else{ 
			sql = "INSERT INTO TREPOSITORY(Name, LastCommitId, RepoLocation) VALUES ('" + 
				repo.getName() + "', -1, '" + repo.getLocation() + "')";
		}
		
		smt.executeUpdate(sql);
		smt.close();
	}
	
	public static void addCommit(CommitNode commit, RepositoryNode repoNode) {
		
		Statement smt;
		String sql;
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		
		try {
			smt = conn.createStatement();

			sql = "INSERT INTO TCOMMIT (RepoId, UserId, HashCode, Date, Message)"
					+ " VALUES ("
					+ "(SELECT id FROM TREPOSITORY TR WHERE TR.Name = '"
					+ repoNode.getName()
					+ "')," 
					+ Integer.toString(getUserId(commit.getUser().getName())) + ","
					+ "'"
					+ commit.getHashCode()
					+ "',"
					+ "'"
					+ sdf.format(commit.getDate())
					+ "',"
					+ "'"
					+ commit.getLogMessage().replaceAll("'", "''")
					+ "');";

			for (FileNode fileNode : commit.getFiles()) {
				sql += "INSERT INTO TFILE (CommitId, NewName, OldName, NewObjId, PackageName, ChangeType)"
						+ " VALUES ("
						+ "(SELECT id FROM TCOMMIT TC WHERE TC.HashCode = '"
						+ commit.getHashCode()
						+ "'),"
						+ "'"
						+ fileNode.newName
						+ "',"
						+ "'"
						+ fileNode.oldName
						+ "',"
						+ "'"
						+ fileNode.newObjId
						+ "',"
						+ "'"
						+ fileNode.packageName
						+ "',"
						+ "'"
						+ fileNode.changeType + "');";

				for (ClassNode classNode : fileNode.getClasses()) {
					sql += "INSERT INTO TCLASS (FileId, Name, LineStart, LineEnd, ChangeType)"
							+ " VALUES ("
							+ "(SELECT id FROM TFILE TF WHERE TF.NewObjId = '"
							+ fileNode.newObjId
							+ "'),"
							+ "'"
							+ classNode.getName()
							+ "',"
							+ classNode.getLineStart()
							+ ","
							+ classNode.getLineEnd()
							+ ","
							+ "'"
							+ classNode.getChangeType() + "');";

					for (FunctionNode functionNode : classNode
							.getFunctions()) {
						sql += "INSERT INTO TFUNCTION (ClassId, Name, LineStart, LineEnd, ChangeType)"
								+ " VALUES ("
								+ "(SELECT TC.id FROM TCLASS TC, TFILE TF WHERE TC.Name = '"
								+ classNode.getName()
								+ "' AND "
								+ "TF.NewObjId = '"
								+ fileNode.newObjId
								+ "' AND TC.FileId = TF.id),"
								+ "'"
								+ functionNode.getName()
								+ "',"
								+ functionNode.getLineStart()
								+ ","
								+ functionNode.getLineEnd()
								+ ","
								+ "'"
								+ functionNode.getChangeType() + "');";
					}
				}
			}
			smt.executeUpdate(sql);
		
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void MineBugs(RepositoryNode repository) throws SQLException {
		
		String sql;
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		// Get all commits
		sql = "SELECT TC.HashCode, TC.Message FROM TCommit TC, TRepository TR "
				+ "WHERE TC.RepoId = TR.id AND TR.Name = '" + repository.getName() + "' "
				+ "ORDER BY TC.Date;";
		rs = smt.executeQuery(sql);
		
		
		List<Entry<String, String>> bugCommit = new ArrayList<Map.Entry<String,String>>(); 
		
		while (rs.next())
		{
			String hashCode = rs.getString("HashCode");
			String message = rs.getString("Message");
			
			String bugId = extractBugId(message, repository.getBugPrefix());
			
			if (bugId != null){
				Map.Entry<String,String> pair = new AbstractMap.SimpleEntry(bugId, hashCode);
				
				bugCommit.add(pair);
			}
		}
		
		
		// Add found bugs
		for (Entry<String, String> record : bugCommit){
			sql = "INSERT INTO TBUG (id, commitId)"
					+ " VALUES ('" + record.getKey() + "', '" + record.getValue() + "');";

			smt.executeUpdate(sql);
		} 
		
		rs.close();
		smt.close();
	}
	
	private static String extractBugId(String text, String idBug){
		
		String res = idBug;
		
		int start = text.toLowerCase().indexOf(idBug);
		
		for (int i = start + idBug.length(); i < text.length(); i++){
			if (!Character.isDigit(text.charAt(i))){
				break;
			}
			res += text.charAt(i);
		}
		
		if (idBug.equals(res))
			return null;
		
		return res;
	}

	
	public static CommitNode getLastCommit(RepositoryNode repoNode){
		Statement smt;
		String sql;
		ResultSet rs;
		CommitNode res = null;
		
		try {
			smt = conn.createStatement();
			
			
			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			
			// Retrieve the last commit and date
			sql = "SELECT Max(date) as date, TC.hashcode, TC.id, TC.message, TU.name FROM TCOMMIT TC, TREPOSITORY TR, TUSER TU "
					+ "WHERE TC.userid = TU.id AND TC.repoid = TR.id AND TR.Name = '" + repoNode.getName() + "'";
			
			rs = smt.executeQuery(sql);
			
			while (rs.next()){
				res = new CommitNode(rs.getString("hashcode"), rs.getString("message"), 
						UserNode.AddOrRetrieveUser(rs.getString("name")), 
						sdf.parse(rs.getString("date")), rs.getInt("id"));
			}
			
			smt.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return res;
	}
	
	public static CommitNode getFirstCommit(RepositoryNode repoNode){
		Statement smt;
		String sql;
		ResultSet rs;
		CommitNode res = null;
		
		try {
			smt = conn.createStatement();
			
			
			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			
			// Retrieve the last commit and date
			sql = "SELECT Min(date) as date, TC.hashcode, TC.id, TC.message, TU.name FROM TCOMMIT TC, TREPOSITORY TR, TUSER TU "
					+ "WHERE TC.userid = TU.id AND TC.repoid = TR.id AND TR.Name = '" + repoNode.getName() + "'";
			
			rs = smt.executeQuery(sql);
			
			while (rs.next()){
				res = new CommitNode(rs.getString("hashcode"), rs.getString("message"), 
						UserNode.AddOrRetrieveUser(rs.getString("name")), 
						sdf.parse(rs.getString("date")), rs.getInt("id"));
			}
			
			smt.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return res;
	}

	public static void updateRepot(RepositoryNode repoNode){
		Statement smt;
		String sql;
		
		try {
			smt = conn.createStatement();
			
			// Update the repository with the last commit
			sql = "UPDATE TREPOSITORY SET LastCommitId = " + repoNode.getLastCommitId() + 
					" WHERE Name = '" + repoNode.getName() + "'";
				
			smt.executeUpdate(sql);
			smt.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
    public static ArrayList<Dominoes> loadAllMatrices(String _database, String _project, 
    		String _device, Date _begin, Date _end) throws IOException, SQLException, Exception {
    	
    	openDatabase(_database);
    	
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
				
				IMatrix2D _mat = loadMatrixFromDatabase(_id, _row_name, _col_name, _device, _begin, _end, _project);
				
				if (_mat != null){
					Dominoes _dom = new Dominoes(_row_ab, _col_ab, _mat, _device);
					_dominoesList.add(_dom);
				}
			}
							
			rs.close();
			smt.close();
		}
    	return _dominoesList;
    }

    private static IMatrix2D loadMatrixFromDatabase(int id, String row_name, String col_name,
    		String _device, Date _begin, Date _end, String _project) throws Exception{
    	
    	IMatrix2D result = null;
    	
    	switch (id){
    	case Developer_Commit:
    		result = loadDeveloperCommit(row_name, col_name, _device, _begin, _end, _project);
    		break;
    		
    	case Commit_File:
    		result = loadCommitFile(row_name, col_name, _device, _begin, _end, _project);
    		break;
    		
    	case Package_File:
    		result = loadPackageFile(row_name, col_name, _device, _begin, _end, _project);
    		break;
    		
    	case File_Class:
    		result = loadFileClass(row_name, col_name, _device, _begin, _end, _project);
    		break;
    		
    	case Class_Method:
    		result = loadClassMethod(row_name, col_name, _device, _begin, _end, _project);
    		break;
    		
    	case Bug_Commit:
    		result = loadBugCommit(row_name, col_name, _device, _begin, _end, _project);
    		break;
    		
    	case Commit_Method:
    		result = loadCommitMethod(row_name, col_name, _device, _begin, _end, _project);
    		break;
    		
    	case File_Method:
    		result = loadFileMethod(row_name, col_name, _device, _begin, _end, _project);

    	}
    	
    	return result;
    }
    
    private static IMatrix2D loadCommitFile(String row, String col, String 
    		_device, Date _begin, Date _end, String _project) throws Exception {
    	String sql;
		MatrixDescriptor descriptor = new MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		StopWatch stopWatch = new StopWatch();
		System.out.println("*Loading Commit x File");
		
		stopWatch.reset();
		stopWatch.start();
		
		sql = "SELECT TC.HashCode, TF.NewName FROM TCOMMIT TC, TREPOSITORY TR " + 
				"LEFT JOIN TFILE AS TF ON TF.CommitId = TC.id " + 
				"WHERE TC.repoid = TR.id AND TR.name = '" + _project + "' ";
		
		if (_begin != null) sql = sql.concat("AND TC.date >= '" + sdf.format(_begin) + "' "); 
		if (_end != null) sql = sql.concat("AND TC.date <= '" + sdf.format(_end) + "' ");
		
		sql = sql.concat("ORDER BY TC.Date, TF.NewName;");
		rs = smt.executeQuery(sql);
		
		stopWatch.stop();
		System.out.println("**SQL (ms): " + stopWatch.getTime());
		System.out.println("Commit x File Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
						
		
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
		
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(_device, descriptor);
		mat.setData(cells);
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
				
		return mat;
    }
     
    private static IMatrix2D loadDeveloperCommit(String row, String col, String _device, 
    		Date _begin, Date _end, String _project) throws Exception{
		String sql;
		MatrixDescriptor descriptor = new MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;

		StopWatch stopWatch = new StopWatch();
		System.out.println("*Loading Developer x Commit");

		stopWatch.reset();
		stopWatch.start();

		
		// Get all commits
		sql = "SELECT TU.name, TC.HashCode FROM TUser TU, TCOMMIT TC, TREPOSITORY TR " +
				"WHERE TC.userID = TU.id AND TC.RepoId = TR.id AND TR.name = '" + _project + "' ";
		
		if (_begin != null) sql = sql.concat("AND TC.date >= '" + sdf.format(_begin) + "' "); 
		if (_end != null) sql = sql.concat("AND TC.date <= '" + sdf.format(_end) + "' ");

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
		
		System.out.println("Developer x Commit Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());

		
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(_device, descriptor);
		mat.setData(cells);
		
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
		
		return mat;
	}
    
    private static IMatrix2D loadPackageFile(String row, String col, String _device, 
    		Date _begin, Date _end, String _project) throws Exception {
    	String sql;
		MatrixDescriptor descriptor = new MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;

		StopWatch stopWatch = new StopWatch();
		System.out.println("*Loading Package x File");

		stopWatch.reset();
		stopWatch.start();

		
		sql = "SELECT TC.HashCode, TF.NewName, TF.PackageName FROM TCOMMIT TC, TREPOSITORY TR, TFILE TF " + 
				"WHERE TF.CommitId = TC.id AND TC.repoid = TR.id AND TR.name = '" + _project + "' ";
		
		if (_begin != null) sql = sql.concat("AND TC.date >= '" + sdf.format(_begin) + "' "); 
		if (_end != null) sql = sql.concat("AND TC.date <= '" + sdf.format(_end) + "' ");
		
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
		
				
		System.out.println("Package x File Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());


		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(_device, descriptor);
		mat.setData(cells);
		
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
		
		return mat;
    }

    private static IMatrix2D loadFileMethod(String row, String col, String _device, 
    		Date _begin, Date _end, String _project) throws Exception {
    	String sql;
		MatrixDescriptor descriptor = new MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;

		StopWatch stopWatch = new StopWatch();
		System.out.println("*Loading File x Method");

		stopWatch.start();
	
		sql = "SELECT TF.NewName, TCL.name, TFUNC.name as FuncName FROM TCOMMIT TC, TREPOSITORY TR, TFILE TF " +
				"LEFT JOIN TCLASS AS TCL ON TCL.fileid = TF.id " +
				"LEFT JOIN TFUNCTION AS TFUNC ON TCL.id = TFUNC.classid " +
				"WHERE TF.CommitId = TC.id AND TC.repoid = TR.id AND TR.name = '" + _project + "' ";
		
		if (_begin != null) sql = sql.concat("AND TC.date >= '" + sdf.format(_begin) + "' "); 
		if (_end != null) sql = sql.concat("AND TC.date <= '" + sdf.format(_end) + "' ");

		sql = sql.concat("ORDER BY TC.Date, TF.PackageName, TF.NewName, TCL.Name, FuncName;");
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
			String funcName = rs.getString("FuncName");
			String composed = className + "$" + funcName;
			
			if (fileName == null || fileName.equals("null")){
				continue;
			}

			if (!oldRow.equals(fileName)) {

				if (!descriptor.hasRow(fileName))
					descriptor.AddRowDesc(fileName);

				oldRow = fileName;
			}
			
			if (funcName == null || funcName.equals("null")){
				continue;
			}

			if (!oldCol.equals(fileName + "$" + composed)) {

				if (!descriptor.hasCol(fileName + "$" + composed))
					descriptor.AddColDesc(fileName + "$" + composed);

				oldCol = fileName + "$" + composed;
			}

			Cell c = new Cell();
			c.row = descriptor.getRowElementIndex(fileName);
			c.col = descriptor.getColElementIndex(fileName + "$" + composed);
			c.value = 1;

			cells.add(c);
		}
			
		System.out.println("File x Method Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(_device, descriptor);
		mat.setData(cells);
		
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
		
		return mat;
    }
    
    
    private static IMatrix2D loadFileClass(String row, String col, String _device, 
    		Date _begin, Date _end, String _project) throws Exception {
    	String sql;
		MatrixDescriptor descriptor = new MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;

		StopWatch stopWatch = new StopWatch();
		System.out.println("*Loading File x Class");

		stopWatch.start();
	
		sql = "SELECT TC.HashCode, TF.NewName, TF.PackageName, TCL.name FROM TCOMMIT TC, TREPOSITORY TR, TFILE TF " +
				"LEFT JOIN TCLASS AS TCL ON TCL.fileid = TF.id " +
				"WHERE TF.CommitId = TC.id AND TC.repoid = TR.id AND TR.name = '" + _project + "' ";
		
		if (_begin != null) sql = sql.concat("AND TC.date >= '" + sdf.format(_begin) + "' "); 
		if (_end != null) sql = sql.concat("AND TC.date <= '" + sdf.format(_end) + "' ");

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
		
		System.out.println("File x Class Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(_device, descriptor);
		mat.setData(cells);
		
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
		
		return mat;
    }
    
    private static IMatrix2D loadClassMethod(String row, String col, String _device, 
    		Date _begin, Date _end, String _project) throws Exception {
    	String sql;
		MatrixDescriptor descriptor = new MatrixDescriptor(row, col);
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
				"AND TF.NewName NOT LIKE 'null' AND TC.repoid = TR.id AND TR.name = '" + _project + "' ";
		
		if (_begin != null) sql = sql.concat("AND TC.date >= '" + sdf.format(_begin) + "' "); 
		if (_end != null) sql = sql.concat("AND TC.date <= '" + sdf.format(_end) + "' ");

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
	
		System.out.println("Class x Method Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(_device, descriptor);
		mat.setData(cells);
		
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
		
		return mat;
    }
    
    private static IMatrix2D loadCommitMethod(String row, String col, String _device, 
    		Date _begin, Date _end, String _project) throws Exception {
		String sql;
		MatrixDescriptor descriptor = new MatrixDescriptor(row, col);
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
				"WHERE TC.repoid = TR.id AND TR.name = '" + _project + "' "; 



		if (_begin != null)
			sql = sql.concat("AND TC.date >= '" + sdf.format(_begin) + "' ");
		if (_end != null)
			sql = sql.concat("AND TC.date <= '" + sdf.format(_end) + "' ");

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
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(_device, descriptor);
		mat.setData(cells);
		
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
		
		return mat;
    }
    
    private static IMatrix2D loadBugCommit(String row, String col, String _device, 
    		Date _begin, Date _end, String _project) throws Exception {
    	String sql;
		MatrixDescriptor descriptor = new MatrixDescriptor(row, col);
		Statement smt = conn.createStatement();
		ResultSet rs;

		StopWatch stopWatch = new StopWatch();
		System.out.println("*Loading Bug x Commit");

		stopWatch.reset();
		stopWatch.start();
		
		sql = "SELECT TB.id, TC.hashcode FROM TCOMMIT TC, TREPOSITORY TR " +
				"LEFT JOIN TBUG AS TB ON TB.commitid = TC.hashcode " +
				"WHERE TC.RepoId = TR.id AND TR.name = '" + _project + "' ";
				
						
		

		
		if (_begin != null) sql = sql.concat("AND TC.date >= '" + sdf.format(_begin) + "' "); 
		if (_end != null) sql = sql.concat("AND TC.date <= '" + sdf.format(_end) + "' ");

		sql = sql.concat("ORDER BY TC.date, TB.id;");
			
		if (_begin != null) sql = sql.concat("AND TC.date >= '" + sdf.format(_begin) + "' "); 
		if (_end != null) sql = sql.concat("AND TC.date <= '" + sdf.format(_end) + "' ");
				
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
		
		System.out.println("Bug x Commit Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols()); 
		
		// Build Matrix
		IMatrix2D mat = Matrix2DFactory.getMatrix2D(_device, descriptor);
		mat.setData(cells);
		
		stopWatch.stop();
		System.out.println("**Building matriz (ms): " + stopWatch.getTime());
		System.out.println("**Size: " + descriptor.getNumRows() + " x " + descriptor.getNumCols());
		
		rs.close();
		smt.close();
		
		return mat;
    }
    
	public static Map<String, Integer> getNumCommits(Group group, String _database, 
			Date _begin, Date _end, String _project) throws SQLException, ClassNotFoundException{	
    	
		String sql = "";
		Statement smt = conn.createStatement();
		ResultSet rs;
		Map<String, Integer> results = new LinkedHashMap<>();
		
		if (group == Group.Month){
			sql = "SELECT strftime('%m/%Y', Date) as Period, count(*) as Total FROM TCOMMIT TC, TREPOSITORY TR " + 
					"WHERE TC.RepoId = TR.id AND TR.name = '" + _project + "' ";
		}
		else if (group == Group.Day){
			sql = "SELECT strftime('%d/%m/%Y', Date) as Period, count(*) as Total FROM TCOMMIT TC, TREPOSITORY TR " + 
					"WHERE TC.RepoId = TR.id AND TR.name = '" + _project + "' ";
		}
		
		if (_begin != null) sql = sql.concat("AND TC.date >= '" + sdf.format(_begin) + "' "); 
		if (_end != null) sql = sql.concat("AND TC.date <= '" + sdf.format(_end) + "' ");
		
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
    
	public static Map<String, Integer> getNumBugs(Group group, Date _begin, Date _end, 
			String _database, String _project) throws SQLException, ClassNotFoundException{	
    	
    	
		String sql = "";
		Statement smt = conn.createStatement();
		ResultSet rs;
		Map<String, Integer> results = new LinkedHashMap<>();
		
		if (group == Group.Month){
			sql = "SELECT strftime('%m/%Y', Date) as Period, count(*) as Total FROM TCOMMIT TC, TBUG TB, TREPOSITORY TR " + 
					"WHERE TB.commitId = TC.hashcode AND TC.RepoId = TR.id AND TR.name = '" + _project + "' ";
		}
		else if (group == Group.Day){
			sql = "SELECT strftime('%d/%m/%Y', Date) as Period, count(*) as Total FROM TCOMMIT TC, TBUG TB, TREPOSITORY TR " + 
					"WHERE TB.commitId = TC.hascode AND TC.RepoId = TR.id AND TR.name = '" + _project + "' ";
		}
		
		if (_begin != null) sql = sql.concat("AND TC.date >= '" + sdf.format(_begin) + "' "); 
		if (_end != null) sql = sql.concat("AND TC.date <= '" + sdf.format(_end) + "' ");
		
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
	
	public static int getUserId(String name) {
		
		name = name.replaceAll("'", "''");
		
		try {	
			Statement smt = conn.createStatement();
			
			String sql = "SELECT * FROM TUSER WHERE name = '" + name + "';";			
			ResultSet rs = smt.executeQuery(sql);
			
			if (!rs.next()){
				sql = "INSERT INTO TUSER (Name) VALUES ('" + name + "');";
				smt.executeUpdate(sql);
			} else {
				return rs.getInt("id");
			}
			
			
			sql = "SELECT * FROM TUSER WHERE name = '" + name + "';";			
			rs = smt.executeQuery(sql);
			
			if (rs.next())			
				return rs.getInt("id");
		
			smt.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		// Error
		return -1;
	}
	
}
