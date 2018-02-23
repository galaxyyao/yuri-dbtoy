package com.galaxyyao.yuri_dbtoy.liquibase;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

public class LiquibaseHelper {
	private static final Logger logger=LoggerFactory.getLogger(LiquibaseHelper.class);
	
	public static void generateSql(String changeLogFilePath) {
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("Oracle JDBC Driver Registered");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(
					"jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=hq-uat-rac-scan.noahwm.com.local)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME= uatrac)))",
					"testdb", "testdb");
			Database database = DatabaseFactory.getInstance()
					.findCorrectDatabaseImplementation(new JdbcConnection(connection));
			database.setDefaultSchemaName("TESTDB");

			Liquibase liquibase = new liquibase.Liquibase(changeLogFilePath, new FileSystemResourceAccessor(),
					database);
			String sqlFilePath = changeLogFilePath.substring(0, changeLogFilePath.lastIndexOf(".")) + ".sql";

			// liquibase.update(new Contexts(), new LabelExpression());
			Writer writer = new FileWriter(sqlFilePath);
			liquibase.update(new Contexts(), writer);
		} catch (SQLException | LiquibaseException | IOException e) {
			logger.error(e.getMessage());
		}
	}
}
