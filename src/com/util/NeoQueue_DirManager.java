package com.util;

import java.io.*;
import java.util.*;

import com.config.Config_File_Receiver;
import com.log.LogWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//2003.10.1 ���� ����
//�ϴ� subfolder�� ���°Ÿ� (1,2,3....�� ���°Ÿ� ) �� mid ������ ���쵵�� �����Ѵ�.
public class NeoQueue_DirManager extends Thread
{
	
	private static final Logger LOGGER = LogManager.getLogger(NeoQueue_DirManager.class.getName());
	
	Config_File_Receiver config_File_Receiver;
	LogWriter logWriter;

	Vector vContentsDir;

	public NeoQueue_DirManager()
	{
		super();

		logWriter = new LogWriter();
		config_File_Receiver = Config_File_Receiver.getInstance();
		vContentsDir = new Vector();
	}

	//���� ������ 1,2,3 ������ ���°Ÿ� �����ش�.
	private void dirDelete(String tmpSubPath)
	{
		//System.out.println("����͵�:"+vContentsDir);
		File fileList = new File(tmpSubPath);

		//1,2,3�� ������ ���ٸ�... �ϴ� �װ��� �����.
		if( fileList.list() == null )
		{
			//System.out.println("fileList�� �ƿ����ٴ� �Ҹ���");
		}
		else	//������ �հ� �մٴ� �Ҹ���
		{
			if( fileList.list().length == 0 )
			{
				while( true ) {
					if( fileList.delete() ) {
						//vContentsDir.remove(tmpSubPath);
						break;
					}
				}
				//	System.out.println("������ ����:"+fileList.getName());
			}
		}
	}

//    private void dirDelete(String tmpSubPath)
//    {
//        File fileList=new File(tmpSubPath);
//        String fileName[]=fileList.list();
//
//        if(fileName!=null)
//        {
//            for(int i=0;i<fileName.length;i++)
//            {
//                File sendFile=new File(fileList.getAbsolutePath()+File.separator+fileName[i]);
//                deleteFile(sendFile);
//            }
//
//            try
//            {
//                deleteFile(fileList);
//            }
//            catch(Exception e)
//            {
//                logWriter.logWrite("NeoQueue_DirManager","dirDelete(String tmpSubPath)",e);
//                return;
//            }
//        }
//        else
//        {
//            fileList.delete();
//            System.out.println("������ ����:"+fileList.getName());
//        }
//    }
//
//    private synchronized void deleteFile(File delFile)
//    {
//        try
//        {
//            if(delFile.exists())
//            {
//            	System.out.println("������ ���ϸ�:"+delFile.getName());
//                delFile.delete();
//            }
//
//        }
//        catch(Exception e)
//        {
//            logWriter.logWrite("NeoQueue_DirManager","deleteFile(File delFile)",e);
//        }
//    }

	public void run()
	{
		while( true )
		{
			try
			{
				makeManageDir();

				this.sleep(100);

				if( !vContentsDir.isEmpty() )
				{
					int vecSize = vContentsDir.size();

					for( int i = 0; i < vecSize; i++ ) {
						dirDelete((String) vContentsDir.get(i));
					}
					//�� �����ְ� clear ��Ų��.

					vContentsDir.clear();
				}
				this.sleep(1000*60*config_File_Receiver.Dir_Delete_Period);
			}
			catch(Exception e) {
				LOGGER.error(e);
				logWriter.logWrite("NeoQueue_DirManager","run()",e);
			}
		}
	}

	private void makeManageDir()
	{
		try
		{
			StringBuffer sb = null;
			int length = config_File_Receiver.Root_Dir.length, length2;
			for( int i = 0; i < length; i++ )
			{
				sb = new StringBuffer();
				File dirlist = new File(sb.append(config_File_Receiver.Root_Dir[i])
										.append(File.separator).append("Merge_Queue").toString());
				String dirlistName[] = dirlist.list();

				if( dirlistName != null )
				{
					length2 = dirlistName.length;
					for( int j = 0; j < length2; j++ )
					{
						File subfile = new File(dirlist, dirlistName[j]);
						String subfileName[] = subfile.list();

						if( subfileName.length == 0 ) {
							if( !vContentsDir.contains(subfile.getAbsolutePath()) ) {
								vContentsDir.addElement(subfile.getAbsolutePath());
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.error(e);
			logWriter.logWrite("NeoQueue_DirManager","makeMangeDir()",e);
		}
	}
}
