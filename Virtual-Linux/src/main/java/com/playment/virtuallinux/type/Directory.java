/**
 * 
 */
package com.playment.virtuallinux.type;

import java.util.ArrayList;
import java.util.List;

/**
 * Directory is primary part of file system implentation. It keeps track of its
 * structure and its childrens
 * 
 * @author Vignesh Baskaran
 *
 */
public class Directory<T> {

	private T data = null;

	private List<Directory<T>> children = new ArrayList<>();

	private Directory<T> parent = null;

	public Directory(T data) {
		this.data = data;
	}

	public Directory<T> createDirectory(Directory<T> directory) {
		directory.setParent(this);
		this.children.add(directory);
		return directory;
	}

	public void createDirectories(List<Directory<T>> directories) {
		directories.forEach(each -> each.setParent(this));
		this.children.addAll(directories);
	}

	public List<Directory<T>> getChildren() {
		return children;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	private void setParent(Directory<T> parent) {
		this.parent = parent;
	}

	public Directory<T> getParent() {
		return parent;
	}
}

