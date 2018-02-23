package com.galaxyyao.yuri_dbtoy.domain.changelog;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="databaseChangeLog")
@XmlAccessorType(XmlAccessType.FIELD)
public class DatabaseChangeLog {
    @XmlElement
    protected List<ChangeSet> changeSet;

	public List<ChangeSet> getChangeSet() {
		return changeSet;
	}

	public void setChangeSet(List<ChangeSet> changeSet) {
		this.changeSet = changeSet;
	}
}
