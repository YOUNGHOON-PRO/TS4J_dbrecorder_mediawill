/*
* Ŭ������: LogWriter.java
* ��������: JDK 1.3.1
* ��༳��: Exception ó�� �� Exception �α� ���� ���
* �ۼ�����: 2003-04-04 �ϱ���
 */

package com.log;

import java.io.*;
import java.util.*;
import java.text.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogWriter
{
	
	private static final Logger LOGGER = LogManager.getLogger(LogWriter.class.getName());
	
	//File logHistoryFile;
	private FileWriter fw = null;
	private StringBuffer sb = null;

	public LogWriter()
	{
	}

	private synchronized String getLogDate()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM_dd");
		return fmt.format(new java.util.Date());
	}

	private synchronized String getLogTime()
	{
		String tmpDateStr = "";
		String returnVal = "";
		Calendar rightNow = Calendar.getInstance();
		int tmpHour = 0;
		if( rightNow.get(Calendar.AM_PM) == 1 ) {
			tmpHour = 12;
		}

		sb = new StringBuffer();
		tmpDateStr= sb.append(tmpHour).append(rightNow.get(Calendar.HOUR)).append(":")
			.append(rightNow.get(Calendar.MINUTE)).append(":")
			.append(rightNow.get(Calendar.SECOND)).toString();

		sb = new StringBuffer();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
		returnVal = sb.append(fmt.format(new java.util.Date())).append(" ").append(tmpDateStr).toString();
		sb = null;
		return returnVal;
	}

	public synchronized void logWrite(String classFile, String classMethod, Exception errorStr)
	{
		PrintWriter historyWriter = null;
		try
		{
			sb = new StringBuffer();
			LOGGER.info(sb.append(classFile).append(" - ").append(classMethod)
					   .append(" : ").append(errorStr).toString());

			sb = new StringBuffer();
			//logHistoryFile=new File("NeoQueueInfo"+File.separator+"error"+File.separator+getLogDate()+".err");
			fw = new FileWriter(sb.append("NeoQueueInfo").append(File.separator)
								.append("error").append(File.separator).append(getLogDate())
								.append(".err").toString(), true);
			historyWriter = new PrintWriter(fw);

			sb = new StringBuffer();
			historyWriter.print(sb.append("[ ").append(getLogTime()).append(" ]")
								.append(classFile).append(" - ").append(classMethod)
								.append(" : ").append(errorStr)
								.append(System.getProperty("line.separator"))
								.append(System.getProperty("line.separator")).toString());
		}
		catch(Exception e) {
			LOGGER.error(e);
		}
		finally {
			sb = null;
			try {
				if( fw != null ) {
					fw.close();
					fw = null;
				}
			}
			catch(Exception e) {}

			try {
				if( historyWriter != null ) {
					historyWriter.close();
					historyWriter = null;
				}
			}
			catch(Exception e) {LOGGER.error(e);}
		}
	}
}
