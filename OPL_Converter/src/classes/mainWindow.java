package classes;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;

import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class mainWindow extends JFrame {

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
		
		SpinnerModel model = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 100);
		
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
		outputinInputFolder.setSelected(true);
		outputinInputFolder.setBackground(Color.WHITE);
		outputinInputFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (outputinInputFolder.isSelected()) {
					File f = new File(inputField.getText());
					
					if (f.exists()) outputField.setText(f.getParentFile().getAbsolutePath());
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
		
		inputField = new JTextField();
		inputPanel.add(inputField, BorderLayout.CENTER);
		inputField.setColumns(10);
		
		JButton btnTabelleRendern = new JButton("Tabelle rendern");
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
		
		table.setColumnSelectionAllowed(true);
		table.setFillsViewportHeight(true);
		tableRenderer.add(table, BorderLayout.CENTER);
		
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
		consoleArea.setRows(3);
		
		consoleSP = new JScrollPane(consoleArea);
		startArea.add(consoleSP, BorderLayout.CENTER);
		
		console = new Console(consoleArea, consoleSP);
		
		//startArea.setMinimumSize(new Dimension(mainFrame.getWidth(), console.getPreferredSize().height));
		//startArea.setMaximumSize(new Dimension(mainFrame.getWidth(), 200));
		//startArea.setPreferredSize(new Dimension(mainFrame.getWidth(), 100));
		
		JPanel buttonPanel = new JPanel();
		startArea.add(buttonPanel, BorderLayout.EAST);
		buttonPanel.setLayout(new BorderLayout(0, 0));
		
		startButton = new JButton("Start");
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
	
	private static ArrayList<File> getFilesFromPath(String[] filePaths) {
		ArrayList<File> files = new ArrayList<File>();
		
		for (int i = 0; i < filePaths.length; i++) {
			files.add(new File(filePaths[i]));
		}
		
		return files;
	}
	
	private static String[] getListElements (ListModel<String> model) {
		String[] str = new String[model.getSize()];
		
		for (int i = 0; i < model.getSize(); i++) {
			str[i] = model.getElementAt(i);
		}
		
		return str;
	}
	
	private void enableAllChildren(Container c, boolean value) {
		for (Component comp : getAllComponents(c)) comp.setEnabled(value);
	}
	
	private static List<Component> getAllComponents(Container c) {
        Component[] comps = c.getComponents();
        List<Component> compList = new ArrayList<Component>();
        for (Component comp : comps) {
            compList.add(comp);
            if (comp instanceof Container)
                compList.addAll(getAllComponents((Container) comp));
        }
        return compList;
}
}
