/**
 * Ŭ������: Statistics_DOMAIN
 * ��������: JDK 1.4.1
 * ��༳��: DOMAIN �� ��� ó��
 * �ۼ�����: 2003-04-04 �ϱ���
 */

package com.dbrecorder;

public class Statistics_DOMAIN
{
    public String DomainName;
    public int Ecode[];
    public String YY;
    public String MM;

    public Statistics_DOMAIN()
    {
        DomainName = "";
        YY = "";
        MM = "";
        Ecode = new int[24];
        for( int i = 0; i < 24; i++ ) {
            Ecode[i] = 0;
        }
    }
}
