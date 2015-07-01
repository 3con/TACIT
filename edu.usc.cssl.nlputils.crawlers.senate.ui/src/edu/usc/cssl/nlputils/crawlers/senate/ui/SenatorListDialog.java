package edu.usc.cssl.nlputils.crawlers.senate.ui;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class SenatorListDialog extends ElementListSelectionDialog {

	public SenatorListDialog(Shell parent, ILabelProvider renderer) {
		super(parent, renderer);
	}
	
	public void refresh(Object[] elements){
		setListElements(elements);
	}

}