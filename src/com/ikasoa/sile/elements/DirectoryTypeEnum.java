package com.ikasoa.sile.elements;

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
