package com.galaxyyao.yuri_dbtoy.domain;

import java.util.List;

public class DocTable {
	private int tableIndex;
	
	private String tableName;
	
	private String tableDesc;
	
	private List<DocColumn> columns;
	
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
}
