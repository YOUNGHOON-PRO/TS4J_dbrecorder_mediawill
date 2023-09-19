/*
 * Ŭ������: DBRecorder_Insert.java
 * ��������: JDK 1.4.1
 * ��༳��: DataBase insert (�߼� �α�)
 * �ۼ�����: 2003-04-04 �ϱ���_a
 */

package com.dbrecorder;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.text.*;

import com.config.Config_File_Receiver;
import com.log.LogWriter;
import com.util.EncryptUtil;

import com.custinfo.safedata.*;

import com.dbrecorder.Statistics_MID;
import com.dbrecorder.Statistics_DOMAIN;
import com.dbrecorder.Domain_List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBRecorder_Insert {
	
  private static final Logger LOGGER = LogManager.getLogger(DBRecorder_Insert.class.getName());
	
  private Config_File_Receiver config_File_Receiver;
  private LogWriter logWriter;

  private Statistics_MID statistics_MID;
  private Statistics_DOMAIN statistics_DOMAIN;
  private Domain_List domain_List;

  private Vector vLog;
  private Vector vErrorLog;
  private Vector vStatistics_MID;
  private Vector vDomainList;
  private Vector vStatistics_DOMAIN;
  private Hashtable domainHash;
  private Vector dataVector = null;

  private Vector vTmpYYMM;

  private Connection conn;
  private Connection conn_stat_MID;
  private Connection conn_stat_DOMAIN;
  private Connection conn_insert_DOMAIN;
  private Connection conn_insert_DOMAIN2;

  PreparedStatement pstmt;
  PreparedStatement pstmt_stat_MID;
  PreparedStatement pstmt_stat_DOMAIN;
  PreparedStatement pstmt_insert_DOMAIN;
  PreparedStatement pstmt_insert_DOMAIN2;

  int logDataNum;
  int errorDataNum;

  public DBRecorder_Insert() {
    config_File_Receiver = Config_File_Receiver.getInstance();
    logWriter = new LogWriter();

    domain_List = new Domain_List();

    dataVector = new Vector();
    vLog = new Vector();
    vErrorLog = new Vector();
    vStatistics_MID = new Vector();
    vStatistics_DOMAIN = new Vector();
    vTmpYYMM = new Vector();
    domainHash = new Hashtable();
  }

  private synchronized String getLogDate() {
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM_dd");
    return fmt.format(new java.util.Date());
  }

  private synchronized String getLogMonth() {
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM");
    return fmt.format(new java.util.Date());
  }

  public synchronized boolean commit() {
    boolean returnValue = true;

    try {
      conn.commit();
      conn_stat_MID.commit();
      conn_stat_DOMAIN.commit();
      conn_insert_DOMAIN.commit();
      conn_insert_DOMAIN2.commit();
    }
    catch (Exception e) {
    	LOGGER.error(e);
      logWriter.logWrite("DBRecorder_Insert", "commit()", e);
      returnValue = false;
    }
    finally {
      try {
        close();
      }
      catch (Exception e) {
    	  LOGGER.error(e);
      }
    }
    return returnValue;
  }

  /**
   * �α� ��� �����丮 ����
   * @param oldLogFile �α� ���� ��ü
   * @param startTime ���� �ð�
   * @param removeLogFile ������ �α�����
   * @param endTime ����ð�
   */
  public synchronized void insertHistory(String oldLogFile, String startTime,
                                         String removeLogFile, String endTime) {
    File logHistory = null;
    FileWriter fw = null;
    PrintWriter historyWriter = null;
    StringBuffer sb = null;

    try {
      sb = new StringBuffer();
      logHistory = new File(sb.append("NeoQueueInfo")
                            .append(File.separator).append("Log")
                            .append(File.separator).append(getLogMonth()).
                            toString());

      if (!logHistory.exists()) {
        logHistory.mkdir();
      }

      sb = new StringBuffer();
      //File logHistoryFile=new File(logHistory.getAbsolutePath(),getLogDate()+".log");
      fw = new FileWriter(sb.append(logHistory.getAbsolutePath())
                          .append(File.separator).append(getLogDate())
                          .append(".log").toString(), true);
      historyWriter = new PrintWriter(fw);

      sb = new StringBuffer();
      historyWriter.print(sb.append("[ ").append(startTime)
                          .append(" ~ ").append(endTime).append(" ] ")
                          .append(System.getProperty("line.separator")).append(
          oldLogFile)
                          .append(" ==> ").append(removeLogFile)
                          .append(System.getProperty("line.separator"))
                          .append("RESULT    : ").append(logDataNum).append("/")
                          .append(errorDataNum)
                          .append(System.getProperty("line.separator")).
                          toString());
    }
    catch (Exception e) {
    	LOGGER.error(e);
      logWriter.logWrite("DBRecorder_Insert", "insertHistory()", e);
    }
    finally {
      sb = null;
      try {
        if (fw != null) {
          fw.close();
          fw = null;
        }
      }
      catch (Exception e) {
    	  LOGGER.error(e);
      }

      try {
        if (historyWriter != null) {
          historyWriter.close();
          historyWriter = null;
        }
      }
      catch (Exception e) {
    	  LOGGER.error(e);
      }
    }
  }

  public void rollback() {
    try {
      conn.rollback();
      conn_stat_MID.rollback();
      conn_stat_DOMAIN.rollback();
      conn_insert_DOMAIN.rollback();
      conn_insert_DOMAIN2.rollback();
    }
    catch (Exception e) {
    	LOGGER.error(e);
      logWriter.logWrite("DBRecorder_Insert", "rollback()", e);
    }
    finally {
      try {
        close();
      }
      catch (Exception e) {
    	  LOGGER.error(e);
      }
    }
  }

  /**
   * ready connections to need.
   */
  private boolean connection() {
    boolean bResult = true;
    try {
      Class.forName(config_File_Receiver.DB_DRIVER);
      conn = getConnection();
      conn_stat_MID = getConnection();
      conn_stat_DOMAIN = getConnection();
      conn_insert_DOMAIN = getConnection();
      conn_insert_DOMAIN2 = getConnection();
    }
    catch (Exception e) {
    	LOGGER.error(e);
      bResult = false;
      logWriter.logWrite("DBRecorder_Insert", "connection()", e);
    }
    return bResult;
  }

  private Connection getConnection() throws Exception {
    Connection connection = DriverManager.getConnection(config_File_Receiver.
        DBURL,
        config_File_Receiver.USER, config_File_Receiver.PASSWD);
    	connection.setAutoCommit(false);
    	connection.commit();

    return connection;
  }

  /**
   * MID �� ��� - �߼� ���� �ڵ忡 ���� �з�
   * @param tmpMID ���� �߼��׸� ���̵�
   * @param tmpEcode ��� �ڵ�
   */
  private synchronized void statistics_MID(String tmpMID, String tmpSUBID,
                                           int tmpEcode) {
    StringBuffer sb = null;
    try {
      int tmpDebugEcode = 0;

      if (vStatistics_MID.isEmpty()) {
        statistics_MID = new Statistics_MID();
        statistics_MID.MID = tmpMID;
        statistics_MID.SUBID = tmpSUBID;

        if (tmpEcode == 41) {
          tmpDebugEcode = 22;
          statistics_MID.Ecode[22] = 1;
        }
        else if (tmpEcode == 42) {
          tmpDebugEcode = 23;
          statistics_MID.Ecode[23] = 1;
        }
        else {
          tmpDebugEcode = tmpEcode;
          statistics_MID.Ecode[tmpEcode] = 1;
        }

        statistics_MID.Ccount = 1;

        if (tmpEcode <= 21) {
          statistics_MID.Scount = 1;
        }

        vStatistics_MID.addElement(statistics_MID);
        sb = new StringBuffer();
        
        LOGGER.info(sb.append("ADD vStatistics_MID\t : MID = ")
		                .append(statistics_MID.MID).append(" Ecode[")
		                .append(tmpEcode).append("] = ")
		                .append(statistics_MID.Ecode[tmpDebugEcode]).
		                toString());
      }
      else {
        boolean isUpdateMID = true;

        for (int i = 0; i < vStatistics_MID.size(); i++) {
          Statistics_MID tmp_MID = (Statistics_MID) vStatistics_MID.elementAt(i);
          if (tmp_MID.MID.equalsIgnoreCase(tmpMID) &&
              tmp_MID.SUBID.equalsIgnoreCase(tmpSUBID)) {
            if (tmpEcode == 41) {
              tmpDebugEcode = 22;
              tmp_MID.Ecode[22] = statistics_MID.Ecode[22] + 1;
            }
            else if (tmpEcode == 42) {
              tmpDebugEcode = 23;
              tmp_MID.Ecode[23] = statistics_MID.Ecode[23] + 1;
            }
            else {
              tmpDebugEcode = tmpEcode;
              tmp_MID.Ecode[tmpEcode] = tmp_MID.Ecode[tmpEcode] + 1;
            }

            //int tmpNum=tmp_MID.Ecode[tmpEcode];
            //tmp_MID.Ecode[tmpEcode]=tmpNum+1;

            tmp_MID.Ccount = tmp_MID.Ccount + 1;

            if (tmpEcode <= 21) {
              tmp_MID.Scount = tmp_MID.Scount + 1;
            }

            isUpdateMID = false;

            sb = new StringBuffer();

            LOGGER.info(sb.append("UPDATE vStatistics_MID\t : MID = ")
		                    .append(tmp_MID.MID).append(" Ecode[").append(tmpEcode)
		                    .append("] = ").append(tmp_MID.Ecode[tmpDebugEcode]).toString());
            
            break;
          }
        }

        if (isUpdateMID) {
          statistics_MID = new Statistics_MID();
          statistics_MID.MID = tmpMID;
          statistics_MID.SUBID = tmpSUBID;

          if (tmpEcode == 41) {
            tmpDebugEcode = 22;
            statistics_MID.Ecode[22] = 1;
          }
          else if (tmpEcode == 42) {
            tmpDebugEcode = 23;
            statistics_MID.Ecode[23] = 1;
          }
          else {
            tmpDebugEcode = tmpEcode;
            statistics_MID.Ecode[tmpEcode] = 1;
          }

          //statistics_MID.Ecode[tmpEcode]=1;
          statistics_MID.Ccount = 1;

          if (tmpEcode <= 21) {
            statistics_MID.Scount = 1;
          }

          vStatistics_MID.addElement(statistics_MID);

          
//          LOGGER.info("ADD vStatistics_MID\t : MID = "
//                         +statistics_MID.MID+" Ecode["+tmpEcode+"] = "
//                         +statistics_MID.Ecode[tmpDebugEcode]);
          
        }
      }
    }
    catch (Exception e) {
    	LOGGER.error(e);
    }
    finally {
      sb = null;
    }
  }

  /**
   * �����κ� ��� - �߼� ���� �ڵ忡 ���� �з�
   * @param tmpDate �߼�����
   */
  private synchronized void addDomainList(String tmpDate) {
    StringTokenizer st = new StringTokenizer(tmpDate, "/");
    String tmpYY = st.nextToken().substring(2, 4);
    String tmpMM = st.nextToken();

    Vector tmpVector = domain_List.getDomainList(conn, tmpYY, tmpMM);

    if (tmpVector.isEmpty()) {
      return;
    }

    for (int i = 0; i < tmpVector.size(); i++) {
      vStatistics_DOMAIN.addElement(tmpVector.elementAt(i));
    }
  }

  /**
   * �����κ� ��� - �߼� ���� �ڵ忡 ���� �з�
   * @param tmpEcode �߼۰�� �ڵ�
   * @param email �̸��� �ּ�
   * @param tmpDate �߼� �ð�
   */
  private synchronized void statistics_Domain(int tmpEcode, String email,
                                              String tmpDate) {
	  
	   //��ȣȭ/��ȣȭ
		String ALGORITHM = "PBEWithMD5AndDES";
		String KEYSTRING = "ENDERSUMS";
		EncryptUtil enc =  new EncryptUtil();
		
		CustInfoSafeData safeDbEnc = new CustInfoSafeData();

		//��ȣȭ
      if("Y".equals(config_File_Receiver.ENC_YN)) {
    	  try {
			email = enc.getJasyptDecryptedFixString(ALGORITHM, KEYSTRING, email);
    		//email = safeDbEnc.getDecrypt(email, "NOT_RNNO");
		} catch (Exception e) {
			LOGGER.error(e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
      }
	  
    LOGGER.info("Email check : " + email);
    StringTokenizer st = new StringTokenizer(tmpDate, "/");
    String tmpYY = st.nextToken().substring(2, 4);
    String tmpMM = st.nextToken();

    StringTokenizer st1 = new StringTokenizer(email.toLowerCase(), "@");
    st1.nextToken();
    String tmpDomainName = st1.nextToken();

    String key = new StringBuffer().append(tmpDomainName).append("\t")
        .append(tmpYY).append("\t").append(tmpMM).toString();

    Statistics_DOMAIN sd = null;
    if (domainHash.containsKey(key)) {
      sd = (Statistics_DOMAIN) domainHash.get(key);
    }
    else {
      sd = new Statistics_DOMAIN();
      sd.DomainName = tmpDomainName;
      sd.YY = tmpYY;
      sd.MM = tmpMM;
      domainHash.put(key, sd);
    }

    if (tmpEcode == 41) {
      sd.Ecode[22] = sd.Ecode[22] + 1;
    }
    else if (tmpEcode == 42) {
      sd.Ecode[23] = sd.Ecode[23] + 1;
    }
    else {
      sd.Ecode[tmpEcode] = sd.Ecode[tmpEcode] + 1;
    }
  }

  /**
   * �߼� �α� Insert RESULTLOG TABLE
   * @param filePath �α����ϸ�
   * @return ���Ե� �α� ��
   */
  public int insertLog(String filePath) {
    LOGGER.info("NOW LOGFILE NAME : " + filePath);
    int returnValue = 0, length = 0;
    StringBuffer sb = null;
    LineNumberReader lineReader = null;

    if (!connection()) {
      return -1;
    }

    try {
      File logFile = new File(filePath);
      lineReader = new LineNumberReader(new FileReader(logFile));

      sb = new StringBuffer();
      String strQuery = sb.append("INSERT INTO TS_RESULTLOG(MID, ")
          .append("SUBID, REFMID, TID, RID, RNAME, RMAIL, SID, ")
          .append("SNAME, SMAIL, RCODE, STIME, BIZKEY) ")
          .append("VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)").toString();
      pstmt = conn.prepareStatement(strQuery);

      sb = new StringBuffer();
      String strQuery_stat = sb.append("UPDATE TS_MAILQUEUE_RSINFO SET ")
          .append("ECODE01 = ECODE01+?, ECODE02 = ECODE02+?, ")
          .append("ECODE03 = ECODE03+?, ECODE04 = ECODE04+?, ")
          .append("ECODE05 = ECODE05+?, ECODE06 = ECODE06+?, ")
          .append("ECODE07 = ECODE07+?, ECODE08 = ECODE08+?, ")
          .append("ECODE09 = ECODE09+?, ECODE10 = ECODE10+?, ")
          .append("ECODE11 = ECODE11+?, ECODE12 = ECODE12+?, ")
          .append("ECODE13 = ECODE13+?, ECODE14 = ECODE14+?, ")
          .append("ECODE15 = ECODE15+?, ECODE16 = ECODE16+?, ")
          .append("ECODE17 = ECODE17+?, ECODE18 = ECODE18+?, ")
          .append("ECODE19 = ?, ECODE20 = ?, ")
          .append("ECODE21 = ECODE21+?, ECODE41 = ECODE41+?, ")
          .append("ECODE42 = ECODE42+?, CCOUNT = CCOUNT+?, SCOUNT = SCOUNT+? ")
          .append("WHERE MID = ? AND SUBID = ?").toString();
      //pstmt_stat_MID = conn_stat_MID.prepareStatement(strQuery_stat);
      pstmt_stat_MID = conn.prepareStatement(strQuery_stat);

      sb = new StringBuffer();
      String strQuery_stat_DOMAIN = sb.append("UPDATE TS_MAILQUEUE_DOMAIN ")
          .append(
          "SET ECODE01 = ECODE01+?, ECODE02 = ECODE02+?, ECODE03 = ECODE03+?, ")
          .append(
          "ECODE04 = ECODE04+?, ECODE05 = ECODE05+?, ECODE06 = ECODE06+?, ")
          .append(
          "ECODE07 = ECODE07+?, ECODE08 = ECODE08+?, ECODE09 = ECODE09+?, ")
          .append(
          "ECODE10 = ECODE10+?, ECODE11 = ECODE11+?, ECODE12 = ECODE12+?, ")
          .append(
          "ECODE13 = ECODE13+?, ECODE14 = ECODE14+?, ECODE15 = ECODE15+?, ")
          .append(
          "ECODE16 = ECODE16+?, ECODE17 = ECODE17+?, ECODE18 = ECODE18+?, ")
          .append(
          "ECODE19 = ECODE19+?, ECODE20 = ECODE20+?, ECODE21 = ECODE21+?, ")
          .append("ECODE41 = ECODE41+?, ECODE42 = ECODE42+? ")
          .append("WHERE DOMAINNAME = ? AND SYY = ? AND SMM = ?").toString();
      //pstmt_stat_DOMAIN = conn_stat_DOMAIN.prepareStatement(strQuery_stat_DOMAIN);
      pstmt_stat_DOMAIN = conn.prepareStatement(strQuery_stat_DOMAIN);

      sb = new StringBuffer();
      String strQuery_insert_DOMAIN = sb.append(
          "INSERT INTO TS_MAILQUEUE_DOMAIN ")
          .append(
          "(DOMAINNAME, SYY, SMM, ECODE01, ECODE02, ECODE03, ECODE04, ECODE05, ")
          .append(
          "ECODE06, ECODE07, ECODE08, ECODE09, ECODE10, ECODE11, ECODE12, ")
          .append(
          "ECODE13, ECODE14, ECODE15, ECODE16, ECODE17, ECODE18, ECODE19, ECODE20, ")
          .append("ECODE21, ECODE41, ECODE42) SELECT ")
          .append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ")
          .append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ")
          .append("FROM TS_COPY WHERE ? IN (SELECT DOMAINNAME FROM ")
          .append("TS_DOMAIN_INFO)").toString();
      //pstmt_insert_DOMAIN = conn_insert_DOMAIN.prepareStatement(strQuery_insert_DOMAIN);
      pstmt_insert_DOMAIN = conn.prepareStatement(strQuery_insert_DOMAIN);
      
      sb = new StringBuffer();
      String strQuery_insert_DOMAIN2 = sb.append(
          "INSERT INTO TS_MAILQUEUE_DOMAIN ")
          .append(
          "(DOMAINNAME, SYY, SMM, ECODE01, ECODE02, ECODE03, ECODE04, ECODE05, ")
          .append(
          "ECODE06, ECODE07, ECODE08, ECODE09, ECODE10, ECODE11, ECODE12, ")
          .append(
          "ECODE13, ECODE14, ECODE15, ECODE16, ECODE17, ECODE18, ECODE19, ECODE20, ")
          .append("ECODE21, ECODE41, ECODE42) VALUES (")
          .append(
          "'etc', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ")
          .append("?, ?, ?, ?, ?, ?)").toString();
      //pstmt_insert_DOMAIN2 = conn_insert_DOMAIN2.prepareStatement(strQuery_insert_DOMAIN2);
      pstmt_insert_DOMAIN2 = conn.prepareStatement(strQuery_insert_DOMAIN2);

      sb = null;
      String logData = null;
      StringTokenizer st = null, st2 = null;
      
      ///��ȣȭ/��ȣȭ
      String ALGORITHM = "PBEWithMD5AndDES";
      String KEYSTRING = "ENDERSUMS";
      EncryptUtil enc =  new EncryptUtil();
      
      CustInfoSafeData safeDbEnc = new CustInfoSafeData();

      while ( (logData = lineReader.readLine()) != null) {
        try {
          st = new StringTokenizer(logData, config_File_Receiver.Log_Sep);

          if(st.countTokens() < 13){
            throw new Exception("Format of Log is invalid.");
          }

          /**
           * logData ��ü�� ������ ������ ����.
           * MODE|MID|SUBID|REFMID|TID|RID|RNAME|RMAIL|SID|SNAME|SMAIL|RCODE|STIME
           * ��ü�̸��� �˾ƺ��� ���� ����� �ҽ� ���Ⱑ �ȴ�.
           */

          //MODE
          String MODE = st.nextToken(); // 225 �Ǵ� 325

          //MID
          String tmp0 = st.nextToken();
          st2 = new StringTokenizer(tmp0, "_");
          String tmp1 = st2.nextToken();

          //SUBID
          int tmp2 = Integer.parseInt(st.nextToken());
          //REFMID
          int tmp3 = Integer.parseInt(st.nextToken());
          //TID
          int tmp4 = Integer.parseInt(st.nextToken());
          //RID
          String tmp5 = st.nextToken();
          //RNAME
          String tmp6 = st.nextToken();
          //RMAIL
          String tmp7 = st.nextToken();
          
          //RMAIL ��ȣȭ
          if("Y".equals(config_File_Receiver.ENC_YN)) {
        	  tmp7 = enc.getJasyptEncryptedFixString(ALGORITHM, KEYSTRING, tmp7);
        	  //tmp7 = safeDbEnc.getEncrypt(tmp7, "NOT_RNNO");
          }
          
          //SID
          String tmp8 = st.nextToken();
          //SNAME
          String tmp9 = st.nextToken();
          //SMAIL
          String tmp10 = st.nextToken();
          
          //SMAIL ��ȣȭ
          if("Y".equals(config_File_Receiver.ENC_YN)) {
        	  tmp10 = enc.getJasyptEncryptedFixString(ALGORITHM, KEYSTRING, tmp10);
        	  //tmp10 = safeDbEnc.getEncrypt(tmp10, "NOT_RNNO");
          }
          
          //RCODE
          String tmp11 = st.nextToken();
          //STIME
          String tmp12 = st.nextToken();

          String tmp13 = st.nextToken();  // IP (192.168.74.175)
          String tmp14 = st.nextToken();  // ������ 0
          String tmp15 = st.nextToken();  // REQUEST_KEY (CMP-20221122132409)
          
          tmp15 = tmp15+"."+tmp5;
          
          if (tmp5.equals("null")) {
            tmp5 = null;
          }

          if (tmp6.equals("null")) {
            tmp6 = null;
          }

          statistics_MID(tmp1, String.valueOf(tmp2), Integer.parseInt(tmp11));
          statistics_Domain(Integer.parseInt(tmp11), tmp7, tmp12);

          pstmt.setString(1, tmp1);
          pstmt.setInt(2, tmp2);
          pstmt.setInt(3, tmp3);
          pstmt.setInt(4, tmp4);
          pstmt.setString(5, tmp5);
          pstmt.setString(6, tmp6);
          pstmt.setString(7, tmp7);
          pstmt.setString(8, tmp8);
          pstmt.setString(9, tmp9);
          pstmt.setString(10, tmp10);
          pstmt.setString(11, tmp11);
          pstmt.setString(12, tmp12);
          pstmt.setString(13, tmp15);

          pstmt.executeUpdate();
          logDataNum++;
        }
        catch (Exception e) {
        	LOGGER.error(e);
          e.printStackTrace();
          logWriter.logWrite("DBRecorder_Insert", "insertLog() - ", e);
          dataVector.addElement(logData);
          errorDataNum++;
        }
      }

      //Update Statistics MAILQUEUE_RSINFO, MAILQUEUE_DOMAIN
      for (int i = 0; i < vStatistics_MID.size(); i++) {
        pstmt_stat_MID.clearParameters();
        statistics_MID = (Statistics_MID) vStatistics_MID.elementAt(i);

        int j = 1;
        for (; j < 24; j++) {
          pstmt_stat_MID.setInt(j, statistics_MID.Ecode[j]);
          //LOGGER.info("Ecode"+j+"\t:"+statistics_MID.Ecode[j]);
        }

        pstmt_stat_MID.setInt(j, statistics_MID.Ccount);
        pstmt_stat_MID.setInt(j + 1, statistics_MID.Scount);
        pstmt_stat_MID.setString(j + 2, statistics_MID.MID);
        pstmt_stat_MID.setString(j + 3, statistics_MID.SUBID);

        pstmt_stat_MID.executeUpdate();
      }
      vStatistics_MID.removeAllElements();

      String key = "";
      Statistics_DOMAIN sd = null;
      Enumeration en = domainHash.keys();
      while (en.hasMoreElements()) {

        key = (String) en.nextElement();
        sd = (Statistics_DOMAIN) domainHash.get(key);

        pstmt_stat_DOMAIN.clearParameters();
        // ������ ��������� ���� ������Ʈ �����Ѵ�.
        int j = 1;
        length = sd.Ecode.length;
        for (; j < length; j++) {
          pstmt_stat_DOMAIN.setInt(j, sd.Ecode[j]);
        }
        pstmt_stat_DOMAIN.setString(j, sd.DomainName);
        pstmt_stat_DOMAIN.setString(j + 1, sd.YY);
        pstmt_stat_DOMAIN.setString(j + 2, sd.MM);

        int count = 0;
        try {

          // ������ ������� ������Ʈ
          count = pstmt_stat_DOMAIN.executeUpdate();

          // ������Ʈ ���� 0�̸�..
          if (count == 0) {

            pstmt_insert_DOMAIN.clearParameters();

            // ������̺� ���ο� ������ ������ �Է��Ѵ�.
            // �Է��� �� TS_DOMAIN_INFO���̺� �ִ� �����͸� �Էµȴ�.
            pstmt_insert_DOMAIN.setString(1, sd.DomainName);
            pstmt_insert_DOMAIN.setString(2, sd.YY);
            pstmt_insert_DOMAIN.setString(3, sd.MM);

            j = 1;
            length = sd.Ecode.length;
            for (; j < length; j++) {
              pstmt_insert_DOMAIN.setInt(j + 3, sd.Ecode[j]);
            }

            pstmt_insert_DOMAIN.setString(27, sd.DomainName);

            try {

              count = pstmt_insert_DOMAIN.executeUpdate();
              // TS_DOMAIN_INFO���̺� ���� ���̺��� �ƴϸ�..
              if (count == 0) {

                pstmt_stat_DOMAIN.clearParameters();

                j = 1;
                length = sd.Ecode.length;
                for (; j < length; j++) {
                  pstmt_stat_DOMAIN.setInt(j, sd.Ecode[j]);
                }

                pstmt_stat_DOMAIN.setString(j, "etc");
                pstmt_stat_DOMAIN.setString(j + 1, sd.YY);
                pstmt_stat_DOMAIN.setString(j + 2, sd.MM);

                try {

                  // etc ������ ��� ������ ������Ʈ�ϰ�..
                  count = pstmt_stat_DOMAIN.executeUpdate();
                  // etc ������ ��� ������ ������..
                  if (count == 0) {
                    pstmt_insert_DOMAIN2.clearParameters();
                    pstmt_insert_DOMAIN2.setString(1, sd.YY);
                    pstmt_insert_DOMAIN2.setString(2, sd.MM);

                    j = 1;
                    length = sd.Ecode.length;
                    for (; j < length; j++) {
                      pstmt_insert_DOMAIN2.setInt(j + 2, sd.Ecode[j]);
                    }

                    try {
                      // etc ������ ��������� �Է��Ѵ�.
                      pstmt_insert_DOMAIN2.executeUpdate();
                    }
                    catch (Exception e) {
                    	LOGGER.error(e);
                    	LOGGER.error(strQuery_insert_DOMAIN2);
                    }
                  }
                }
                catch (Exception e) {
                	LOGGER.error(e);
                	LOGGER.error(strQuery_stat_DOMAIN);
                  count = 0;
                }
              }
            }
            catch (Exception e) {
            	LOGGER.error(e);
            	LOGGER.error(strQuery_insert_DOMAIN);
              count = 0;
            }
          }
        }
        catch (Exception e) {
          LOGGER.error(e);
          LOGGER.error(strQuery_stat_DOMAIN);
        }
      } //end of while

      logFile = null;
    }
    catch (Exception e) {
    	LOGGER.error(e);
      e.printStackTrace();
      logWriter.logWrite("DBRecorder_Insert", "insertLog()", e);
      return -2;
    }
    finally {
      try {
        this.writeLog(dataVector);
      }
      catch (Exception e) {LOGGER.error(e);}

      sb = null;
      try {
        if (lineReader != null) {
          lineReader.close();
          lineReader = null;
        }
      }
      catch (Exception e) {}
    }

    return logDataNum;
  }

  private void writeLog(Vector dtVector) {
    if (dtVector.isEmpty()) {
      return;
    }

    StringBuffer sb = new StringBuffer();
    String path = sb.append(config_File_Receiver.Root_Dir)
        .append(File.separator).append("Exception").toString();
    File filePath = new File(path);

    if (!filePath.exists()) {
      filePath.mkdir();
    }

    PrintWriter errorPrint = null;
    try {
      sb = new StringBuffer();
      errorPrint = new PrintWriter(
          new FileWriter(sb.append(path)
                         .append(File.separator).append("Exception.log").
                         toString(), true));
      Iterator it = dtVector.iterator();
      while (it.hasNext()) {
        String strData = (String) it.next();
        errorPrint.println(strData);
        errorPrint.flush();
      }
    }
    catch (IOException ie) {
    	LOGGER.error(ie);
}
    finally {
      if (!dtVector.isEmpty()) {
        dtVector.clear();
      }

      try {
        if (errorPrint != null) {
          errorPrint.close();
          errorPrint = null;
        }
      }
      catch (Exception e) {
    	  LOGGER.error(e);
      }
    }
  }

  private void close() throws Exception {
    pstmt.close();
    pstmt_stat_MID.close();
    pstmt_stat_DOMAIN.close();
    pstmt_insert_DOMAIN.close();
    pstmt_insert_DOMAIN2.close();
    conn.close();
    conn_stat_MID.close();
    conn_stat_DOMAIN.close();
    conn_insert_DOMAIN.close();
    conn_insert_DOMAIN2.close();
  }
}
