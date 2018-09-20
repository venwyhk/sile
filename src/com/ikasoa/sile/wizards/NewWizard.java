package com.ikasoa.sile.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.ikasoa.sile.elements.Directory;
import com.ikasoa.sile.elements.Sources;
import com.ikasoa.sile.elements.DirectoryTypeEnum;
import com.ikasoa.sile.xml.XmlSourceServiceImpl;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.PreferenceConstants;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.*;

import java.io.*;
import org.eclipse.ui.*;

public class NewWizard extends Wizard implements INewWizard, IImportWizard {

	private NewWizardPage page;

	// private EclikasoaImportWizardPage mainPage;

	private IStructuredSelection selection;

	/**
	 * Constructor for EclikasoaNewWizard.
	 */
	public NewWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		page = new NewWizardPage(selection);
		addPage(page);
		// mainPage = new EclikasoaImportWizardPage("Import File",selection);
		// addPage(mainPage);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We will
	 * create an operation and run it using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		final IProject project = root.getProject(page.getProjectName());

		IWorkspace workspace = root.getWorkspace();
		final IProjectDescription description = workspace.newProjectDescription(project.getName());

		String[] javaNature = description.getNatureIds();
		String[] newJavaNature = new String[javaNature.length + 1];
		System.arraycopy(javaNature, 0, newJavaNature, 0, javaNature.length);
		newJavaNature[javaNature.length] = "org.eclipse.jdt.core.javanature";
		description.setNatureIds(newJavaNature);

		try {
			NullProgressMonitor monitor = new NullProgressMonitor();
			project.create(description, monitor);
			project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1000));
		} catch (CoreException e) {
			return false;
		}

		// 转化成java工程
		IJavaProject javaProject = JavaCore.create(project);

		try {
			// 获取默认的JRE库
			IClasspathEntry[] jreLibrary = PreferenceConstants.getDefaultJRELibrary();
			// 获取原来的build path
			IClasspathEntry[] oldClasspathEntries = javaProject.getRawClasspath();
			List<IClasspathEntry> list = new ArrayList<>();
			list.addAll(Arrays.asList(jreLibrary));
			list.addAll(Arrays.asList(oldClasspathEntries));

			javaProject.setRawClasspath(list.toArray(new IClasspathEntry[list.size()]), null);
		} catch (JavaModelException e) {
			return false;
		}

		try {
			// 读取配置文件
			Sources sources = new XmlSourceServiceImpl().getSrouces(new URL(page.getConfigureFileUrl()));
			// 创建文件
			buildFiles(javaProject, sources.getFileList(), null);
			for (Directory directory : sources.getDirectoryList()) {
				buildDirectory(javaProject, directory);
			}

		} catch (Exception e) {
			System.out.println(e);
			return false;
		}

		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	private void buildFiles(IJavaProject javaProject, List<com.ikasoa.sile.elements.File> fileList, String path)
			throws Exception {
		for (com.ikasoa.sile.elements.File file : fileList) {
			URL u = new URL(file.getUrl());
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			InputStream inputStream = conn.getInputStream();
			String fileName = file.getName();
			if (path != null)
				fileName = path + File.separator + file.getName();
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
		System.out.println("包名 : " + packageName);

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
			System.out.println("--- : " + _parentFolder.getName());
			return parentFolder.exists() ? folder : parentFolder;
		} else
			return folder;
	}

}