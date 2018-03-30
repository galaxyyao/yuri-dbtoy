package com.galaxyyao.yuri_dbtoy.domain.changelog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ChangeSet {
	@XmlAttribute
	protected String id;
	
	@XmlAttribute
	protected String author;
	
	@XmlElement
	protected CreateTable createTable;
	
	@XmlElement
	protected DropTable dropTable;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public CreateTable getCreateTable() {
		return createTable;
	}

	public void setCreateTable(CreateTable createTable) {
		this.createTable = createTable;
	}

	public DropTable getDropTable() {
		return dropTable;
	}

	public void setDropTable(DropTable dropTable) {
		this.dropTable = dropTable;
	}
}
