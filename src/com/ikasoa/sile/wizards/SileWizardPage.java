package com.ikasoa.sile.wizards;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;

public class SileWizardPage extends WizardPage {

	private Text projectNameText;

	private Group configureGroup;

	private Text configureFile;
	
	private Text configureFileUrl;

	private boolean isOnlineConfigure = false;

	public SileWizardPage(ISelection selection) {
		super("Sile Setting Page", "Sile Code Generator", ImageDescriptor.createFromFile(SileWizardPage.class, "title_bar.png"));
		setDescription("生成一个新的项目.");
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);

		// project name

		Label label = new Label(container, SWT.NULL);
		label.setText("&Project Name :");
		projectNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		projectNameText.setLayoutData(gd);
		projectNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		label = new Label(container, SWT.NULL);

		// group
		configureGroup = new org.eclipse.swt.widgets.Group(container, SWT.NONE);
		configureGroup.setText("&Source Configure");
		configureGroup.setLayout(layout);
		gd = new org.eclipse.swt.layout.GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = org.eclipse.swt.SWT.FILL;
		configureGroup.setLayoutData(gd);

		// configure file

		Button cb1 = new Button(configureGroup, SWT.RADIO);
		cb1.setText("&Configure File :");
		cb1.setSelection(true);
		configureFile = new Text(configureGroup, SWT.BORDER | SWT.SINGLE);
		configureFile.setLayoutData(gd);
		configureFile.setEditable(false);
		configureFile.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		Button browseButton = new Button(configureGroup, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setEnabled(true); // 默认选中
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);

		// configure file path

		Button cb2 = new Button(configureGroup, SWT.RADIO);
		cb2.setText("&Configure File URL :");
		configureFileUrl = new Text(configureGroup, SWT.BORDER | SWT.SINGLE);
		configureFileUrl.setLayoutData(gd);
		configureFileUrl.setEnabled(false);
		configureFileUrl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		label = new Label(container, SWT.NULL);

		cb1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browseButton.setEnabled(true);
				configureFileUrl.setEnabled(false);
				isOnlineConfigure = false;
			}
		});
		cb2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				configureFileUrl.setEnabled(true);
				browseButton.setEnabled(false);
				isOnlineConfigure = true;
			}
		});

		initialize();
		dialogChanged();
		setControl(container);
	}

	private void initialize() {
		projectNameText.setText("ExampleProject");
		configureFileUrl.setText("https://"); // https://raw.githubusercontent.com/venwyhk/tuangou-vu-admin/master/sile.xml
	}

	private void handleBrowse() {
		ResourceListSelectionDialog dialog = new ResourceListSelectionDialog(getShell(),
				ResourcesPlugin.getWorkspace().getRoot(),
				IResource.DEPTH_INFINITE | IResource.FOLDER | IResource.FILE) {
			protected String adjustPattern() {
				String s = super.adjustPattern();
				if (s.equals(""))
					s = "*.xml";
				return s;
			}
		};
		if (dialog.open() == ResourceListSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				configureFile.setText(result[0].toString().replaceAll("L/", ""));
			}
		}
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
		String configureFile = getConfigureFile();
		if (!isOnlineConfigure() && configureFile.length() == 0) {
			updateStatus("请选择配置文件.");
			return;
		}
		String configureUrl = getConfigureFileUrl();
		if (isOnlineConfigure() && configureUrl.length() == 0) {
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

	public String getConfigureFileUrl() {
		return configureFileUrl.getText();
	}

	public String getConfigureFile() {
		return configureFile.getText();
	}

	public boolean isOnlineConfigure() {
		return isOnlineConfigure;
	}

}