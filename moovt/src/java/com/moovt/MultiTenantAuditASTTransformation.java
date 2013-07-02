package com.moovt;

import static org.springframework.asm.Opcodes.ACC_PUBLIC;
import static org.springframework.asm.Opcodes.ACC_STATIC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * This class performs the AST Transformation that adds a tenantId, createdBy, lastUpdatedBy, lastUpdated, dateCreated and CRUD Message to
 * all domain classes.
 * 
 * @author egoncalves
 *
 */

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class MultiTenantAuditASTTransformation implements ASTTransformation {

	public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {

		String tenantIdField = "tenantId";
		String createdByField = "createdBy";
		String lastUpdatedByField = "lastUpdatedBy";
		String lastUpdatedField = "lastUpdated";
		String dateCreatedField = "dateCreated";
		String CRUDMessageField = "CRUDMessage";

		// static transients = [ "CRUDMessage" ];
		// Call the setAttributes setAuditAttributes(this);

		for (ASTNode astNode : astNodes) {
			if (astNode instanceof ClassNode) {
				ClassNode classNode = (ClassNode) astNode;
				classNode.addProperty(tenantIdField, Modifier.PUBLIC,
						new ClassNode(Long.class), null,
						null, null);
				classNode.addProperty(createdByField, Modifier.PUBLIC,
						new ClassNode(Long.class), null,
						null, null);
				classNode.addProperty(lastUpdatedByField, Modifier.PUBLIC,
						new ClassNode(Long.class), null,
						null, null);
				classNode.addProperty(dateCreatedField, Modifier.PUBLIC,
						new ClassNode(java.util.Date.class), null, null, null);
				classNode.addProperty(lastUpdatedField, Modifier.PUBLIC,
						new ClassNode(java.util.Date.class), null, null, null);
				classNode.addProperty(CRUDMessageField, Modifier.PUBLIC,
						new ClassNode(java.lang.String.class), null, null, null);

				addNullableConstraint(classNode,"tenantId");
				addNullableConstraint(classNode,"createdBy");
				addNullableConstraint(classNode,"lastUpdatedBy");
				addNullableConstraint(classNode,"lastUpdated");
				addNullableConstraint(classNode,"dateCreated");
				
				//Make CRUD Message Transient
				
				// If there is already a transients field, capture the pre existing Expression and remove the transients field
				FieldNode transients = classNode.getDeclaredField("transients");
				ListExpression list = new ListExpression();
				
				if (transients != null) {
					list = (ListExpression) transients.getInitialExpression();
					classNode.removeField("transients");
				}
				
				//Create a new transients node and add it to the class
				
				list.addExpression(new ConstantExpression("CRUDMessage"));
				transients = new FieldNode("transients", ACC_PUBLIC
							| ACC_STATIC, new ClassNode(Object.class),
							classNode, list);
				transients.setDeclaringClass(classNode);
				classNode.addField(transients);
		
				
				//Add a method
				
			}

		}
	}

	// public void addTableAndIdMapping(ClassNode classNode){
	// FieldNode closure = classNode.getDeclaredField("mapping");
	//
	// if(closure!=null){
	// boolean hasTable=hasFieldInClosure(closure,"table");
	// boolean hasId=hasFieldInClosure(closure,"id");
	//
	// ClosureExpression exp =
	// (ClosureExpression)closure.getInitialExpression();
	// BlockStatement block = (BlockStatement) exp.getCode();
	//
	// //this just adds an s to the class name for the table if its not
	// specified
	// Boolean pluralize = (Boolean)getMap(CO,"stamp.mapping.pluralTable");
	// if(!hasTable && pluralize!=null && pluralize){
	// String tablename =
	// GrailsClassUtils.getShortName(classNode.getName())+"s";
	// //LOG.info("Added new mapping to assign table: " + tablename);
	// MethodCallExpression tableMeth = new MethodCallExpression(
	// VariableExpression.THIS_EXPRESSION,
	// new ConstantExpression("table"),
	// new ArgumentListExpression(new ConstantExpression(tablename))
	// );
	// //block = (BlockStatement) exp.getCode();
	// block.addStatement(new ExpressionStatement(tableMeth));
	// //System.out.println(classNode.getName()+" - Added table mapping " +
	// tablename );
	// }
	// //This adds the ID generator that we use for domian classes
	// Map tableconf = (Map)getMap(CO,"stamp.mapping.id");
	// if(!hasId && tableconf!=null){
	// NamedArgumentListExpression namedarg = new NamedArgumentListExpression();
	// if(tableconf.get("column") != null){
	// namedarg.addMapEntryExpression(new ConstantExpression("column"), new
	// ConstantExpression(tableconf.get("column").toString()));
	// }
	// if(tableconf.get("generator") != null){
	// namedarg.addMapEntryExpression(new ConstantExpression("generator"), new
	// ConstantExpression(tableconf.get("generator").toString()));
	// }
	// MethodCallExpression tableMeth = new MethodCallExpression(
	// VariableExpression.THIS_EXPRESSION,
	// new ConstantExpression("id"),
	// namedarg
	// );
	// //block = (BlockStatement) exp.getCode();
	// block.addStatement(new ExpressionStatement(tableMeth));
	// //System.out.println(classNode.getName() + " - Added ID mapping with "+
	// tableconf);
	// }
	// }
	// //System.out.println(block.toString());
	// }
	
	 public void addNullableConstraint(ClassNode classNode,String fieldName){
	 FieldNode closure = classNode.getDeclaredField("constraints");
	
	 if(closure!=null){
		 
	 ClosureExpression exp =
	 (ClosureExpression)closure.getInitialExpression();
	 BlockStatement block = (BlockStatement) exp.getCode();
	
	 if(!hasFieldInClosure(closure,fieldName)){
	 NamedArgumentListExpression namedarg = new NamedArgumentListExpression();
	 namedarg.addMapEntryExpression(new ConstantExpression("nullable"), new
	 ConstantExpression(true));
	 MethodCallExpression constExpr = new MethodCallExpression(
	 VariableExpression.THIS_EXPRESSION,
	 new ConstantExpression(fieldName),
	 namedarg
	 );
	 block.addStatement(new ExpressionStatement(constExpr));
	 System.out.println(classNode.getName() +
	 " - Added nullabel constraint for "+ fieldName);
	 }
	 }
	 //System.out.println(block.toString());
	 }

	
	 public boolean hasFieldInClosure(FieldNode closure, String fieldName){
	 if(closure != null){
	 ClosureExpression exp = (ClosureExpression)
	 closure.getInitialExpression();
	 BlockStatement block = (BlockStatement) exp.getCode();
	 List<Statement> ments = block.getStatements();
	 for(Statement expstat : ments){
	 if(expstat instanceof ExpressionStatement &&
	 ((ExpressionStatement)expstat).getExpression() instanceof
	 MethodCallExpression){
	 MethodCallExpression methexp =
	 (MethodCallExpression)((ExpressionStatement)expstat).getExpression();
	 ConstantExpression conexp = (ConstantExpression)methexp.getMethod();
	 if(conexp.getValue().equals(fieldName)){
	 return true;
	 }
	 }
	 }
	 }
	 return false;
	 }

	static public String getContents(File aFile) {
		// ...checks on aFile are elided
		StringBuilder contents = new StringBuilder();

		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			BufferedReader input = new BufferedReader(new FileReader(aFile));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		System.out.println("*******" + contents.toString());
		return contents.toString();
	}
	//
	// static public Object getMap(Map configMap, String keypath) {
	// String keys[] = keypath.split("\\.");
	// Map map = configMap;
	// for(String key : keys){
	// Object val = map.get(key);
	// if(val !=null){
	// //System.out.println("got a key for are " +key);
	// if(val instanceof Map){
	// map = (Map)map.get(key);
	// } else{
	// return val;
	// }
	// }else{
	// return null;
	// }
	// }
	// return map;
	// }

}

// FUTURE
/**
 * java.math.BigDecimal java.lang.Integer java.lang.Long java.util.Date
 * java.lang.String java.lang.Boolean
 */

/**
 * since grails has everything default to nullable:false, we change that to
 * nullable:true here since omost of the time we condider it ok explicity set
 * nullable:false as the exception
 * 
 * public void addConstraintDefaults(ClassNode classNode){ List<FieldNode>
 * fnlist = classNode.getFields(); for(FieldNode fnode : fnlist){
 * if(!fnode.isStatic()){ //check if the type is in our list
 * System.out.println("*" + fnode.getName() + " - " +
 * fnode.getType().getName()); } }
 * 
 * boolean hasConstraint=false;
 * 
 * }
 **/

/*
 * org.codehaus.groovy.ast.stmt.BlockStatement@f4b2da[
 * org.codehaus.groovy.ast.stmt.ExpressionStatement@a0a4a[
 * expression:org.codehaus.groovy.ast.expr.MethodCallExpression@29aa5a[ object:
 * org.codehaus.groovy.ast.expr.VariableExpression@6f0383[variable: this]
 * method: ConstantExpression[discDate] arguments:
 * org.codehaus.groovy.ast.expr.NamedArgumentListExpression@4fb195[
 * org.codehaus.groovy.ast.expr.MapEntryExpression@13becc(key:
 * ConstantExpression[nullable], value: ConstantExpression[true]) ] ] ],.....
 * 
 * /* { org.codehaus.groovy.ast.stmt.BlockStatement@f0bc0[
 * org.codehaus.groovy.ast.stmt.ExpressionStatement@cc9e15[
 * expression:org.codehaus.groovy.ast.expr.MethodCallExpression@9e94e8[ object:
 * org.codehaus.groovy.ast.expr.VariableExpression@3c2282[variable: this]
 * method: ConstantExpression[table] arguments:
 * org.codehaus.groovy.ast.expr.ArgumentListExpression
 * 
 * @42428a[ConstantExpression[SyncSteps]] ] ],
 * org.codehaus.groovy.ast.stmt.ExpressionStatement@1eafb4[
 * expression:org.codehaus.groovy.ast.expr.MethodCallExpression@a17663[ object:
 * org.codehaus.groovy.ast.expr.VariableExpression@3c2282[variable: this]
 * method: ConstantExpression[id] arguments:
 * org.codehaus.groovy.ast.expr.NamedArgumentListExpression@636202[
 * org.codehaus.groovy.ast.expr.MapEntryExpression@b781ea( key:
 * ConstantExpression[column], value: ConstantExpression[OID] ),
 * org.codehaus.groovy.ast.expr.MapEntryExpression@b25934( key:
 * ConstantExpression[generator], value:
 * ConstantExpression[nineci.hibernate.NewObjectIdGenerator] ) ] ] ],
 * org.codehaus.groovy.ast.stmt.ExpressionStatement@fe6f06[
 * expression:org.codehaus.groovy.ast.expr.MethodCallExpression@2b0459[ object:
 * org.codehaus.groovy.ast.expr.VariableExpression@3c2282[variable: this]
 * method: ConstantExpression[syncBatch] arguments:
 * org.codehaus.groovy.ast.expr.NamedArgumentListExpression@2a938f[
 * org.codehaus.groovy.ast.expr.MapEntryExpression@3dbf04(key:
 * ConstantExpression[column], value: ConstantExpression[SyncBatchId])]]]] }
 */