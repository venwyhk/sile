package com.ikasoa.sile.elements;

import java.util.List;

public class Sources {

	public final static String DIRECTORY = "directory";

	public final static String FILE = "file";

	private List<Directory> directoryList;

	private List<File> fileList;

	public Sources(List<Directory> directoryList, List<File> fileList) {
		this.directoryList = directoryList;
		this.fileList = fileList;
	}

	public List<Directory> getDirectoryList() {
		return directoryList;
	}

	public void setDirectoryList(List<Directory> directoryList) {
		this.directoryList = directoryList;
	}

	public List<File> getFileList() {
		return fileList;
	}

	public void setFileList(List<File> fileList) {
		this.fileList = fileList;
	}

}