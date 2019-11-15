package com.project.search.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Calendar;
import java.util.Date;


public class JwtUtil {

	/**
	 * jwt
	 */
	public static final String JWT_ID = "exsdjwt";
	public static final String JWT_SECRET = "A53337C396C5A75D82E70DD7867A85B3";
	public static final int JWT_TTL = 60 * 60 * 1000; // millisecond
	public static final int JWT_REFRESH_INTERVAL = 55 * 60 * 1000; // millisecond
	public static final int JWT_REFRESH_TTL = 12 * 60 * 60 * 1000; // millisecond

	/**
	 * 由字符串生成加密key
	 * 
	 * @return
	 */
	public static SecretKey generalKey() {
		String stringKey = JWT_SECRET;
		byte[] encodedKey = Base64.decodeBase64(stringKey);
        // 根据给定的字节数组使用AES加密算法构造一个密钥，使用 encodedKey中的始于且包含 0 到前 leng 个字节这是当然是所有。
		SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length,
				"AES");
		return key;
	}

	/**
	 * 创建jwt
	 * 
	 * @param id
	 * @param subject
	 * @param expires
	 * @return
	 */
	public static String createJWT(String id, String subject, Date expires) {
        //指定签名的时候使用的签名算法，也就是header那部分，jjwt已经将这部分内容封装好了。
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        //生成JWT的时间
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		//生成加密key
		SecretKey key = generalKey();
		JwtBuilder builder = Jwts.builder().
                //是JWT的唯一标识，根据业务需要，这个可以设置为一个不重复的值，主要用来作为一次性token,从而回避重放攻击。
                setId(id).
                //iat: jwt的签发时间
                setIssuedAt(now).
                //代表这个JWT的主体，这个是一个json格式的字符串，可以存放什么userid，roldid之类的，作为什么用户的唯一标志。
                setSubject(subject).
                signWith(signatureAlgorithm, key).
                setHeaderParam("authSystem", "mstarDemo");
		if (expires.getTime() >= 0) {
			builder.setExpiration(expires);
		}
		return builder.compact();
	}


	/**
	 * 创建jwt
	 *
	 * @param id
	 * @param subject
	 * @return
	 */
	public static String createJWT(String id, String subject) {
		//指定签名的时候使用的签名算法，也就是header那部分，jjwt已经将这部分内容封装好了。
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		//生成JWT的时间
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		//生成加密key
		SecretKey key = generalKey();
		JwtBuilder builder = Jwts.builder().
				setId(id).
				setIssuedAt(now).
				setSubject(subject).
				signWith(signatureAlgorithm, key).
				setHeaderParam("authSystem", "mstarDemo");
		return builder.compact();
	}

	/**
	 * 解密jwt
	 * 
	 * @param jwt
	 * @return
	 * @throws Exception
	 */
	public static Claims parseJWT(String jwt) {
		SecretKey key = generalKey();
		Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt)
				.getBody();
		return claims;
	}

	public static Date getExpiryDate(int minutes) {
		if(minutes>0){
			// 根据当前日期，来得到到期日期
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.MINUTE, minutes);
			return calendar.getTime();	
		}else{
			return new Date(0);
		}
	}

	public static String extractJwtTokenFromAuthorizationHeader(String auth) {
		if (StringUtils.isBlank(auth)) {
			return null;
		}
		return auth.replaceFirst("[B|b][E|e][A|a][R|r][E|e][R|r] ", "").replace(" ", "");
	}


//	public static Operator extractOperatorFromAuthorizationHeader(String auth) {
//		if (StringUtils.isBlank(auth)) {
//			return null;
//		}
//
//		String token = auth.replaceFirst("[B|b][E|e][A|a][R|r][E|e][R|r] ", "").replace(" ", "");
//		Claims claims = parseJWT(token);
//		String subj = claims.getSubject();
//		Operator operator = JsonMapper.fromJsonString(subj, Operator.class);
//		return operator;
//	}



	public static void main(String[] args) throws Exception{
//		String subj="{\"id\": \"1\",\"name\": \"dfds\"}";
//        Date expiry = getExpiryDate(30 * 24 * 60); //设置过期时间，30天
//        String resultToken = createJWT(JwtUtil.JWT_ID, subj, expiry);
//        System.out.println(resultToken+"=============");
//
//		Claims claims = JwtUtil.parseJWT(resultToken);
//		String subj1 = claims.getSubject();
//		if(StringUtils.isBlank(subj1)){
//			System.out.println("shibai===========");
//		}else{
//			String[] user = subj1.split(",");
//			String userId = user[0];
//			System.out.println("=userId===="+userId);
//		}

//        String auth = "BEARER 123";
//        String s = auth.replaceFirst("[B|b][E|e][A|a][R|r][E|e][R|r] ", "").replace(" ", "");
//        System.out.println(s);
	}
	
}
