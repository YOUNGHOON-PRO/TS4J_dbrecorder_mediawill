/*
* Ŭ������: Statistics_MID.java
* ��������: JDK 1.4.1
* ��༳��: MID �� ��� ó��
* �ۼ�����: 2003-04-04 �ϱ���
 */

package com.dbrecorder;

public class Statistics_MID
{
	public String MID;
	public String SUBID;
	public int TID;
	public int Ecode[];
	public int Scount;
	public int Ccount;

	public Statistics_MID()
	{
		MID = "";
		SUBID = "";
		TID = 0;
		Scount = 0;
		Ccount = 0;
		Ecode = new int[24];
		for( int i = 0; i < 24; i++ ) {
			Ecode[i]=0;
		}
	}
}
