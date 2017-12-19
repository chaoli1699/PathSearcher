package cn.cienet.pathsearcher.utils;

import javax.crypto.Cipher;

import cn.cienet.encrypt.encode.Base64Util;
import cn.cienet.encrypt.symmetric.DESUtil;

public class EncryptUtils {

	private static final String ENCRYPT_KEY="cn.cienet.pathsearcher";
	
	public static String encodeString(String content){
		return Base64Util.base64EncodeStr(DESUtil.des(content, ENCRYPT_KEY, Cipher.ENCRYPT_MODE));
	}
	
	public static String decodeString(String content){
		return DESUtil.des(Base64Util.base64DecodedStr(content), ENCRYPT_KEY, Cipher.DECRYPT_MODE);
	}
}
