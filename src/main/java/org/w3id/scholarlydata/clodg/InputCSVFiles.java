package org.w3id.scholarlydata.clodg;

import java.io.File;

public class InputCSVFiles {

	public static final String AUTHORS = "authors.csv";
	public static final String COMMITTEE = "committee.csv";
	public static final String KEYNOTE = "keynote.csv";
	public static final String ORGANISING = "organising.csv";
	public static final String SESSION = "session.csv";
	public static final String SUBMISSION = "submission.csv";
	public static final String SWC_ROLES = "swc_roles.csv";
	public static final String TALK = "talk.csv";
	public static final String TRACK= "track.csv";
	
	
	private boolean authors, committee, keynote, organising, session, submission, swcRoles, talk, track;
	
	public InputCSVFiles() {
		authors = committee = keynote = organising = session = submission = swcRoles = talk = track = false;
	}
	
	public InputCSVFiles(File...files) {
		this();
		
		for(File file : files){
			String fileName = file.getName();
			
			if(fileName.equals(AUTHORS)) authors = true;
			else if(fileName.equals(COMMITTEE)) committee = true;
			else if(fileName.equals(KEYNOTE)) keynote = true;
			else if(fileName.equals(ORGANISING)) organising = true;
			else if(fileName.equals(SESSION)) session = true;
			else if(fileName.equals(SUBMISSION)) submission = true;
			else if(fileName.equals(SWC_ROLES)) swcRoles = true;
			else if(fileName.equals(TALK)) talk = true;
			else if(fileName.equals(TRACK)) track = true;
			
		}
	}
	
	public void setAuthors(boolean authors) {
		this.authors = authors;
	}
	
	public boolean hasAuthors() {
		return authors;
	}
	
	public void setCommittee(boolean committee) {
		this.committee = committee;
	}
	
	public boolean hasCommittee() {
		return committee;
	}
	
	public void setKeynote(boolean keynote) {
		this.keynote = keynote;
	}
	
	public boolean hasKeynote() {
		return keynote;
	}
	
	public void setOrganising(boolean organising) {
		this.organising = organising;
	}
	
	public boolean hasOrganising() {
		return organising;
	}
	
	public void setSession(boolean session) {
		this.session = session;
	}
	
	public boolean hasSession() {
		return session;
	}
	
	public void setSubmission(boolean submission) {
		this.submission = submission;
	}
	
	public boolean hasSubmission() {
		return submission;
	}
	
	public void setSwcRoles(boolean swcRoles) {
		this.swcRoles = swcRoles;
	}
	
	public boolean hasSwcRoles() {
		return swcRoles;
	}
	
	public void setTalk(boolean talk) {
		this.talk = talk;
	}
	
	public boolean hasTalk() {
		return talk;
	}
	
	public void setTrack(boolean track) {
		this.track = track;
	}
	
	public boolean hasTrack() {
		return track;
	}
	
}
