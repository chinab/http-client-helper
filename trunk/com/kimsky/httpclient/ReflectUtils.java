/**
 * 
 */
package com.kimsky.httpclient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author ghost
 * @email fuzhao918@163.com
 * @time 2012-11-18 上午10:31:36
 */
public class ReflectUtils {
	private static final Log logger = LogFactory.getLog(ReflectUtils.class);

	private static Map<Class<?>, Object> defaultValues = new HashMap<Class<?>, Object>();
	private static final Map<Class<?>, String[]> methodMap = new HashMap<Class<?>, String[]>();
	private static final Map<Class<?>, Map<String, ?>> boolmap = new HashMap<Class<?>, Map<String, ?>>();
	private static final Map<String, Boolean> booleanMap = new HashMap<String, Boolean>();
	private static final String DEFAULT_KEY = "default";
	
	static {
		boolmap.put(Boolean.class, booleanMap);
		boolmap.put(boolean.class, booleanMap);
		
		booleanMap.put("yes", Boolean.TRUE);
		booleanMap.put("true", Boolean.TRUE);
		booleanMap.put("y", Boolean.TRUE);
		booleanMap.put("t", Boolean.TRUE);
		booleanMap.put("1", Boolean.TRUE);
		booleanMap.put(DEFAULT_KEY, Boolean.FALSE);
		
		defaultValues.put(short.class, 0);
		defaultValues.put(int.class, 0);
		defaultValues.put(long.class, 0);
		defaultValues.put(float.class, 0);
		defaultValues.put(double.class, 0);
		
		methodMap.put(Short.class, new String[] { Short.class.getName(), "parseShort" });
		methodMap.put(Integer.class, new String[] { Integer.class.getName(), "parseInt" });
		methodMap.put(Long.class, new String[] { Long.class.getName(), "parseLong" });
		methodMap.put(Float.class, new String[] { Float.class.getName(), "parseFloat" });
		methodMap.put(Double.class, new String[] { Double.class.getName(), "parseDouble" });
		methodMap.put(short.class, new String[] { Short.class.getName(), "valueOf" });
		methodMap.put(int.class, new String[] { Integer.class.getName(), "valueOf" });
		methodMap.put(long.class, new String[] { Long.class.getName(), "valueOf" });
		methodMap.put(float.class, new String[] { Float.class.getName(), "valueOf" });
		methodMap.put(double.class, new String[] { Double.class.getName(), "valueOf" });

		methodMap.put(java.util.Date.class, new String[] { DateTimeUtil.class.getName(), "toDate" });
		methodMap.put(java.sql.Date.class, new String[] { DateTimeUtil.class.getName(), "toSQLDate" });
		methodMap.put(Calendar.class, new String[] { DateTimeUtil.class.getName(), "toCalendar" });

		methodMap.put(BigDecimal.class, new String[] { ReflectUtils.class.getName(), "toBigDecimal" });
	}

	/**
	 * 克隆对象
	 * 
	 * @author ghost
	 * @param <T>
	 * @time 2013-3-25 下午4:39:32
	 * @param orignal
	 * @param destClass
	 * @return
	 */
	public static <T> T clone(Object orignal, Class<T> destClass) {
		T instance = null;
		try {
			instance = destClass.newInstance();
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		if (orignal == null) {
			return instance;
		}
		for (Field field : getFields(orignal.getClass())) {
			Object value = getFieldValue(orignal, field.getName());
			setField(instance, field.getName(), value);
		}
		return instance;
	}

	/**
	 * 拷贝所有属性
	 * 
	 * @author ghost
	 * @time 2013-3-25 下午4:39:59
	 * @param orignal
	 * @param dest
	 */
	public static void copyProperties(Object orignal, Object dest) {
		if (orignal == null || dest == null) {
			return;
		}
		for (Field field : getFields(orignal.getClass())) {
			Object value = getFieldValue(orignal, field.getName());
			setField(dest, field.getName(), value);
		}
	}

	/**
	 * @author ghost
	 * @time 2013-3-25 下午4:33:12
	 * @param instance
	 * @param field
	 * @param value
	 */
	private static void setField(Object instance, String fieldName, Object value) {
		Field field = null;
		try {
			field = getField(instance.getClass(), fieldName);
		} catch (Exception e) {
			logger.warn(e);
		}

		boolean flag = false;
		try {
			if (Modifier.isFinal(field.getModifiers())) {
				return;
			}
			if (!field.isAccessible()) {
				flag = true;
				field.setAccessible(true);
			}
			try {
				field.set(instance, value);
			} catch (IllegalAccessException e) {
				logger.error(e);
			}
		} finally {
			if (flag) {
				field.setAccessible(false);
			}
		}
	}

	/**
	 * 设置属性值
	 * 
	 * @author ghost
	 * @time 2013-2-20 上午10:23:43
	 * @param instance
	 *            类实例
	 * @param fieldName
	 *            属性名
	 * @param value
	 *            属性String值
	 */
	public static void setField(final Object instance, final String fieldName, final String value) {
		if (instance == null) {
			return;
		}
		Field field = getField(instance.getClass(), fieldName);
		boolean flag = false;
		try {
			if (Modifier.isFinal(field.getModifiers())) {
				return;
			}
			if (!field.isAccessible()) {
				flag = true;
				field.setAccessible(true);
			}
			try {
				Object val = toObject(value, field.getType());
				if (val == null) {
					field.set(instance, defaultValues.get(field.getType()));
				} else {
					field.set(instance, val);
				}
			} catch (IllegalAccessException e) {
				logger.error(e);
				throw new RuntimeException(e);
			}
		} finally {
			if (flag) {
				field.setAccessible(false);
			}
		}
	}

	/**
	 * 获取类的所有属性，包括private/final/static/extends
	 * 
	 * @author ghost
	 * @time 2012-11-18 上午10:33:31
	 * @param clazz
	 * @return
	 */
	public static List<Field> getFields(Class<?> clazz) {
		if (clazz == Object.class) {
			return new ArrayList<Field>();
		}
		List<Field> fields = getFields(clazz.getSuperclass());
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		return fields;
	}

	/**
	 * 获取类的指定属性，包括private/final/static/extends
	 * 
	 * @author ghost
	 * @time 2012-11-18 上午10:38:33
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static Field getField(Class<?> clazz, String fieldName) {
		if (clazz == Object.class) {
			throw new IllegalArgumentException(clazz.getName() + "." + fieldName + "不存在");
		}
		try {
			Field field = clazz.getDeclaredField(fieldName);
			return field;
		} catch (SecurityException e) {
			logger.error(e);
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			return getField(clazz.getSuperclass(), fieldName);
		}
	}

	/**
	 * 获取类的所有方法，包括private/final/static/extends
	 * 
	 * @author ghost
	 * @time 2012-11-18 上午11:26:23
	 * @param clazz
	 * @return
	 */
	public static List<Method> getMethods(Class<?> clazz) {
		if (clazz == Object.class) {
			List<Method> methods = new ArrayList<Method>();
			for (Method method : clazz.getDeclaredMethods()) {
				methods.add(method);
			}
			return methods;
		}
		List<Method> methods = getMethods(clazz.getSuperclass());
		methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
		return methods;
	}

	/**
	 * 获取类的指定方法，包括private/final/static/extends
	 * 
	 * @author ghost
	 * @time 2012-11-18 上午10:38:33
	 * @param clazz
	 * @param methodName
	 * @return
	 */
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		if (clazz == Object.class) {
			throw new IllegalArgumentException(clazz.getName() + "." + methodName + "不存在");
		}
		try {
			Method method = clazz.getDeclaredMethod(methodName, paramTypes);
			return method;
		} catch (SecurityException e) {
			logger.error(e);
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			return getMethod(clazz.getSuperclass(), methodName, paramTypes);
		}
	}

	/**
	 * 获取类的指定属性值，包括private/final/static/extends
	 * 
	 * @author ghost
	 * @time 2012-11-18 上午10:51:49
	 * @param instance
	 * @param fieldName
	 * @return
	 */
	public static Object getFieldValue(Object instance, String fieldName) {
		if (instance == null) {
			throw new IllegalArgumentException("ReflectUtils.getFieldValue参数instance不可为空.");
		}
		Field field = getField(instance.getClass(), fieldName);
		boolean flag = false;
		try {
			if (!field.isAccessible()) {
				flag = true;
				field.setAccessible(true);
			}
			try {
				return field.get(instance);
			} catch (IllegalAccessException e) {
				logger.error(e);
				throw new RuntimeException(e);
			}
		} finally {
			if (flag) {
				field.setAccessible(false);
			}
		}
	}

	public static List<Field> getBeanFields(Class<?> clazz) {
		List<Field> fields = getFields(clazz);
		Iterator<Field> itor = fields.iterator();
		while (itor.hasNext()) {
			Field field = itor.next();
			int mod = field.getModifiers();
			if (Modifier.isAbstract(mod) || Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
				itor.remove();
				continue;
			}
			try {
				getMethod(clazz, "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1));
				getMethod(clazz, "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1), field.getType());
			} catch (IllegalArgumentException e) {
				itor.remove();
			}
		}
		return fields;
	}

	public static Method getMethod(Class<?> clazz, String methodName, Object[] paramValues) {
		Class<?>[] paramTypes = new Class<?>[paramValues.length];
		for (int i = 0; i < paramValues.length; i++) {
			if (paramValues[i] == null) {
				paramTypes[i] = Object.class;
			} else {
				paramTypes[i] = paramValues[i].getClass();
			}
		}
		return getMatchMethod(clazz, methodName, paramTypes);
	}

	public static Method getMatchMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
		try {
			return getMethod(clazz, methodName, paramTypes);
		} catch (Exception e) {
		}

		List<Method> methods = getMethods(clazz);
		List<Method> matchMethods = new ArrayList<Method>();
		for (Method m : methods) {
			if (m.getName().equals(methodName)) {
				if (m.getGenericParameterTypes().length == paramTypes.length) {
					matchMethods.add(m);
				}
			}
		}
		if (matchMethods.size() == 0) {
			throw new IllegalArgumentException(clazz.getName() + "." + methodName + "(...)不存在");
		}
		if (matchMethods.size() == 1) {
			return matchMethods.get(0);
		}
		TreeMap<Integer, Method> rtnMths = new TreeMap<Integer, Method>();

		Outer: for (Method m : matchMethods) {
			Class<?> paramClz = null;
			for (int i = 0; i < m.getGenericParameterTypes().length; i++) {
				if (m.getGenericParameterTypes()[i] == paramTypes[i]) {
					if (i == 0) {
						paramClz = paramTypes[i];
					}
					continue;
				}
				Type paramType = m.getGenericParameterTypes()[i];
				Class<?> clz = null;
				if (paramType instanceof Class<?>) {
					clz = (Class<?>) paramType;
				} else if (paramType instanceof ParameterizedType) {
					clz = (Class<?>) ((ParameterizedType) paramType).getRawType();
				} else {
					throw new RuntimeException("paramType不能转换");
				}
				if (i == 0) {
					paramClz = clz;
				}
				if (!clz.isAssignableFrom(paramTypes[i])) {
					continue Outer;
				}
			}
			rtnMths.put(getDepth(paramClz, paramTypes[0]), m);
		}
		if (rtnMths.size() == 0) {
			throw new IllegalArgumentException(clazz.getName() + "." + methodName + "(...)不存在");
		}
		return rtnMths.firstEntry().getValue();
	}

	public static String toString(Object obj) {
		if (obj == null) {
			return "";
		}
		StringBuffer rtn = new StringBuffer();
		for (Field f : getBeanFields(obj.getClass())) {
			if (rtn.length() > 0) {
				rtn.append(",");
			}
			Object val = getFieldValue(obj, f.getName());
			rtn.append(f.getName()).append("=");
			if (val != null) {
				rtn.append(val);
			}
		}
		return rtn.toString();
	}

	private static int getClassDepth(Class<?> c1, Class<?> c2, int depth) {
		if (c1 == Object.class) {
			return 99;
		}
		if (c2 == null) {
			return -1;
		}
		if (c1 == c2) {
			return depth;
		}
		return getClassDepth(c1, c2.getSuperclass(), ++depth);
	}

	private static int getInterfaceDepth(Class<?> c1, Class<?> c2, int depth) {
		++depth;
		for (Class<?> c : c2.getInterfaces()) {
			int clzDepth = getClassDepth(c1, c, depth);
			if (clzDepth > -1) {
				return clzDepth;
			}
			return getInterfaceDepth(c1, c, depth);
		}
		return -1;
	}

	private static int getDepth(Class<?> c1, Class<?> c2, int... depth) {
		int d = 0;
		if (!ArrayUtils.isEmpty(depth)) {
			d = depth[0];
		}
		if (c1.isInterface()) {
			return getInterfaceDepth(c1, c2, d);
		} else {
			return getClassDepth(c1, c2, d);
		}
	}
	
	public static Object toObject(final String src, Class<?> clazz) {
		if (clazz == String.class) {
			return src;
		}
		if (StringUtils.isBlank(src)) {
			if (clazz == Boolean.class || clazz == boolean.class) {
				return Boolean.FALSE;
			} else {
				return null;
			}
		}

		String original = StringUtils.trim(src);

		Map<String, ?> defm = boolmap.get(clazz);
		if (defm != null) {
			Object result = defm.get(original.toLowerCase());
			if (result == null) {
				return defm.get(DEFAULT_KEY);
			} else {
				return result;
			}
		}

		String[] clzmethod = methodMap.get(clazz);
		if (clzmethod != null) {
			Method mtd;
			try {
				mtd = Class.forName(clzmethod[0]).getDeclaredMethod(clzmethod[1], String.class);
				return mtd.invoke(Class.forName(clzmethod[0]), src);
			} catch (Exception e1) {
				throw new IllegalArgumentException(e1);
			}
		} else {
			throw new IllegalArgumentException(String.format("转换字符串%s为%s失败", src, clazz.getSimpleName()));
		}
	}
	
	public static Object toBigDecimal(final String src) {
		if (StringUtils.isBlank(src)) {
			return null;
		}
		if (StringUtils.indexOf(src, '.') > -1) {
			return BigDecimal.valueOf(Double.valueOf(StringUtils.trim(src)));
		} else {
			return BigDecimal.valueOf(Long.valueOf(StringUtils.trim(src)));
		}
	}

}
