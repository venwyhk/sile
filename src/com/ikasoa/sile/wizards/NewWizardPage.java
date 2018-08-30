package com.ikasoa.sile.wizards;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class NewWizardPage extends WizardPage {

	private Text projectNameText;

	private Text projectVersionText;

	private Text packageNameText;
	
	private Text configureFileUrl;

	private ISelection selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("Create Sile project");
		setDescription("创建Sile项目.");
		this.selection = selection;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		layout.numColumns = 2;
		layout.verticalSpacing = 9;

		// project name

		Label label = new Label(container, SWT.NULL);
		label.setText("&Project Name (项目名) :");
		projectNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		projectNameText.setLayoutData(gd);
		projectNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		// project version

		label = new Label(container, SWT.NULL);
		label.setText("&Project Version (项目版本) :");
		projectVersionText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		projectVersionText.setLayoutData(gd);
		projectVersionText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		// package name

		label = new Label(container, SWT.NULL);
		label.setText("&Package Name (包名) :");
		packageNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		packageNameText.setLayoutData(gd);
		packageNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		// configure file path

		label = new Label(container, SWT.NULL);
		label.setText("&Configure File Url (配置文件地址) :");
		configureFileUrl = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		configureFileUrl.setLayoutData(gd);
		configureFileUrl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		projectNameText.setText("ExampleProject");
		projectVersionText.setText("1.0");
		packageNameText.setText("com.example");
		configureFileUrl.setText("");
	}

	private void dialogChanged() {
		String projectName = getProjectName();
		if (projectName.length() == 0) {
			updateStatus("项目名不能为空!");
			return;
		}
		if (projectName.length() > 32) {
			updateStatus("项目名长度不能超过32个字符!");
			return;
		}
		String projectVersion = getProjectVersion();
		if (projectVersion.length() == 0) {
			updateStatus("项目版本不能为空!");
			return;
		}
		if (projectVersion.length() > 32) {
			updateStatus("项目版本长度不能超过32个字符!");
			return;
		}
		String packageName = getPackageName();
		if (packageName.length() == 0) {
			updateStatus("包名不能为空!");
			return;
		}
		if (packageName.length() > 32) {
			updateStatus("包名长度不能超过32个字符!");
			return;
		}
		String configureFileUrl = getConfigureFileUrl();
		if (configureFileUrl.length() == 0) {
			updateStatus("配置文件地址不能为空!");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getProjectName() {
		return projectNameText.getText();
	}

	public String getProjectVersion() {
		return projectVersionText.getText();
	}
	
	public String getPackageName() {
		return packageNameText.getText();
	}

	public String getConfigureFileUrl() {
		return configureFileUrl.getText();
	}

}