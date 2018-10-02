package com.ikasoa.sile.wizards;

import java.io.File;

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

/**
 * SileWizardPage
 * 
 * @author <a href="mailto:larry7696@gmail.com">Larry</a>
 * @version 0.1
 */
public class SileWizardPage extends WizardPage {

	private static final String CONFIGURE_SUFFIX = ".xml";

	private Text projectName;

	private Button locationCheck;

	private Text location;

	private Group configureGroup;

	private Text configureFile;

	private Text configureFileUrl;

	private boolean isOnlineConfigure = false;

	private boolean syncLock = true;

	public SileWizardPage(ISelection selection) {
		super("Sile Setting Page", "Sile Codegen",
				ImageDescriptor.createFromFile(SileWizardPage.class, "title_bar.png"));
		setDescription("Create a Java (Sile) project in the workspace or in an external location.");
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
		projectName.setText(""); // 默认值
		projectName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
				String name = getProjectName();
				if (name != null && name.indexOf(File.separator) >= 0)
					setProjectName(name.replaceAll(File.separator, "")); // 项目名不允许有空格
				else if (name != null && name.indexOf(" ") >= 0)
					setProjectName(name.replaceAll(" ", "")); // 项目名不允许有separator
				else if (syncLock)
					syncLocation();
			}
		});

		// Use default location

		locationCheck = new Button(composite, SWT.CHECK);
		locationCheck.setText("Use default location");
		locationCheck.setSelection(true); // 默认选中
		locationCheck.setLayoutData(getGridData(3));

		// location

		label = new Label(composite, SWT.NULL);
		label.setText("&Location :");
		label.setFont(composite.getFont());
		location = new Text(composite, SWT.BORDER | SWT.SINGLE);
		location.setLayoutData(getGridData(2));
		location.setEnabled(false);
		location.setText(Platform.getLocation().toOSString() + File.separator + getProjectName()); // 默认值
		location.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
				if (syncLock)
					syncProjectName(); // 同步修改项目名称
			}
		});

		locationCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (location.getEnabled())
					location.setEnabled(false);
				else {
					location.setEnabled(true);
					location.setFocus();
				}
			}
		});

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
				dialogChanged();
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
				dialogChanged();
			}
		});
		configureFileUrl = new Text(configureGroup, SWT.BORDER | SWT.SINGLE);
		configureFileUrl.setLayoutData(getGridData(2));
		configureFileUrl.setEnabled(false);
		configureFileUrl.setText("http://"); // https://raw.githubusercontent.com/venwyhk/tuangou-vu-admin/master/sile.xml
		configureFileUrl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

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
		if (!isUseDefaultSelected() && getLocation().length() == 0) {
			updateStatus("Location is null.");
			return;
		}
		if (!isOnlineConfigure() && getConfigureFile().length() == 0) {
			updateStatus("Select a configure file.");
			return;
		}
		String configureFileUrl = getConfigureFileUrl();
		if (isOnlineConfigure() && configureFileUrl.length() == 0) {
			updateStatus("Configure file URL is null.");
			return;
		}
		if (isOnlineConfigure() && (configureFileUrl.indexOf(".") == -1
				|| (configureFileUrl.indexOf("http://") != 0 && configureFileUrl.indexOf("https://") != 0))) {
			updateStatus("Configure file URL error.");
			return;
		}
		String configureFileUrlSuffix = configureFileUrl.substring(configureFileUrl.lastIndexOf("."));
		if (isOnlineConfigure() && !CONFIGURE_SUFFIX.equalsIgnoreCase(configureFileUrlSuffix.trim().toLowerCase())) {
			updateStatus("Configure file type must be XML.");
			return;
		}
		updateStatus(null);
	}

	private synchronized void syncProjectName() {
		String locationText = getLocation();
		if (locationText != null) {
			syncLock = false;
			setProjectName(locationText.substring(locationText.lastIndexOf(File.separator) + 1));
			syncLock = true;
		}
	}

	private synchronized void syncLocation() {
		String locationText = getLocation();
		if (locationText != null) {
			syncLock = false;
			setLocation(locationText.substring(0, locationText.lastIndexOf(File.separator)) + File.separator
					+ getProjectName());
			syncLock = true;
		}
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
				return s.equals("") ? "*" + CONFIGURE_SUFFIX : s;
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

	public void setProjectName(String text) {
		if (projectName != null)
			projectName.setText(text);
	}

	public boolean isUseDefaultSelected() {
		return locationCheck != null ? locationCheck.getSelection() : true;
	}

	public String getLocation() {
		return location != null ? location.getText() : Platform.getLocation().toOSString();
	}

	public void setLocation(String text) {
		if (location != null)
			location.setText(text);
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