package com.galaxyyao.yuri_dbtoy;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galaxyyao.yuri_dbtoy.domain.DocColumn;
import com.galaxyyao.yuri_dbtoy.domain.DocTable;
import com.galaxyyao.yuri_dbtoy.domain.changelog.ChangeSet;
import com.galaxyyao.yuri_dbtoy.domain.changelog.Column;
import com.galaxyyao.yuri_dbtoy.domain.changelog.Constraints;
import com.galaxyyao.yuri_dbtoy.domain.changelog.CreateTable;
import com.galaxyyao.yuri_dbtoy.domain.changelog.DatabaseChangeLog;
import com.galaxyyao.yuri_dbtoy.poi.ExcelUtil;

public class BackgroundWorker {
	private static final Logger logger = LoggerFactory.getLogger(BackgroundWorker.class);

	public List<DocTable> readExcelAndGenerateSql(String path) {
		return ExcelUtil.read(path);
	}

	public String generateCreateTableChangeLog(String folderPath, List<DocTable> docTables) {
		try {
			DatabaseChangeLog databaseChangeLog = new DatabaseChangeLog();
			List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
			for (int i = 0; i < docTables.size(); i++) {
				DocTable docTable = docTables.get(i);
				logger.info("Generating changeSet: " + docTable.getTableName());
				ChangeSet changeSet = new ChangeSet();
				changeSet.setId(String.valueOf(i + 1));
				changeSet.setAuthor("yuridbtoy");
				CreateTable createTable = generateCreateTable(docTable);
				changeSet.setCreateTable(createTable);
				changeSets.add(changeSet);
			}
			databaseChangeLog.setChangeSet(changeSets);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
			String fileName = "changeLog" + LocalDateTime.now().format(formatter) + ".xml";
			File file = new File(folderPath + fileName);
			JAXBContext jaxbContext = JAXBContext.newInstance(DatabaseChangeLog.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			StringBuilder sbSchemaLocation = new StringBuilder();
			sbSchemaLocation.append("http://www.liquibase.org/xml/ns/dbchangelog ");
			sbSchemaLocation.append("http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd ");
			sbSchemaLocation.append("http://www.liquibase.org/xml/ns/dbchangelog-ext ");
			sbSchemaLocation.append("http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd");
			jaxbMarshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, sbSchemaLocation.toString());
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(databaseChangeLog, file);
			jaxbMarshaller.marshal(databaseChangeLog, System.out);
			return folderPath + fileName;
		} catch (JAXBException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	private CreateTable generateCreateTable(DocTable docTable) {
		CreateTable createTable = new CreateTable();
		createTable.setTableName(docTable.getTableName());
		List<Column> columns = new ArrayList<Column>();
		for (DocColumn docColumn : docTable.getColumns()) {
			Column column = new Column();
			column.setName(docColumn.getColName());
			column.setType(docColumn.getColType());
			Constraints constraints = new Constraints();
			if (docColumn.getIsPrimaryKey()) {
				constraints.setPrimaryKey("true");
			}
			constraints.setNullable(docColumn.getIsAllowNull() ? "true" : "false");
			column.setConstraints(constraints);
			columns.add(column);
		}
		createTable.setColumn(columns);
		return createTable;
	}
}
