package com.josericardojunior.RepositoryImporter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.ldap.SortControl;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;

public class ClassNode  {

	public int getLineEnd() {
		return lineEnd;
	}

	int lineStart;
	int lineEnd;
	String data;
	ChangeType changeType;
	String name;
	List<FunctionNode> functions = new ArrayList<FunctionNode>();
	
	public List<FunctionNode> getFunctions() {
		return functions;
	}

	public int getLineStart() {
		return lineStart;
	}

	public String getData() {
		return data;
	}

	public ChangeType getChangeType() {
		return changeType;
	}

	public String getName() {
		return name;
	}
	
	public void Debug(){
		System.out.println("----Name: " + name);
		System.out.println("------LineStart: " + lineStart);
		System.out.println("------LineEnd: " + lineEnd);
		System.out.println("------ChangeType: " + changeType.toString());
		
		for (FunctionNode func : functions)
			func.Debug();
	}

	
	private static Map<String,ClassNode> ExtractClasses(String source){
		final Map<String,ClassNode> _classes = new HashMap<String, ClassNode>();
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Hashtable<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.DISABLED);
		parser.setCompilerOptions(options);
		
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		cu.accept(new ASTVisitor() {
			
			public boolean visit(TypeDeclaration node){
				ClassNode _c = new ClassNode();
				_c.data = node.toString();
				_c.lineStart = cu.getLineNumber(node.getName().getStartPosition());
				_c.lineEnd = cu.getLineNumber(node.getStartPosition() + node.getLength());
				_c.name = node.getName().toString();
				
				_classes.put(_c.name, _c);
				return false;
			}
		});

		
		return _classes;
		
	}
	
	public static List<ClassNode> Parse(FileNode _file, Repository _repo) throws MissingObjectException, IOException{
		
		List<ClassNode> resultClass = new ArrayList<ClassNode>();

		switch (_file.changeType) {
		case ADD: {
			ObjectLoader newFile = _repo.open(_file.newObjId);
			String newData = FileNode.readStream(newFile.openStream());

			resultClass.addAll(ExtractClasses(newData).values());

			for (ClassNode _class : resultClass) {
				_class.changeType = ChangeType.ADD;
				_class.functions = FunctionNode.Parse(_class, null);
			}

		}
			break;

		case MODIFY: {
			// Extract classes from all files in order to see if there is any
			// modification on it
			ObjectLoader oldFile = _repo.open(_file.oldObjId);
			String oldData = FileNode.readStream(oldFile.openStream());

			ObjectLoader newFile = _repo.open(_file.newObjId);
			String newData = FileNode.readStream(newFile.openStream());

			Map<String, ClassNode> oldClasses = ExtractClasses(oldData);
			Map<String, ClassNode> newClasses = ExtractClasses(newData);

			resultClass = FindClassDiff(oldClasses, newClasses);
		}
			break;

		default:
			break;

		}
		return resultClass;
	}
	
	private static List<ClassNode> FindClassDiff(Map<String,ClassNode> oldClasses,
			Map<String,ClassNode> newClasses) {
		
		List<ClassNode> _res = new ArrayList<ClassNode>();
		
		// Compare each class
		for (ClassNode _classNew : newClasses.values()){
			
			// Try to find the class in previous version
			ClassNode _oldClass = oldClasses.get(_classNew.name);
			
			if (_oldClass != null){
				
				// See if they are different
				if (_classNew.data.compareTo(_oldClass.data) != 0){
					_classNew.changeType = ChangeType.MODIFY;
					
					// Check for functions modified
					_classNew.functions = FunctionNode.Parse(_classNew, _oldClass);
					
					_res.add(_classNew);
				}
			} else { // The old file does not have this class (NEW)
				_classNew.changeType = ChangeType.ADD;
				_classNew.functions = FunctionNode.Parse(_classNew, null);
				_res.add(_classNew);
			}
			
		}
		
		return _res;
	}

}
