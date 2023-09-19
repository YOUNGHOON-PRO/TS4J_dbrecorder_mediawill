/**
 * 클래스명: Statistics_DOMAIN
 * 버전정보: JDK 1.4.1
 * 요약설명: DOMAIN 별 통계 처리
 * 작성일자: 2003-04-04 하광범
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
