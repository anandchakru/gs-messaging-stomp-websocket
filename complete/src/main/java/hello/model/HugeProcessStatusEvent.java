package hello.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class HugeProcessStatusEvent implements Serializable {
	private String generationId;
	private String sessionId;
	private String status;
	private boolean isCompleted;

	public HugeProcessStatusEvent() {
		super();
	}
	public HugeProcessStatusEvent(String sessionId, String status) {
		super();
		this.sessionId = sessionId;
		this.status = status;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public boolean isCompleted() {
		return isCompleted;
	}
	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}
	public String getGenerationId() {
		return generationId;
	}
	public void setGenerationId(String generationId) {
		this.generationId = generationId;
	}
}