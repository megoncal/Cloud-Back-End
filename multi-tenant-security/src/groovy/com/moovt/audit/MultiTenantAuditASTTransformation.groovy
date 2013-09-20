package com.moovt.audit;

import static org.springframework.asm.Opcodes.ACC_PUBLIC;
import static org.springframework.asm.Opcodes.ACC_STATIC;

import com.moovt.audit.DomainHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier; 
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
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

// @GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
@GroovyASTTransformation(phase=CompilePhase.SEMANTIC_ANALYSIS)
public class MultiTenantAuditASTTransformation implements ASTTransformation {

	public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {

		String tenantIdField = "tenantId";
		String createdByField = "createdBy";
		String lastUpdatedByField = "lastUpdatedBy";
		String lastUpdatedField = "lastUpdated";
		String dateCreatedField = "dateCreated";
		String CRUDMessageField = "CRUDMessage";

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

				System.out.println("....added audit fields to class " + classNode.getName());
				

				//List<ASTNode> nodes = new AstBuilder().buildFromCode { "Hello" }
				
				//Prepare the statement
				StaticMethodCallExpression mce = new StaticMethodCallExpression(new ClassNode(DomainHelper.class),
						                                            "setAuditAttributes",
						                                            new ArgumentListExpression(
						                                                    new VariableExpression("this")));
				
				Statement stmt = new ExpressionStatement (mce);

				//Check for the existence of the methods
				boolean hasBeforeInsertMethod = classNode.hasMethod("beforeUpdate", [] as Parameter[]);

				boolean hasBeforeUpdateMethod = classNode.hasMethod("beforeUpdate", [] as Parameter[]);
				
				if (hasBeforeInsertMethod) {
					//Get the contents of the method
					MethodNode beforeInsertMethod = classNode.getMethod("beforeInsert", [] as Parameter[]);
					BlockStatement code = (BlockStatement) beforeInsertMethod.getCode();
					code.addStatement(stmt);
					beforeInsertMethod.setCode(code);
					System.out.println("....modified beforeInsert method in " + classNode.getName());
				} else {
					MethodNode newBeforeInsertMethod = new MethodNode("beforeInsert", Modifier.PUBLIC, ClassHelper.OBJECT_TYPE, [] as Parameter[] , [] as  ClassNode[], stmt);
					classNode.addMethod(newBeforeInsertMethod);
					System.out.println("....added beforeInsert method in " + classNode.getName());
				}
				
				if (hasBeforeUpdateMethod) {
					//Get the contents of the method
					MethodNode beforeUpdateMethod = classNode.getMethod("beforeUpdate", [] as Parameter[]);
					BlockStatement code = (BlockStatement) beforeUpdateMethod.getCode();
					code.addStatement(stmt);
					beforeUpdateMethod.setCode(code);
					System.out.println("....modified beforeUpdate method in " + classNode.getName());
				} else {
					MethodNode newBeforeUpdateMethod = new MethodNode("beforeUpdate", Modifier.PUBLIC, ClassHelper.OBJECT_TYPE, [] as Parameter[] , [] as  ClassNode[], stmt);
					classNode.addMethod(newBeforeUpdateMethod);
					System.out.println("....added beforeUpdate method in " + classNode.getName());
				}
								
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

			}

		}
	}


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
			}
		}
	}
 

	public boolean hasFieldInClosure(FieldNode closure, String fieldName){
		if(closure != null){
			ClosureExpression exp = closure.getInitialExpression();
			BlockStatement block = exp.getCode();
			List<Statement> ments = block.getStatements();
			for(Statement expstat : ments){
				if(expstat instanceof ExpressionStatement && expstat.getExpression() instanceof MethodCallExpression){
					MethodCallExpression methexp = expstat.getExpression();
					ConstantExpression conexp = methexp.getMethod();
					if(conexp.getValue() == fieldName){
						return true;
					}
				}
			}
		}
		return false;
	}

}