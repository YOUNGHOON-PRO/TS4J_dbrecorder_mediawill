/*
* Ŭ������: DBRecorder.java
* ��������: JDK 1.4.1
* ��༳��: ���� ����
* �ۼ�����: 2003-04-04 �ϱ���
 */

package com.dbrecorder;

import java.io.*;
import java.util.*;
import java.text.*;

import com.config.Config_File_Receiver;
import com.log.LogWriter;
import com.dbrecorder.DBRecorder_Insert;
import com.util.NeoQueue_DirManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBRecorder extends Thread
{
	
	private static final Logger LOGGER = LogManager.getLogger(DBRecorder.class.getName());
	
	Config_File_Receiver config_File_Receiver;
	LogWriter logWriter;
	DBRecorder_Insert dbRecorder_Insert;
	NeoQueue_DirManager neoQueue_DirManager;

	Vector vLogFileList;
	Vector vRollbackLogFileList;

	String startTime;

	public DBRecorder()
	{
		config_File_Receiver = Config_File_Receiver.getInstance();
		logWriter = new LogWriter();

		vLogFileList = new Vector();
		vRollbackLogFileList = new Vector();

		neoQueue_DirManager = new NeoQueue_DirManager();
		neoQueue_DirManager.start();

		this.start();
		
		new DemonCheck_DBrecorder("DBrecorder").start();
	}

	public void run()
	{
		while( true )
		{
			try
			{
				makeLogFileList();
				insertLog();
				this.sleep(config_File_Receiver.Log_Write_Period*1000*60);
			}
			catch(Exception e) {
				LOGGER.error(e);
				logWriter.logWrite("DBRecorder", "run()", e);
			}
		}
	}

	private synchronized String getLogDate()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM_dd");
		return fmt.format(new java.util.Date());
	}

	private synchronized String getLogTime()
	{
		String tmpDateStr = "";
		Calendar rightNow = Calendar.getInstance();
		int tmpHour = 0;

		if( rightNow.get(Calendar.AM_PM) == 1 ) {
			tmpHour=12;
		}

		tmpDateStr = (new StringBuffer().append(tmpHour).append(rightNow.get(Calendar.HOUR))
					  .append(":").append(rightNow.get(Calendar.MINUTE)).append(":")
					  .append(rightNow.get(Calendar.SECOND))).toString();

		return tmpDateStr;
	}

	/**
	 *    �߼� �α� ��� : DBRecorder_Insert.java insertLog() -�߼� �α� Insert RESULTLOG TABLE
	 *    @param    none
	 *    @return   none
	 */
	private synchronized void insertLog()
	{
		if( vLogFileList.isEmpty() ) {
			return;
		}

		for( int i = 0; i < vLogFileList.size(); i++ )
		{
			startTime = getLogDate() + " " + getLogTime();
			dbRecorder_Insert = new DBRecorder_Insert();
			int tmpResult = dbRecorder_Insert.insertLog((String)vLogFileList.elementAt(i));
			//debug
			LOGGER.info("tmpResult:"+tmpResult);
			if( tmpResult > 0 )
			{
				if( dbRecorder_Insert.commit() ) {
					removeLogFile((String)vLogFileList.elementAt(i));
				}
			}
			else
			{
				dbRecorder_Insert.rollback();
				vRollbackLogFileList.addElement((String)vLogFileList.elementAt(i));
				LOGGER.info("tdbRecorder_Insert.rollback()");
			}
		}

		vLogFileList.removeAllElements();
	}

	/**
	 *    �߼� �α� Insert RESULTLOG TABLE : �Ϸ�� ȭ�� ���
	 *    @param    String logFile
	 *    @return   none
	 */
	private synchronized void removeLogFile(String logFile)
	{
		StringBuffer sb = new StringBuffer();
		File logDir = new File(sb.append("NeoQueueInfo").append(File.separator)
							  .append("bak").append(File.separator+getLogDate()).toString());

		if( !logDir.exists() ) {
			logDir.mkdir();
		}

		if( logDir.exists() )
		{
			File file = new File(logFile);
			File removeFile = new File(logDir.getAbsolutePath(), file.getName());

			if( file.renameTo(removeFile) )
			{
				sb = new StringBuffer();
				String endTime = sb.append(getLogDate()).append(" ").append(getLogTime()).toString();
				dbRecorder_Insert.insertHistory(logFile, startTime, removeFile.getAbsolutePath(), endTime);
			}
		}
	}

	/**
	 *    �߼� �α� ȭ�� ����Ʈ ����
	 *    @param    none
	 *    @return   none
	 */

	private synchronized void makeLogFileList()
	{
		int length = config_File_Receiver.Root_Dir.length;
		int length2, length3;

		for( int i = 0; i < length; i++ )
		{
			String lFile = new StringBuffer().append(config_File_Receiver.Root_Dir[i])
			 .append(File.separator).append("Log").toString();
			File logFile = new File(lFile);
			String logFileName[] = logFile.list();

			if( logFileName != null )
			{
				length2 = logFileName.length;
				for( int k = 0; k < length2; k++ )
				{
					File logDir = new File(logFile, logFileName[k]);
					String logDirName[] = logDir.list();

					if( logDirName != null )
					{
						length3 = logDirName.length;
						for( int j = 0; j < length3; j++ )
						{
							File tmpFile = new File(logDir, logDirName[j]);

							Date nowDate = new Date();
							long nowDateLong = nowDate.getTime();

							int tmpPeriod = (int)((nowDateLong-tmpFile.lastModified())/(1000*60));

							if( tmpPeriod > config_File_Receiver.Log_Write_Period ) {
								vLogFileList.addElement(tmpFile.getAbsolutePath());
							}
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		new DBRecorder();
	}

        public static void shutdown() {
          LOGGER.info("DBRecorder shutdown.");
          System.exit(0);
        }

}
