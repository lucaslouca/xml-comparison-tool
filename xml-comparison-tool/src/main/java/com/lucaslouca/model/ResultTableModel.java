package com.lucaslouca.model;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class ResultTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String[] columnNames = { "V12 Ticket", "Match", "Matching V14 Ticket" };
	private Vector<String[]> rows_data = new Vector<String[]>();

	@Override
	public int getRowCount() {
		return rows_data == null ? 0 : rows_data.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return rows_data.elementAt(rowIndex)[columnIndex];
	}

	public void addRow(String[] row) {
		rows_data.add(row);
		fireTableDataChanged();
	}

	public void clear() {
		rows_data.removeAllElements();
		fireTableDataChanged();
	}
}
