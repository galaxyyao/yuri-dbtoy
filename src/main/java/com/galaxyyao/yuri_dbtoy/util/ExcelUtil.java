package com.galaxyyao.yuri_dbtoy.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galaxyyao.yuri_dbtoy.constant.DbToolConstant;
import com.galaxyyao.yuri_dbtoy.domain.DocColumn;
import com.galaxyyao.yuri_dbtoy.domain.DocTable;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;

public class ExcelUtil {
	private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

	public static List<DocTable> read(String inputFilePath) {
		FileInputStream fileIn = null;
		List<DocTable> docTables = new ArrayList<DocTable>();

		try {
			fileIn = new FileInputStream(inputFilePath);

			Workbook wb = WorkbookFactory.create(fileIn);

			int sheetNumber = wb.getNumberOfSheets();
			if (sheetNumber < DbToolConstant.MIN_SHEET_NUM) {
				logger.error("Invalid sheet number.");
			}
			Sheet categorySheet = wb.getSheetAt(0);
			docTables = getDocTableFromCategory(categorySheet);

			for (int i = 1; i < sheetNumber; i++) {
				Sheet sheet = wb.getSheetAt(i);
				logger.info("Reading sheet name: " + sheet.getSheetName());
				DocTable docTable = docTables.stream()
						.filter(dt -> sheet.getSheetName().toLowerCase().equals(dt.getTableName())).findFirst()
						.orElse(null);
				if (docTable == null) {
					docTables.remove(docTable);
				} else {
					setTableColumns(docTable, sheet);
				}
			}
			return docTables;
		} catch (IOException | EncryptedDocumentException | InvalidFormatException e) {
			logger.error("Error occurs:", e);
			System.out.println("ioe" + e);
			return null;
		} catch (Exception e) {
			System.out.println("occurs" + e);
			logger.error("Error occurs:", e);
			return null;
		} finally {
			if (fileIn != null) {
				try {
					fileIn.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}

	private static List<DocTable> getDocTableFromCategory(Sheet sheet) {
		int rowsCount = sheet.getLastRowNum();
		logger.info("Table number: " + rowsCount);

		Config conf = ConfigUtil.getConfig();
		int tableNameColNo = conf.getInt("template.db.colno.tablename");
		int tableDescColNo = conf.getInt("template.db.colno.tabledesc");
		int syncColumnColNo = conf.getInt("template.db.colno.syncolumn");

		List<DocTable> docTables = new ArrayList<DocTable>();
		for (int i = 1; i <= rowsCount; i++) {
			Row row = sheet.getRow(i);
			if (row == null || row.getCell(tableNameColNo) == null || row.getCell(tableDescColNo) == null) {
				continue;
			}
			String tableName = row.getCell(tableNameColNo).getStringCellValue().toLowerCase();
			String tableDesc = row.getCell(tableDescColNo).getStringCellValue().trim();
			if (Strings.isNullOrEmpty(tableName) || Strings.isNullOrEmpty(tableDesc)) {
				continue;
			}
			// 填充同步字段
			List<String> tableSyncColumns;
			if (row.getCell(syncColumnColNo) == null) {
				tableSyncColumns = new ArrayList<String>();
			} else {
				String tableSyncColumnText = row.getCell(4).getStringCellValue();
				tableSyncColumns = Lists.newArrayList(tableSyncColumnText.split(","));
				for (String tableSyncColumn : tableSyncColumns) {
					tableSyncColumn = tableSyncColumn.trim().toLowerCase();
				}
			}

			DocTable docTable = new DocTable();
			docTable.setTableIndex(i);
			docTable.setTableName(tableName.trim());
			docTable.setTableDesc(tableDesc);
			docTable.setSyncColumns(tableSyncColumns);
			docTable.setIsSelected(false);
			docTables.add(docTable);
		}
		return docTables;
	}

	private static void setTableColumns(DocTable docTable, Sheet sheet) {
		Config conf = ConfigUtil.getConfig();
		int startRowNo = conf.getInt("template.table.rowno.startrow");
		int colNameColNo = conf.getInt("template.table.colno.colname");
		int colTypeColNo = conf.getInt("template.table.colno.coltype");
		int colAllowNullColNo = conf.getInt("template.table.colno.colallownull");
		int colDescColNo = conf.getInt("template.table.colno.coldesc");

		int rowsCount = sheet.getLastRowNum();
		logger.info("Row number: " + (rowsCount - 1));
		List<DocColumn> docColumns = new ArrayList<DocColumn>();
		for (int i = startRowNo; i <= rowsCount; i++) {
			Row row = sheet.getRow(i);
			DocColumn docColumn = new DocColumn();
			docColumn.setColIndex(i - 1);
			String columnName = row.getCell(colNameColNo).getStringCellValue().toLowerCase().trim();
			if(Strings.isNullOrEmpty(columnName)) {
				continue;
			}
			docColumn.setColName(columnName);
			docColumn.setColType(row.getCell(colTypeColNo).getStringCellValue());
			String isAllowNullText = (row.getCell(colAllowNullColNo) == null) ? ""
					: row.getCell(colAllowNullColNo).getStringCellValue();
			docColumn.setIsAllowNull(!"N".equals(isAllowNullText));
			String colDesc = (row.getCell(colDescColNo) == null) ? "" : row.getCell(colDescColNo).getStringCellValue();
			docColumn.setColDesc(colDesc);
			docColumns.add(docColumn);
		}
		docTable.setColumns(docColumns);
	}
}
