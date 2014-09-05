package com.qbit.commons.log.model;

import com.qbit.commons.model.Identifiable;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alex
 */
@Entity
@Access(AccessType.FIELD)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Log implements Identifiable<String>, Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private String id;
	@Temporal(TemporalType.TIMESTAMP)
	private Date logTimestamp;
	private OperationType type;
	private String userId;
	private String userAdditionalIds;
	private String entityId;
	private String fieldName;
	@Lob
	private String fieldValue;
	private String machineId;
	@OneToOne(cascade = CascadeType.ALL)
	private Location location;
	private String sessionId;
	private String locale;

	@Override
	public String getId() {
		return id;
	}

	public Date getLogTimestamp() {
		return logTimestamp;
	}

	public void setLogTimestamp(Date logTimestamp) {
		this.logTimestamp = logTimestamp;
	}

	public OperationType getType() {
		return type;
	}

	public void setType(OperationType type) {
		this.type = type;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	public String getMachineId() {
		return machineId;
	}

	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	public String getUserAdditionalIds() {
		return userAdditionalIds;
	}

	public void setUserAdditionalIds(String userAdditionalIds) {
		this.userAdditionalIds = userAdditionalIds;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	public boolean isValid() {
		return (logTimestamp != null)
				&& (type != null)
				&& (userId != null) && !userId.isEmpty();
	}

	@Override
	public String toString() {
		return "Log{" + "id=" + id + ", logTimestamp=" + logTimestamp + ", type=" + type + ", userId=" + userId + ", entityId=" + entityId + ", fieldName=" + fieldName + ", fieldValue=" + fieldValue + ", machineId=" + machineId + '}';
	}
}
