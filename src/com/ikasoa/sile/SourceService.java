package com.ikasoa.sile;

import java.io.File;
import java.net.URL;

import com.ikasoa.sile.elements.Sources;

public interface SourceService {

	Sources getSrouces(File file) throws Exception;

	Sources getSrouces(URL url) throws Exception;
	
	String replace(String name);

}
