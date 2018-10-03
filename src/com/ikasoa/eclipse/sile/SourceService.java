package com.ikasoa.eclipse.sile;

import java.io.File;
import java.net.URL;

import com.ikasoa.eclipse.sile.elements.Sources;

/**
 * SourceService
 * 
 * @author <a href="mailto:larry7696@gmail.com">Larry</a>
 * @version 0.1
 */
public interface SourceService {

	Sources getSrouces(File file) throws Exception;

	Sources getSrouces(URL url) throws Exception;
	
	String replace(String name);

}
