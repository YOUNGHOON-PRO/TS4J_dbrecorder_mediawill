package com.dbrecorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DemonCheck_DBrecorder extends Thread{
	
	private static final Logger LOGGER = LogManager.getLogger(DemonCheck_DBrecorder.class.getName());
	
	private String task="";
	
	public DemonCheck_DBrecorder(String task) {
		
		this.task=task;
		LOGGER.info("["+task+"] Start!!");
	}
	
	public void run() {
		
		while(true) {
			try {
				LOGGER.info("["+task+"] Alive..");
				sleep(15000);
			}catch (InterruptedException e) {
				LOGGER.error(e);
			}
		}
		
	}
}
