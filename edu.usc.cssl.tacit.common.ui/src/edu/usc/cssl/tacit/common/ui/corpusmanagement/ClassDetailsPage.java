package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import edu.usc.cssl.tacit.common.queryprocess.QueryProcessorConstants;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.CorpusMangementValidation;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;

public class ClassDetailsPage implements IDetailsPage {
	private IManagedForm mform;
	private CorpusClass selectedCorpusClass;
	private Text classPathTxt;
	private Text classNameTxt;
	private ScrolledForm corpusMgmtViewform;
	private ManageCorpora corpusManagement;
	private Text keyFieldTxt;
	private Label keyFields;

	public ClassDetailsPage(ScrolledForm corpusMgmtViewform) {
		corpusManagement = new ManageCorpora();
		this.corpusMgmtViewform = corpusMgmtViewform;
	}

	@Override
	public void initialize(IManagedForm mform) {
		this.mform = mform;
	}

	@Override
	public void createContents(Composite parent) {
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false)
				.applyTo(parent);
		FormToolkit toolkit = mform.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION
				| Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText("Class Details");
		section.setDescription("Enter the details of class.");

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sc);

		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sectionClient);
		section.setClient(sectionClient);

		TacitFormComposite.createEmptyRow(toolkit, sectionClient);

		final Label classLbl = toolkit.createLabel(sectionClient,
				"Class Name:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(classLbl);
		classNameTxt = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0)
				.applyTo(classNameTxt);
		if (null != selectedCorpusClass) {
			classNameTxt.setText(selectedCorpusClass.getClassName());
		}
		classNameTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (CorpusMangementValidation.isClassnameValid(
						classNameTxt.getText(), selectedCorpusClass,
						corpusMgmtViewform)) {
					selectedCorpusClass.setClassName(classNameTxt.getText());
					selectedCorpusClass.getViewer().refresh();
				}

				if (!selectedCorpusClass.getClassName().isEmpty())
					corpusMgmtViewform.getMessageManager().removeMessage(
							"classNameEmpty");
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (CorpusMangementValidation.isClassnameValid(
						classNameTxt.getText(), selectedCorpusClass,
						corpusMgmtViewform)) {
					selectedCorpusClass.setClassName(classNameTxt.getText());
					selectedCorpusClass.getViewer().refresh();
				}

				if (!selectedCorpusClass.getClassName().isEmpty())
					corpusMgmtViewform.getMessageManager().removeMessage(
							"classNameEmpty");
			}
		});
		final Label classPathLbl = toolkit.createLabel(sectionClient,
				"Class Path:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(classPathLbl);
		classPathTxt = toolkit.createText(sectionClient, "", SWT.BORDER);
		if (null != selectedCorpusClass)
			classPathTxt.setText(selectedCorpusClass.getClassPath());

		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(classPathTxt);
		final Button browseBtn = toolkit.createButton(sectionClient,
				"Browse...", SWT.PUSH);
		browseBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(browseBtn.getShell(),
						SWT.OPEN);
				dlg.setText("Open");
				String path = dlg.open();
				if (path == null)
					return;
				classPathTxt.setText(path);
				selectedCorpusClass.setClassPath(classPathTxt.getText());
				corpusMgmtViewform.getMessageManager().removeMessage(
						"classPath");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		classPathTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (CorpusMangementValidation.isClassPathValid(
						classPathTxt.getText(),
						selectedCorpusClass.getClassName(), corpusMgmtViewform))
					selectedCorpusClass.setClassPath(classPathTxt.getText());

				if (!selectedCorpusClass.getClassPath().isEmpty())
					corpusMgmtViewform.getMessageManager().removeMessage(
							"classPath");
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (CorpusMangementValidation.isClassPathValid(
						classPathTxt.getText(),
						selectedCorpusClass.getClassName(), corpusMgmtViewform))
					selectedCorpusClass.setClassPath(classPathTxt.getText());

				if (!selectedCorpusClass.getClassPath().isEmpty())
					corpusMgmtViewform.getMessageManager().removeMessage(
							"classPath");
			}
		});
		keyFields = toolkit.createLabel(sectionClient, "Analysis Fields:",
				SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(keyFields);
		keyFieldTxt = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(keyFieldTxt);
		keyFieldTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				selectedCorpusClass.setKeyTextFields(keyFieldTxt.getText());
			}

			@Override
			public void keyPressed(KeyEvent e) {
				selectedCorpusClass.setKeyTextFields(keyFieldTxt.getText());
			}
		});
	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void commit(boolean onSave) {

	}

	@Override
	public boolean setFormInput(Object input) {
		return false;
	}

	@Override
	public void setFocus() {

	}

	@Override
	public boolean isStale() {
		return false;
	}

	@Override
	public void refresh() {

	}

	@Override
	public void selectionChanged(IFormPart part, ISelection selection) {
		corpusMgmtViewform.getMessageManager().removeAllMessages();
		selectedCorpusClass = (CorpusClass) ((IStructuredSelection) selection)
				.getFirstElement();
		classNameTxt.setText(selectedCorpusClass.getClassName());
		classPathTxt.setText(selectedCorpusClass.getClassPath());
		CorpusMangementValidation.validateClassData(selectedCorpusClass,
				corpusMgmtViewform);
		if ((selectedCorpusClass.getParent().getDatatype() != CMDataType.REDDIT_JSON && selectedCorpusClass
				.getParent().getDatatype() != CMDataType.TWITTER_JSON)) {
			keyFields.setVisible(false);
			keyFieldTxt.setVisible(false);

		} else {
			keyFields.setVisible(true);
			keyFieldTxt.setVisible(true);
			keyFieldTxt.setText(selectedCorpusClass.getKeyTextFields());
			if(keyFieldTxt.getText().isEmpty()) {					
				if(selectedCorpusClass.getParent().getDatatype() == CMDataType.REDDIT_JSON)
					keyFieldTxt.setText(QueryProcessorConstants.DEFAULT_REDDIT_DATA_FIELDS);
				else if(selectedCorpusClass.getParent().getDatatype() == CMDataType.TWITTER_JSON)
					keyFieldTxt.setText(QueryProcessorConstants.DEFAULT_TWITTER_DATA_FIELDS);		
			}
		}
	}

}
