package com.ikasoa.sile.wizards;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
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

	private Text projectName;

	private Text location;

	private Group configureGroup;

	private Text configureFile;

	private Text configureFileUrl;

	private boolean isOnlineConfigure = false;

	public SileWizardPage(ISelection selection) {
		super("Sile Setting Page", "Sile Codegen",
				ImageDescriptor.createFromFile(SileWizardPage.class, "title_bar.png"));
		setDescription("生成一个新的项目.");
	}

	@Override
	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(getGridLayout(3));
		composite.setFont(parent.getFont());

		// project name

		Label label = new Label(composite, SWT.NULL);
		label.setText("&Project name :");
		label.setFont(composite.getFont());
		projectName = new Text(composite, SWT.BORDER | SWT.SINGLE);
		projectName.setLayoutData(getGridData(2));
		projectName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		projectName.setText("ExampleProject"); // 默认值

		// location

		label = new Label(composite, SWT.NULL);
		label.setText("&Location :");
		label.setFont(composite.getFont());
		location = new Text(composite, SWT.BORDER | SWT.SINGLE);
		location.setLayoutData(getGridData(2));
		location.setEnabled(false);
		location.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		location.setText(Platform.getLocation().toOSString()); // 默认值

		// group

		configureGroup = new Group(composite, SWT.NONE);
		configureGroup.setText("&Source configure");
		configureGroup.setFont(composite.getFont());
		configureGroup.setLayout(getGridLayout(3));
		configureGroup.setLayoutData(getGridData(3));

		// configure file

		Button cb1 = new Button(configureGroup, SWT.RADIO);
		cb1.setText("&Configure file :");
		cb1.setFont(configureGroup.getFont());
		cb1.setSelection(true);
		configureFile = new Text(configureGroup, SWT.BORDER | SWT.SINGLE);
		configureFile.setLayoutData(getGridData(1));
		configureFile.setEditable(false);
		configureFile.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		Button browseButton = new Button(configureGroup, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setFont(configureGroup.getFont());
		browseButton.setEnabled(true); // 默认选中
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		cb1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browseButton.setEnabled(true);
				configureFileUrl.setEnabled(false);
				isOnlineConfigure = false;
			}
		});

		// configure file url

		Button cb2 = new Button(configureGroup, SWT.RADIO);
		cb2.setText("&Configure file URL :");
		cb2.setFont(configureGroup.getFont());
		cb2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				configureFileUrl.setEnabled(true);
				browseButton.setEnabled(false);
				isOnlineConfigure = true;
			}
		});
		configureFileUrl = new Text(configureGroup, SWT.BORDER | SWT.SINGLE);
		configureFileUrl.setLayoutData(getGridData(2));
		configureFileUrl.setEnabled(false);
		configureFileUrl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		configureFileUrl.setText("https://"); // https://raw.githubusercontent.com/venwyhk/tuangou-vu-admin/master/sile.xml

		dialogChanged();
		setControl(composite);
	}

	private void dialogChanged() {
		String projectName = getProjectName();
		if (projectName.length() == 0) {
			updateStatus("Enter a project name.");
			return;
		}
		if (projectName.length() > 32) {
			updateStatus("Project name too long.");
			return;
		}
		if (!isOnlineConfigure() && getConfigureFile().length() == 0) {
			updateStatus("Select a configure file.");
			return;
		}
		if (isOnlineConfigure() && getConfigureFileUrl().length() == 0) {
			updateStatus("Configure file URL is null.");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	private GridLayout getGridLayout(int numColumns) {
		GridLayout layout = new GridLayout(numColumns, false);
		layout.verticalSpacing = 10;
		return layout;
	}

	private GridData getGridData(int span) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.horizontalSpan = span;
		return gd;
	}

	private void handleBrowse() {
		ResourceListSelectionDialog dialog = new ResourceListSelectionDialog(getShell(),
				ResourcesPlugin.getWorkspace().getRoot(),
				IResource.DEPTH_INFINITE | IResource.FOLDER | IResource.FILE) {
			protected String adjustPattern() {
				String s = super.adjustPattern();
				return s.equals("") ? "*.xml" : s;
			}
		};
		if (dialog.open() == ResourceListSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1)
				configureFile.setText(result[0].toString().replaceAll("L/", ""));
		}
	}

	public String getProjectName() {
		return projectName != null ? projectName.getText() : "";
	}

	public String getLocation() {
		return location != null ? location.getText() : Platform.getLocation().toOSString();
	}

	public String getConfigureFileUrl() {
		return configureFileUrl != null ? configureFileUrl.getText() : "";
	}

	public String getConfigureFile() {
		return configureFile != null ? configureFile.getText() : "";
	}

	public boolean isOnlineConfigure() {
		return isOnlineConfigure;
	}

}