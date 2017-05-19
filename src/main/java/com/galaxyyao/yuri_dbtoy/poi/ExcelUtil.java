package com.galaxyyao.yuri_dbtoy.poi;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelUtil {
	public static void read(String inputFilePath) {
		FileInputStream fileIn = null;

		try {
			fileIn = new FileInputStream(inputFilePath);
			
			Workbook wb = WorkbookFactory.create(fileIn);
			Sheet sheet = wb.getSheetAt(2);
			System.out.println(sheet.getSheetName());
			Header header = sheet.getHeader();

            int rowsCount = sheet.getLastRowNum();
            System.out.println("Total Number of Rows: " + (rowsCount + 1));
            for (int i = 0; i <= rowsCount; i++) {
                Row row = sheet.getRow(i);
                int colCounts = row.getLastCellNum();
                System.out.println("Total Number of Cols: " + colCounts);
                for (int j = 0; j < 4; j++) {
                    Cell cell = row.getCell(j);
                    System.out.println("[" + i + "," + j + "]=" + cell.getStringCellValue());
                }
            }

		} catch (IOException | EncryptedDocumentException | InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fileIn != null) {
				try {
					fileIn.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
