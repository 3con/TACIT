package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.json.simple.parser.ParseException;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusManagementConstants;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;
import edu.usc.cssl.tacit.common.ui.internal.TreeParent;
import edu.usc.cssl.tacit.common.ui.utility.INlpCommonUiConstants;
import edu.usc.cssl.tacit.common.ui.utility.IconRegistry;

public class MasterDetailsPage extends MasterDetailsBlock {
	private ScrolledForm corpusMgmtViewform;
	List<ICorpus> corpusList;
	ManageCorpora corpusManagement;
	IViewSite viewSite;
	MasterDetailsPage(ScrolledForm form, IViewSite viewSite) throws IOException, ParseException {
		corpusList = new ArrayList<ICorpus>();
		corpusManagement = new ManageCorpora();
		corpusList = corpusManagement.getAllCorpusDetails();
		this.corpusMgmtViewform = form;
		this.viewSite = viewSite;
	}
	
	class MasterContentProvider implements ITreeContentProvider {
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof List) return ((List)inputElement).toArray();
			else if(inputElement instanceof ICorpus) return  ((ICorpus)inputElement).getClasses().toArray();
			else if(inputElement instanceof String) {
				File tacitLocationFiles = new File((String) inputElement);
				if(tacitLocationFiles.isDirectory()) {
					return tacitLocationFiles.listFiles();
				}
			}
			return new Object[] {};
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if(element instanceof ICorpus) return true;
			return false;
		}

	}

	class MasterLabelProvider extends LabelProvider implements
			ILabelProvider {
		@Override
		public String getText(Object element) {
			if(element instanceof ICorpus)
				return ((ICorpus) element).getCorpusName();
			else if(element instanceof ICorpusClass)
				return ((ICorpusClass) element).getClassName();
			else if(element instanceof String) {
				File tacitLocationFiles = new File((String) element);
				if(tacitLocationFiles.exists()) 
					return tacitLocationFiles.getAbsolutePath();
			}			
			return null;
		}
		
		@Override
		public Image getImage(Object element) {
			
			if(element instanceof ICorpusClass)
				return IconRegistry.getImageIconFactory().getImage(
						INlpCommonUiConstants.CORPUS_CLASS);
			else if(element instanceof ICorpus)
				return IconRegistry.getImageIconFactory().getImage(
						INlpCommonUiConstants.CORPUS);
			else if(element instanceof String) {
				return IconRegistry.getImageIconFactory().getImage(
						INlpCommonUiConstants.FILE_OBJ);
			}			
			return null;
		}
	}

	@Override
	protected void createMasterPart(final IManagedForm managedForm,
			Composite parent) {
		FormToolkit toolkit = managedForm.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText("Corpora"); //$NON-NLS-1$
 		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);
		
		//Create a tree to hold all corpuses
		toolkit.paintBordersFor(client);
		
		final TreeViewer corpusViewer = new TreeViewer(client, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 400; gd.widthHint = 100;
		corpusViewer.getTree().setLayoutData(gd);
		
		//Add all required buttons in the composite
		Composite buttonComposite = new Composite(client, SWT.NONE);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginWidth = buttonLayout.marginHeight = 0;
		buttonLayout.makeColumnsEqualWidth = true;
		buttonComposite.setLayout(buttonLayout);
		buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		Button addCorpus = toolkit.createButton(buttonComposite, "Add Corpus", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(addCorpus);
		
		final Button addClass = toolkit.createButton(buttonComposite, "Add Class", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(addClass);
		
		Button remove = toolkit.createButton(buttonComposite, "Remove", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(remove);		
		
		section.setClient(client);
		final SectionPart spart = new SectionPart(section);
		managedForm.addPart(spart);
		
		corpusViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				managedForm.fireSelectionChanged(spart, event.getSelection());
				 try {
					 	//corpusMgmtViewform.getMessageManager().removeAllMessages(); // removes all error messages
					 	IStructuredSelection selection  = (IStructuredSelection) event.getSelection();
					 	Object selectedObj = selection.getFirstElement();
					 	if(selectedObj instanceof ICorpus) {
					 		addClass.setEnabled(true);
					 	} else if(selectedObj instanceof ICorpusClass) {
					 		addClass.setEnabled(false);
					 	}
		         } catch(Exception exp) { //exception means item selected is not a corpus but a class.
		         }				
			}
		});
		corpusViewer.setContentProvider(new MasterContentProvider());
		corpusViewer.setLabelProvider(new MasterLabelProvider());
		for(ICorpus corpus : corpusList) { // set the viewer for the old corpuses loaded form disk
			((Corpus)corpus).setViewer(corpusViewer);
			for(ICorpusClass cc: corpus.getClasses()){
				((CorpusClass)cc).setViewer(corpusViewer);
			}
		}
		corpusViewer.setInput(corpusList);
		
		addCorpus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringBuilder corpusTempName = new StringBuilder("Corpus ");
				corpusTempName.append(corpusList.size()+1);
				Corpus c = new Corpus(new String(corpusTempName), CMDataType.PLAIN_TEXT, corpusViewer);
				c.addClass(new CorpusClass("Class 1", ICorpusManagementConstants.DEFAULT_CLASSPATH, corpusViewer));;
				corpusList.add(c);
				Object[] expandedItems = corpusViewer.getExpandedElements();
				corpusViewer.setInput(corpusList);
				corpusViewer.setExpandedElements(expandNewCorpus(expandedItems, c));
				corpusViewer.setSelection(new StructuredSelection(c),true);
			}
		});	
		
		addClass.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)corpusViewer.getSelection();
				 try {
					 	ICorpus corpusSelected = (ICorpus)selection.getFirstElement();
					 	int corpusIndex = corpusList.indexOf(corpusSelected);					 	
					 	StringBuilder classTempName = new StringBuilder("Class ");
					 	classTempName.append(corpusList.get(corpusIndex).getClasses().size()+1);
					 	CorpusClass newClass = new CorpusClass(new String(classTempName),ICorpusManagementConstants.DEFAULT_CLASSPATH, corpusViewer);
		            	((Corpus)corpusSelected).addClass(newClass);
					 	corpusList.set(corpusIndex, corpusSelected);
					 	corpusViewer.refresh(); 
						corpusViewer.setExpandedElements(expandNewCorpus(corpusViewer.getExpandedElements(), (Corpus) corpusList.get(corpusIndex)));
						corpusViewer.setSelection(new StructuredSelection(newClass),true);
		             } catch(Exception exp) { 
		             }
			}
		});
		
		final MessageDialog dg = new MessageDialog(
				corpusMgmtViewform.getShell(), "Delete Corpus/Class", null, "Are you sure you want to delete?", MessageDialog.QUESTION_WITH_CANCEL, 
				new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 0);
		
		remove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)corpusViewer.getSelection();
				 try {
						switch(dg.open()) {
							case 1:
								return;
						}
					 	Object selectedObj = selection.getFirstElement();
					 	if(selectedObj instanceof ICorpus) {
					 		ICorpus selectedCorpus = (ICorpus) selection.getFirstElement(); 
					 		corpusList.remove(selectedCorpus);
					 		ManageCorpora.removeCorpus((Corpus) selectedCorpus, true);
					 	} else if(selectedObj instanceof ICorpusClass){
					 		ITreeSelection classSelection = (ITreeSelection)selection;
					 		ICorpusClass selectedClass = (ICorpusClass) selection.getFirstElement();
			     			Corpus parentCorpus = (Corpus)classSelection.getPaths()[0].getParentPath().getLastSegment();
			            	parentCorpus.removeClass(selectedClass);					
			            	ManageCorpora.removeCorpus(parentCorpus, false);
					 	}
					 	corpusViewer.refresh();
		         } catch(Exception exp) { //exception means item selected is not a corpus but a class.
		         }
			}
		});	
		
	}

	protected Object[] expandNewCorpus(Object[] expanded, Corpus c) {
		Object[] newExpandedSet = new Object[expanded.length+1];
		int index = 0;
		for(Object o : expanded) {
			newExpandedSet[index++] = o;
		}
		newExpandedSet[index] = c;
		return newExpandedSet;
	}

	@Override
	protected void registerPages(DetailsPart detailsPart) {
		detailsPart.registerPage(Corpus.class, new CorpusDetailsPage(corpusMgmtViewform, corpusList, viewSite));
		detailsPart.registerPage(CorpusClass.class, new ClassDetailsPage(corpusMgmtViewform));
	}

	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		Action haction = new Action("hor", Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			@Override
			public void run() {
				sashForm.setOrientation(SWT.HORIZONTAL);
				form.reflow(true);
			}
		};
		haction.setChecked(true);
		Action vaction = new Action("ver", Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			@Override
			public void run() {
				sashForm.setOrientation(SWT.VERTICAL);
				form.reflow(true);
			}
		};
		vaction.setChecked(false);
		form.getToolBarManager().add(haction);
		form.getToolBarManager().add(vaction);
	}
}
