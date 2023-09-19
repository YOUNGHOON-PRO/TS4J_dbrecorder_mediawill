/**
 * Ŭ������: Config_File_Receiver.java
 * ��������: JDK 1.4.1
 * ��༳��: ���� ����
 * �ۼ�����: 2003-04-04 �ϱ���
 */


//2003.10.1 ���ǰ�ħ
//�̱��� �𵨷� �ٲ�
package com.config;

import java.io.*;
import java.util.*;

import com.log.LogWriter;
import com.util.EncryptUtil;

public class Config_File_Receiver
{
	private static Config_File_Receiver instance;

	LogWriter logWriter;

	public static int Log_Write_Period;
	public static int Dir_Delete_Period;
	public static String Log_Sep;

        //DB Configuration
	public static String DB_DRIVER;
	public static String DBURL;
	public static String USER;
	public static String PASSWD;
	
	public static String PASSWD_YN;
	public static String ENC_YN;
	
	public static int NeoSMTP_Agent;
	public static String Root_Dir[];

	public static String MERGE_QUEUE_FOLDER;

	//2003.10.22���� �߰�
	public static int GARBAGE_START_TIME;

	StringTokenizer st;

	//��ü�� ��´�.
	public static Config_File_Receiver getInstance()
	{
		if( instance == null )//���� �����
		{
			instance = new Config_File_Receiver();
			return instance;

		}
		else	//�ι�° ����...
		{
			return instance;
		}
	}

        /**
         *  load configuration.
         */
        private Config_File_Receiver()
	{
		logWriter = new LogWriter();

                //FileInputStream for configuration.
                FileInputStream config = null;
                FileInputStream dbconf = null;

                Properties props = new Properties();
                
    	        //��ȣȭ
    			String ALGORITHM = "PBEWithMD5AndDES";
    			String KEYSTRING = "ENDERSUMS";
    			EncryptUtil enc =  new EncryptUtil();                

		try
		{
			//configuration for work.
                        //config = new FileInputStream("../config/DBRecorder.conf");
                        config = new FileInputStream("./config/DBRecorder.conf");
			props.load(config);

			NeoSMTP_Agent=Integer.parseInt(props.getProperty("NeoSMTP_Agent"));
			ENC_YN = props.getProperty("ENC_YN");
			
			String Root_Dir_str=props.getProperty("Root_Dir");
			st=new StringTokenizer(Root_Dir_str,",");

			Root_Dir=new String[NeoSMTP_Agent];

			for( int i = 0; i < NeoSMTP_Agent; i++ ) {
				Root_Dir[i]=(st.nextToken().trim());
			}

			MERGE_QUEUE_FOLDER = Root_Dir[0] + File.separator + "Merge_Queue";

			Log_Write_Period=Integer.parseInt(props.getProperty("Log_Write_Period"));
			Dir_Delete_Period=Integer.parseInt(props.getProperty("Dir_Delete_Period"));
			Log_Sep=props.getProperty("Log_Sep");

                        //configuration for db connection.
                        //dbconf = new FileInputStream("../config/database.conf");
                        dbconf = new FileInputStream("./config/database.conf");
                        props.load(dbconf);

                        DB_DRIVER=props.getProperty("DRIVER");
			DBURL=props.getProperty("URL");
			USER=props.getProperty("USER");
			PASSWD=props.getProperty("PASSWARD");
			
			PASSWD_YN = props.getProperty("PASSWARD_YN");
			
			
			//��ȣȭ
			if("Y".equals(PASSWD_YN)) {
				PASSWD = enc.getJasyptDecryptedFixString(ALGORITHM, KEYSTRING, PASSWD);
			}
		}
		catch(Exception e)
		{
			logWriter.logWrite("CONFIG_FILE_RECEIVER ERROR","construct",e);
		}finally{
                  try{
                    if (config != null)
                      config.close();
                    if (dbconf != null)
                      dbconf.close();
                  }catch(Exception e){}
                }
	}
}
