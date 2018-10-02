package com.ikasoa.sile.elements;

import java.util.List;

/**
 * Directory
 * 
 * @author <a href="mailto:larry7696@gmail.com">Larry</a>
 * @version 0.1
 */
public class Directory {

	public final static String NAME = "name";

	public final static String TYPE = "type";

	private String name;

	private DirectoryTypeEnum type;

	private List<File> fileList;

	private List<Directory> directoryList;

	public Directory(String name, DirectoryTypeEnum type, List<File> fileList) {
		this.name = name;
		this.type = type;
		this.fileList = fileList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DirectoryTypeEnum getType() {
		return type;
	}

	public void setType(DirectoryTypeEnum type) {
		this.type = type;
	}

	public List<File> getFileList() {
		return fileList;
	}

	public void setFileList(List<File> fileList) {
		this.fileList = fileList;
	}

	public List<Directory> getDirectoryList() {
		return directoryList;
	}

	public void setDirectoryList(List<Directory> directoryList) {
		this.directoryList = directoryList;
	}

}
