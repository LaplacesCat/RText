/*
 * 11/05/2009
 *
 * ToolOptionPanel.java - Option panel for managing external tools.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.fife.rtext.KeyStrokeCellRenderer;
import org.fife.rtext.plugins.tools.NewToolDialog;
import org.fife.rtext.plugins.tools.Tool;
import org.fife.ui.UIUtil;
import org.fife.ui.app.GUIApplicationConstants;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.modifiabletable.AbstractRowHandler;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.ModifiableTableChangeEvent;
import org.fife.ui.modifiabletable.ModifiableTableListener;


/**
 * Options panel for managing external tools.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ToolOptionPanel extends PluginOptionsDialogPanel
			implements ModifiableTableListener, GUIApplicationConstants {

	static final String MSG = "org.fife.rtext.plugins.tools.OptionPanel";

	private Listener listener;
	private JCheckBox visibleCB;
	private JComboBox locationCombo;
	private DefaultTableModel model;
	private ModifiableTable toolTable;

	private static final String PROPERTY		= "property";
	static final String TITLE_KEY				= "Title";


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin.
	 */
	public ToolOptionPanel(ToolPlugin plugin) {

		super(plugin);
		listener = new Listener();

		ResourceBundle msg = ResourceBundle.getBundle(MSG);
		setName(msg.getString(TITLE_KEY));
		ResourceBundle gpb = ResourceBundle.getBundle(
				"org/fife/ui/app/GUIPlugin");

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		JPanel cp = new JPanel(new BorderLayout());
		cp.setBorder(new OptionPanelBorder(msg.getString("Tools")));
		add(cp);

		Box topPanel = Box.createVerticalBox();
		cp.add(topPanel, BorderLayout.NORTH);

		// A check box toggling the plugin's visibility.
		JPanel temp = new JPanel(new BorderLayout());
		visibleCB = new JCheckBox(gpb.getString("Visible"));
		visibleCB.addActionListener(listener);
		temp.add(visibleCB, BorderLayout.LINE_START);
		temp.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		topPanel.add(temp);
		topPanel.add(Box.createVerticalStrut(5));

		// A combo in which to select the dockable window's placement.
		Box locationPanel = createHorizontalBox();
		locationCombo = new JComboBox();
		UIUtil.fixComboOrientation(locationCombo);
		locationCombo.addItem(gpb.getString("Location.top"));
		locationCombo.addItem(gpb.getString("Location.left"));
		locationCombo.addItem(gpb.getString("Location.bottom"));
		locationCombo.addItem(gpb.getString("Location.right"));
		locationCombo.addItem(gpb.getString("Location.floating"));
		locationCombo.addItemListener(listener);
		JLabel locLabel = new JLabel(gpb.getString("Location.title"));
		locLabel.setLabelFor(locationCombo);
		locationPanel.add(locLabel);
		locationPanel.add(Box.createHorizontalStrut(5));
		locationPanel.add(locationCombo);
		locationPanel.add(Box.createHorizontalGlue());
		topPanel.add(locationPanel);
		topPanel.add(Box.createVerticalStrut(20));
		topPanel.add(Box.createVerticalGlue());

		model = new DefaultTableModel(new String[] {
				msg.getString("TableHeader.Tool"),
				msg.getString("TableHeader.Shortcut"),
				msg.getString("TableHeader.Description") }, 0);

		toolTable = new ModifiableTable(model, ModifiableTable.BOTTOM,
										ModifiableTable.ADD_REMOVE_MODIFY);
		toolTable.addModifiableTableListener(this);
		toolTable.setRowHandler(new ToolTableRowHandler());
		JTable table = toolTable.getTable();
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(0).setCellRenderer(new ToolCellRenderer());
		tcm.getColumn(1).setCellRenderer(new KeyStrokeCellRenderer());
		table.setPreferredScrollableViewportSize(new Dimension(300,300));
		cp.add(toolTable);

		applyComponentOrientation(orientation);

	}


	/**
	 * {@inheritDoc}
	 */
	protected void doApplyImpl(Frame owner) {

		ToolPlugin p = (ToolPlugin)getPlugin();
		DockableWindow wind = p.getDockableWindow();
		wind.setActive(visibleCB.isSelected());
		wind.setPosition(getToolOutputPanelPlacement());

		ToolManager tm = ToolManager.get();
		tm.clearTools();
		for (int i=0; i<model.getRowCount(); i++) {
			Tool tool = (Tool)model.getValueAt(i, 0);
			tm.addTool(tool);
		}

	}


	/**
	 * {@inheritDoc}
	 */
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * Returns the selected placement for the tool output panel.
	 *
	 * @return The selected placement.
	 * @see #setToolOutputPanelPlacement(int)
	 */
	public int getToolOutputPanelPlacement() {
		return locationCombo.getSelectedIndex();
	}


	/**
	 * {@inheritDoc}
	 */
	public JComponent getTopJComponent() {
		return toolTable;
	}


	/**
	 * {@inheritDoc}
	 */
	public void modifiableTableChanged(ModifiableTableChangeEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(PROPERTY, null, new Integer(e.getRow()));
	}


	/**
	 * Sets the tool output panel placement placement displayed by this panel.
	 *
	 * @param placement The placement displayed; one of
	 *        <code>GUIApplication.LEFT</code>, <code>TOP</code>,
	 *        <code>RIGHT</code>, <code>BOTTOM</code> or <code>FLOATING</code>.
	 * @see #getToolOutputPanelPlacement()
	 */
	private void setToolOutputPanelPlacement(int placement) {
		if (!DockableWindow.isValidPosition(placement))
			placement = LEFT;
		locationCombo.setSelectedIndex(placement);
	}


	/**
	 * {@inheritDoc}
	 */
	protected void setValuesImpl(Frame owner) {

		ToolPlugin p = (ToolPlugin)getPlugin();
		DockableWindow wind = p.getDockableWindow();
		visibleCB.setSelected(wind.isActive());
		setToolOutputPanelPlacement(wind.getPosition());

		ToolManager tm = ToolManager.get();
		model.setRowCount(0);
		for (Iterator i=tm.getToolIterator(); i.hasNext(); ) {
			Tool tool = (Tool)i.next();
			model.addRow(new Object[] { tool,
					KeyStroke.getKeyStroke(tool.getAccelerator()),
					tool.getDescription()
			});
		}

	}


	/**
	 * Listens for events in this panel.
	 */
	private class Listener implements ActionListener, ItemListener {

		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (source==visibleCB) {
				hasUnsavedChanges = true;
				boolean visible = visibleCB.isSelected();
				firePropertyChange(PROPERTY, !visible, visible);
			}

		}

		public void itemStateChanged(ItemEvent e) {
			if (e.getSource()==locationCombo &&
					e.getStateChange()==ItemEvent.SELECTED) {
				hasUnsavedChanges = true;
				int placement = getToolOutputPanelPlacement();
				firePropertyChange(PROPERTY, -1, placement);
			}
		}

	}


	/**
	 * Renderer for tools in the JTable.
	 */
	private static class ToolCellRenderer extends DefaultTableCellRenderer {

		public Component getTableCellRendererComponent(JTable table,
								Object value, boolean isSelected,
								boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
										 hasFocus, row, column);
			setText(((Tool)value).getName());
			setComponentOrientation(table.getComponentOrientation());
			return this;
		}

	}


	/**
	 * Handles modification of tool table values.
	 */
	private class ToolTableRowHandler extends AbstractRowHandler {

		public Object[] getNewRowInfo(Object[] oldData) {
			NewToolDialog toolDialog = new NewToolDialog(getOptionsDialog());
			Tool old = null;
			if (oldData!=null) {
				old = (Tool)oldData[0];
				toolDialog.setTool(old);
			}
			toolDialog.setLocationRelativeTo(ToolOptionPanel.this);
			toolDialog.setVisible(true);
			Tool tool = toolDialog.getTool();
			if (tool!=null) {
				return new Object[] { tool,
						KeyStroke.getKeyStroke(tool.getAccelerator()),
						tool.getDescription() };
			}
			return null;
		}

	}


}