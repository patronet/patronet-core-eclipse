package hu.patronet.patronetcoreeclipse.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class EntityWizardPage extends WizardPage {

	private Text containerText;
	private Text namespaceText;
	private Text classNameText;
	private Text tableNameText;
	private Text tableIdCellText;
	private Text tableAliasText;

	private ISelection selection;

	public EntityWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("PatroNet Core Entity");
		setDescription("This wizard creates an entity based on ActiveRecordEntity.");
		this.selection = selection;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		
		addContainerRow(container);
		addNamespaceRow(container);
		addFileRow(container);
		addTableNameRow(container);
		addTableIdCellRow(container);
		addTableAliasRow(container);
		
		initialize();
		dialogChanged();
		setControl(container);
	}
	
	private void addContainerRow(Composite container) {
		Label label = new Label(container, SWT.NULL);
		label.setText("C&ontainer:");

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		containerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		containerText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
	}

	private void addNamespaceRow(Composite container) {
		Label label = new Label(container, SWT.NULL);
		label.setText("&Namespace:");

		namespaceText = new Text(container, SWT.BORDER | SWT.SINGLE);
		namespaceText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		namespaceText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		new Label(container, SWT.NULL);
	}

	private void addFileRow(Composite container) {
		Label label = new Label(container, SWT.NULL);
		label.setText("&Class name:");

		classNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		classNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		classNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		new Label(container, SWT.NULL);
	}

	private void addTableNameRow(Composite container) {
		Label label = new Label(container, SWT.NULL);
		label.setText("Table &name:");

		tableNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		tableNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tableNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		new Label(container, SWT.NULL);
	}

	private void addTableIdCellRow(Composite container) {
		Label label = new Label(container, SWT.NULL);
		label.setText("Table &ID cell:");

		tableIdCellText = new Text(container, SWT.BORDER | SWT.SINGLE);
		tableIdCellText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tableIdCellText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		new Label(container, SWT.NULL);
	}

	private void addTableAliasRow(Composite container) {
		Label label = new Label(container, SWT.NULL);
		label.setText("Table &alias:");

		tableAliasText = new Text(container, SWT.BORDER | SWT.SINGLE);
		tableAliasText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tableAliasText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		new Label(container, SWT.NULL);
	}
	
	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				containerText.setText(container.getFullPath().toString());
			}
		}
		namespaceText.setText("Some\\Namespace");
		classNameText.setText("Entity");
		tableNameText.setText("tbl_entity");
		tableIdCellText.setText("id");
		tableAliasText.setText("entity");
	}

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()));
		String entityName = getClassName();

		if (getContainerName().length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("File container must exist");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		if (entityName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (entityName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getNamespace() {
		return namespaceText.getText();
	}

	public String getClassName() {
		return classNameText.getText();
	}

	public String getTableName() {
		return tableNameText.getText();
	}

	public String getTableIdCell() {
		return tableIdCellText.getText();
	}

	public String getTableAlias() {
		return tableAliasText.getText();
	}
	
}