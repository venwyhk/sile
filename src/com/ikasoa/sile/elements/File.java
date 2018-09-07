package com.ikasoa.sile.elements;

public class File {
	
	public final static String NAME = "name";
	
	public final static String URL = "url";

	private String name;

	private String url;

	public File(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
