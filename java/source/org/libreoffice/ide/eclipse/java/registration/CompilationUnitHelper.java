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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * This class will permit to parse Java source code with the help of AST.
 */


public final class CompilationUnitHelper {

    private IType mType = null;
    private CompilationUnit mUnit = null;
    private String mKey = null;
    private String mPattern = "Ljava/lang/Class<L%s;>;.getName()Ljava/lang/String;";

    public CompilationUnitHelper(IType type, ICompilationUnit unit) {
        mType = type;
        mUnit = getCompilationUnit(unit);
        mKey = String.format(mPattern, getTypeName().replace('.', '/'));
    }

    public CompilationUnit getCompilationUnit() {
        return mUnit;
    }

    public String getTypeName() {
        return mType.getFullyQualifiedName();
    }

    public ASTNode findDeclaringNode(String key) {
        ASTNode node = null;
        if (mKey.equals(key)) {
            node = getKeyLiteral();
        } else {
            node = mUnit.findDeclaringNode(key);
        }
        return node;
    };

    private CompilationUnit getCompilationUnit(ICompilationUnit unit) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setSource(unit);
        return (CompilationUnit) parser.createAST(null);
    }

    private StringLiteral getKeyLiteral() {
        StringLiteral literal = mUnit.getAST().newStringLiteral();
        literal.setLiteralValue(getTypeName());
        return literal;
    }
}
