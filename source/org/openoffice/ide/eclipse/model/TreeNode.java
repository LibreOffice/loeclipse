/*************************************************************************
 *
 * $RCSfile: TreeNode.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/10 12:07:20 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the following licenses
 *
 *     - GNU Lesser General Public License Version 2.1
 *     - Sun Industry Standards Source License Version 1.1
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
 *
 * Sun Industry Standards Source License Version 1.1
 * =================================================
 * The contents of this file are subject to the Sun Industry Standards
 * Source License Version 1.1 (the "License"); You may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.openoffice.org/license.html.
 *
 * Software provided under this License is provided on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 * MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 * See the License for the specific provisions governing your rights and
 * obligations concerning the Software.
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
package org.openoffice.ide.eclipse.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

public abstract class TreeNode implements  IVisitable, INodeListener {

	public TreeNode(TreeNode parent, String aPath) {
		setPath(aPath);
		setParent(parent);
	}
	
	//------------------------------------------------------- Members managment
	
	private String path;
	
	/**
	 * Returns the full path of the node in the tree. 
	 * 
	 * @return Full path of the node
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the name of the node. Subclasses have to implement this method.
	 * 
	 * @return name of the node
	 */
	public abstract String getName();
	
	private void setPath(String path) {
		this.path = path;
	}
	
	//-------------------------------------------------------- Parent managment
	
	private TreeNode parent;
	
	private void setParent(TreeNode node){
		parent = node;
	}
	
	/**
	 * Returns the tree node parent of the node
	 * 
	 * @return parent of the node
	 */
	public TreeNode getParent(){
		return parent;
	}

	//------------------------------------------------------ Children managment
	
	private HashMap nodes = new HashMap();
	
	/**
	 * Use this method for quick access to the children count, even if you can 
	 * get the children count by using the <code>getChildren()</code> method.
	 * 
	 * @return number of children 
	 */
	public int getNodeCount(){
		return nodes.size();
	}
	
	/**
	 * Returns the children in non guaranteed order. To get the children in 
	 * order, use <code>getSortedChildren()</code>.
	 * 
	 * @return children of this node.
	 */
	public Vector getNodes(){
		return new Vector(nodes.values());
	}
	
	/**
	 * Returns the sorted children of this node. They are sorted using a 
	 * SortedMap, but subclasses may reimplement it.
	 *   
	 * @return the sorted children
	 */
	public Vector getSortedNodes(){
		TreeMap map = new TreeMap(nodes);
		return new Vector(map.values());
	}
	
	/**
	 * Returns whether the given node is a child of this one.
	 * 
	 * @param node node to check
	 * @return <code>true</code> if the node is a child of this one,
	 *         <code>false</code> otherwise.
	 */
	public boolean containsNode(TreeNode node) {
		boolean contained = false;
		
		if (nodes.containsValue(node)){
			contained = true;
		}
		return contained;
	}
	
	public void addNode(TreeNode node){
		
		if (!nodes.containsKey(node.getPath())){
			nodes.put(node.getPath(), node);
			fireNodeAdded(node);
		}
	}
	
	public void removeNode(TreeNode node){
		
		nodes.remove(node.getPath());
		fireNodeRemoved(node);
	}
	
	public void removeAllNodes(){
		
		Set keys = ((HashMap)nodes.clone()).keySet();
		Iterator iter = keys.iterator();
		
		while (iter.hasNext()){
			String key = (String)iter.next();
			TreeNode node = (TreeNode)nodes.get(key);
			nodes.remove(node);
			fireNodeRemoved(node);
		}
	}
	
	//------------------------------------ Visitor design pattern implementation 
	
	/**
	 * Accepts or refuse a tree visitor.
	 * 
	 * @param visitor visitor to recieve or reject
	 * @param node that asked for the visits
	 */
	public void accepts(ITreeVisitor visitor) {
		boolean visitChildren = visitor.visits(this);
		
		if (visitChildren){
			Vector children = getNodes();
			for (int i=0, length=getNodeCount(); i<length; i++) {
				IVisitable visitable = (IVisitable)children.get(i);
				visitable.accepts(visitor);
			}
		}
	}
	
	public String toString(TreeNode callingNode){
		
		String output = computeBeforeString(callingNode);
		
		Set keys = nodes.keySet();
		Iterator iter = keys.iterator();
		
		while (iter.hasNext()){
			String key = (String)iter.next();
			TreeNode node = (TreeNode)nodes.get(key);
			output = output + node.toString(callingNode);
		}
		
		return output + computeAfterString(callingNode);
	}
	
	public String computeBeforeString(TreeNode callingNode){
		return "";
	}
	
	public String computeAfterString(TreeNode callingNode){
		return "";
	}
	
	//-------------------------------- Implementation of the children listening
	
	public void nodeAdded(TreeNode node) {
		
		fireNodeAdded(node);
	}
	
	public void nodeRemoved(TreeNode node) {
		
		fireNodeRemoved(node);
	}
	
	public void nodeChanged(TreeNode node) {
		
		fireNodeChanged(node);
	}
	
	//-------------------------------------- Implementation of the notification
	
	private Vector listeners = new Vector();
	
	public void addNodeListener(INodeListener listener){
		listeners.add(listener);
	}
	
	public void removeNodeListener(INodeListener listener){
		listeners.remove(listener);
	}
	
	public void fireNodeAdded(TreeNode node){
		for (int i=0, length=listeners.size(); i<length; i++){
			((INodeListener)listeners.get(i)).nodeAdded(node);
		}
	}
	
	public void fireNodeRemoved(TreeNode node){
		for (int i=0, length=listeners.size(); i<length; i++){
			((INodeListener)listeners.get(i)).nodeRemoved(node);
		}
	}
	
	public void fireNodeChanged(TreeNode node){
		for (int i=0, length=listeners.size(); i<length; i++){
			((INodeListener)listeners.get(i)).nodeChanged(node);
		}
	}
}