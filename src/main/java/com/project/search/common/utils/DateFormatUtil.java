package com.project.search.common.utils;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Component
public class DateFormatUtil {

	public static final String DATE_FORMAT_DEFAULT_STYLE = "yyyy-MM-dd";

	public static final String DAY_FORMAT = "yyyy-MM-dd";
	
	public static final String TIME_FORMAT = "yyyyMMddHHmmss";
	
	public static final String SLASH_FORMAT = "MM/dd/yyyy";
	
	public static final String ENGLISH_FORMAT = "MMMM dd, yyyy | HH:mm aa z";

	public static Date stringToDate(String paramDate, String style) throws Exception {
		Date date = new Date();
		if (StringUtils.isBlank(style)) {
			style = DATE_FORMAT_DEFAULT_STYLE;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(style);
		if (StringUtils.isNotBlank(paramDate)) {
			date = simpleDateFormat.parse(paramDate);
		}
		return date;
	}

	public static String dateToString(Date date, String style) {
		String formatDate = "";
		if (date == null) {
			return formatDate;
		}
		if (StringUtils.isBlank(style)) {
			style = DATE_FORMAT_DEFAULT_STYLE;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(style);
		formatDate = simpleDateFormat.format(date);
		return formatDate;
	}
	
	public static String englishStr(Date date) {
		if(null == date) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(ENGLISH_FORMAT, Locale.ENGLISH);
		return sdf.format(date);
	}

	public static Date getNowDay() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static String getNowDayStr() {
		return dateToString(getNowDay(), DAY_FORMAT);
	}
	
	public static String getDayStr(Date date) {
		return dateToString(date, DAY_FORMAT);
	}

	public static Date addMinute(Date date, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MINUTE, minute);
		return cal.getTime();
	}

	public static String getDocTime(String dateStr) {
		// 1、取得本地时间：
        Calendar cal = Calendar.getInstance();
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        int offset = (zoneOffset + dstOffset) / 1000 / 60 /60;
        String offsetStr = "+08:00";
    	offsetStr = (offset >= 0 ? (Math.abs(offset)>9?"+":"+0") : (Math.abs(offset)>9?"-":"-0")) + Math.abs(offset) + ":00";
        
		return dateStr.replace(" ", "T") + offsetStr;
	}

	public static void main(String[] args) {
		Date date = null;
		try {
			date = stringToDate("2018-07-01", "yyyy-MM-dd");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(date);

		Date now = new Date();
		String nowStr = englishStr(now);
		System.out.println(nowStr);
	}
	
	public static int getCurrentTimeZone() {
		Calendar cal = Calendar.getInstance();
        int offset = cal.get(Calendar.ZONE_OFFSET);
        cal.add(Calendar.MILLISECOND, -offset);
        Long timeStampUTC = cal.getTimeInMillis();
        Long timeStamp = new Date().getTime();
        Long timeZone = (timeStamp - timeStampUTC) / (1000 * 3600);
        System.out.println(timeZone.intValue());
        return timeZone.intValue();
	}
}
