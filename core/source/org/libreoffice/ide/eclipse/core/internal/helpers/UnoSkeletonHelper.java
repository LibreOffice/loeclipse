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
package org.libreoffice.ide.eclipse.core.internal.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;

/**
 * This class is an helper for modifying the skeleton produced by uno-skeletonmaker
 * when it does not support passive registration.
 *
 */
public final class UnoSkeletonHelper {

    public static boolean supportPassiveRegistration(IUnoidlProject prj, String command, IProgressMonitor monitor) {
        String version = "0.4"; //$NON-NLS-1$
        Process process = prj.getSdk().runTool(prj, command + " --version", monitor); //$NON-NLS-1$
        // To get uno-skeletonmaker version we need to process the error stream
        try {
            String error = readErrorStream(process);
            version = error.substring(error.lastIndexOf(' ') + 1);
        } catch (IOException e) { }
        return version.compareTo("0.5") > -1; //$NON-NLS-1$
    }

    public static String readErrorStream(Process process) throws IOException {
        String error = null;
        try (InputStream input = process.getErrorStream();
                 Scanner scanner = new Scanner(input).useDelimiter("\\A")) { //$NON-NLS-1$
            if (scanner.hasNext()) {
                error = scanner.next().trim();
            }
        }
        return error;
    }

    public static void cleanupJavaSkeleton(IFile file, String serviceName, IProgressMonitor monitor) {
        try {
            Document document = removeUnneededCode(file, monitor);
            // Save the document
            Files.writeString(file.getLocation().toPath(), document.get());
        } catch (MalformedTreeException | BadLocationException |
                 IOException | JavaModelException | IllegalArgumentException e) {
            PluginLogger.error("UnoSkeletonHelper.cleanupJavaSkeleton ERROR", e); //$NON-NLS-1$
        }
    }

    private static Document removeUnneededCode(IFile file, IProgressMonitor monitor)
        throws MalformedTreeException, BadLocationException,
               JavaModelException, IllegalArgumentException {
        ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(file);
        Document document = new Document(compilationUnit.getSource());
        CompilationUnit unit = getCompilationUnit(compilationUnit, monitor);
        ASTRewrite rewriter = ASTRewrite.create(unit.getAST());
        // Remove all import except XComponentContext and WeakBase
        for (ImportDeclaration imp : ImportDeclarationFinder.perform(unit)) {
            rewriter.remove(imp, null);
        }
        // Remove all methods starting with "__"
        for (MethodDeclaration method : MethodDeclarationFinder.perform(unit)) {
            rewriter.remove(method, null);
        }
        // Apply changes
        TextEdit edits = rewriter.rewriteAST();
        edits.apply(document);
        return document;
    }

    private static CompilationUnit getCompilationUnit(ICompilationUnit unit, IProgressMonitor monitor) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setSource(unit);
        return (CompilationUnit) parser.createAST(monitor);
    }

    private static final class MethodDeclarationFinder extends ASTVisitor {
        private final List <MethodDeclaration> mMethods = new ArrayList <> ();

        public static List<MethodDeclaration> perform(ASTNode node) {
            MethodDeclarationFinder finder = new MethodDeclarationFinder();
            node.accept(finder);
            return finder.getMethods();
        }

        @Override
        public boolean visit (final MethodDeclaration method) {
            if (method.getName().getIdentifier().startsWith("__")) {
                mMethods.add (method);
            }
            return super.visit(method);
        }

        /**
         * @return an immutable list view of the methods discovered by this visitor
         */
        public List <MethodDeclaration> getMethods() {
            return Collections.unmodifiableList(mMethods);
        }
    }

    private static final class ImportDeclarationFinder extends ASTVisitor {
        private final List <ImportDeclaration> mImports = new ArrayList <> ();

        public static List<ImportDeclaration> perform(ASTNode node) {
            ImportDeclarationFinder finder = new ImportDeclarationFinder();
            node.accept(finder);
            return finder.getImports();
        }

        @Override
        public boolean visit (final ImportDeclaration imp) {
            String fullName = imp.getName().getFullyQualifiedName();
            if (!(fullName.endsWith("XComponentContext") || fullName.endsWith("WeakBase"))) {
                mImports.add (imp);
            }
            return super.visit(imp);
        }

        /**
         * @return an immutable list view of the imports discovered by this visitor
         */
        public List <ImportDeclaration> getImports() {
            return Collections.unmodifiableList(mImports);
        }
    }

}
