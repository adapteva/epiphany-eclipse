/**
 * 
 */
package adapteva.com.cdt.build.adspgcc.templateengine;

import org.eclipse.cdt.ui.templateengine.IPagesAfterTemplateSelectionProvider;
import org.eclipse.cdt.ui.templateengine.IWizardDataPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * @author oraikhman
 *
 */
public class TRY_AFT implements IPagesAfterTemplateSelectionProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.templateengine.IPagesAfterTemplateSelectionProvider#createAdditionalPages(org.eclipse.ui.IWorkbenchWizard, org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public IWizardDataPage[] createAdditionalPages(IWorkbenchWizard wizard,
			IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.templateengine.IPagesAfterTemplateSelectionProvider#getCreatedPages(org.eclipse.ui.IWorkbenchWizard)
	 */
	public IWizardDataPage[] getCreatedPages(IWorkbenchWizard wizard) {
		// TODO Auto-generated method stub
		return null;
	}

}
