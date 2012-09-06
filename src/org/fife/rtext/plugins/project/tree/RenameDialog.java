/*
 * 08/28/2012
 *
 * RenameDialog.java - Dialog for renaming workspace tree nodes.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.rsta.ui.DecorativeIconPanel;
import org.fife.rsta.ui.EscapableDialog;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.plugins.project.Messages;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;


/**
 * A dialog for renaming nodes in the workspace outline tree.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RenameDialog extends EscapableDialog{

	private JButton okButton;
	private JButton cancelButton;
	private JTextField nameField;
	private DecorativeIconPanel renameDIP;
	private NameChecker nameChecker;

	private String type;

	private static Icon ERROR_ICON;


	/**
	 * Constructor.
	 *
	 * @param owner The rtext window that owns this dialog.
	 * @param type The type of node being renamed.
	 */
	public RenameDialog(RText owner, String type, NameChecker checker) {

		super(owner);
		this.type = type;
		Listener listener = new Listener();
		this.nameChecker = checker;

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
		ResourceBundle bundle = owner.getResourceBundle();

		JPanel cp =new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		setContentPane(cp);

		// A panel containing the main content.
		String key = "RenameDialog.Field.Label";
		JLabel label = new JLabel(Messages.getString(key));
		label.setDisplayedMnemonic(Messages.getString(key + ".Mnemonic").charAt(0));
		nameField = new JTextField(40);
		nameField.getDocument().addDocumentListener(listener);
		label.setLabelFor(nameField);
		renameDIP = new DecorativeIconPanel();
		Box box = new Box(BoxLayout.LINE_AXIS);
		box.add(label);
		box.add(Box.createHorizontalStrut(5));
		box.add(RTextUtilities.createAssistancePanel(nameField, renameDIP));
		box.add(Box.createHorizontalGlue());

		// Make a panel containing the OK and Cancel buttons.
		JPanel buttonPanel = new JPanel(new GridLayout(1,2, 5,5));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
		okButton = UIUtil.createRButton(bundle, "OKButtonLabel", "OKButtonMnemonic");
		okButton.setActionCommand("OK");
		okButton.addActionListener(listener);
		cancelButton = UIUtil.createRButton(bundle, "Cancel", "CancelMnemonic");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(listener);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		// Put everything into a neat little package.
		cp.add(box, BorderLayout.NORTH);
		JPanel temp = new JPanel();
		temp.add(buttonPanel);
		cp.add(temp, BorderLayout.SOUTH);
		JRootPane rootPane = getRootPane();
		rootPane.setDefaultButton(okButton);
		setTitle(Messages.getString("RenameDialog.Title", type));
		setModal(true);
		applyComponentOrientation(orientation);
		pack();
		setLocationRelativeTo(owner);

	}


	/**
	 * Returns the icon to use for fields with errors.
	 *
	 * @return The icon.
	 */
	private Icon getErrorIcon() {
		if (ERROR_ICON==null) {
			URL res = getClass().getResource("error_co.gif");
			ERROR_ICON = new ImageIcon(res);
		}
		return ERROR_ICON;
	}


	/**
	 * Returns the name selected.
	 *
	 * @return The name selected, or <code>null</code> if the dialog was
	 *         cancelled.
	 * @see #setName(String)
	 */
	public String getName() {
		String name = nameField.getText();
		return name.length()>0 ? name : null;
	}


	private void setBadNameValue(String reasonKey) {
		renameDIP.setShowIcon(true);
		String reason = Messages.getString(
				"RenameDialog.InvalidName." + reasonKey, type);
		renameDIP.setIcon(getErrorIcon());
		renameDIP.setToolTipText(reason);
		okButton.setEnabled(false);
	}


	private void setGoodNameValue() {
		renameDIP.setShowIcon(false);
		renameDIP.setToolTipText(null);
		okButton.setEnabled(true);
	}


	/**
	 * Sets the name displayed in this dialog.
	 *
	 * @param name The name to display.
	 * @see #getName()
	 */
	public void setName(String name) {
		nameField.setText(name);
		nameField.requestFocusInWindow();
		nameField.selectAll();
	}


	/**
	 * Listens for events in this dialog.
	 */
	private class Listener implements ActionListener, DocumentListener {

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("OK")) {
				escapePressed();
			}
			else if (command.equals("Cancel")) {
				nameField.setText(null); // So user gets back nothing
				escapePressed();
			}
		}

		public void changedUpdate(DocumentEvent e) {
		}

		private void handleDocumentEvent(DocumentEvent e) {

			if (nameField.getDocument().getLength()==0) {
				setBadNameValue("empty");
				return;
			}

			String text = nameField.getText();
			if (!nameChecker.isValid(text)) {
				setBadNameValue("invalidChars");
				return;
			}

//			if (isNew && MacroManager.get().containsMacroNamed(name)) {
//				setWarnMacroName();
//			}
//			else {
//				setGoodMacroName();
//			}
setGoodNameValue();

		}

		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

	}


}