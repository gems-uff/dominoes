package com.josericardojunior.RepositoryImporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;

public class FunctionNode {
	int lineStart;
	public int getLineStart() {
		return lineStart;
	}

	public int getLineEnd() {
		return lineEnd;
	}

	public ChangeType getChangeType() {
		return changeType;
	}

	public String getName() {
		return name;
	}

	int lineEnd;
	ChangeType changeType;
	String name;
	
	private static Map<String,List<MethodDeclaration>> ExtractFunctions(ClassNode _classNode){
		final Map<String,List<MethodDeclaration> > _functions = new HashMap<String, List<MethodDeclaration> >();
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(_classNode.getData().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Hashtable<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.DISABLED);
		parser.setCompilerOptions(options);
		
		
		
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		cu.accept(new ASTVisitor() {
			
			public boolean visit(MethodDeclaration node){
				
				List<MethodDeclaration> _mdl = _functions.get(node.getName().toString());
				
				if (_mdl == null){
					_mdl = new ArrayList<MethodDeclaration>();
					_functions.put(node.getName().toString(), _mdl);
				}
				
				_mdl.add(node);
				
				return false;
			}
		});

		
		return _functions;
		
	}
	
	public static List<FunctionNode> Parse(ClassNode _newClass, ClassNode _oldClass) {

		List<FunctionNode> resultFunctions = new ArrayList<FunctionNode>();
		
		switch (_newClass.getChangeType()) {
		case ADD: {
			
			Map<String,List<MethodDeclaration>> methodsDec = ExtractFunctions(_newClass);
			
			for (Entry<String, List<MethodDeclaration>> _entry : methodsDec.entrySet()) {
				List<MethodDeclaration> _mdl = _entry.getValue();
				
				for (MethodDeclaration _md : _mdl){				
					FunctionNode _functionNode = new FunctionNode(); 
					_functionNode.name = _md.getName().toString();
					_functionNode.lineStart = _md.getStartPosition();
					_functionNode.lineEnd = _md.getStartPosition() + _md.getLength();
					_functionNode.changeType = ChangeType.ADD;
					
					resultFunctions.add(_functionNode);
				}
			}
		}
			break;

		case MODIFY: {
			// Extract functions from class in order to see if there is any
			// modification on it
			Map<String, List<MethodDeclaration>> oldFunctions = ExtractFunctions(_oldClass);
			Map<String, List<MethodDeclaration>> newFunctions = ExtractFunctions(_newClass);

			resultFunctions = FindFunctionsDiff(oldFunctions, newFunctions, _oldClass, _newClass);
		}
			break;

		default:
			break;

		}
		return resultFunctions;
	}
	
	
	private static List<FunctionNode> FindFunctionsDiff(Map<String,List<MethodDeclaration>> oldFunctions,
			Map<String,List<MethodDeclaration>> newFunctions, ClassNode _oldClass, ClassNode _newClass) {
		
		List<FunctionNode> _res = new ArrayList<FunctionNode>();
		
		// Compare each Function
		for (Entry<String,List<MethodDeclaration>> _entryN : newFunctions.entrySet()){
			
			List<MethodDeclaration> _mdNList = _entryN.getValue();
			
			for (MethodDeclaration _mdN : _mdNList){
			
				FunctionNode _functionNodeNew = new FunctionNode(); 
				_functionNodeNew.name = _mdN.getName().toString();
				_functionNodeNew.lineStart = _mdN.getStartPosition();
				_functionNodeNew.lineEnd = _mdN.getStartPosition() + _mdN.getLength();
			
				// Try to find if the function exists previously
				List<MethodDeclaration> _mdOList = oldFunctions.get(_entryN.getKey());
				
				if (_mdOList != null){
					// Get the function's body to see if they are different
					String _newFunctionBody = _newClass.getData().substring(
							_functionNodeNew.lineStart, _functionNodeNew.lineEnd);
					
					boolean changed = true;
			
					for (MethodDeclaration _mdO : _mdOList){
						String _oldFunctionBody = _oldClass.getData().substring(
								_mdO.getStartPosition(), _mdO.getStartPosition() + _mdO.getLength());
						
						if (_newFunctionBody.compareTo(_oldFunctionBody) == 0){
							changed = false;
							break;
						}
					}
					
					if (changed){
						_functionNodeNew.changeType = ChangeType.MODIFY;
						_res.add(_functionNodeNew);
					}
				} else {
					_functionNodeNew.changeType = ChangeType.ADD;
					_res.add(_functionNodeNew);
				}
			}
		}		
		
		return _res;
	}
	
	public void Debug(){
		System.out.println("------Name: " + name);
		System.out.println("--------LineStart: " + lineStart);
		System.out.println("--------LineEnd: " + lineEnd);
		System.out.println("--------ChangeType: " + changeType.toString());
	}
}
