package com.galaxyyao.yuri_dbtoy.domain;

import java.util.List;

public class DocTable {
	private int tableIndex;
	
	private String tableName;
	
	private String tableDesc;
	
	private List<String> syncColumns;
	
	private List<DocColumn> columns;
	
	private List<String> uniqueConstraintColumns;
	
	private List<DocIndex> indexes;
	
	private Boolean isSelected;

	public int getTableIndex() {
		return tableIndex;
	}

	public void setTableIndex(int tableIndex) {
		this.tableIndex = tableIndex;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableDesc() {
		return tableDesc;
	}

	public void setTableDesc(String tableDesc) {
		this.tableDesc = tableDesc;
	}

	public List<DocColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<DocColumn> columns) {
		this.columns = columns;
	}

	public Boolean getIsSelected() {
		return isSelected;
	}

	public void setIsSelected(Boolean isSelected) {
		this.isSelected = isSelected;
	}

	public List<String> getSyncColumns() {
		return syncColumns;
	}

	public void setSyncColumns(List<String> syncColumns) {
		this.syncColumns = syncColumns;
	}

	public List<String> getUniqueConstraintColumns() {
		return uniqueConstraintColumns;
	}

	public void setUniqueConstraintColumns(List<String> uniqueConstraintColumns) {
		this.uniqueConstraintColumns = uniqueConstraintColumns;
	}

	public List<DocIndex> getIndexes() {
		return indexes;
	}

	public void setIndexes(List<DocIndex> indexes) {
		this.indexes = indexes;
	}
}
