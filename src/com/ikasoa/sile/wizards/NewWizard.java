package com.ikasoa.sile.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.operation.*;

import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.resources.*;

import java.io.*;
import org.eclipse.ui.*;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the
 * provided container. If the container resource (a folder or a project) is
 * selected in the workspace when the wizard is opened, it will accept it as the
 * target container. The wizard creates one file with the extension "mpe". If a
 * sample multi-page editor (also available as a template) is registered for the
 * same extension, it will be able to open it.
 */

public class NewWizard extends Wizard implements INewWizard, IImportWizard {

	private NewWizardPage page;

	// private EclikasoaImportWizardPage mainPage;

	private IStructuredSelection selection;

	private final static String FILE_SPLIT_STR = System.getProperty("file.separator");

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

//		IFolder binFolder = javaProject.getProject().getFolder("bin");
//		try {
//			binFolder.create(true, true, null);
//			javaProject.setOutputLocation(binFolder.getFullPath(), null);
//		} catch (CoreException e) {
//			return false;
//		}

		try {
			IProjectDescription description2 = javaProject.getProject().getDescription();
			ICommand command = description2.newCommand();
			command.setBuilderName("org.eclipse.jdt.core.javabuilder");
			description2.setBuildSpec(new ICommand[] { command });
			description2.setNatureIds(new String[] { "org.eclipse.jdt.core.javanature" });
			javaProject.getProject().setDescription(description2, null);
		} catch (CoreException e) {
			return false;
		}

		IFolder srcFolder = javaProject.getProject().getFolder("src");
		try {
			srcFolder.create(true, true, null);
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
			IClasspathEntry temp = JavaCore.newSourceEntry(new Path(FILE_SPLIT_STR + page.getProjectName()));
			if (list.contains(temp)) {
				list.remove(temp);
			}

			javaProject.setRawClasspath(list.toArray(new IClasspathEntry[list.size()]), null);
		} catch (CoreException e) {
			return false;
		}

		IPackageFragmentRoot[] IPackageFragmentRoots = javaProject
				.getPackageFragmentRoots(JavaCore.newSourceEntry(srcFolder.getFullPath()));
		IPackageFragmentRoot projectRoot = javaProject.getPackageFragmentRoot(javaProject.getResource());
		IPackageFragmentRoot projectSrcRoot = projectRoot;
		if (IPackageFragmentRoots.length > 0)
			projectSrcRoot = javaProject.getPackageFragmentRoot(IPackageFragmentRoots[0].getResource());

		try {
			IPackageFragment packageFragment = projectSrcRoot.createPackageFragment(page.getPackageName(), true, null);
			String javaCode = "package " + page.getPackageName()
					+ ";public class HelloWorld{public static void main(String[] args){System.out.println(\"helloworld!\");}}";
			packageFragment.createCompilationUnit("HelloWorld.java", javaCode, true, new NullProgressMonitor());
		} catch (JavaModelException e) {
			return false;
		}
		
		//
		
		
		IFile pomFile = javaProject.getProject().getFile("pom.xml");
		try {
			URL u = new URL("https://raw.githubusercontent.com/venwyhk/simpleryo_shop_demo/master/pom.xml");
            HttpURLConnection conn = (HttpURLConnection)u.openConnection();
            InputStream inputStream = conn.getInputStream();
			pomFile.create(inputStream, true, new NullProgressMonitor());
		} catch (Exception e) {
			return false;
		}
		
		
		IFolder resourcesFolder = javaProject.getProject().getFolder("resources");
		try {
			resourcesFolder.create(true, true, null);
		} catch (CoreException e) {
			return false;
		}
		
		
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
	
}