package com.galaxyyao.yuri_dbtoy.liquibase;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

public class LiquibaseHelper {
	public static void main(String[] args) throws IOException, LiquibaseException, SQLException {
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

			Liquibase liquibase = new liquibase.Liquibase("D:\\changelogtest.xml", new FileSystemResourceAccessor(), database);

			// liquibase.update(new Contexts(), new LabelExpression());
			Writer writer = new FileWriter("D:\\output.sql");
			liquibase.update(new Contexts(), writer);
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
	}
}
