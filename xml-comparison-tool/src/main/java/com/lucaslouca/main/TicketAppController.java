package com.lucaslouca.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import com.lucaslouca.model.AbstractTicketModel;
import com.lucaslouca.model.ResultTableModel;
import com.lucaslouca.parser.AbstractTicketParser;
import com.lucaslouca.parser.IParserListener;
import com.lucaslouca.parser.V12Parser;
import com.lucaslouca.parser.V14Parser;

public class TicketAppController extends JPanel implements ActionListener, IParserListener {
	private static final long serialVersionUID = 1L;
	private final String STATUS_OK = "YES";
	private final String STATUS_NOMATCH = "NO";
	private final String VERIFY_BUTTON_TEXT_START = "Verify";
	private final String VERIFY_BUTTON_TEXT_STOP = "Stop";
	private ResultTableModel resultTableModel;
	private JTable resultTable;
	private final int STATUS_COLUMN_INDEX = 1;
	private JTextArea consoleTextArea;
	private JButton fileChooserButtonV12;
	private JButton fileChooserButtonV14;
	private JButton verifyButton;
	private JLabel v12PathLabel;
	private JLabel v14PathLabel;
	private JProgressBar progressBar;
	private AbstractTicketParser v12Parser;
	private AbstractTicketParser v14Parser;
	private SwingWorker<Boolean, String[]> verificationWorker;
	private boolean isVerificationStarted = false;
	private int matchCountV12;
	private int missCountV12;

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
	private void initComponent() {
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

		// Result TextArea
		consoleTextArea = new JTextArea();
		consoleTextArea.setFont(new Font("monospaced", Font.PLAIN, 12));
		JScrollPane resultScrollArea = new JScrollPane(consoleTextArea);
		gradBagConstraints.gridx = 0;
		gradBagConstraints.gridy = 3;
		gradBagConstraints.weightx = 0;
		gradBagConstraints.weighty = 1;
		gradBagConstraints.gridwidth = 2;
		add(resultScrollArea, gradBagConstraints);
		consoleTextArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		consoleTextArea.setEditable(false);
		add(resultScrollArea, gradBagConstraints);

		// Verify Button
		gradBagConstraints.gridx = 0;
		gradBagConstraints.gridy = 4;
		gradBagConstraints.gridwidth = 1;
		gradBagConstraints.weighty = 0;
		verifyButton = new JButton(VERIFY_BUTTON_TEXT_START);
		verifyButton.setEnabled(false);
		verifyButton.addActionListener(this);
		add(verifyButton, gradBagConstraints);

		// ProgressBar
		gradBagConstraints.gridx = 1;
		gradBagConstraints.gridy = 4;
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
		} else if (e.getSource() == verifyButton && isVerificationStarted == false) {
			startVerification();
		} else if (e.getSource() == verifyButton && isVerificationStarted == true) {
			stopVerification();
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
	private void showDirectoryChooser(String title, TicketSystem system) {
		JFileChooser chooser;
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String path = chooser.getSelectedFile().getAbsolutePath();
			if (system == TicketSystem.V12) {
				v12PathLabel.setText(path);
				v12Parser = new V12Parser();
				v12Parser.setTicketDirPath(path);
				v12Parser.addParserListener(this);
				v12Parser.parse();
				toggleVerifyButton();
			} else if (system == TicketSystem.V14) {
				v14PathLabel.setText(path);
				v14Parser = new V14Parser();
				v14Parser.setTicketDirPath(path);
				v14Parser.addParserListener(this);
				v14Parser.parse();
				toggleVerifyButton();
			}
		}
	}

	/**
	 * Checks if we have a path for V12 and V14. If that is the case then we
	 * enable the verify button. If not, we disable it.
	 */
	private void toggleVerifyButton() {
		boolean v12Ok = (v12Parser != null) && (v12Parser.getTicketDirPath() != null && !v12Parser.getTicketDirPath().isEmpty());
		boolean v14Ok = (v14Parser != null) && (v14Parser.getTicketDirPath() != null && !v14Parser.getTicketDirPath().isEmpty());
		verifyButton.setEnabled(v12Ok && v14Ok);
	}

	/**
	 * Action triggered when the verify button is hit.
	 * 
	 * Here we iterate over all the v12 ticket and search for a matching v14
	 * ticket.
	 * 
	 * @see http
	 *      ://www.javaadvent.com/2012/12/multi-threading-in-java-swing-with.
	 *      html
	 */
	private void startVerification() {
		progressBar.setMaximum(v12Parser.getTickets().size());
		progressBar.setValue(0);
		resultTableModel.clear();
		matchCountV12 = 0;
		missCountV12 = 0;

		verificationWorker = new SwingWorker<Boolean, String[]>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				// Simulate doing something useful.
				for (AbstractTicketModel v12Ticket : v12Parser.getTickets()) {
					String v12Path = v12Ticket.getAbsoluteXmlPath();
					AbstractTicketModel match = v14Parser.getCorrespondingTicket(v12Ticket);

					// The type we pass to publish() is determined
					// by the second template parameter.
					if (match != null) {
						publish(new String[] { v12Path, STATUS_OK, match.getAbsoluteXmlPath() });
					} else {
						publish(new String[] { v12Path, STATUS_NOMATCH, "" });
					}

					if (isCancelled()) {
						return false;
					}
					Thread.sleep(100);
				}

				// Here we can return some object of whatever type
				// we specified for the first template parameter.
				// (in this case we're auto-boxing 'true').
				return true;
			}

			// Can safely update the GUI from this method.
			@Override
			protected void done() {
				try {
					// Retrieve the return value of doInBackground.
					get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (CancellationException e) {
					e.printStackTrace();
				} finally {
					consoleTextArea.setText(consoleTextArea.getText() + matchCountV12 + " V12 tickets were matched\n");
					consoleTextArea.setText(consoleTextArea.getText() + missCountV12 + " V12 tickets could not be matched\n");
					isVerificationStarted = false;
					verifyButton.setText(VERIFY_BUTTON_TEXT_START);
				}
			}

			@Override
			// Can safely update the GUI from this method.
			protected void process(List<String[]> chunks) {
				// Here we receive the values that we publish().
				// They may come grouped in chunks.
				String[] mostRecentValue = chunks.get(chunks.size() - 1);
				resultTableModel.addRow(mostRecentValue);
				progressBar.setValue(progressBar.getValue() + 1);

				if (STATUS_OK.equals(mostRecentValue[STATUS_COLUMN_INDEX])) {
					matchCountV12++;
				} else {
					missCountV12++;
				}
			}

		};

		verificationWorker.execute();
		isVerificationStarted = true;
		verifyButton.setText(VERIFY_BUTTON_TEXT_STOP);
	}

	/**
	 * Cancels current working verification
	 */
	private void stopVerification() {
		verificationWorker.cancel(true);
		isVerificationStarted = false;
		verifyButton.setText(VERIFY_BUTTON_TEXT_START);
	}

	/**
	 * IParserListener implementation. Receive messages through this callback
	 * and print it on the console.
	 */
	@Override
	public void parserMessageNotification(String error) {
		consoleTextArea.setText(consoleTextArea.getText() + error + "\n");
	}

	/*********************************** MAIN ENTRY POINT ***********************************/
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
