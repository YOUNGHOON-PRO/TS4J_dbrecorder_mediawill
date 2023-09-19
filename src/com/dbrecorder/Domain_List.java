/*
* 클래스명: DBRecorder_Insert.java
* 버전정보: JDK 1.4.1
* 요약설명: DOMAIN 별 통계 처리
* 작성일자: 2003-04-04 하광범_a
 */

package com.dbrecorder;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.text.*;

import com.config.Config_File_Receiver;
import com.log.LogWriter;
import com.dbrecorder.Statistics_DOMAIN;

public class Domain_List
{
	Config_File_Receiver config_File_Receiver;
	LogWriter logWriter;

	public Domain_List()
	{
		config_File_Receiver = Config_File_Receiver.getInstance();
		logWriter = new LogWriter();
	}

	/**
	 *    MID  별 등록 DOMAIN 리스트 만듬
	 *    @param    int tmpMID
	 *    @return   Vector
	 */

	public Vector getDomainList(Connection conn, String tmpYY, String tmpMM)
	{
		Vector vTmp = new Vector();

		Statement stmt = null;
		ResultSet rs = null;

		try
		{
//			String strQuery_stat_DOMAIN = "SELECT DOMAINNAME, SYY, SMM FROM TS_MAILQUEUE_DOMAIN WHERE SYY = ? AND SMM = ?";
			String strQuery_stat_DOMAIN = "SELECT DOMAINNAME FROM TS_DOMAIN_INFO";

			stmt = conn.createStatement();
//			stmt.setString(1, tmpYY);
//			stmt.setString(2, tmpMM);

			rs = stmt.executeQuery(strQuery_stat_DOMAIN);

			while( rs.next() )
			{
				Statistics_DOMAIN statistics_DOMAIN = new Statistics_DOMAIN();
				statistics_DOMAIN.DomainName = (rs.getString("DOMAINNAME")).toLowerCase();
				statistics_DOMAIN.YY = tmpYY; //rs.getString("SYY");
				statistics_DOMAIN.MM = tmpMM; //rs.getString("SMM");

				vTmp.addElement(statistics_DOMAIN);
			}

			return vTmp;
		}
		catch(Exception e)
		{
			logWriter.logWrite("Domain_List", "getDomainList", e);
			return null;
		}
		finally {
			try
			{
				if( stmt != null ) {
					stmt.close();
					stmt = null;
				}
			}
			catch(Exception e) {
			}

			try
			{
				if( rs != null ) {
					rs.close();
					rs = null;
				}
			}
			catch(Exception e) {
			}
		}
	}
} // End of Class
