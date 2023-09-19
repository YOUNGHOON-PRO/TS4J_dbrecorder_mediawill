package com.custinfo.safedata;

import com.util.EncryptUtil;

public class CustInfoSafeData {
	
	/**
	 * ?•”?˜¸?™”
	 * @param data
	 * @param type (RNNO/NOT_RNNO)
	 * @return
	 * @throws Exception
	 */
	public synchronized String getEncrypt(String data, String type) throws Exception {
		return EncryptUtil.getJasyptEncryptedFixString("PBEWithMD5AndDES", type, data);
	}
	
	/**
	 * ë³µí˜¸?™”
	 * @param data
	 * @param type (RNNO/NOT_RNNO)
	 * @return
	 * @throws Exception
	 */
	public synchronized String getDecrypt(String data, String type) throws Exception {
		return EncryptUtil.getJasyptDecryptedFixString("PBEWithMD5AndDES", type, data);
	}
}
