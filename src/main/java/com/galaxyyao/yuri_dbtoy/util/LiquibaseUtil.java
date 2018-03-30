package com.galaxyyao.yuri_dbtoy.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galaxyyao.yuri_dbtoy.BackgroundWorker.OPERATION_TYPE_ENUM;
import com.galaxyyao.yuri_dbtoy.domain.DocColumn;
import com.galaxyyao.yuri_dbtoy.domain.DocTable;
import com.galaxyyao.yuri_dbtoy.domain.changelog.ChangeSet;
import com.galaxyyao.yuri_dbtoy.domain.changelog.Column;
import com.galaxyyao.yuri_dbtoy.domain.changelog.Constraints;
import com.galaxyyao.yuri_dbtoy.domain.changelog.CreateTable;
import com.galaxyyao.yuri_dbtoy.domain.changelog.DatabaseChangeLog;
import com.galaxyyao.yuri_dbtoy.domain.changelog.DropTable;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import com.typesafe.config.Config;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

public class LiquibaseUtil {
	private static final Logger logger = LoggerFactory.getLogger(LiquibaseUtil.class);

	public static void generateSql(String changeLogFilePath) {
		Config conf = ConfigUtil.getConfig();
		try {
			Class.forName(conf.getString("database.driver"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}
		logger.info("JDBC Driver Registered");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(conf.getString("database.jdbcurl"),
					conf.getString("database.username"), conf.getString("database.password"));
			Database database = DatabaseFactory.getInstance()
					.findCorrectDatabaseImplementation(new JdbcConnection(connection));
			database.setDefaultSchemaName(conf.getString("database.defaultschema"));
			Liquibase liquibase = new liquibase.Liquibase(changeLogFilePath, new FileSystemResourceAccessor(),
					database);
			String sqlFilePath = changeLogFilePath.substring(0, changeLogFilePath.lastIndexOf(".")) + ".sql";

			// liquibase.update(new Contexts(), new LabelExpression());
			Writer writer = new FileWriter(sqlFilePath);
			liquibase.update(new Contexts(), writer);
			removeSetDefineLine(sqlFilePath);
		} catch (SQLException | LiquibaseException | IOException e) {
			logger.error(e.getMessage());
		} catch(Exception e) {
			logger.error(e.getMessage());
		}
	}

	private static void removeSetDefineLine(String sqlFilePath) throws IOException {
		File sqlFile = new File(sqlFilePath);
		ImmutableList<String> lines = Files.asCharSource(sqlFile, Charsets.UTF_8).readLines();
		File newSqlFile = new File(sqlFilePath.substring(0, sqlFilePath.lastIndexOf(".")) + "_nodefine" + ".sql");
		CharSink sink = Files.asCharSink(newSqlFile, Charsets.UTF_8);
		List<String> newSqlFileLines =new ArrayList<String>();
		for (String line : lines) {
			if (!line.contains("SET DEFINE")) {
				newSqlFileLines.add(line);
			}
		}
		sink.writeLines(newSqlFileLines);
		logger.info("Sql file without set define generated.");
	}

	public static CreateTable generateCreateTable(DocTable docTable) {
		CreateTable createTable = new CreateTable();
		createTable.setTableName(docTable.getTableName());
		List<Column> columns = new ArrayList<Column>();
		int i = 0;
		for (DocColumn docColumn : docTable.getColumns()) {
			Column column = new Column();
			column.setName(docColumn.getColName());
			logger.info(docColumn.getColName());
			column.setType(docColumn.getColType());
			Constraints constraints = new Constraints();
			if (i == 0) {
				constraints.setPrimaryKey("true");
			}
			i++;
			constraints.setNullable(docColumn.getIsAllowNull() ? "true" : "false");
			column.setConstraints(constraints);
			columns.add(column);
		}
		createTable.setColumn(columns);
		return createTable;
	}

	public static DropTable generateDropTable(DocTable docTable) {
		DropTable dropTable = new DropTable();
		dropTable.setTableName(docTable.getTableName());
		dropTable.setCascadeConstraints("false");
		Config conf = ConfigUtil.getConfig();
		dropTable.setCatalogName(conf.getString("database.defaultschema"));
		dropTable.setSchemaName(conf.getString("database.defaultschema"));
		return dropTable;
	}

	public static DatabaseChangeLog generateDatabaseChangeLog(OPERATION_TYPE_ENUM operationType,
			List<DocTable> docTables) {
		DatabaseChangeLog databaseChangeLog = new DatabaseChangeLog();
		List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
		for (int i = 0; i < docTables.size(); i++) {
			DocTable docTable = docTables.get(i);
			logger.info("Generating changeSet: " + docTable.getTableName());
			ChangeSet changeSet = new ChangeSet();
			changeSet.setId(String.valueOf(i + 1));
			String osUserName = System.getProperties().getProperty("user.name");
			changeSet.setAuthor(Strings.isNullOrEmpty(osUserName) ? "dbtool" : osUserName);
			if (operationType.equals(OPERATION_TYPE_ENUM.CREATE)) {
				CreateTable createTable = generateCreateTable(docTable);
				changeSet.setCreateTable(createTable);
			} else if (operationType.equals(OPERATION_TYPE_ENUM.DROP)) {
				DropTable dropTable = generateDropTable(docTable);
				changeSet.setDropTable(dropTable);
			}
			changeSets.add(changeSet);
		}
		databaseChangeLog.setChangeSet(changeSets);
		return databaseChangeLog;
	}

	public static void generateDatabaseChangeLog(DatabaseChangeLog databaseChangeLog, String folderPath,
			String fileName) throws JAXBException, PropertyException {
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
	}
}
