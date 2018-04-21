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
import com.galaxyyao.yuri_dbtoy.domain.DocIndex;
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

			//获取表的列定义
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
			
			//获取索引定义
			Config conf = ConfigUtil.getConfig();
			String indexSheetName = conf.getString("template.index.sheetname");
			for (int i = 1; i < sheetNumber; i++) {
				Sheet sheet = wb.getSheetAt(i);
				if(indexSheetName.equals(sheet.getSheetName())) {
					setIndex(sheet, docTables);
					break;
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
	
	private static void setIndex(Sheet sheet, List<DocTable> docTables) {
		Config conf = ConfigUtil.getConfig();
		int tableNameColNo = conf.getInt("template.index.colno.tablename");
		int indexColNo = conf.getInt("template.index.colno.indexcol");
		int indexNameColNo = conf.getInt("template.index.colno.indexname");
		
		int rowsCount = sheet.getLastRowNum();
		for (int i = 1; i <= rowsCount; i++) {
			Row row = sheet.getRow(i);
			if (row == null || row.getCell(tableNameColNo) == null || row.getCell(indexColNo) == null || row.getCell(indexNameColNo) == null) {
				continue;
			}
			String tableName = row.getCell(tableNameColNo).getStringCellValue().toLowerCase();
			String indexName = row.getCell(indexNameColNo).getStringCellValue().toLowerCase();
			List<String> indexColumns;
			if (row.getCell(indexColNo) == null) {
				indexColumns = new ArrayList<>();
			} else {
				String indexColText = row.getCell(indexColNo).getStringCellValue();
				indexColumns = Lists.newArrayList(indexColText.split(","));
				for (int j = 0; j < indexColumns.size(); j++) {
					indexColumns.set(j, indexColumns.get(j).trim().toLowerCase());
				}
			}
			
			DocIndex docIndex = new DocIndex();
			docIndex.setTableName(tableName);
			docIndex.setIndexName(indexName);
			docIndex.setColumns(indexColumns);
			DocTable docTable = docTables.stream().filter(dt -> dt.getTableName().equals(tableName)).findAny().orElse(null);
			if(docTable!=null) {
				if(docTable.getIndexes()==null) {
					docTable.setIndexes(new ArrayList<DocIndex>());
				}
				docTable.getIndexes().add(docIndex);
			}
		}
	}

	private static List<DocTable> getDocTableFromCategory(Sheet sheet) {
		int rowsCount = sheet.getLastRowNum();
		logger.info("Table number: " + rowsCount);

		Config conf = ConfigUtil.getConfig();
		int tableNameColNo = conf.getInt("template.db.colno.tablename");
		int tableDescColNo = conf.getInt("template.db.colno.tabledesc");
		int syncColumnColNo = conf.getInt("template.db.colno.synccolumn");
		int uniqueConstraintColNo = conf.getInt("template.db.colno.uniqueconstraintcolumn");

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
				tableSyncColumns = new ArrayList<>();
			} else {
				String tableSyncColumnText = row.getCell(syncColumnColNo).getStringCellValue();
				tableSyncColumns = Lists.newArrayList(tableSyncColumnText.split(","));
				for (int j = 0; j < tableSyncColumns.size(); j++) {
					tableSyncColumns.set(j, tableSyncColumns.get(j).trim().toLowerCase());
				}
			}

			// 填充唯一约束字段
			List<String> uniqueConstraintColumns;
			if (row.getCell(uniqueConstraintColNo) == null) {
				uniqueConstraintColumns = new ArrayList<>();
			} else {
				String uniqueConstraintColumnText = row.getCell(uniqueConstraintColNo).getStringCellValue();
				uniqueConstraintColumns = Lists.newArrayList(uniqueConstraintColumnText.split(","));
				for (int j = 0; j < uniqueConstraintColumns.size(); j++) {
					uniqueConstraintColumns.set(j, uniqueConstraintColumns.get(j).trim().toLowerCase());
				}
			}

			DocTable docTable = new DocTable();
			docTable.setTableIndex(i);
			docTable.setTableName(tableName.trim());
			docTable.setTableDesc(tableDesc);
			docTable.setSyncColumns(tableSyncColumns);
			docTable.setUniqueConstraintColumns(uniqueConstraintColumns);
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
			if (Strings.isNullOrEmpty(columnName)) {
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
