package com.kimsky.httpclient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 * 日期时间工具类
 * 
 * @author ghost
 * @email fuzhao918@163.com
 * @time 2012-10-22 下午4:01:07
 */
public class DateTimeUtil {
	private static final Map<String, String> dtfmt = new HashMap<String, String>();
	private static final Map<String, DateFormat> fmt = new HashMap<String, DateFormat>();
	static {
		dtfmt.put("\\d{8}", "yyyyMMdd");
		dtfmt.put("\\d{12}", "yyyyMMddHHmm");
		dtfmt.put("\\d{14}", "yyyyMMddHHmmss");
		dtfmt.put("\\d{4}-\\d{2}-\\d{2}", "yyyy-MM-dd");
		dtfmt.put("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}", "yyyy-MM-dd HH:mm");
		dtfmt.put("\\d{4}-\\d{2}-\\d{2} \\d{2}\\d{2}", "yyyy-MM-dd HHmm");
		dtfmt.put("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}", "yyyy-MM-dd HH:mm:ss");
		dtfmt.put("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\..*", "yyyy-MM-dd HH:mm:ss.SSS");
		dtfmt.put("\\d{4}/\\d{2}/\\d{2}", "yyyy/MM/dd");
		dtfmt.put("\\d{4}年\\d{2}月\\d{2}日", "yyyy年MM月dd日");
		dtfmt.put("\\d{4}年\\d{2}月\\d{2}日 \\d{2}时\\d{2}分\\d{2}秒", "yyyy年MM月dd日 HH时mm分ss秒");

		fmt.put("yyyyMMdd", new SimpleDateFormat("yyyyMMdd"));
		fmt.put("yyyyMMddHHmm", new SimpleDateFormat("yyyyMMddHHmm"));
		fmt.put("yyyyMMddHHmmss", new SimpleDateFormat("yyyyMMddHHmmss"));
		fmt.put("yyyy-MM-dd", new SimpleDateFormat("yyyy-MM-dd"));
		fmt.put("yyyy-MM-dd HH:mm", new SimpleDateFormat("yyyy-MM-dd HH:mm"));
		fmt.put("yyyy-MM-dd HHmm", new SimpleDateFormat("yyyy-MM-dd HHmm"));
		fmt.put("yyyy-MM-dd HH:mm:ss", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		fmt.put("yyyy-MM-dd HH:mm:ss.SSS", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
		fmt.put("yyyy年MM月dd日", new SimpleDateFormat("yyyy年MM月dd日"));
		fmt.put("yyyy年MM月dd日 HH时mm分ss秒", new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒"));
		fmt.put("yyyy/MM/dd", new SimpleDateFormat("yyyy/MM/dd"));
	}

//	public static String process(final String orig){
//		
//	}
	/**
	 * 根据日期字符串取得日期格式
	 * 
	 * @param dateStr
	 * @return
	 */
	public static String getDatePattern(String dateStr) {
		if (StringUtils.isBlank(dateStr)) {
			return null;
		}
		String pattern = null;
		for (String key : dtfmt.keySet()) {
			if (dateStr.matches(key)) {
				return dtfmt.get(key);
			}
		}
		return pattern;
	}

	/**
	 * 根据日期字符串生成java.util.Date
	 * 
	 * @param dateStr
	 * @return
	 */
	public static java.util.Date toDate(String dateStr) {
		if (StringUtils.isBlank(dateStr)) {
			return null;
		}
		String pattern = getDatePattern(dateStr);
		if (StringUtils.isBlank(pattern)) {
			throw new IllegalArgumentException("无法识别日期字符串格式[" + dateStr + "]");
		}
		return toDate(dateStr, pattern);
	}

	/**
	 * 根据日期字符串及格式生成java.util.Date
	 * 
	 * @param dateStr
	 * @param pattern
	 * @return
	 */
	public static java.util.Date toDate(String dateStr, String pattern) {
		if (StringUtils.isBlank(dateStr)) {
			return null;
		}
		try {
			return DateUtils.parseDate(dateStr, new String[] { pattern });
		} catch (ParseException e) {
			throw new IllegalArgumentException(String.format("转换日期字符串不正确,字符串:%s,格式:%s", dateStr, pattern));
		}
	}

	/**
	 * 根据日期字符串生成java.sql.Date
	 * 
	 * @param dateStr
	 * @return
	 */
	public static java.sql.Date toSQLDate(String dateStr) {
		return new java.sql.Date(toDate(dateStr).getTime());
	}

	/**
	 * 根据日期字符串及格式生成java.sql.Date
	 * 
	 * @param dateStr
	 * @param pattern
	 * @return
	 */
	public static java.sql.Date toSQLDate(String dateStr, String pattern) {
		return new java.sql.Date(toDate(dateStr, pattern).getTime());
	}

	/**
	 * 根据日期字符串生成java.util.Calendar
	 * 
	 * @author ghost
	 * @time 2012-11-28 上午11:05:05
	 * @param src
	 * @return
	 */
	public static Calendar toCalendar(final String src) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(toDate(src));
		return calendar;
	}

	/**
	 * 格式化日期为指定格式字符串
	 * 
	 * @author ghost
	 * @time 2012-10-29 上午11:02:14
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format(final java.util.Date date, final String pattern) {
		if (StringUtils.isBlank(pattern) || !fmt.containsKey(pattern)) {
			throw new IllegalArgumentException("不支持格式化Pattern");
		}
		return fmt.get(pattern).format(date);
	}
}