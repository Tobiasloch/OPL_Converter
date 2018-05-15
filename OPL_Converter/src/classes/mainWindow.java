package classes;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import javax.swing.JButton;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.awt.event.ActionEvent;
import java.awt.Color;

import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

@SuppressWarnings("serial")
public class mainWindow extends JFrame {

	/* errorcodes 1 - 99
	 * 0: no error
	 * 
	 * 1: outputFile error
	 * 2: already running process
	 * 
	 * */
	
	public final String DEFAULT_OUTPUT_NAME = "output.txt";
	
	private JPanel contentPane;
	private static JTextField outputField;
	
	private JButton startButton;
	
	private JCheckBox outputinInputFolder;
	
	private JTextArea consoleArea;
	private JScrollPane consoleSP;
	private Console console;
	
	private ArrayList<Pattern> separators = new ArrayList<Pattern>();
	private ArrayList<Pattern> types = new ArrayList<Pattern>();
	
	private JTextField inputField;
	private JTable table;
	private DefaultTableModel tableModel;
	
	OplHeader header = new OplHeader();
	convertOPL oplConverter = new convertOPL();
	
	private JFrame mainFrame = this;
	convertOplThread thread = new convertOplThread(oplConverter, mainFrame);
	
	public mainWindow() {
		setTitle("Dateitrennsystem");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		setMinimumSize(new Dimension(500, 500));
		setLocationRelativeTo(null);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		DefaultListModel<String> listModel_2 = new DefaultListModel<String>();
		listModel_2.addElement("00:00:00");
		separators.add(Pattern.compile(listModel_2.get(0)));
		
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		listModel.addElement("\\d\\d:\\d\\d:\\d\\d");
		types.add(Pattern.compile(listModel.get(0)));
		
		JPanel inputOutputArea = new JPanel();
		splitPane.setLeftComponent(inputOutputArea);
		inputOutputArea.setLayout(new BorderLayout(0, 0));
		
		JSplitPane inputOutputSplit = new JSplitPane();
		inputOutputSplit.setResizeWeight(0.5);
		inputOutputArea.add(inputOutputSplit, BorderLayout.CENTER);
		
		JPanel outputPanel = new JPanel();
		inputOutputSplit.setRightComponent(outputPanel);
		outputPanel.setBackground(Color.WHITE);
		outputPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel mainOutputPanel = new JPanel();
		mainOutputPanel.setBackground(Color.WHITE);
		outputPanel.add(mainOutputPanel, BorderLayout.NORTH);
		mainOutputPanel.setLayout(new BorderLayout(0, 0));
		
		JButton button = new JButton("durchsuchen...");
		mainOutputPanel.add(button, BorderLayout.EAST);
		
		JLabel lblOutput = new JLabel(" Output:");
		mainOutputPanel.add(lblOutput, BorderLayout.NORTH);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				File f = new File(outputField.getText());
				if (f.exists()) fc.setCurrentDirectory(f);
				
				fc.showSaveDialog(null);
				if (fc.getSelectedFile() != null) outputField.setText(fc.getSelectedFile().getPath());
			}
		});
		
		JPanel inputPanel = new JPanel();
		inputOutputSplit.setLeftComponent(inputPanel);
		inputPanel.setBackground(Color.WHITE);
		inputPanel.setLayout(new BorderLayout(0, 0));
		
		outputField = new JTextField();
		mainOutputPanel.add(outputField, BorderLayout.CENTER);
		
		outputinInputFolder = new JCheckBox("selber Ordner wie ausgew\u00E4hlte Datei");
		outputinInputFolder.setBackground(Color.WHITE);
		outputinInputFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (outputinInputFolder.isSelected()) {
					File f = new File(inputField.getText());
					
					if (f.exists()) outputField.setText(f.getParentFile().getAbsolutePath() + "\\" + DEFAULT_OUTPUT_NAME);
				}
			}
		});
		outputPanel.add(outputinInputFolder, BorderLayout.SOUTH);
		
		JLabel lblInput = new JLabel(" Input:");
		inputPanel.add(lblInput, BorderLayout.NORTH);
		
		JButton btnDurchsuchen = new JButton("Durchsuchen...");
		btnDurchsuchen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				File f = new File(inputField.getText());
				if (f.exists()) fc.setCurrentDirectory(f);
				
				fc.showSaveDialog(null);
				if (fc.getSelectedFile() != null) inputField.setText(fc.getSelectedFile().getPath());
			}
		});
		inputPanel.add(btnDurchsuchen, BorderLayout.EAST);
		
		inputField = new JTextField("C:\\Users\\T.loch\\Desktop\\OPL Dateien Konvertieren\\22_325_2017.09.27_07-00_2017.09.27_13-00.opl");
		inputPanel.add(inputField, BorderLayout.CENTER);
		inputField.setColumns(10);
		
		JButton btnTabelleRendern = new JButton("Tabelle rendern");
		btnTabelleRendern.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File file = new File(inputField.getText());
				DefaultTableModel tableModel = new DefaultTableModel();
				table.setModel(tableModel);
				
				if (file.exists()) {
					header = new OplHeader(file, console);
					
					header.extractHeaderInformation();
					for (OplType item : header.types) {
						tableModel.addColumn(item.getType());
						
						if (item.getElements().size() > 0) {
							int activeCol = tableModel.getColumnCount()-1;
							
							int activeRow = 0;
							for (OplTypeElement elem : item.getElements()) {
								String text = elem.getName() + "(" + elem.getId() + ")";
								
								if (activeRow >= tableModel.getRowCount()) {
									String[] s = new String[tableModel.getColumnCount()];
									s[activeCol] = text;
									
									tableModel.addRow(s);
								} else {
									tableModel.setValueAt(text, activeRow, activeCol);
								}
								
								activeRow++;
							}
						}
					}
					table.repaint();
				} else {
					JOptionPane.showConfirmDialog(mainFrame, "Die angegebene Datei existiert nicht!", "Fehler!", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		});
		inputPanel.add(btnTabelleRendern, BorderLayout.SOUTH);
		/*inputField.getDocument().addDocumentListener(new DocumentListener() { Needs to be fixed
			@Override
			public void changedUpdate(DocumentEvent arg0) {changed();}
			@Override
			public void insertUpdate(DocumentEvent arg0) {changed();}
			@Override
			public void removeUpdate(DocumentEvent arg0) {changed();}
			
			private void changed() {
				if (outputinInputFolder.isSelected()) {
					File f = new File(inputField.getText());
					System.out.println(f.getParentFile().getName());
					if (f.exists() && f.getParentFile().exists()) outputField.setText(f.getParentFile().getAbsolutePath());
				}
			}
		});*/
		
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(1.0);
		splitPane_1.setContinuousLayout(true);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setRightComponent(splitPane_1);
		
		JPanel settingsArea = new JPanel();
		splitPane_1.setLeftComponent(settingsArea);
		settingsArea.setLayout(new BorderLayout(5, 5));
		
		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setContinuousLayout(true);
		splitPane_2.setResizeWeight(1.0);
		splitPane_2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		settingsArea.add(splitPane_2, BorderLayout.CENTER);
		
		JPanel tableRenderer = new JPanel();
		splitPane_2.setLeftComponent(tableRenderer);
		tableRenderer.setLayout(new BorderLayout(0, 0));
		
		tableModel = new DefaultTableModel();
		table = new JTable(tableModel);
		table.setCellSelectionEnabled(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setSurrendersFocusOnKeystroke(true);
		table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {}
			@Override
			public void columnRemoved(TableColumnModelEvent e) {}
			
			@Override
			public void columnMoved(TableColumnModelEvent e) {
				if (e.getToIndex() != e.getFromIndex()) {
					Collections.swap(header.types, e.getFromIndex(), e.getToIndex());
				}
			}
			
			@Override
			public void columnMarginChanged(ChangeEvent e) {}
			
			@Override
			public void columnAdded(TableColumnModelEvent e) {}
		});
		
		table.setColumnSelectionAllowed(true);
		table.setFillsViewportHeight(true);
		
		JScrollPane tableRendererScroller = new JScrollPane(table);
		tableRenderer.add(tableRendererScroller, BorderLayout.CENTER);
		
		JPanel settingsPanel = new JPanel();
		splitPane_2.setRightComponent(settingsPanel);
		settingsPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblEinstellungen = new JLabel("Einstellungen:");
		settingsPanel.add(lblEinstellungen, BorderLayout.NORTH);
		
		JPanel startArea = new JPanel();
		
		splitPane_1.setRightComponent(startArea);
		startArea.setLayout(new BorderLayout(0, 0));
		
		consoleArea = new JTextArea();
		consoleArea.setEditable(false);
		consoleArea.setRows(4);
		
		consoleSP = new JScrollPane(consoleArea);
		startArea.add(consoleSP, BorderLayout.CENTER);
		
		console = new Console(consoleArea, consoleSP);
		
		startArea.setMinimumSize(new Dimension(mainFrame.getWidth(), consoleArea.getPreferredSize().height));
		startArea.setMaximumSize(new Dimension(mainFrame.getWidth(), 200));
		startArea.setPreferredSize(new Dimension(mainFrame.getWidth(), 100));
		
		JPanel buttonPanel = new JPanel();
		startArea.add(buttonPanel, BorderLayout.EAST);
		buttonPanel.setLayout(new BorderLayout(0, 0));
		
		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!thread.isAlive()) {
					// checks if the header is read correctly
					if (header.checkErrorStatus() != 0) {
						console.printConsoleErrorLine("Die Headerdatei konnte nicht richtig gelesen werden!", header.checkErrorStatus());
						JOptionPane.showMessageDialog(mainFrame, "Die Headerdatei konnte nicht richtig gelesen werden! errorcode:" + header.checkErrorStatus()
								,"Fehler!"
								, JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					File outputFile = new File(outputField.getText());
					
					// checks if the output file is correct
					if (outputFile.getParent() == null) {
						console.printConsoleErrorLine("Es gab ein Problem mit der output Datei!", 1);
						JOptionPane.showMessageDialog(mainFrame, "Es gab ein Problem mit der output Datei! errorcode:1"
								,"Fehler!"
								, JOptionPane.ERROR_MESSAGE);
						return;
					}

					if (outputFile.exists()) {
						int option = JOptionPane.showConfirmDialog(mainFrame, "Die output Datei existiert bereits, soll sie überschrieben werden?"
								,"Fehler!"
								, JOptionPane.YES_NO_OPTION);
						
						if (option == JOptionPane.YES_OPTION) {
							outputFile.delete();
							outputFile = new File (outputField.getText());
							console.printConsoleLine("die Datei:\"" + outputFile + "\" wurde überschrieben!");
						} else {
							console.printConsole("Der Vorgang wurde abgebrochen!");
							return;
						}
					}
					
					oplConverter = new convertOPL(header, outputFile, console);
					oplConverter.setMainFrame(mainFrame);
					thread = new convertOplThread(oplConverter, mainFrame);
					thread.setConsole(console);

					thread.start();
				} else {
					console.printConsoleErrorLine("Es wird bereits eine Datei exportiert!", 2);
					JOptionPane.showMessageDialog(mainFrame, "Es wird bereits eine Datei exportiert! errorcode:" + 2
							, "Fehler!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		buttonPanel.add(startButton);
		
		JButton clearConsole = new JButton ("leere Konsole");
		buttonPanel.add(clearConsole, BorderLayout.SOUTH);
		clearConsole.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				console.clearConsole();
			}
		});
	}
	
	public static File[] convertArrayListToArray (ArrayList<File> f) {
		File[] files = new File[f.size()];
		
		for (int i = 0; i < files.length; i++) files[i] = f.get(i);
		
		return files;
	}
	
	public TableColumn[] getColumnsInView(JTable table) {
	    TableColumn[] result = new TableColumn[table.getColumnCount()];

	    // Use an enumeration
	    Enumeration e = table.getColumnModel().getColumns();
	    for (int i = 0; e.hasMoreElements(); i++) {
	      result[i] = (TableColumn) e.nextElement();
	    }

	    return result;
	  }
}
