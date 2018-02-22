package AuditTrailSQL;

import java.sql.Date;
import java.sql.Timestamp;

public class AuditTrailEntry {
	private String recordDescriptor;
	private Timestamp dateAdded;
	private String message;
	
	public AuditTrailEntry(String recordDescriptor, Timestamp dateAdded, String message){
		this.recordDescriptor = recordDescriptor;
		this.dateAdded = dateAdded;
		this.message = message;
	}
	
	public AuditTrailEntry(){
		recordDescriptor = "";
		dateAdded = null;
		message = "";
	}

	public String getRecordDescriptor() {
		return recordDescriptor;
	}

	public void setRecordDescriptor(String recordDescriptor) {
		this.recordDescriptor = recordDescriptor;
	}

	public Timestamp getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(Timestamp dateAdded) {
		this.dateAdded = dateAdded;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	 public String toString(){
		 return dateAdded + "\n" + message;
	 }
}
