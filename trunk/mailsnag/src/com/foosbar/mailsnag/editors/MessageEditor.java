package com.foosbar.mailsnag.editors;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.browser.WebBrowserEditor;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.foosbar.mailsnag.model.MessageData;

/**
 * A multi-tab editor for inspecting emails
 * <ul>
 * <li>page 0 (Optional) Html Message preview
 * <li>page 1 (Optional) Text Message preview
 * <li>page 2 Raw Data for inspecting the entire email stream.
 * </ul>
 */
public class MessageEditor extends MultiPageEditorPart implements IResourceChangeListener{

	/* Message Data */
	private MessageData messageData;
	
	public MessageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	/**
	 * Creates a page/tab to display the raw email for inspection.
	 */
	void createRawPage() {
		Composite composite = new Composite(getContainer(), SWT.NONE);

		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		
		StyledText text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);
		text.setText(messageData.getMessage());

		int index = addPage(composite);
		setPageText(index, "Raw Data");
	}
	
	/**
	 * Creates a page/tab to display the text email for preview.
	 */
	void createTextPage() {

		if(messageData == null || !messageData.hasTextMessage())
			return;
		
		Composite composite = new Composite(getContainer(), SWT.NONE);

		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		
		StyledText text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);
		text.setText(messageData.getTextMessage());
		
		int index = addPage(composite);
		setPageText(index, "Text Format");
	}
	
	/**
	 * Creates a page/tab to display the html email for preview.
	 */
	void createHtmlPage() {
		
		if(messageData == null || !messageData.hasHtmlMessage())
			return;
		
		try {
			File fileToOpen = File.createTempFile("email",".html");
			fileToOpen.deleteOnExit();
			Writer writer = new BufferedWriter(new FileWriter(fileToOpen));
			writer.write(messageData.getHtmlMessage());
			writer.close();
					
			WebBrowserEditor editor = new WebBrowserEditor();
			WebBrowserEditorInput input = new WebBrowserEditorInput(fileToOpen.toURL());
	
			setPageText(
					addPage(editor, input), 
					"HTML Format");
			
		} catch (Exception e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating nested text editor",
				null,
				null);
		}
	}
	
	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		createHtmlPage();
		createTextPage();
		createRawPage();
	}
	/**
	 * The <code>MultiPageEditorPart</code> implementation of this 
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}
	/**
	 * Saves the multi-page editor's document as another file.
	 * Also updates the text for page 0's tab, and updates this multi-page editor's input
	 * to correspond to the nested editor's.
	 */
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}
	/* (non-Javadoc)
	 * Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}
	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		super.init(site, editorInput);
		if (!(editorInput instanceof MessageEditorInput))
			throw new PartInitException("Invalid Input: Must be MessageEditorInput");
		
		setSite(site); 
		setInput(editorInput);
		
		this.messageData = 
			((MessageEditorInput) editorInput).getMessageData();
		
		setPartName(((MessageEditorInput) editorInput).getName());
	}
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 2) {
			return; // Do nothing
		}
	}
	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event){
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE){
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					//IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					/*
					for (int i = 0; i<pages.length; i++){
						if(((FileEditorInput)editor.getEditorInput()).getFile().getProject().equals(event.getResource())){
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart,true);
						}
					}
					*/
				}            
			});
		}
	}
}