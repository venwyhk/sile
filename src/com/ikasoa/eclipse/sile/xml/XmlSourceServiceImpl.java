package com.ikasoa.eclipse.sile.xml;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ikasoa.eclipse.sile.SourceService;
import com.ikasoa.eclipse.sile.elements.Directory;
import com.ikasoa.eclipse.sile.elements.DirectoryTypeEnum;
import com.ikasoa.eclipse.sile.elements.File;
import com.ikasoa.eclipse.sile.elements.Sources;

/**
 * XmlSourceServiceImpl
 * 
 * @author <a href="mailto:larry7696@gmail.com">Larry</a>
 * @version 0.1
 */
public class XmlSourceServiceImpl implements SourceService {

	private final static String PROPERTIES = "properties";

	private final static String SOURCES = "sources";

	private Map<String, String> propMap = new HashMap<>();

	private String keyPrefix = "{";

	private String keySuffix = "}";

	public XmlSourceServiceImpl() {
	}

	public XmlSourceServiceImpl(String keyPrefix, String keySuffix) {
		this.keyPrefix = keyPrefix;
		this.keySuffix = keySuffix;
	}

	@Override
	public Sources getSrouces(java.io.File file) throws Exception {
		return getSources(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file));
	}

	@Override
	public Sources getSrouces(URL url) throws Exception {
		return getSources(DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(((HttpURLConnection) url.openConnection()).getInputStream()));
	}

	@Override
	public String replace(String name) {
		if (name == null)
			return name;
		if (propMap.isEmpty())
			throw new RuntimeException("Properties is empty.");
		for (Entry<String, String> entry : propMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (key != null && !"".equals(key.trim()) && name.indexOf(key) > 0)
				name = name.replace(keyPrefix + key + keySuffix, value);
		}
		return name;
	}

	private Sources getSources(Document document) {
		// properties
		NodeList propertieNodeList = document.getElementsByTagName(PROPERTIES);
		for (int i = 0; i < propertieNodeList.getLength(); i++) {
			Element propElm = (Element) propertieNodeList.item(i);
			NodeList childPropNodeList = propElm.getChildNodes();
			for (int j = 0; j < childPropNodeList.getLength(); j++) {
				Node childPropNode = childPropNodeList.item(j);
				if (childPropNode.getNodeType() == Node.ELEMENT_NODE) {
					Element childPropElm = (Element) childPropNode;
					String key = childPropElm.getAttribute("key");
					String value = childPropElm.getTextContent();
					if (key != null && !"".equals(key.trim()))
						propMap.put(key, value);
				}
			}
		}
		// sources
		NodeList sourceNodeList = document.getElementsByTagName(SOURCES);
		if (sourceNodeList != null && sourceNodeList.getLength() > 0) {
			Node sourceNode = sourceNodeList.item(0);
			if (sourceNode.getNodeType() == Node.ELEMENT_NODE) {
				Element sourceElm = (Element) sourceNode;
				NodeList childSourceNodeList = sourceElm.getChildNodes();
				List<Directory> directoryList = new ArrayList<>();
				List<File> fileList = new ArrayList<>();
				for (int m = 0; m < childSourceNodeList.getLength(); m++)
					buildDirectory(childSourceNodeList.item(m), directoryList, fileList);
				return new Sources(directoryList, fileList);
			}
		}
		return null;
	}

	private void buildDirectory(Node node, List<Directory> directoryList, List<File> fileList) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element) node;
			if (Sources.DIRECTORY.equalsIgnoreCase(element.getTagName())) {
				List<File> childFileList = new ArrayList<>();
				NodeList childDirList = element.getChildNodes();
				for (int x = 0; x < childDirList.getLength(); x++) {
					Node childFileNode = childDirList.item(x);
					if (childFileNode.getNodeType() == Node.ELEMENT_NODE)
						childFileList.add(getFile((Element) childFileNode));
				}
				Directory directory = getDirectory(element, childFileList);
				if (directory != null) {
					NodeList childDirectoryNodeList = element.getChildNodes();
					List<Directory> _directoryList = new ArrayList<>();
					List<File> _fileList = new ArrayList<>();
					for (int y = 0; y < childDirectoryNodeList.getLength(); y++) {
						Node childDirectoryNode = childDirectoryNodeList.item(y);
						if (childDirectoryNode.getNodeType() == Node.ELEMENT_NODE) {
							buildDirectory((Element) childDirectoryNode, _directoryList, _fileList);
							directory.setDirectoryList(_directoryList);
							directory.setFileList(_fileList);
						}
					}
					directoryList.add(directory);
				}
			}
			if (Sources.FILE.equalsIgnoreCase(element.getTagName())) {
				fileList.add(getFile(element));
			}
		}
	}

	private Directory getDirectory(Element element, List<File> fileList) {
		if (DirectoryTypeEnum.SOURCE.getValue().equalsIgnoreCase(element.getAttribute(Directory.TYPE)))
			return new Directory(replace(element.getAttribute(Directory.NAME)), DirectoryTypeEnum.SOURCE, fileList);
		else if (DirectoryTypeEnum.PACKAGE.getValue().equalsIgnoreCase(element.getAttribute(Directory.TYPE)))
			return new Directory(replace(element.getAttribute(Directory.NAME)), DirectoryTypeEnum.PACKAGE, fileList);
		else if (DirectoryTypeEnum.FOLDER.getValue().equalsIgnoreCase(element.getAttribute(Directory.TYPE)))
			return new Directory(replace(element.getAttribute(Directory.NAME)), DirectoryTypeEnum.FOLDER, fileList);
		return null;
	}

	private File getFile(Element element) {
		return element.hasAttribute(File.NAME)
				? new File(replace(element.getAttribute(File.NAME)), replace(element.getAttribute(File.URL)))
				: new File(getFileName(replace(element.getAttribute(File.URL))),
						replace(element.getAttribute(File.URL)));
	}

	private String getFileName(String fileUrl) {
		return fileUrl.substring(fileUrl.lastIndexOf(java.io.File.separator) + 1);
	}

}
