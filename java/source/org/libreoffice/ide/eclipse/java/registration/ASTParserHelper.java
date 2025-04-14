/*************************************************************************
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * Sun Microsystems Inc., October, 2000
 *
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2000 by Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, USA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 *
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 *
 * Copyright: 2002 by Sun Microsystems, Inc.
 *
 * All Rights Reserved.
 *
 * Contributor(s): Cedric Bosdonnat
 *
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.java.registration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.libreoffice.ide.eclipse.core.PluginLogger;

/**
 * This class will permit to parse Java source code with the help of AST.
 */
public final class ASTParserHelper {


    /**
     * Get the java type of resource.
     *
     * @param unit the ICompilationUnit.
     * @return <code>type</code> if it contains the necessary methods for Java
     *      UNO service implementation registration or <code>null</code> if not.
     * @throws JavaModelException 
     */
    public static final IType getImplementationType(ICompilationUnit unit) throws JavaModelException {

        IType implementation = null;
        String[] parameters = {"QXComponentContext;"}; //$NON-NLS-1$
        int i = 0;
        IType[] types = unit.getAllTypes();
        while (i < types.length && implementation == null) {
            IType type = types[i];
            // Does this type has a constructor with XComponentContext has unique parameter
            IMethod method = type.getMethod(type.getElementName(), parameters);
            if (method.exists() && method.isConstructor()) {
                implementation = type;
            }
            i++;
        }
        return implementation;
    }

    /**
     * Get field declaration AST node.
     *
     * @param node the AST node.
     * @param field the field name.
     * @return the field declaration AST node.
     */
    public static final ASTNode getFieldDeclarationNode(ASTNode node, String field) {
        return NodeFinder.getFieldDeclarationNode(node, field);
    }

    /**
     * Get serviceNames value.
     *
     * @param unit the CompilationUnit.
     * @param field the field declaration having service names.
     * @return the service names value from the given field declaration.
     * @throws JavaModelException 
     */
    public static final String[] getServiceNames(CompilationUnitHelper unit, ASTNode field) {
        List<String> services = new ArrayList<>();
        parseNode(services, new ArrayList<>(), unit, field, null, null, -1);
        return services.toArray(new String[services.size()]);
    }

    private static void parseNode(List<String> services, List<Integer> indexes, CompilationUnitHelper unit,
                                  ASTNode node, ASTNode previous, ASTNode sibling, int index) {
        if (node != null) {
            parseNextNode(services, indexes, unit, node, previous, sibling, index);
        } else {
            logParserError(previous, "ASTParserHelper.ParseError"); //$NON-NLS-1$
        }
    }

    private static final void logParserError(ASTNode node, String template) {
        String name = ASTNode.nodeClassForType(node.getNodeType()).getSimpleName();
        PluginLogger.debug(String.format(Messages.getString(template), name, node.toString()));
    }

    private static void parseNextNode(List<String> services, List<Integer> indexes, CompilationUnitHelper unit,
                                      ASTNode node, ASTNode previous, ASTNode sibling, int index) {
        switch (node.getNodeType()) {
            // These nodes contain child nodes
            case ASTNode.FIELD_DECLARATION:
                parseFieldDeclaration(services, indexes, unit,
                                      (FieldDeclaration) node,
                                      previous, sibling, index);
                break;
            case ASTNode.VARIABLE_DECLARATION_STATEMENT:
                parseVariableDeclarationStatement(services, indexes, unit,
                                                  (VariableDeclarationStatement) node,
                                                  previous, sibling, index);
                break;
            case ASTNode.ARRAY_INITIALIZER:
                parseArrayInitializer(services, indexes, unit,
                                      (ArrayInitializer) node,
                                      previous, sibling, index);
                break;
            case ASTNode.BLOCK:
                parseBlock(services, indexes, unit,
                           (Block) node,
                           previous, sibling, index);
                break;
            // This node contain the assignment of array element
            case ASTNode.ASSIGNMENT:
                parseAssignment(services, indexes, unit,
                                (Assignment) node,
                                previous, sibling, index);
                break;
            // This node contain the array index of service name entry
            case ASTNode.NUMBER_LITERAL:
                parseNumberLiteral(services, indexes, unit,
                                   (NumberLiteral) node,
                                   previous, sibling, index);
                break;
            // These nodes require binding resolution
            case ASTNode.SIMPLE_NAME:
                parseSimpleName(services, indexes, unit,
                                (SimpleName) node,
                                previous, sibling, index);
                break;
            case ASTNode.METHOD_INVOCATION:
                parseMethodInvocation(services, indexes, unit,
                                      (MethodInvocation) node,
                                      previous, sibling, index);
                break;
            // These nodes contain only properties
            case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
                parseVariableDeclarationFragment(services, indexes, unit,
                                                 (VariableDeclarationFragment) node,
                                                 previous, sibling, index);
                break;
            case ASTNode.ARRAY_CREATION:
                parseArrayCreation(services, indexes, unit,
                                   (ArrayCreation) node,
                                   previous, sibling, index);
                break;
            case ASTNode.ARRAY_ACCESS:
                parseArrayAccess(services, indexes, unit,
                                 (ArrayAccess) node,
                                 previous, sibling, index);
                break;
            case ASTNode.EXPRESSION_STATEMENT:
                parseExpressionStatement(services, indexes, unit,
                                         (ExpressionStatement) node,
                                         previous, sibling, index);
                break;
            case ASTNode.METHOD_DECLARATION:
                parseMethoddDeclaration(services, indexes, unit,
                                        (MethodDeclaration) node,
                                        previous, sibling, index);
                break;
            case ASTNode.RETURN_STATEMENT:
                parseReturnStatement(services, indexes, unit,
                                     (ReturnStatement) node,
                                     previous, sibling, index);
                break;
            // This node contain the service name as String literal
            case ASTNode.STRING_LITERAL:
                parseStringLiteral(services, indexes,
                                   (StringLiteral) node,
                                   index);
                break;
            // For unsupported nodes we log
            default:
                logParserError(node, "ASTParserHelper.UnsupportedNode"); //$NON-NLS-1$
        }
    }

    private static final void parseFieldDeclaration(List<String> services, List<Integer> indexes,
                                                    CompilationUnitHelper unit,
                                                    FieldDeclaration node,
                                                    ASTNode previous, ASTNode sibling, int index) {
        Iterator<?> fragments = node.fragments().iterator();
        while (fragments.hasNext()) {
            parseNode(services, indexes, unit, (VariableDeclarationFragment) fragments.next(), node, sibling, index);
        }
    }

    private static final void parseVariableDeclarationStatement(List<String> services, List<Integer> indexes,
                                                                CompilationUnitHelper unit,
                                                                VariableDeclarationStatement node,
                                                                ASTNode previous, ASTNode sibling, int index) {
        Iterator<?> fragments = node.fragments().iterator();
        while (fragments.hasNext()) {
            parseNode(services, indexes, unit, (VariableDeclarationFragment) fragments.next(), node, sibling, index);
        }
    }

    private static final void parseArrayInitializer(List<String> services, List<Integer> indexes,
                                                    CompilationUnitHelper unit,
                                                    ArrayInitializer node,
                                                    ASTNode previous, ASTNode sibling, int index) {
        Iterator<?> expressions = node.expressions().iterator();
        while (expressions.hasNext()) {
            parseNode(services, indexes, unit, (Expression) expressions.next(), node, sibling, index);
        }
    }

    private static final void parseBlock(List<String> services, List<Integer> indexes,
                                         CompilationUnitHelper unit,
                                         Block node,
                                         ASTNode previous, ASTNode sibling, int index) {
        Iterator<?> statements = node.statements().iterator();
        while (statements.hasNext()) {
            parseNode(services, indexes, unit, (Statement) statements.next(), node, sibling, index);
        }
    }

    private static final void parseAssignment(List<String> services, List<Integer> indexes,
                                              CompilationUnitHelper unit,
                                              Assignment node,
                                              ASTNode previous, ASTNode sibling, int index) {
        parseNode(services, indexes, unit, node.getLeftHandSide(), node, node.getRightHandSide(), index);
    }

    private static final void parseNumberLiteral(List<String> services, List<Integer> indexes,
                                                 CompilationUnitHelper unit,
                                                 NumberLiteral node,
                                                 ASTNode previous, ASTNode sibling, int index) {
        Object position = node.resolveConstantExpressionValue();
        if (position != null) {
            parseNode(services, indexes, unit, sibling, node, null, (int) position);
        } else {
            parseNode(services, indexes, unit, null, node, sibling, index);
        }
    }

    private static final void parseSimpleName(List<String> services, List<Integer> indexes,
                                              CompilationUnitHelper unit,
                                              SimpleName node,
                                              ASTNode previous, ASTNode sibling, int index) {
        IBinding binding = node.resolveBinding();
        if (binding != null) {
            parseNode(services, indexes, unit, unit.findDeclaringNode(binding.getKey()), node, sibling, index);
        } else {
            parseNode(services, indexes, unit, null, node, sibling, index);
        }
    }

    private static final void parseMethodInvocation(List<String> services, List<Integer> indexes,
                                                    CompilationUnitHelper unit,
                                                    MethodInvocation node,
                                                    ASTNode previous, ASTNode sibling, int index) {
        IMethodBinding binding = node.resolveMethodBinding();
        if (binding != null) {
            parseNode(services, indexes, unit, unit.findDeclaringNode(binding.getKey()), node, sibling, index);
        } else {
            parseNode(services, indexes, unit, null, node, sibling, index);
        }
    }

    private static final void parseVariableDeclarationFragment(List<String> services, List<Integer> indexes,
                                                               CompilationUnitHelper unit,
                                                               VariableDeclarationFragment node,
                                                               ASTNode previous, ASTNode sibling, int index) {
        parseNode(services, indexes, unit, node.getInitializer(), node, sibling, index);
    }

    private static final void parseArrayCreation(List<String> services, List<Integer> indexes,
                                                 CompilationUnitHelper unit,
                                                 ArrayCreation node, 
                                                 ASTNode previous, ASTNode sibling, int index) {
        ArrayInitializer initializer = node.getInitializer();
        // Array dimension is not required. We only care about initializer.
        if (initializer != null) {
            parseNode(services, indexes, unit, initializer, node, sibling, index);
        }
    }

    private static final void parseArrayAccess(List<String> services, List<Integer> indexes,
                                               CompilationUnitHelper unit,
                                               ArrayAccess node,
                                               ASTNode previous, ASTNode sibling, int index) {
        parseNode(services, indexes, unit, node.getIndex(), node, sibling, index);
    }

    private static final void parseExpressionStatement(List<String> services, List<Integer> indexes,
                                                       CompilationUnitHelper unit,
                                                       ExpressionStatement node,
                                                       ASTNode previous, ASTNode sibling, int index) {
        parseNode(services, indexes, unit, node.getExpression(), node, sibling, index);
    }

    private static final void parseMethoddDeclaration(List<String> services, List<Integer> indexes,
                                                      CompilationUnitHelper unit,
                                                      MethodDeclaration node,
                                                      ASTNode previous, ASTNode sibling, int index) {
        parseNode(services, indexes, unit, node.getBody(), node, sibling, index);
    }

    private static final void parseReturnStatement(List<String> services, List<Integer> indexes,
                                                   CompilationUnitHelper unit,
                                                   ReturnStatement node,
                                                   ASTNode previous, ASTNode sibling, int index) {
        parseNode(services, indexes, unit, node.getExpression(), node, sibling, index);
    }

    private static final void parseStringLiteral(List<String> services, List<Integer> indexes,
                                                 StringLiteral literal, int index) {
        int position = indexes.size();
        for (int i = 0; i < indexes.size(); i++) {
            if (indexes.get(i) > index && position > i) {
                position = i;
            }
        }
        indexes.add(position, index);
        services.add(position, literal.getLiteralValue());
    }

    private static final class NodeFinder extends ASTVisitor {
        private ASTNode mNode = null;
        private String mName;

        private NodeFinder(String name) {
            mName = name;
        }

        public static ASTNode getFieldDeclarationNode(ASTNode node, String name) {
            NodeFinder finder = new NodeFinder(name);
            node.accept(finder);
            return finder.mNode;
        }

        @Override
        public boolean visit (final VariableDeclarationFragment node) {
            boolean visit = mNode == null;
            if (visit && mName.equals(node.getName().getIdentifier())) {
                ASTNode parent = node.getParent();
                if (parent.getNodeType() == ASTNode.FIELD_DECLARATION) {
                    FieldDeclaration field = (FieldDeclaration) parent;
                    visit = !isServiceNamesField(field);
                }
            }
            return visit;
        }

        private boolean isServiceNamesField(FieldDeclaration field) {
            return isStaticFinal(field.getModifiers()) &&
                   field.getType().isArrayType() &&
                   isArrayElementString(field);
        }

        private boolean isStaticFinal(int modifiers) {
            return (modifiers & Modifier.STATIC) == Modifier.STATIC &&
                   (modifiers & Modifier.FINAL) == Modifier.FINAL; 
        }

        private boolean isArrayElementString(FieldDeclaration field) {
            boolean isString = false;
            ArrayType arrayType = (ArrayType) field.getType();
            Type elementType = arrayType.getElementType();
            if (elementType.isSimpleType()) {
                SimpleType simpleType = (SimpleType) elementType;
                if (simpleType.getName().getFullyQualifiedName().equals("String")) {
                    mNode = field;
                    isString = true;
                }
            }
            return isString;
        }
    }

}
