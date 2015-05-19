package com.accenture.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import com.accenture.model.AbstractTicketModel;
import com.accenture.model.ResultTableModel;
import com.accenture.parser.AbstractTicketParser;
import com.accenture.parser.V12Parser;
import com.accenture.parser.V14Parser;

public class TicketAppController extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ResultTableModel resultTableModel;
	private JTable resultTable;
	private final int STATUS_COLUMN_INDEX = 1;
	private final String STATUS_OK = "YES";
	private final String STATUS_NOMATCH = "NO";

	private JButton fileChooserButtonV12;
	private JButton fileChooserButtonV14;
	private JButton verifyButton;

	private JLabel v12PathLabel;
	private JLabel v14PathLabel;

	private JProgressBar progressBar;

	AbstractTicketParser v12Parser;
	AbstractTicketParser v14Parser;

	private enum TicketSystem {
		V12, V14
	}

	/**
	 * Constructor
	 */
	public TicketAppController() {
		initComponent();
	}

	/**
	 * Add GUI Components to the Panel
	 */
	public void initComponent() {
		setLayout(new GridBagLayout());
		GridBagConstraints gradBagConstraints = new GridBagConstraints();
		gradBagConstraints.anchor = GridBagConstraints.NORTHWEST;

		// V12 FileChooser Button
		gradBagConstraints.gridx = 0;
		gradBagConstraints.gridy = 0;
		fileChooserButtonV12 = new JButton("Choose V12 folder...");
		fileChooserButtonV12.addActionListener(this);
		add(fileChooserButtonV12, gradBagConstraints);

		// V12 Path Label
		gradBagConstraints.gridx = 1;
		gradBagConstraints.weightx = 0;
		v12PathLabel = new JLabel("No path selected for v12...");
		add(v12PathLabel, gradBagConstraints);

		// V14 FileChooser Button
		gradBagConstraints.weightx = 0.5;
		gradBagConstraints.weighty = 0;
		gradBagConstraints.gridx = 0;
		gradBagConstraints.gridy = 1;
		fileChooserButtonV14 = new JButton("Choose V14 folder...");
		fileChooserButtonV14.addActionListener(this);
		add(fileChooserButtonV14, gradBagConstraints);

		// V14 Path Label
		gradBagConstraints.gridx = 1;
		gradBagConstraints.gridy = 1;
		gradBagConstraints.weightx = 0;
		v14PathLabel = new JLabel("No path selected for v14...");
		add(v14PathLabel, gradBagConstraints);

		// Result Table
		gradBagConstraints.fill = GridBagConstraints.BOTH;
		gradBagConstraints.gridx = 0;
		gradBagConstraints.gridy = 2;
		gradBagConstraints.gridwidth = 2;
		gradBagConstraints.weighty = 1;
		resultTableModel = new ResultTableModel();
		resultTable = new JTable(resultTableModel);
		resultTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

				String status = (String) table.getModel().getValueAt(row, STATUS_COLUMN_INDEX);
				if (STATUS_OK.equals(status)) {
					setBackground(Color.GREEN);
					setForeground(Color.BLACK);
				} else {
					setBackground(Color.RED);
					setForeground(Color.WHITE);
				}
				return this;
			}
		});

		JScrollPane scrollPane = new JScrollPane(resultTable);
		resultTable.setFillsViewportHeight(true);
		add(scrollPane, gradBagConstraints);

		// Verify Button

		gradBagConstraints.gridx = 0;
		gradBagConstraints.gridy = 3;
		gradBagConstraints.gridwidth = 1;
		gradBagConstraints.weighty = 0;
		verifyButton = new JButton("Verify");
		verifyButton.setEnabled(false);
		verifyButton.addActionListener(this);
		add(verifyButton, gradBagConstraints);

		// ProgressBar
		gradBagConstraints.gridx = 1;
		gradBagConstraints.gridy = 3;
		gradBagConstraints.gridwidth = 1;
		gradBagConstraints.weighty = 0;
		progressBar = new JProgressBar();
		add(progressBar, gradBagConstraints);
	}

	/**
	 * Action listener method implementation for the 2 file chooser buttons and
	 * verify button.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == fileChooserButtonV12) {
			showDirectoryChooser("Choose V12 Directory", TicketSystem.V12);
		} else if (e.getSource() == fileChooserButtonV14) {
			showDirectoryChooser("Choose V14 Directory", TicketSystem.V14);
		} else if (e.getSource() == verifyButton) {
			verify();
		}

	}

	/**
	 * Displays a JFileChooser and wait for input. Based on the provided
	 * TicketSystem we then instantiate an appropriate parser (v12 or v14) and
	 * update the corresponding path labels.
	 * 
	 * @param title
	 *            Title of the file chooser
	 * @param system
	 *            for what system we want to choose a directory
	 */
	public void showDirectoryChooser(String title, TicketSystem system) {
		JFileChooser chooser;
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("C:/sample-v12-v14"));
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String path = chooser.getSelectedFile().getAbsolutePath();
			if (system == TicketSystem.V12) {
				v12Parser = new V12Parser(path);
				v12PathLabel.setText(path);
				toggleVerifyButton();
			} else if (system == TicketSystem.V14) {
				v14Parser = new V14Parser(path);
				v14PathLabel.setText(path);
				toggleVerifyButton();
			}
		}
	}

	/**
	 * Checks if we have a path for V12 and V14. If that is the case then we
	 * enable the verify button. If not, we disable it.
	 */
	public void toggleVerifyButton() {
		boolean v12Ok = (v12Parser != null) && (v12Parser.getTicketDirPath() != null && !v12Parser.getTicketDirPath().isEmpty());
		boolean v14Ok = (v14Parser != null) && (v14Parser.getTicketDirPath() != null && !v14Parser.getTicketDirPath().isEmpty());
		verifyButton.setEnabled(v12Ok && v14Ok);
	}

	/**
	 * Action triggered when the verify button is hit.
	 * 
	 * Here we iterate over all the v12 ticket and search for a matching v14
	 * ticket.
	 */
	private void verify() {
		System.out.println("Verifying...");
		progressBar.setMaximum(v12Parser.getTickets().size());
		progressBar.setValue(0);

		for (AbstractTicketModel v12Ticket : v12Parser.getTickets()) {
			String v12Path = v12Ticket.getAbsoluteXmlPath();
			System.out.println("Verifying V12 ticket '" + v12Path + "'...");
			AbstractTicketModel match = v14Parser.getCorrespondingTicket(v12Ticket);
			if (match != null) {
				resultTableModel.addRow(new String[] { v12Path, STATUS_OK, match.getAbsoluteXmlPath() });
			} else {
				resultTableModel.addRow(new String[] { v12Path, STATUS_NOMATCH, "" });
			}

			progressBar.setValue(progressBar.getValue() + 1);
		}
	}

	/***********************************/
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			JFrame frame = new JFrame("Ticket Comparison");
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent evt) {
					System.exit(0);
				}
			});
			frame.getContentPane().add(new TicketAppController());
			frame.setVisible(true);
			frame.pack();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
