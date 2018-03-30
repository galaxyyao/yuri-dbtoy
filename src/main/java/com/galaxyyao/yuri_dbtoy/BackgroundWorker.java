package com.galaxyyao.yuri_dbtoy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galaxyyao.yuri_dbtoy.domain.DocColumn;
import com.galaxyyao.yuri_dbtoy.domain.DocTable;
import com.galaxyyao.yuri_dbtoy.domain.changelog.DatabaseChangeLog;
import com.galaxyyao.yuri_dbtoy.util.ConfigUtil;
import com.galaxyyao.yuri_dbtoy.util.ExcelUtil;
import com.galaxyyao.yuri_dbtoy.util.LiquibaseUtil;
import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import com.typesafe.config.Config;

public class BackgroundWorker {
	private static final Logger logger = LoggerFactory.getLogger(BackgroundWorker.class);

	public enum OPERATION_TYPE_ENUM {
		CREATE, CREATE_WITH_COMMON_COLUMNS, DROP, TRUNCATE_AND_INSERT, ON_INSERT_TRIGGER, ON_UPDATE_TRIGGER, ON_DELETE_TRIGGER, ENABLE_TRIGGER, DISABLE_TRIGGER, GRANT_DML_PRIVILEGE
	}

	public List<DocTable> readExcel(String path, String filename) throws FileNotFoundException {
		List<DocTable> docTables = ExcelUtil.read(path);
		return docTables;
	}

	public DocTable getDocTableWithCommonColumns(DocTable docTable) {
		List<DocColumn> originDocColumns = docTable.getColumns();
		List<DocColumn> newDocColumns = new ArrayList<DocColumn>();

		DocColumn idColumn = new DocColumn();
		idColumn.setColName("C_" + docTable.getTableName().substring(1, docTable.getTableName().length()) + "ID");
		idColumn.setColType("VARCHAR2(36)");
		idColumn.setIsAllowNull(false);
		idColumn.setColDesc("主键");
		newDocColumns.add(idColumn);

		newDocColumns.addAll(originDocColumns);

		DocColumn isDeletedColumn = new DocColumn();
		isDeletedColumn.setColName("C_ISDELETED");
		isDeletedColumn.setColType("CHAR(1)");
		isDeletedColumn.setIsAllowNull(false);
		isDeletedColumn.setColDesc("是否删除");
		newDocColumns.add(isDeletedColumn);

		DocColumn createdByIdColumn = new DocColumn();
		createdByIdColumn.setColName("C_CREATEDBYID");
		createdByIdColumn.setColType("VARCHAR2(36)");
		createdByIdColumn.setIsAllowNull(false);
		createdByIdColumn.setColDesc("创建人");
		newDocColumns.add(createdByIdColumn);

		DocColumn createdTimeColumn = new DocColumn();
		createdTimeColumn.setColName("D_CREATEDTIME");
		createdTimeColumn.setColType("TIMESTAMP");
		createdTimeColumn.setIsAllowNull(false);
		createdTimeColumn.setColDesc("创建时间");
		newDocColumns.add(createdTimeColumn);

		DocColumn lastModifiedByIdColumn = new DocColumn();
		lastModifiedByIdColumn.setColName("C_LASTMODIFIEDBYID");
		lastModifiedByIdColumn.setColType("VARCHAR2(36)");
		lastModifiedByIdColumn.setIsAllowNull(false);
		lastModifiedByIdColumn.setColDesc("最后修改人");
		newDocColumns.add(lastModifiedByIdColumn);

		DocColumn lastModifiedTimeColumn = new DocColumn();
		lastModifiedTimeColumn.setColName("D_LASTMODIFIEDTIME");
		lastModifiedTimeColumn.setColType("TIMESTAMP");
		lastModifiedTimeColumn.setIsAllowNull(false);
		lastModifiedTimeColumn.setColDesc("最后修改时间");
		newDocColumns.add(lastModifiedTimeColumn);

		DocTable newDocTable = new DocTable();
		newDocTable.setIsSelected(true);
		newDocTable.setSyncColumns(docTable.getSyncColumns());
		newDocTable.setTableDesc(docTable.getTableDesc());
		newDocTable.setTableIndex(docTable.getTableIndex());
		newDocTable.setTableName(docTable.getTableName());
		newDocTable.setColumns(newDocColumns);
		return newDocTable;
	}

	public String generateCreateTableChangeLog(String folderPath, List<DocTable> docTables) {
		try {
			DatabaseChangeLog databaseChangeLog = LiquibaseUtil.generateDatabaseChangeLog(OPERATION_TYPE_ENUM.CREATE,
					docTables);
			String fileName = getOutputFileName(OPERATION_TYPE_ENUM.CREATE);
			LiquibaseUtil.generateDatabaseChangeLog(databaseChangeLog, folderPath, fileName);
			return folderPath + fileName;
		} catch (JAXBException e) {
			logger.error("CreateTable ChangeLog generate failed.", e);
			return null;
		}
	}

	public String generateDropTableChangeLog(String folderPath, List<DocTable> docTables) {
		try {
			DatabaseChangeLog databaseChangeLog = LiquibaseUtil.generateDatabaseChangeLog(OPERATION_TYPE_ENUM.DROP,
					docTables);
			String fileName = getOutputFileName(OPERATION_TYPE_ENUM.DROP);
			LiquibaseUtil.generateDatabaseChangeLog(databaseChangeLog, folderPath, fileName);
			return folderPath + fileName;
		} catch (JAXBException e) {
			logger.error("CreateTable ChangeLog generate failed.", e);
			return null;
		}
	}

	public String generateTruncateAndInsertSql(String folderPath, List<DocTable> srcDocTables,
			List<DocTable> destDocTables) {
		Config conf = ConfigUtil.getConfig();
		String defaultSchemaName = conf.getString("database.defaultschema");
		String fromSchemaName = conf.getString("database.fromschema");

		String fileName = getOutputFileName(OPERATION_TYPE_ENUM.TRUNCATE_AND_INSERT);
		StringBuilder sbSql = new StringBuilder();
		for (DocTable docTable : destDocTables) {
			sbSql.append("TRUNCATE TABLE " + defaultSchemaName + "." + docTable.getTableName() + ";");
			sbSql.append(System.lineSeparator());
		}
		sbSql.append(System.lineSeparator());
		for (DocTable destDocTable : destDocTables) {
			DocTable srcDocTable = srcDocTables.stream()
					.filter(dt -> dt.getTableName().equals(destDocTable.getTableName())).findFirst().orElse(null);
			if (srcDocTable == null) {
				continue;
			}
			boolean isColumnNumEquals = (destDocTable.getColumns().size() == srcDocTable.getColumns().size());

			sbSql.append("INSERT INTO " + defaultSchemaName + "." + destDocTable.getTableName());
			sbSql.append(System.lineSeparator());
			sbSql.append("(");
			for (int i = 0; i < destDocTable.getColumns().size(); i++) {
				DocColumn docColumn = destDocTable.getColumns().get(i);
				sbSql.append(docColumn.getColName());
				if (i != destDocTable.getColumns().size() - 1) {
					sbSql.append(",");
				}
				sbSql.append(System.lineSeparator());
			}
			sbSql.append(") ");
			sbSql.append(System.lineSeparator());
			sbSql.append("SELECT ");
			sbSql.append(System.lineSeparator());
			if (isColumnNumEquals) {
				appendDocColumnsToSql(sbSql, "", srcDocTable);
			} else {
				// 来源表和目标表字段数量不相同
				sbSql.append("SYS_GUID() AS UUID, ");
				sbSql.append(System.lineSeparator());
				appendDocColumnsToSql(sbSql, "", srcDocTable);
				sbSql.append(",");
				sbSql.append("'0', ");
				sbSql.append(System.lineSeparator());
				sbSql.append("'SYSTEM' AS D_CREATEDBYID, ");
				sbSql.append(System.lineSeparator());
				sbSql.append("SYSDATE AS D_CREATEDTIME, ");
				sbSql.append(System.lineSeparator());
				sbSql.append("'SYSTEM' AS C_LASTMODIFIEDBYID, ");
				sbSql.append(System.lineSeparator());
				sbSql.append("SYSDATE AS D_LASTMODIFIEDTIME");
				sbSql.append(System.lineSeparator());
			}

			sbSql.append(" FROM " + fromSchemaName + "." + srcDocTable.getTableName() + ";");
			sbSql.append(System.lineSeparator());
			sbSql.append(System.lineSeparator());
		}

		writeSqlToFile(folderPath, fileName, sbSql);
		return folderPath + fileName;
	}

	public String generateOnInsertTrigger(String folderPath, List<DocTable> srcDocTables,
			List<DocTable> destDocTables) {
		Config conf = ConfigUtil.getConfig();
		String defaultSchemaName = conf.getString("database.defaultschema");

		String fileName = getOutputFileName(OPERATION_TYPE_ENUM.ON_INSERT_TRIGGER);
		StringBuilder sbSql = new StringBuilder();
		for (DocTable destDocTable : destDocTables) {
			DocTable srcDocTable = srcDocTables.stream()
					.filter(dt -> dt.getTableName().equals(destDocTable.getTableName())).findFirst().orElse(null);
			if (srcDocTable == null) {
				continue;
			}
			sbSql.append("create or replace trigger trg_i_" + destDocTable.getTableName().substring(1)
					+ " AFTER INSERT ON ");
			sbSql.append(System.lineSeparator());
			sbSql.append(destDocTable.getTableName() + " FOR EACH ROW BEGIN ");
			sbSql.append(System.lineSeparator());
			sbSql.append("INSERT INTO ");
			sbSql.append(defaultSchemaName + "." + destDocTable.getTableName() + "(");
			sbSql.append(System.lineSeparator());
			appendDocColumnsToSql(sbSql, "", destDocTable);
			sbSql.append(") VALUES (");
			sbSql.append(System.lineSeparator());
			sbSql.append("SYS_GUID(), ");
			sbSql.append(System.lineSeparator());
			appendDocColumnsToSql(sbSql, ":new.", srcDocTable);
			sbSql.append(",");
			sbSql.append("'0', ");
			sbSql.append("'SYSTEM', ");
			sbSql.append("SYSDATE, ");
			sbSql.append("'SYSTEM', ");
			sbSql.append("SYSDATE);");
			sbSql.append(System.lineSeparator());
			sbSql.append("END;");
			sbSql.append(System.lineSeparator());
			sbSql.append("/");
			sbSql.append(System.lineSeparator());
			sbSql.append(System.lineSeparator());
		}
		writeSqlToFile(folderPath, fileName, sbSql);
		return folderPath + fileName;
	}

	public String generateOnUpdateTrigger(String folderPath, List<DocTable> docTables) {
		Config conf = ConfigUtil.getConfig();
		String defaultSchemaName = conf.getString("database.defaultschema");

		String fileName = getOutputFileName(OPERATION_TYPE_ENUM.ON_UPDATE_TRIGGER);
		StringBuilder sbSql = new StringBuilder();
		for (DocTable docTable : docTables) {
			sbSql.append(
					"create or replace trigger trg_u_" + docTable.getTableName().substring(1) + " AFTER UPDATE ON ");
			sbSql.append(System.lineSeparator());
			sbSql.append(docTable.getTableName() + " FOR EACH ROW BEGIN UPDATE ");
			sbSql.append(System.lineSeparator());
			sbSql.append(defaultSchemaName + "." + docTable.getTableName() + " SET ");
			sbSql.append(System.lineSeparator());
			for (int i = 0; i < docTable.getColumns().size(); i++) {
				DocColumn docColumn = docTable.getColumns().get(i);
				sbSql.append(docColumn.getColName() + " =:new." + docColumn.getColName());
				sbSql.append(",");
				sbSql.append(System.lineSeparator());
			}
			sbSql.append("C_LASTMODIFIEDBYID = 'SYSTEM', ");
			sbSql.append(System.lineSeparator());
			sbSql.append("D_LASTMODIFIEDTIME = SYSDATE");
			sbSql.append(System.lineSeparator());
			sbSql.append("WHERE ");
			sbSql.append(System.lineSeparator());
			for (int i = 0; i < docTable.getSyncColumns().size(); i++) {
				String syncColumn = docTable.getSyncColumns().get(i);
				sbSql.append(syncColumn + " =:old." + syncColumn);
				if (i != docTable.getSyncColumns().size() - 1) {
					sbSql.append(" AND ");
					sbSql.append(System.lineSeparator());
				}
			}
			sbSql.append(System.lineSeparator());
			sbSql.append(";");
			sbSql.append(System.lineSeparator());
			sbSql.append("END;");
			sbSql.append(System.lineSeparator());
			sbSql.append("/");
			sbSql.append(System.lineSeparator());
			sbSql.append(System.lineSeparator());
		}
		writeSqlToFile(folderPath, fileName, sbSql);
		return folderPath + fileName;
	}

	public String generateOnDeleteTrigger(String folderPath, List<DocTable> docTables) {
		Config conf = ConfigUtil.getConfig();
		String defaultSchemaName = conf.getString("database.defaultschema");

		String fileName = getOutputFileName(OPERATION_TYPE_ENUM.ON_DELETE_TRIGGER);
		StringBuilder sbSql = new StringBuilder();
		for (DocTable docTable : docTables) {
			sbSql.append(
					"create or replace trigger trg_d_" + docTable.getTableName().substring(1) + " AFTER DELETE ON ");
			sbSql.append(System.lineSeparator());
			sbSql.append(docTable.getTableName() + " FOR EACH ROW BEGIN UPDATE ");
			sbSql.append(System.lineSeparator());
			sbSql.append(defaultSchemaName + "." + docTable.getTableName() + " SET ");
			sbSql.append(System.lineSeparator());
			sbSql.append("C_ISDELETED = '1',");
			sbSql.append(System.lineSeparator());
			sbSql.append("D_LASTMODIFIEDTIME = SYSDATE,");
			sbSql.append(System.lineSeparator());
			sbSql.append("C_LASTMODIFIEDBYID = 'SYSTEM' ");
			sbSql.append(System.lineSeparator());
			sbSql.append("WHERE ");
			sbSql.append(System.lineSeparator());
			for (int i = 0; i < docTable.getSyncColumns().size(); i++) {
				String syncColumn = docTable.getSyncColumns().get(i);
				sbSql.append(syncColumn + " =:old." + syncColumn);
				if (i != docTable.getSyncColumns().size() - 1) {
					sbSql.append(" AND ");
					sbSql.append(System.lineSeparator());
				}
			}
			sbSql.append(System.lineSeparator());
			sbSql.append(";");
			sbSql.append(System.lineSeparator());
			sbSql.append("END;");
			sbSql.append(System.lineSeparator());
			sbSql.append("/");
			sbSql.append(System.lineSeparator());
			sbSql.append(System.lineSeparator());
		}
		writeSqlToFile(folderPath, fileName, sbSql);
		return folderPath + fileName;
	}

	public String generateEnableTrigger(String folderPath, List<DocTable> docTables) {
		Config conf = ConfigUtil.getConfig();
		String defaultSchemaName = conf.getString("database.defaultschema");

		String fileName = getOutputFileName(OPERATION_TYPE_ENUM.ENABLE_TRIGGER);
		StringBuilder sbSql = new StringBuilder();
		for (DocTable docTable : docTables) {
			sbSql.append("ALTER TABLE " + defaultSchemaName + "." + docTable.getTableName() + " ENABLE ALL TRIGGERS;");
			sbSql.append(System.lineSeparator());
		}
		writeSqlToFile(folderPath, fileName, sbSql);
		return folderPath + fileName;
	}

	public String generateDisableTrigger(String folderPath, List<DocTable> docTables) {
		Config conf = ConfigUtil.getConfig();
		String defaultSchemaName = conf.getString("database.defaultschema");

		String fileName = getOutputFileName(OPERATION_TYPE_ENUM.DISABLE_TRIGGER);
		StringBuilder sbSql = new StringBuilder();
		for (DocTable docTable : docTables) {
			sbSql.append("ALTER TABLE " + defaultSchemaName + "." + docTable.getTableName() + " DISABLE ALL TRIGGERS;");
			sbSql.append(System.lineSeparator());
		}
		writeSqlToFile(folderPath, fileName, sbSql);
		return folderPath + fileName;
	}

	public String generateGrantDmlPrivilege(String folderPath, List<DocTable> docTables) {
		Config conf = ConfigUtil.getConfig();
		String defaultSchemaName = conf.getString("database.defaultschema");
		String fromSchemaName = conf.getString("database.fromschema");

		String fileName = getOutputFileName(OPERATION_TYPE_ENUM.GRANT_DML_PRIVILEGE);
		StringBuilder sbSql = new StringBuilder();
		for (DocTable docTable : docTables) {
			sbSql.append("GRANT SELECT, INSERT, UPDATE, DELETE ON " + defaultSchemaName + "." + docTable.getTableName()
					+ " TO " + fromSchemaName + ";");
			sbSql.append(System.lineSeparator());
		}
		writeSqlToFile(folderPath, fileName, sbSql);
		return folderPath + fileName;
	}

	private String getOutputFileName(OPERATION_TYPE_ENUM operationType) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		switch (operationType) {
		case CREATE:
			return "CreateTable_" + LocalDateTime.now().format(formatter) + ".xml";
		case CREATE_WITH_COMMON_COLUMNS:
			return "CreateTableWithCommonColumns_" + LocalDateTime.now().format(formatter) + ".xml";
		case DROP:
			return "DropTable_" + LocalDateTime.now().format(formatter) + ".xml";
		case TRUNCATE_AND_INSERT:
			return "TruncateAndInsert_" + LocalDateTime.now().format(formatter) + ".sql";
		case ON_INSERT_TRIGGER:
			return "OnInsertTrigger_" + LocalDateTime.now().format(formatter) + ".sql";
		case ON_UPDATE_TRIGGER:
			return "OnUpdateTrigger_" + LocalDateTime.now().format(formatter) + ".sql";
		case ON_DELETE_TRIGGER:
			return "OnDeleteTrigger_" + LocalDateTime.now().format(formatter) + ".sql";
		case ENABLE_TRIGGER:
			return "EnableTrigger_" + LocalDateTime.now().format(formatter) + ".sql";
		case DISABLE_TRIGGER:
			return "DisableTrigger_" + LocalDateTime.now().format(formatter) + ".sql";
		case GRANT_DML_PRIVILEGE:
			return "GrantDmlPrivilege_" + LocalDateTime.now().format(formatter) + ".sql";
		default:
			break;
		}
		return "";
	}

	private void writeSqlToFile(String folderPath, String fileName, StringBuilder sbSql) {
		File file = new File(folderPath + fileName);
		CharSink sink = Files.asCharSink(file, Charsets.UTF_8);
		try {
			sink.write(sbSql.toString());
		} catch (IOException e) {
			logger.error("Write to file failed.", e);
		}
	}

	private void appendDocColumnsToSql(StringBuilder sbSql, String prefix, DocTable docTable) {
		for (int i = 0; i < docTable.getColumns().size(); i++) {
			DocColumn docColumn = docTable.getColumns().get(i);
			sbSql.append(prefix + docColumn.getColName());
			if (i != docTable.getColumns().size() - 1) {
				sbSql.append(",");
			}
			sbSql.append(System.lineSeparator());
		}
	}
}
