package com.galaxyyao.yuri_dbtoy.poi;

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

import com.galaxyyao.yuri_dbtoy.domain.DocColumn;
import com.galaxyyao.yuri_dbtoy.domain.DocTable;

public class ExcelUtil {
	private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

	public static List<DocTable> read(String inputFilePath) {
		FileInputStream fileIn = null;
		List<DocTable> docTables = new ArrayList<DocTable>();

		try {
			fileIn = new FileInputStream(inputFilePath);

			Workbook wb = WorkbookFactory.create(fileIn);
			int sheetNumber = wb.getNumberOfSheets();
			if (sheetNumber < 2) {
				logger.error("Invalid sheet number.");
			}
			Sheet categorySheet = wb.getSheetAt(0);
			docTables = getDocTableFromCategory(categorySheet);

			for (int i = 1; i < sheetNumber; i++) {
				Sheet sheet = wb.getSheetAt(i);
				logger.info("Reading sheet name: " + sheet.getSheetName());
				DocTable docTable = docTables.stream().filter(dt -> sheet.getSheetName().equals(dt.getTableName()))
						.findFirst().orElse(null);
				if (docTable == null) {
					docTables.remove(docTable);
				} else {
					setTableColumns(docTable, sheet);
				}
			}
			return docTables;
		} catch (IOException | EncryptedDocumentException | InvalidFormatException e) {
			logger.error(e.getMessage());
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
		logger.info("Total Number of Rows: " + (rowsCount + 1));
		List<DocTable> docTables = new ArrayList<DocTable>();
		for (int i = 1; i <= rowsCount; i++) {
			DocTable docTable = new DocTable();
			docTable.setTableIndex(i);

			Row row = sheet.getRow(i);
			docTable.setTableName(row.getCell(2).getStringCellValue());
			docTable.setTableDesc(row.getCell(3).getStringCellValue());
			docTable.setIsSelected(false);
			docTables.add(docTable);
		}
		return docTables;
	}

	private static void setTableColumns(DocTable docTable, Sheet sheet) {
		int rowsCount = sheet.getLastRowNum();
		logger.info("Total Number of Rows: " + (rowsCount + 1));
		List<DocColumn> docColumns = new ArrayList<DocColumn>();
		for (int i = 2; i <= rowsCount; i++) {
			Row row = sheet.getRow(i);
			DocColumn docColumn = new DocColumn();
			docColumn.setColIndex(i - 1);
			docColumn.setColName(row.getCell(0).getStringCellValue());
			docColumn.setColType(row.getCell(1).getStringCellValue());
			String isPrimaryKeyText = (row.getCell(2) == null) ? "" : row.getCell(2).getStringCellValue();
			docColumn.setIsPrimaryKey("Y".equals(isPrimaryKeyText));
			String isAllowNullText = (row.getCell(3) == null) ? "" : row.getCell(3).getStringCellValue();
			docColumn.setIsAllowNull(!"N".equals(isAllowNullText));
			String defaultValue = (row.getCell(4) == null) ? "" : row.getCell(4).getStringCellValue();
			docColumn.setDefaultValue(defaultValue);
			String colDesc = (row.getCell(5) == null) ? "" : row.getCell(5).getStringCellValue();
			docColumn.setColDesc(colDesc);
			docColumns.add(docColumn);
		}
		docTable.setColumns(docColumns);
	}
}
