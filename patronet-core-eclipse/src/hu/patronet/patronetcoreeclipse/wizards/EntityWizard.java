package hu.patronet.patronetcoreeclipse.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

public class EntityWizard extends Wizard implements INewWizard {
	private EntityWizardPage page;
	private ISelection selection;

	public EntityWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public void addPages() {
		page = new EntityWizardPage(selection);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String namespace = page.getNamespace();
		final String className = page.getClassName();
		final String tableName = page.getTableName();
		final String tableIdCell = page.getTableIdCell();
		final String tableAlias = page.getTableAlias();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, namespace, className, tableName, tableIdCell, tableAlias, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	private void doFinish(
		String containerName,
		String namespace,
		String className,
		String tableName,
		String tableIdCell,
		String tableAlias,
		IProgressMonitor monitor
	) throws CoreException {
		String fileName = className + ".php";
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(namespace, className, tableName, tableIdCell, tableAlias);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}
	
	private InputStream openContentStream(
		String namespace,
		String className,
		String tableName,
		String tableIdCell,
		String tableAlias
	) {
		InputStream templateStream = getClass().getClassLoader().getResourceAsStream("templates/Entity.php");
		Reader templateReader = new InputStreamReader(templateStream);
		StringBuilder templateBuilder = new StringBuilder();
		char[] buffer = new char[1024];
		try {
			int s = -1;
			while ((s = templateReader.read(buffer)) >= 0) {
				if (s > 0) {
					templateBuilder.append(buffer, 0, s);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String template = templateBuilder.toString();
		
		Map<String, String> variableMap = new HashMap<>();
		variableMap.put("namespace", namespace);
		variableMap.put("class", className);
		variableMap.put("tableName", tableName);
		variableMap.put("tableIdCell", tableIdCell);
		variableMap.put("tableAlias", tableAlias);
		String content = putTemplateVariables(template, variableMap);
		
		return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
	}
	
	private String putTemplateVariables(String template, Map<String, String> variableMap) {
		StringBuilder resultBuilder = new StringBuilder();
		
		int position = 0;
		Pattern pattern = Pattern.compile("\\$(?:(\\$)|\\{([0-9a-zA-Z\\-_\\.:]+)\\})");
		Matcher matcher = pattern.matcher(template);
		while (matcher.find()) {
			String between = template.substring(position, matcher.start());
			resultBuilder.append(between);
			boolean isDollar = (matcher.group(1) != null);
			if (isDollar) {
				resultBuilder.append("$");
			} else {
				String variableName = matcher.group(2);
				boolean present = variableMap.containsKey(variableName);
				String variableContent = present ? variableMap.get(variableName) : variableName;
				resultBuilder.append(variableContent);
			}
			position = matcher.end();
		}
		String tail = template.substring(position);
		resultBuilder.append(tail);
		
		return resultBuilder.toString();
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "patronet-core-eclipse", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}