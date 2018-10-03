package com.ikasoa.eclipse.sile.elements;

/**
 * DirectoryTypeEnum
 * 
 * @author <a href="mailto:larry7696@gmail.com">Larry</a>
 * @version 0.1
 */
public enum DirectoryTypeEnum {

	SOURCE("source"), PACKAGE("package"), FOLDER("folder");

	private String value;

	DirectoryTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
