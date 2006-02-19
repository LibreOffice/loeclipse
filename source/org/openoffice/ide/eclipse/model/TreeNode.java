/*************************************************************************
 *
 * $RCSfile: TreeNode.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/02/19 11:32:41 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
package org.openoffice.ide.eclipse.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;

public class TreeNode implements  IVisitable, INodeListener {

	public TreeNode(TreeNode parent, String aName) {
		setName(aName);
		setParent(parent);
	}
	
	public void dispose() {
		
		// Cloning the map to get the entries avoid concurrent modifications
		Iterator iter = ((HashMap)nodes.clone()).entrySet().iterator();
		
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			TreeNode child = (TreeNode)entry.getValue();
			
			child.dispose();
		}
		
		getParent().removeNode(this);
		parent = null;
		nodes = null;
		listeners.removeAllElements();
		listeners = null;
		root = null;
	}
	
	//------------------------------------------------------- Members managment
	
	private String name;
	
	/**
	 * Calculate the path of the node. 
	 * 
	 * @return Full path of the node
	 */
	public String getPath() {
		
		String path = getName();
		if (null != getParent() && null != getParent().getPath()){
			path = getParent().getPath() + getParent().getSeparator() + path;
		}
		
		return path;
	}

	/**
	 * Subclasses should override this method
	 * 
	 * @param aName new name of the node. It does not move the node.
	 */
	public void setName(String aName){
		name = aName;
	}
	
	/**
	 * Returns the name of the node.
	 * 
	 * @return name of the node
	 */
	public String getName(){
		return name;
	}
	
	//-------------------------------------------------------- Parent managment
	
	private TreeNode parent;
	private ModelTree root;
	
	public void setParent(TreeNode node){
		parent = node;
		
		if (null != parent){
			root = parent.getTreeRoot();
		}
	}
	
	/**
	 * Returns the tree node parent of the node
	 * 
	 * @return parent of the node
	 */
	public TreeNode getParent(){
		return parent;
	}

	public ModelTree getTreeRoot(){
		return root;
	}
	
	//------------------------------------------------------ Children managment
	
	private HashMap nodes = new HashMap();
	
	public String getSeparator(){
		return ".";
	}
	
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
	 * Returns whether the node is valid or not. This method should be
	 * implemented by subclasses. This implementation returns allways
	 * <code>true</code>.
	 * 
	 * @param node node to check
	 * @return <code>true</code> if the node is valid, <code>false</code>
	 *         otherwise.
	 */
	protected boolean isValidNode(TreeNode node){
		return true;
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
	
	/**
	 * TODO test
	 * 
	 * Try to find to node with the given path in this part of the tree. If
	 * the node can't be found, <code>null</code> is returned.
	 * 
	 * @param path path to the searched node
	 * @return existing node or <code>null</code> if not found.
	 */
	public TreeNode findNode(String path){
		TreeNode result = null;
		
		if (null != path) {
			String regex = getPath() + getSeparator() + "(.*)";
			Matcher nodeMatcher = Pattern.compile(regex).matcher(path);
			
			if (nodeMatcher.matches()){
				Iterator iter = nodes.entrySet().iterator();
				String suffix = nodeMatcher.group(1);
				
				while (null == result && iter.hasNext()){
					Map.Entry entryi = (Map.Entry)iter.next();
					TreeNode childi = (TreeNode)entryi.getValue();
					
					if (suffix.startsWith(childi.getName() + childi.getSeparator())) {
						// The right way has been found
						result = childi.findNode(path);
						
					} else if (suffix.equals(childi.getName())) {
						// The node is found
						result = childi;
					}
				}
			}
		}
		return result;
	}
	
	public void move(TreeNode newParent, String newName) throws TreeException {
		
		if (null != newParent) {
			
			// Parent found, change the node references
			getParent().removeNode(this);
			
			// Sets the new node name and parent before changing the parent
			setParent(newParent);
			setName(newName);
			
			newParent.addNode(this);
			
		} else {
			String path = "";
			if (null != newParent) {
				path = newParent.getPath();
			}
			
			throw new TreeException(TreeException.NODE_NOT_FOUND,
					OOEclipsePlugin.getTranslationString(
							I18nConstants.NO_NEW_PARENT) + path);
		}
	}
	
	/**
	 * TODO Test
	 * 
	 * @param newParentPath
	 * @param newName
	 * @throws TreeException
	 */
	public void moveFind(String newParentPath, String newName) throws TreeException {
		
		try {
			if (null != getTreeRoot()) {
				
				ModelTree root = getTreeRoot();
				TreeNode newParent = root.findNode(newParentPath);
				
				move(newParent, newName);
				
				
			} else {
				throw new TreeException(TreeException.NO_ROOT,
						OOEclipsePlugin.getTranslationString(
								I18nConstants.NO_ROOT_DEFINED) + getPath());
			}
		} catch (Exception e) {
			if (e instanceof TreeException) {
				throw (TreeException)e;
			} else {
				throw new TreeException(TreeException.UNKOWN_ERROR, e.getMessage());
			}
		}
	}
	
	/**
	 * Adds the given node to the children if the node is valid and isn't 
	 * already present among the children.
	 * 
	 * @param node node to add.
	 */
	public void addNode(TreeNode node){
		
		if (isValidNode(node) && !nodes.containsKey(node.getName())){
			nodes.put(node.getName(), node);
			fireNodeAdded(node);
		}
	}
	
	public void removeNode(TreeNode node){
		
		nodes.remove(node.getName());
		fireNodeRemoved(node);
	}
	
	public void removeAllNodes(){
		
		Set keys = ((HashMap)nodes.clone()).keySet();
		Iterator iter = keys.iterator();
		
		while (iter.hasNext()){
			String key = (String)iter.next();
			TreeNode node = (TreeNode)nodes.get(key);
			node.dispose();
		}
	}
	
	//------------------------------------ Visitor design pattern implementation 
	
	/**
	 * Accepts or refuse a tree visitor.
	 * 
	 * @param visitor visitor to recieve or reject
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