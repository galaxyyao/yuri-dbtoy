package com.galaxyyao.yuri_dbtoy.domain.changelog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class DropTable {
	@XmlAttribute
	protected String cascadeConstraints;

	@XmlAttribute
	protected String catalogName;

	@XmlAttribute
	protected String schemaName;

	@XmlAttribute
	protected String tableName;

	public String getCascadeConstraints() {
		return cascadeConstraints;
	}

	public void setCascadeConstraints(String cascadeConstraints) {
		this.cascadeConstraints = cascadeConstraints;
	}

	public String getCatalogName() {
		return catalogName;
	}

	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
