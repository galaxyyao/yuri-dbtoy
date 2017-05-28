package com.galaxyyao.yuri_dbtoy.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.galaxyyao.yuri_dbtoy.BackgroundWorker;

public class MainPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -4693833475415137547L;

	static private final String NEW_LINE = "\n";
	JButton openButton;
	JTextArea openFileLog;
	JFileChooser fileChooser;

	public MainPanel() {
		super(new BorderLayout());
		
		openFileLog = new JTextArea(5, 20);
		openFileLog.setMargin(new Insets(5, 5, 5, 5));
		openFileLog.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(openFileLog);

		fileChooser = new JFileChooser();

		openButton = new JButton("Open a File...");
		openButton.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(openButton);

		add(buttonPanel, BorderLayout.PAGE_START);
		add(logScrollPane, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openButton) {
			int returnVal = fileChooser.showOpenDialog(MainPanel.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				openFileLog.append("Opening: " + file.getName() + "." + NEW_LINE);
				String result = doBackgroundWork(file.getPath());
				openFileLog.append("Create SQL generated: " + result + "." + NEW_LINE);
			} else {
				openFileLog.append("Open command cancelled by user." + NEW_LINE);
			}
			openFileLog.setCaretPosition(openFileLog.getDocument().getLength());
		}
	}
	
	private String doBackgroundWork(String path) {
		Callable<String> task = () -> {
		    BackgroundWorker worker=new BackgroundWorker(path);
			return worker.readExcelAndGenerateSql();
		};
		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Future<String> future = executor.submit(task);

		String result;
		try {
			result = future.get();
			return result;
		} catch (InterruptedException | ExecutionException e) {
			return null;
		}
	}

	public static void createAndShowGUI() {
		JFrame frame = new JFrame("Yuri DB Toy - To my dearest Yuri");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		MainPanel mainPanel = new MainPanel();
		mainPanel.setPreferredSize(new Dimension(600, 400));
		frame.add(mainPanel);

		frame.pack();
		//position frame in the center of screen
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
