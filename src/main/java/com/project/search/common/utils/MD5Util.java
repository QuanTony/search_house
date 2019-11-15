package com.project.search.common.utils;

import com.project.search.common.exception.BusinessException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public class MD5Util {
	
	public static String md5(String src) {
		return DigestUtils.md5Hex(src);
	}
	
	public static String salt = "";

	MD5Util(String salt){
		this.salt = salt;
	}

	/**
	 * 获取加盐md5后的密码
	 * @param formPwd
	 * @param salt
	 * @return
	 */
	public static String formPassToDBPass(String formPwd, String salt) {
		String str = "";
		if (StringUtils.isNotBlank(salt)){
//			str = "" + salt.charAt(0)+ salt.charAt(2) + formPwd + salt.charAt(5) + salt.charAt(4);
			str = "" + salt + formPwd + salt;
		} else {
			throw new BusinessException("加密密码失败");
		}

		return md5(str);
	}

	/**
	 * 校验密码是否一致
	 * @param formPwd
	 * @param dbPwd
	 * @param salt
	 * @return
	 */
	public static boolean checkPwd(String formPwd,String dbPwd,String salt) {
		boolean isEqual = false;

		if (dbPwd.equals(formPassToDBPass(formPwd,salt))){
			isEqual = true;
		}

		return isEqual;
	}
	
	public static void main(String[] args) {
		System.out.println(formPassToDBPass("123456", "2"));
	}
	
}
