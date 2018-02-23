package com.galaxyyao.yuri_dbtoy.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galaxyyao.yuri_dbtoy.BackgroundWorker;
import com.galaxyyao.yuri_dbtoy.domain.DocTable;

public class MainPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -4693833475415137547L;

	private static final Logger logger = LoggerFactory.getLogger(MainPanel.class);

	static private final String NEW_LINE = "\n";
	private JButton openButton;
	private JButton generateCreateTableSqlButton;
	private JTextArea logTextArea;
	private JFileChooser fileChooser;
	private JTable dbTable;
	private static List<DocTable> docTables = null;

	public MainPanel() {
		super(new BorderLayout());

		openButton = new JButton("选择表结构文档");
		openButton.addActionListener(this);
		generateCreateTableSqlButton = new JButton("生成建表语句");
		generateCreateTableSqlButton.addActionListener(this);
		generateCreateTableSqlButton.setEnabled(false);

		logTextArea = new JTextArea(5, 20);
		logTextArea.setMargin(new Insets(5, 5, 5, 5));
		logTextArea.setEditable(false);
		fileChooser = new JFileChooser();

		Object rowData[][] = {};
		Object columnNames[] = { "序号", "表名", "说明", "是否操作" };
		final Class<?>[] columnClass = new Class[] { Integer.class, String.class, String.class, Boolean.class };
		DefaultTableModel tableModel = new DefaultTableModel(rowData, columnNames) {
			private static final long serialVersionUID = -7373407805868967994L;

			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 3) {
					return true;
				}
				return false;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnClass[columnIndex];
			}
		};
		dbTable = new JTable(tableModel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(openButton);
		buttonPanel.add(generateCreateTableSqlButton);
		JScrollPane dbTableScrollPanel = new JScrollPane(dbTable);
		JScrollPane logScrollPanel = new JScrollPane(logTextArea);

		add(buttonPanel, BorderLayout.PAGE_START);
		add(dbTableScrollPanel, BorderLayout.CENTER);
		add(logScrollPanel, BorderLayout.PAGE_END);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openButton) {
			int returnVal = fileChooser.showOpenDialog(MainPanel.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				if (!file.getName().contains(".")) {
					logTextArea.append("Illegal file: " + file.getName() + "." + NEW_LINE);
					return;
				}
				String fileExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
				if (!fileExtension.equals("xlsx") || fileExtension.equals("xls")) {
					logTextArea.append("Illegal file extension: " + fileExtension + "." + NEW_LINE);
					return;
				}

				logTextArea.append("Opening: " + file.getName() + "." + NEW_LINE);
				docTables = doBackgroundWork(file.getPath());
				if (docTables == null) {
					logTextArea.append("Excel document read failed: " + file.getName() + "." + NEW_LINE);
					return;
				}
				logTextArea.append("Excel document read successfully." + NEW_LINE);
				setDbTableModel(docTables);
			} else {
				logTextArea.append("Open command cancelled by user." + NEW_LINE);
				return;
			}
			logTextArea.setCaretPosition(logTextArea.getDocument().getLength());

			generateCreateTableSqlButton.setEnabled(true);
		} else if (e.getSource() == generateCreateTableSqlButton) {
			logger.info("Generate create table SQL.");
		}
	}

	private void setDbTableModel(List<DocTable> docTables) {
		DefaultTableModel model = (DefaultTableModel) dbTable.getModel();
		for (int i = 0; i < docTables.size(); i++) {
			DocTable docTable = docTables.get(i);
			model.addRow(new Object[] { docTable.getTableIndex(), docTable.getTableName(), docTable.getTableDesc(),
					docTable.getIsSelected() });
		}
	}

	private List<DocTable> doBackgroundWork(String path) {
		Callable<List<DocTable>> task = () -> {
			BackgroundWorker worker = new BackgroundWorker(path);
			return worker.readExcelAndGenerateSql();
		};

		ExecutorService executor = Executors.newFixedThreadPool(1);
		Future<List<DocTable>> future = executor.submit(task);

		List<DocTable> docTables;
		try {
			docTables = future.get();
			return docTables;
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
		// position frame in the center of screen
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
