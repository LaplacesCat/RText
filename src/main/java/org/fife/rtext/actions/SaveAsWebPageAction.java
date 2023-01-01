/*
 * 03/30/2004
 *
 * SaveAsWebPageAction.java - Action to save a copy of the current
 * file as HTML in RText.
 * Copyright (C) 2004 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.ui.UIUtil;
import org.fife.ui.app.AppAction;
import org.fife.ui.rsyntaxtextarea.HtmlUtil;
import org.fife.ui.rtextfilechooser.RTextFileChooser;
import org.fife.ui.rtextfilechooser.filters.HTMLFileFilter;
import org.fife.ui.rsyntaxtextarea.Token;


/**
 * Action used by an <code>RText</code> to save a copy
 * of the current document as HTML.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SaveAsWebPageAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	SaveAsWebPageAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "SaveAsWebPageAction");
		setIcon(icon);
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		RText owner = getApplication();

		RTextFileChooser chooser = new RTextFileChooser();
		chooser.setCustomTitle(owner.getString("SaveAsWebPage"));
		chooser.addChoosableFileFilter(new HTMLFileFilter());
		chooser.setEncoding("UTF-8");

		RTextEditorPane editor = owner.getMainView().getCurrentTextArea();
		String htmlFileName = editor.getFileFullPath();
		int extensionStart = htmlFileName.lastIndexOf('.');
		if (extensionStart!=-1) {
			htmlFileName = htmlFileName.substring(0,extensionStart) + ".html";
		}
		else {
			htmlFileName += ".html";
		}
		chooser.setSelectedFile(new File(htmlFileName));
		chooser.setComponentOrientation(owner.getComponentOrientation());

		if (chooser.showSaveDialog(owner)==JFileChooser.APPROVE_OPTION) {

			File chosenFile = chooser.getSelectedFile();
			String chosenFilePath = chosenFile.getAbsolutePath();

			// Prompt before overwriting file if it already exists.
			if (chosenFile.exists()) {
				String temp = owner.getString("FileAlreadyExists",
									chosenFile.getAbsolutePath());
				if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(
						owner, temp,
						owner.getString("ConfDialogTitle"),
						JOptionPane.YES_NO_OPTION)) {
					return;
						}
			}

			// Ensure that it has an HTML extension.
			if (!chosenFilePath.matches("[^.]*\\.html?"))
				chosenFilePath = chosenFilePath + ".html";

			// Try and write output to the current filename.
			try {
				saveAs(chosenFilePath);
			} catch (IOException ioe) {
				String desc = owner.getString("ErrorWritingFile",
									chosenFilePath, ioe.getMessage());
				JOptionPane.showMessageDialog(owner, desc,
								owner.getString("ErrorDialogTitle"),
								JOptionPane.ERROR_MESSAGE);
				owner.setMessages(null, "ERROR:  Could not save file!");
				ioe.printStackTrace();
			}

		}

	}


	private void saveAs(String path) throws IOException {

		String[] styles = new String[Token.DEFAULT_NUM_TOKEN_TYPES];
		StringBuilder temp = new StringBuilder();
		StringBuilder sb = new StringBuilder();

		PrintWriter out = new PrintWriter(new BufferedWriter(
			new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)));
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
			"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
		out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		out.println("<head>");
		out.println("<!-- Generated by RText -->");
		out.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />");
		out.println("<title>" + path + "</title>");

		RText rtext = getApplication();
		RTextEditorPane textArea = rtext.getMainView().getCurrentTextArea();
		int lineCount = textArea.getLineCount();
		for (int i=0; i<lineCount; i++) {
			Token token = textArea.getTokenListForLine(i);
			while (token!=null && token.isPaintable()) {
				if (styles[token.getType()]==null) {
					temp.setLength(0);
					temp.append(".s").append(token.getType()).append(" {\n");
					Font font = textArea.getFontForTokenType(token.getType());
					if (font.isBold()) {
						temp.append("font-weight: bold;\n");
					}
					if (font.isItalic()) {
						temp.append("font-style: italic;\n");
					}
					Color c = textArea.getForegroundForToken(token);
					temp.append("color: ").append(UIUtil.getHTMLFormatForColor(c)).append(";\n");
					temp.append("}");
					styles[token.getType()] = temp.toString();
				}
				sb.append("<span class=\"s").append(token.getType()).append("\">");
				sb.append(HtmlUtil.escapeForHtml(token.getLexeme(), "\n", true));
				sb.append("</span>");
				token = token.getNextToken();
			}
			sb.append('\n');
		}

		// Print CSS styles
		out.println("<style type=\"text/css\">");
		for (String style : styles) {
			if (style != null) {
				out.println(style);
			}
		}
		out.println("</style>");

		// Print the body
		out.println("</head>");
		out.println("<body>\n<pre>");
		out.println(sb);
		out.println("</pre>\n</body>\n</html>");

		out.close();

	}


}
