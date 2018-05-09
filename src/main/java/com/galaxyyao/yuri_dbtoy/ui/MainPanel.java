package com.galaxyyao.yuri_dbtoy.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galaxyyao.yuri_dbtoy.BackgroundWorker;
import com.galaxyyao.yuri_dbtoy.BackgroundWorker.OPERATION_TYPE_ENUM;
import com.galaxyyao.yuri_dbtoy.constant.DbToolConstant;
import com.galaxyyao.yuri_dbtoy.domain.DocTable;
import com.galaxyyao.yuri_dbtoy.util.LiquibaseUtil;
import com.google.common.base.Strings;

public class MainPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -4693833475415137547L;

	private static final Logger logger = LoggerFactory.getLogger(MainPanel.class);

	/**
	 * 打开Excel表结构设计文档按钮
	 */
	private JButton openExcelButton;
	/**
	 * 生成建表语句按钮
	 */
	private JButton generateCreateTableSqlButton;
	/**
	 * 生成带通用字段的建表语句按钮
	 */
	private JButton generateCreateTableSqlWithCommonColumnsButton;
	/**
	 * 生成Drop表语句按钮
	 */
	private JButton generateDropTableSqlButton;
	/**
	 * 生成全删全导语句按钮
	 */
	private JButton generateTruncateAndInsertSqlButton;
	/**
	 * 生成On Insert触发器语句按钮
	 */
	private JButton generateOnInsertTriggerButton;
	/**
	 * 生成On Update触发器语句按钮
	 */
	private JButton generateOnUpdateTriggerButton;
	/**
	 * 生成On Delete触发器语句按钮
	 */
	private JButton generateOnDeleteTriggerButton;
	/**
	 * 生成激活触发器语句按钮
	 */
	private JButton generateEnableTriggerButton;
	/**
	 * 生成禁用触发器语句按钮
	 */
	private JButton generateDisableTriggerButton;
	/**
	 * 生成授权表DML权限语句按钮
	 */
	private JButton generateGrantDmlPrivilegeButton;
	/**
	 * 生成创建唯一约束语句按钮
	 */
	private JButton generateCreateUniqueConstraintButton;
	/**
	 * 生成创建索引语句按钮
	 */
	private JButton generateCreateIndexButton;

	/**
	 * 日志文本框
	 */
	private JTextArea logTextArea;
	/**
	 * 文件选择器
	 */
	private JFileChooser fileChooser = new JFileChooser();
	/**
	 * 表结构表格组件
	 */
	private JTable dbTable;

	/**
	 * 局部变量-表定义数组
	 */
	private static List<DocTable> docTables = null;
	/**
	 * 局部变量-文件路径
	 */
	private static String filePath = null;
	/**
	 * 局部变量-目录路径
	 */
	private static String folderPath = null;

	/**
	 * 主面板UI初始化
	 */
	public MainPanel() {
		super(new BorderLayout());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(DbToolConstant.MAIN_PANEL_WIDTH, 100));
		initButtonPanel(buttonPanel);

		logTextArea = new JTextArea(5, 20);
		logTextArea.setMargin(new Insets(5, 5, 5, 5));
		logTextArea.setEditable(false);
		JScrollPane logScrollPanel = new JScrollPane(logTextArea);

		initDbTable();
		JScrollPane dbTableScrollPanel = new JScrollPane(dbTable);

		add(buttonPanel, BorderLayout.PAGE_START);
		add(dbTableScrollPanel, BorderLayout.CENTER);
		add(logScrollPanel, BorderLayout.PAGE_END);
	}

	private void initDbTable() {
		Object rowData[][] = {};
		Object columnNames[] = { "序号", "表名", "说明", "是否操作" };
		final Class<?>[] columnClass = new Class[] { Integer.class, String.class, String.class, Boolean.class };
		DefaultTableModel tableModel = new DefaultTableModel(rowData, columnNames) {
			private static final long serialVersionUID = -7373407805868967994L;

			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == DbToolConstant.DBTABLE_ENABLE_OPERATABLE_COLUMN_NO) {
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
	}

	private void initButtonPanel(JPanel buttonPanel) {
		openExcelButton = new JButton("选择表结构文档");
		openExcelButton.addActionListener(this);
		buttonPanel.add(openExcelButton);

		generateCreateTableSqlButton = new JButton("生成建表语句");
		generateCreateTableSqlButton.addActionListener(this);
		generateCreateTableSqlButton.setEnabled(false);
		buttonPanel.add(generateCreateTableSqlButton);

		generateCreateTableSqlWithCommonColumnsButton = new JButton("生成带通用字段的建表语句");
		generateCreateTableSqlWithCommonColumnsButton.addActionListener(this);
		generateCreateTableSqlWithCommonColumnsButton.setEnabled(false);
		buttonPanel.add(generateCreateTableSqlWithCommonColumnsButton);

		generateDropTableSqlButton = new JButton("生成Drop表语句");
		generateDropTableSqlButton.addActionListener(this);
		generateDropTableSqlButton.setEnabled(false);
		buttonPanel.add(generateDropTableSqlButton);

		generateTruncateAndInsertSqlButton = new JButton("生成全删全导语句按钮");
		generateTruncateAndInsertSqlButton.addActionListener(this);
		generateTruncateAndInsertSqlButton.setEnabled(false);
		buttonPanel.add(generateTruncateAndInsertSqlButton);

		generateOnInsertTriggerButton = new JButton("生成On Insert触发器语句");
		generateOnInsertTriggerButton.addActionListener(this);
		generateOnInsertTriggerButton.setEnabled(false);
		buttonPanel.add(generateOnInsertTriggerButton);

		generateOnUpdateTriggerButton = new JButton("生成On Update触发器语句");
		generateOnUpdateTriggerButton.addActionListener(this);
		generateOnUpdateTriggerButton.setEnabled(false);
		buttonPanel.add(generateOnUpdateTriggerButton);

		generateOnDeleteTriggerButton = new JButton("生成On Delete触发器语句");
		generateOnDeleteTriggerButton.addActionListener(this);
		generateOnDeleteTriggerButton.setEnabled(false);
		buttonPanel.add(generateOnDeleteTriggerButton);

		generateEnableTriggerButton = new JButton("生成激活触发器语句");
		generateEnableTriggerButton.addActionListener(this);
		generateEnableTriggerButton.setEnabled(false);
		buttonPanel.add(generateEnableTriggerButton);

		generateDisableTriggerButton = new JButton("生成禁用触发器语句");
		generateDisableTriggerButton.addActionListener(this);
		generateDisableTriggerButton.setEnabled(false);
		buttonPanel.add(generateDisableTriggerButton);

		generateGrantDmlPrivilegeButton = new JButton("生成授权表DML权限语句");
		generateGrantDmlPrivilegeButton.addActionListener(this);
		generateGrantDmlPrivilegeButton.setEnabled(false);
		buttonPanel.add(generateGrantDmlPrivilegeButton);

		generateCreateUniqueConstraintButton = new JButton("生成创建唯一约束语句");
		generateCreateUniqueConstraintButton.addActionListener(this);
		generateCreateUniqueConstraintButton.setEnabled(false);
		buttonPanel.add(generateCreateUniqueConstraintButton);

		generateCreateIndexButton = new JButton("生成创建索引语句");
		generateCreateIndexButton.addActionListener(this);
		generateCreateIndexButton.setEnabled(false);
		buttonPanel.add(generateCreateIndexButton);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openExcelButton) {
			guiOnOpenExcelButtonClicked();
		} else if (e.getSource() == generateCreateTableSqlButton) {
			guiOnGenerateCreateTableSqlButtonClicked();
		} else if (e.getSource() == generateCreateTableSqlWithCommonColumnsButton) {
			guiOnGenerateCreateTableSqlWithCommonColumnsButtonClicked();
		} else if (e.getSource() == generateDropTableSqlButton) {
			guiOnGenerateDropTableSqlButtonClicked();
		} else if (e.getSource() == generateTruncateAndInsertSqlButton) {
			guiOnGenerateTruncateAndInsertSqlButtonClicked();
		} else if (e.getSource() == generateOnInsertTriggerButton) {
			guiOnGenerateOnInsertTriggerButtonClicked();
		} else if (e.getSource() == generateOnUpdateTriggerButton) {
			guiOnGenerateOnUpdateTriggerButtonClicked();
		} else if (e.getSource() == generateOnDeleteTriggerButton) {
			guiOnGenerateOnDeleteTriggerButtonClicked();
		} else if (e.getSource() == generateEnableTriggerButton) {
			guiOnGenerateEnableTriggerButtonClicked();
		} else if (e.getSource() == generateDisableTriggerButton) {
			guiOnGenerateDisableTriggerButtonClicked();
		} else if (e.getSource() == generateGrantDmlPrivilegeButton) {
			guiOnGenerateGrantDmlPrivilegeButtonClicked();
		} else if (e.getSource() == generateCreateUniqueConstraintButton) {
			guiOnGenerateCreateUniqueConstraintButtonClicked();
		} else if (e.getSource() == generateCreateIndexButton) {
			guiOnGenerateCreateIndexButtonClicked();
		}
	}

	private void guiOnOpenExcelButtonClicked() {
		int returnVal = fileChooser.showOpenDialog(MainPanel.this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (!file.getName().contains(".")) {
				logTextArea.append("非法的文件名：" + file.getName() + "." + DbToolConstant.NEW_LINE);
				return;
			}
			String fileExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
			if (!fileExtension.equals("xlsx") || fileExtension.equals("xls")) {
				logTextArea.append("非法的文件扩展名：" + fileExtension + "." + DbToolConstant.NEW_LINE);
				return;
			}
			filePath = file.getPath();
			folderPath = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1);
			logTextArea.append("正在打开文件：" + file.getName() + "." + DbToolConstant.NEW_LINE);
			docTables = readExcelInBackground(file.getPath(), file.getName());
			if (docTables == null) {
				logTextArea.append("Excel文件解析失败：" + file.getName() + "." + DbToolConstant.NEW_LINE);
				return;
			}
			setDbTableModel(docTables);
			logTextArea.append("Excel文件读取成功。" + DbToolConstant.NEW_LINE);
		} else {
			logTextArea.append("打开文件操作取消" + DbToolConstant.NEW_LINE);
			return;
		}
		logTextArea.setCaretPosition(logTextArea.getDocument().getLength());

		generateCreateTableSqlButton.setEnabled(true);
		generateCreateTableSqlWithCommonColumnsButton.setEnabled(true);
		generateDropTableSqlButton.setEnabled(true);
		generateTruncateAndInsertSqlButton.setEnabled(true);
		generateOnInsertTriggerButton.setEnabled(true);
		generateOnUpdateTriggerButton.setEnabled(true);
		generateOnDeleteTriggerButton.setEnabled(true);
		generateEnableTriggerButton.setEnabled(true);
		generateDisableTriggerButton.setEnabled(true);
		generateGrantDmlPrivilegeButton.setEnabled(true);
		generateCreateUniqueConstraintButton.setEnabled(true);
		generateCreateIndexButton.setEnabled(true);
	}

	private void guiOnGenerateCreateTableSqlButtonClicked() {
		logger.info("Generate create table SQL.");
		logger.info("DatabaseChangeLog location: " + folderPath);
		List<DocTable> operableDocTables = getOperableDocTables();
		generateSql(folderPath, operableDocTables, OPERATION_TYPE_ENUM.CREATE);
		logTextArea.append("建表语句已生成" + DbToolConstant.NEW_LINE);
	}

	private List<DocTable> getOperableDocTables() {
		DefaultTableModel model = (DefaultTableModel) dbTable.getModel();
		List<String> operableTableNameList = new ArrayList<String>();
		for (int i = 0; i < model.getRowCount(); i++) {
			Boolean isOperable = (Boolean) model.getValueAt(i, DbToolConstant.DBTABLE_ENABLE_OPERATABLE_COLUMN_NO);
			if (isOperable) {
				String tableName = (String) model.getValueAt(i, 1);
				operableTableNameList.add(tableName);
			}
		}
		List<DocTable> operableDocTables = docTables.stream()
				.filter(dt -> operableTableNameList.contains(dt.getTableName())).collect(Collectors.toList());
		return operableDocTables;
	}

	private void guiOnGenerateCreateTableSqlWithCommonColumnsButtonClicked() {
		logger.info("Generate create table SQL.");
		logger.info("DatabaseChangeLog location: " + folderPath);
		List<DocTable> operableDocTables = getOperableDocTables();
		List<DocTable> operableDocTablesWithCommonColumns = new ArrayList<DocTable>();
		BackgroundWorker worker = new BackgroundWorker();
		for (DocTable operableDocTable : operableDocTables) {
			operableDocTablesWithCommonColumns.add(worker.getDocTableWithCommonColumns(operableDocTable));
		}
		generateSql(folderPath, operableDocTablesWithCommonColumns, OPERATION_TYPE_ENUM.CREATE);
		logTextArea.append("带通用字段的建表语句已生成" + DbToolConstant.NEW_LINE);
	}

	private void guiOnGenerateDropTableSqlButtonClicked() {
		logger.info("Generate drop table SQL.");
		logger.info("DatabaseChangeLog location: " + folderPath);
		List<DocTable> operableDocTables = getOperableDocTables();
		generateSql(folderPath, operableDocTables, OPERATION_TYPE_ENUM.DROP);
		logTextArea.append("Drop表语句已生成" + DbToolConstant.NEW_LINE);
	}

	private void guiOnGenerateTruncateAndInsertSqlButtonClicked() {
		logger.info("Generate truncate and insert table SQL.");
		BackgroundWorker worker = new BackgroundWorker();
		List<DocTable> operableDocTables = getOperableDocTables();
		List<DocTable> operableDocTablesWithCommonColumns = new ArrayList<DocTable>();
		for (DocTable operableDocTable : operableDocTables) {
			operableDocTablesWithCommonColumns.add(worker.getDocTableWithCommonColumns(operableDocTable));
		}
		String sqlFilePath = worker.generateTruncateAndInsertSql(folderPath, operableDocTables,
				operableDocTablesWithCommonColumns);
		logger.info("SQL File location: " + sqlFilePath);
		logTextArea.append("全删全导语句已生成" + DbToolConstant.NEW_LINE);
	}

	private void guiOnGenerateOnInsertTriggerButtonClicked() {
		logger.info("Generate on insert trigger SQL.");
		List<DocTable> operableDocTables = getOperableDocTables();
		BackgroundWorker worker = new BackgroundWorker();
		List<DocTable> operableDocTablesWithCommonColumns = new ArrayList<DocTable>();
		for (DocTable operableDocTable : operableDocTables) {
			operableDocTablesWithCommonColumns.add(worker.getDocTableWithCommonColumns(operableDocTable));
		}
		String sqlFilePath = worker.generateOnInsertTrigger(folderPath, operableDocTables,
				operableDocTablesWithCommonColumns);
		logger.info("SQL File location: " + sqlFilePath);
		logTextArea.append("On Insert触发器语句已生成" + DbToolConstant.NEW_LINE);
	}

	private void guiOnGenerateOnUpdateTriggerButtonClicked() {
		logger.info("Generate on update trigger SQL.");
		List<DocTable> operableDocTables = getOperableDocTables();
		BackgroundWorker worker = new BackgroundWorker();
		String sqlFilePath = worker.generateOnUpdateTrigger(folderPath, operableDocTables);
		logger.info("SQL File location: " + sqlFilePath);
		logTextArea.append("On Update触发器语句已生成" + DbToolConstant.NEW_LINE);
	}

	private void guiOnGenerateOnDeleteTriggerButtonClicked() {
		logger.info("Generate on delete trigger SQL.");
		List<DocTable> operableDocTables = getOperableDocTables();
		BackgroundWorker worker = new BackgroundWorker();
		String sqlFilePath = worker.generateOnDeleteTrigger(folderPath, operableDocTables);
		logger.info("SQL File location: " + sqlFilePath);
		logTextArea.append("On Delete触发器语句已生成" + DbToolConstant.NEW_LINE);
	}

	private void guiOnGenerateEnableTriggerButtonClicked() {
		logger.info("Generate enable trigger SQL.");
		List<DocTable> operableDocTables = getOperableDocTables();
		BackgroundWorker worker = new BackgroundWorker();
		String sqlFilePath = worker.generateEnableTrigger(folderPath, operableDocTables);
		logger.info("SQL File location: " + sqlFilePath);
		logTextArea.append("激活触发器语句已生成" + DbToolConstant.NEW_LINE);
	}

	private void guiOnGenerateDisableTriggerButtonClicked() {
		logger.info("Generate disable trigger SQL.");
		List<DocTable> operableDocTables = getOperableDocTables();
		BackgroundWorker worker = new BackgroundWorker();
		String sqlFilePath = worker.generateDisableTrigger(folderPath, operableDocTables);
		logger.info("SQL File location: " + sqlFilePath);
		logTextArea.append("禁用触发器语句已生成" + DbToolConstant.NEW_LINE);
	}

	private void guiOnGenerateGrantDmlPrivilegeButtonClicked() {
		logger.info("Generate grant dml privilege SQL.");
		List<DocTable> operableDocTables = getOperableDocTables();
		BackgroundWorker worker = new BackgroundWorker();
		String sqlFilePath = worker.generateGrantDmlPrivilege(folderPath, operableDocTables);
		logger.info("SQL File location: " + sqlFilePath);
		logTextArea.append("授权表DML权限语句已生成" + DbToolConstant.NEW_LINE);
	}

	private void guiOnGenerateCreateUniqueConstraintButtonClicked() {
		logger.info("Generate create unique constraint SQL.");
		List<DocTable> operableDocTables = getOperableDocTables();
		BackgroundWorker worker = new BackgroundWorker();
		String sqlFilePath = worker.generateCreateUniqueConstraint(folderPath, operableDocTables);
		logger.info("SQL File location: " + sqlFilePath);
		logTextArea.append("创建唯一约束语句已生成" + DbToolConstant.NEW_LINE);
	}

	private void guiOnGenerateCreateIndexButtonClicked() {
		logger.info("Generate create index SQL.");
		List<DocTable> operableDocTables = getOperableDocTables();
		BackgroundWorker worker = new BackgroundWorker();
		String sqlFilePath = worker.generateCreateIndex(folderPath, operableDocTables);
		logger.info("SQL File location: " + sqlFilePath);
		logTextArea.append("创建索引语句已生成" + DbToolConstant.NEW_LINE);
	}

	private void setDbTableModel(List<DocTable> docTables) {
		DefaultTableModel model = (DefaultTableModel) dbTable.getModel();
		while (model.getRowCount() > 0) {
			model.removeRow(0);
		}
		for (int i = 0; i < docTables.size(); i++) {
			DocTable docTable = docTables.get(i);
			model.addRow(new Object[] { docTable.getTableIndex(), docTable.getTableName(), docTable.getTableDesc(),
					docTable.getIsSelected() });
		}
	}

	private List<DocTable> readExcelInBackground(String path, String filename) {
		Callable<List<DocTable>> task = () -> {
			BackgroundWorker worker = new BackgroundWorker();
			logger.info("Start background thread.");
			return worker.readExcel(path, filename);
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

	private void generateSql(String folderPath, List<DocTable> operableDocTables, OPERATION_TYPE_ENUM operationType) {
		Runnable task = () -> {
			BackgroundWorker worker = new BackgroundWorker();
			String filePath = null;
			if (OPERATION_TYPE_ENUM.CREATE.equals(operationType)) {
				filePath = worker.generateCreateTableChangeLog(folderPath, operableDocTables);
			} else if (OPERATION_TYPE_ENUM.DROP.equals(operationType)) {
				filePath = worker.generateDropTableChangeLog(folderPath, operableDocTables);
			}
			if (!Strings.isNullOrEmpty(filePath)) {
				LiquibaseUtil.generateSql(filePath);
			}
		};

		ExecutorService executor = Executors.newFixedThreadPool(1);
		Future<?> future = executor.submit(task);
		try {
			future.get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void createAndShowGUI() {
		JFrame frame = new JFrame("dbtool");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		MainPanel mainPanel = new MainPanel();
		mainPanel.setPreferredSize(new Dimension(DbToolConstant.MAIN_PANEL_WIDTH, DbToolConstant.MAIN_PANEL_HEIGHT));
		frame.add(mainPanel);

		frame.pack();
		// position frame in the center of screen
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
