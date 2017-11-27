package com.nauth.api;

public class SessionInfo {
	public String getAnnounceIp() {
		return announceIp;
	}
	public void setAnnounceIp(String announceIp) {
		this.announceIp = announceIp;
	}
	public String getAnnounceUseragent() {
		return announceUseragent;
	}
	public void setAnnounceUseragent(String announceUseragent) {
		this.announceUseragent = announceUseragent;
	}
	public String getAnnounceInfo() {
		return announceInfo;
	}
	public void setAnnounceInfo(String announceInfo) {
		this.announceInfo = announceInfo;
	}
	public byte[] getAnnounceLogo() {
		return announceLogo;
	}
	public void setAnnounceLogo(byte[] announceLogo) {
		this.announceLogo = announceLogo;
	}
	public String getSessionIp() {
		return sessionIp;
	}
	public void setSessionIp(String sessionIp) {
		this.sessionIp = sessionIp;
	}
	public String getSessionUseragent() {
		return sessionUseragent;
	}
	public void setSessionUseragent(String sessionUseragent) {
		this.sessionUseragent = sessionUseragent;
	}
	public String getSessionInfo() {
		return sessionInfo;
	}
	public void setSessionInfo(String sessionInfo) {
		this.sessionInfo = sessionInfo;
	}
	public byte[] getSessionLogo() {
		return sessionLogo;
	}
	public void setSessionLogo(byte[] sessionLogo) {
		this.sessionLogo = sessionLogo;
	}
	private String announceIp;
	private String announceUseragent;
	private String announceInfo;
	private byte[] announceLogo;
	
	private String sessionIp;
	private String sessionUseragent;
	private String sessionInfo;
	private byte[] sessionLogo;
	
}
