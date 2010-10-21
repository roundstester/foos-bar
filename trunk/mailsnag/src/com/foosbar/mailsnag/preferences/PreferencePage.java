package com.foosbar.mailsnag.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.foosbar.mailsnag.Activator;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class PreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("SMTP Capture will listen on the specified port and capture incoming emails.  Useful for development purposes where a full fledged SMTP server is unnecessary.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		//addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH, 
		//		"&Directory preference:", getFieldEditorParent()));
		//addField(
		//	new BooleanFieldEditor(
		//		PreferenceConstants.P_BOOLEAN,
		//		"&An example of a boolean preference",
		//		getFieldEditorParent()));

		//addField(new RadioGroupFieldEditor(
		//		PreferenceConstants.P_CHOICE,
		//	"An example of a multiple-choice preference",
		//	1,
		//	new String[][] { { "&Choice 1", "choice1" }, {
		//		"C&hoice 2", "choice2" }
		//}, getFieldEditorParent()));
		//addField(
		//	new StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:", getFieldEditorParent()));
		IntegerFieldEditor port = 
			new IntegerFieldEditor(PreferenceConstants.PARAM_PORT, "Listener Port", getFieldEditorParent());

		port.setValidRange(1,65535);
		
		BooleanFieldEditor persist = 
			new BooleanFieldEditor(PreferenceConstants.PARAM_PERSIST, "Persist emails between sessions", getFieldEditorParent());
		
		BooleanFieldEditor debug = 
			new BooleanFieldEditor(PreferenceConstants.PARAM_DEBUG, "Print debug messages to STDOUT", getFieldEditorParent());
		
		addField(port);
		addField(persist);
		addField(debug);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}