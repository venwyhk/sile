package com.ikasoa.eclipse.sile.wizards;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.ikasoa.eclipse.sile.ConsoleShower;
import com.ikasoa.eclipse.sile.elements.Directory;
import com.ikasoa.eclipse.sile.elements.DirectoryTypeEnum;
import com.ikasoa.eclipse.sile.elements.Sources;
import com.ikasoa.eclipse.sile.xml.XmlSourceServiceImpl;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.PreferenceConstants;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * SileNewWizard
 * 
 * @author <a href="mailto:larry7696@gmail.com">Larry</a>
 * @version 0.1
 */
public class SileNewWizard extends Wizard implements INewWizard, IImportWizard {

	private SileWizardPage page;

	private IStructuredSelection selection;

	private ConsoleShower consoleShower = new ConsoleShower();

	public SileNewWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new SileWizardPage(selection);
		addPage(page);
	}

	@Override
	public boolean performFinish() {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String name = page.getProjectName();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(name);

		// 判断项目是否已存在
		if (project.exists()) {
			errorWindow("Project " + name + " is exists.");
			return false;
		}

		IProjectDescription description = workspace.newProjectDescription(name);
		if (!page.isUseDefaultSelected())
			description.setLocation(Path.fromOSString(page.getLocation()));

		String[] javaNature = description.getNatureIds();
		String[] newJavaNature = new String[javaNature.length + 1];
		System.arraycopy(javaNature, 0, newJavaNature, 0, javaNature.length);
		newJavaNature[javaNature.length] = "org.eclipse.jdt.core.javanature";
		description.setNatureIds(newJavaNature);

		try {
			NullProgressMonitor monitor = new NullProgressMonitor();
			project.create(description, monitor);
			if (!project.isOpen())
				project.open(IResource.BACKGROUND_REFRESH, monitor);
		} catch (CoreException e) {
			errorWindow(e.getMessage());
			return false;
		}

		consoleShower.show("Create project " + name + ".");

		// 转化成java工程
		IJavaProject javaProject = JavaCore.create(project);

		try {
			List<IClasspathEntry> list = new ArrayList<>();
			// 获取默认的JRE库
			list.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));
			// 获取原来的build path
			list.addAll(Arrays.asList(javaProject.getRawClasspath()));
			javaProject.setRawClasspath(list.toArray(new IClasspathEntry[list.size()]), null);
		} catch (JavaModelException e) {
			errorWindow(e.getMessage());
			return false;
		}

		consoleShower.show("Start build.");

		try {
			// 读取配置文件
			Sources sources = page.isOnlineConfigure()
					? new XmlSourceServiceImpl().getSrouces(new URL(page.getConfigureFileUrl()))
					: new XmlSourceServiceImpl().getSrouces(new File(ResourcesPlugin.getWorkspace().getRoot()
							.getFile(Path.fromOSString(page.getConfigureFile())).getLocationURI()));
			// 创建文件
			buildFiles(javaProject, sources.getFileList(), null);
			for (Directory directory : sources.getDirectoryList())
				buildDirectory(javaProject, directory);
		} catch (Exception e) {
			errorWindow(e.getMessage());
			return false;
		}

		consoleShower.show("Build complete.");

		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	private void buildFiles(IJavaProject javaProject, List<com.ikasoa.eclipse.sile.elements.File> fileList, String path)
			throws Exception {
		for (com.ikasoa.eclipse.sile.elements.File file : fileList) {
			URL url = new URL(file.getUrl());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			InputStream inputStream = conn.getInputStream();
			String fileName = file.getName();
			if (path != null)
				fileName = path + File.separator + file.getName();
			consoleShower.show("Create file : " + fileName);
			javaProject.getProject().getFile(fileName).create(inputStream, true, new NullProgressMonitor());
		}
	}

	private void buildDirectory(IJavaProject javaProject, Directory directory) throws Exception {
		switch (directory.getType()) {
		case SOURCE:
			buildSource(javaProject, directory);
		case FOLDER:
			buildFolder(javaProject, directory.getName());
		default:
			break;
		}
	}

	private void buildSource(IJavaProject javaProject, Directory directory) throws Exception {

		// 创建目录
		IFolder srcFolder = buildFolder(javaProject, directory.getName());

		// this.createFolder(srcFolder);
		// 创建SourceLibrary
		IClasspathEntry srcClasspathEntry = JavaCore.newSourceEntry(srcFolder.getFullPath());

		// 得到旧的build path
		IClasspathEntry[] oldClasspathEntries = javaProject.readRawClasspath();

		// 添加新的
		List<IClasspathEntry> list = new ArrayList<>();
		list.addAll(Arrays.asList(oldClasspathEntries));
		list.add(srcClasspathEntry);

		// 原来存在一个与工程名相同的源文件夹,必须先删除
		IClasspathEntry temp = JavaCore.newSourceEntry(new Path(File.separator + page.getProjectName()));
		if (list.contains(temp))
			list.remove(temp);

		javaProject.setRawClasspath(list.toArray(new IClasspathEntry[list.size()]), null);

		// 创建包
		String packageName = buildPackage(javaProject, directory.getDirectoryList(), directory.getName());
		consoleShower.show("Builded package : " + packageName);

		// 创建文件
		buildFiles(javaProject, directory.getFileList(), directory.getName());
	}

	private String buildPackage(IJavaProject javaProject, List<Directory> directoryList, String path) throws Exception {
		String packageName = "";
		// 创建包
		for (Directory cDir : directoryList) {
			if (DirectoryTypeEnum.PACKAGE.getValue().equals(cDir.getType().getValue()))
				packageName = cDir.getName().replace("/", ".");
			String fullPath = path + File.separator + cDir.getName();
			buildFolder(javaProject, fullPath);
			buildFiles(javaProject, cDir.getFileList(), fullPath);
			// 创建子包
			buildPackage(javaProject, cDir.getDirectoryList(), fullPath);
		}
		return packageName;
	}

	private IFolder buildFolder(IJavaProject javaProject, String name) throws Exception {
		IFolder folder = javaProject.getProject().getFolder(name);
		if (!folder.exists()) {
			consoleShower.show("Create folder : " + name);
			createParentFolder(folder);
			folder.create(true, true, null);
		}
		return folder;
	}

	private IFolder createParentFolder(IFolder folder) throws Exception {
		if (!folder.getParent().exists() && folder.getParent() instanceof IFolder) {
			IFolder parentFolder = (IFolder) folder.getParent();
			IFolder _parentFolder = createParentFolder(parentFolder);
			_parentFolder.create(true, true, null);
			return parentFolder.exists() ? folder : parentFolder;
		} else
			return folder;
	}

	private void errorWindow(String message) {
		consoleShower.show("Error : " + message);
		MessageDialog.openInformation(null, "Error", message);
	}

}