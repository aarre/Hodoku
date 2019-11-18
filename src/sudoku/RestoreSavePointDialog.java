/*
 * Copyright (C) 2008-12  Bernhard Hobiger
 *
 * This file is part of HoDoKu.
 *
 * HoDoKu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HoDoKu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HoDoKu. If not, see <http://www.gnu.org/licenses/>.
 */
package sudoku;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author hobiwan
 */
public class RestoreSavePointDialog extends javax.swing.JDialog {
	private static final long serialVersionUID = 1L;

	private MainFrame mainFrame;
	private List<GuiState> savePoints;
	private String[][] data;
	private String[] columnNames = new String[] {
			ResourceBundle.getBundle("intl/RestoreSavePoint").getString("RestoreSavePointDialog.col1"),
			ResourceBundle.getBundle("intl/RestoreSavePoint").getString("RestoreSavePointDialog.col2") };
	private boolean okPressed = false;

	/**
	 * Creates new form HistoryDialog
	 * 
	 * @param parent
	 * @param modal
	 */
	public RestoreSavePointDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();

		mainFrame = (MainFrame) parent;
		savePoints = mainFrame.getSavePoints();

		initTable();

		getRootPane().setDefaultButton(okButton);

		KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", escapeAction);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		jScrollPane1 = new javax.swing.JScrollPane();
		savePointTable = new javax.swing.JTable();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("intl/RestoreSavePoint"); // NOI18N
		setTitle(bundle.getString("title")); // NOI18N

		okButton.setMnemonic(java.util.ResourceBundle.getBundle("intl/RestoreSavePoint")
				.getString("RestoreSavePointDialog.okButton.mnemonic").charAt(0));
		okButton.setText(bundle.getString("RestoreSavePointDialog.okButton.text")); // NOI18N
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});

		cancelButton.setMnemonic(java.util.ResourceBundle.getBundle("intl/RestoreSavePoint")
				.getString("RestoreSavePointDialog.cancelButton.mnemonic").charAt(0));
		cancelButton.setText(bundle.getString("RestoreSavePointDialog.cancelButton.text")); // NOI18N
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		savePointTable
				.setModel(new javax.swing.table.DefaultTableModel(
						new Object[][] { { null, null, null, null }, { null, null, null, null },
								{ null, null, null, null }, { null, null, null, null } },
						new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
		savePointTable.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				savePointTableMouseClicked(evt);
			}
		});
		jScrollPane1.setViewportView(savePointTable);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										layout.createSequentialGroup().addComponent(okButton)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(cancelButton))
								.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
						.addContainerGap()));

		layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] { cancelButton, okButton });

		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				javax.swing.GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup().addContainerGap()
						.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 154,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
								javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(okButton).addComponent(cancelButton))
						.addContainerGap()));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_okButtonActionPerformed
		if (savePointTable.getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(this,
					ResourceBundle.getBundle("intl/RestoreSavePoint").getString("RestoreSavePointDialog.error.message"),
					ResourceBundle.getBundle("intl/RestoreSavePoint").getString("RestoreSavePointDialog.error.title"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		mainFrame.setState(savePoints.get(savePointTable.getSelectedRow()));
		okPressed = true;
		setVisible(false);
	}// GEN-LAST:event_okButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cancelButtonActionPerformed
		okPressed = false;
		setVisible(false);
	}// GEN-LAST:event_cancelButtonActionPerformed

	private void savePointTableMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_savePointTableMouseClicked
		if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2) {
			okButtonActionPerformed(null);
		}
	}// GEN-LAST:event_savePointTableMouseClicked

	/**
	 * Creates an uneditable DefaultTableModel and fills it with data from
	 * {@link MainFrame}.
	 */
	private void initTable() {
		data = new String[savePoints.size()][2];
		DateFormat tf = DateFormat.getTimeInstance();
		for (int i = 0; i < savePoints.size(); i++) {
			GuiState state = savePoints.get(i);
			data[i][0] = tf.format(state.getTimestamp());
			data[i][1] = state.getName();
		}
		savePointTable.setModel(new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		});
		savePointTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				tableSelectionChanged(e);
			}
		});
		// set first column wider than the others
		TableCellRenderer renderer = new MyTableCellRenderer();
		TableColumnModel cm = savePointTable.getColumnModel();
		for (int i = 0; i < cm.getColumnCount(); i++) {
			TableColumn column = cm.getColumn(i);
			if (i == 0) {
				// first column is smaller
				column.setPreferredWidth(50);
				// column.
			} else {
				column.setPreferredWidth(150);
			}
			column.setCellRenderer(renderer);
		}
		// column headers centered
		renderer = savePointTable.getTableHeader().getDefaultRenderer();
		JLabel label = (JLabel) renderer;
		label.setHorizontalAlignment(JLabel.CENTER);
	}

	/**
	 * Selection listener for {@link #historyTable}. Sets the puzzle in
	 * {@link MainFrame}.
	 * 
	 * @param e
	 */
	private void tableSelectionChanged(ListSelectionEvent e) {
		// System.out.println(e.getFirstIndex() + "/" + e.getLastIndex() + "/" +
		// e.getValueIsAdjusting() + "/" + historyTable.getSelectedRow());
		if (e.getValueIsAdjusting() == false) {
			// row is finally selected
			mainFrame.setState(savePoints.get(savePointTable.getSelectedRow()));
		}
	}

	/**
	 * @return the okPressed
	 */
	public boolean isOkPressed() {
		return okPressed;
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				RestoreSavePointDialog dialog = new RestoreSavePointDialog(new javax.swing.JFrame(), true);
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {

					@Override
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}

	class MyTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			MyTableCellRenderer comp = (MyTableCellRenderer) super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
			comp.setHorizontalAlignment(SwingConstants.CENTER);
			return comp;
		}
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton cancelButton;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JButton okButton;
	private javax.swing.JTable savePointTable;
	// End of variables declaration//GEN-END:variables
}
