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
 * ����ʱ�乤����
 * 
 * @author ghost
 * @email fuzhao918@163.com
 * @time 2012-10-22 ����4:01:07
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
		dtfmt.put("\\d{4}��\\d{2}��\\d{2}��", "yyyy��MM��dd��");
		dtfmt.put("\\d{4}��\\d{2}��\\d{2}�� \\d{2}ʱ\\d{2}��\\d{2}��", "yyyy��MM��dd�� HHʱmm��ss��");

		fmt.put("yyyyMMdd", new SimpleDateFormat("yyyyMMdd"));
		fmt.put("yyyyMMddHHmm", new SimpleDateFormat("yyyyMMddHHmm"));
		fmt.put("yyyyMMddHHmmss", new SimpleDateFormat("yyyyMMddHHmmss"));
		fmt.put("yyyy-MM-dd", new SimpleDateFormat("yyyy-MM-dd"));
		fmt.put("yyyy-MM-dd HH:mm", new SimpleDateFormat("yyyy-MM-dd HH:mm"));
		fmt.put("yyyy-MM-dd HHmm", new SimpleDateFormat("yyyy-MM-dd HHmm"));
		fmt.put("yyyy-MM-dd HH:mm:ss", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		fmt.put("yyyy-MM-dd HH:mm:ss.SSS", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
		fmt.put("yyyy��MM��dd��", new SimpleDateFormat("yyyy��MM��dd��"));
		fmt.put("yyyy��MM��dd�� HHʱmm��ss��", new SimpleDateFormat("yyyy��MM��dd�� HHʱmm��ss��"));
		fmt.put("yyyy/MM/dd", new SimpleDateFormat("yyyy/MM/dd"));
	}

//	public static String process(final String orig){
//		
//	}
	/**
	 * ���������ַ���ȡ�����ڸ�ʽ
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
	 * ���������ַ�������java.util.Date
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
			throw new IllegalArgumentException("�޷�ʶ�������ַ�����ʽ[" + dateStr + "]");
		}
		return toDate(dateStr, pattern);
	}

	/**
	 * ���������ַ�������ʽ����java.util.Date
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
			throw new IllegalArgumentException(String.format("ת�������ַ�������ȷ,�ַ���:%s,��ʽ:%s", dateStr, pattern));
		}
	}

	/**
	 * ���������ַ�������java.sql.Date
	 * 
	 * @param dateStr
	 * @return
	 */
	public static java.sql.Date toSQLDate(String dateStr) {
		return new java.sql.Date(toDate(dateStr).getTime());
	}

	/**
	 * ���������ַ�������ʽ����java.sql.Date
	 * 
	 * @param dateStr
	 * @param pattern
	 * @return
	 */
	public static java.sql.Date toSQLDate(String dateStr, String pattern) {
		return new java.sql.Date(toDate(dateStr, pattern).getTime());
	}

	/**
	 * ���������ַ�������java.util.Calendar
	 * 
	 * @author ghost
	 * @time 2012-11-28 ����11:05:05
	 * @param src
	 * @return
	 */
	public static Calendar toCalendar(final String src) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(toDate(src));
		return calendar;
	}

	/**
	 * ��ʽ������Ϊָ����ʽ�ַ���
	 * 
	 * @author ghost
	 * @time 2012-10-29 ����11:02:14
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format(final java.util.Date date, final String pattern) {
		if (StringUtils.isBlank(pattern) || !fmt.containsKey(pattern)) {
			throw new IllegalArgumentException("��֧�ָ�ʽ��Pattern");
		}
		return fmt.get(pattern).format(date);
	}
}