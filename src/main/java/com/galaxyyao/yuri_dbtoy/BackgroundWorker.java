package com.galaxyyao.yuri_dbtoy;

import java.util.List;

import com.galaxyyao.yuri_dbtoy.domain.DocTable;
import com.galaxyyao.yuri_dbtoy.poi.ExcelUtil;

public class BackgroundWorker {
	private String path;

	public BackgroundWorker(String path) {
		this.path = path;
	}

	public List<DocTable> readExcelAndGenerateSql() {
		return ExcelUtil.read(path);
	}
}
