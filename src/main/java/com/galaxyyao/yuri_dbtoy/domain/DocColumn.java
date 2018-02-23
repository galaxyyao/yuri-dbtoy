package com.galaxyyao.yuri_dbtoy.domain;

public class DocColumn {
	private int colIndex;
	
	private String colName;
	
	private String colDesc;
	
	private String colType;
	
	private Boolean isAllowNull;
	
	private Boolean isPrimaryKey;
	
	private String defaultValue;

	public int getColIndex() {
		return colIndex;
	}

	public void setColIndex(int colIndex) {
		this.colIndex = colIndex;
	}

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public String getColDesc() {
		return colDesc;
	}

	public void setColDesc(String colDesc) {
		this.colDesc = colDesc;
	}

	public String getColType() {
		return colType;
	}

	public void setColType(String colType) {
		this.colType = colType;
	}

	public Boolean getIsAllowNull() {
		return isAllowNull;
	}

	public void setIsAllowNull(Boolean isAllowNull) {
		this.isAllowNull = isAllowNull;
	}

	public Boolean getIsPrimaryKey() {
		return isPrimaryKey;
	}

	public void setIsPrimaryKey(Boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
