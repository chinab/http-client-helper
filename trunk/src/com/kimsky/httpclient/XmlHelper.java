package com.kimsky.httpclient;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class XmlHelper {
	public final Log log = LogFactory.getLog(getClass());
	
	//private Element root;
	
	private Document document;
	
	public  XmlHelper(String fileName) {
		reLoadXml(fileName);
	}
	
	public void reLoadXml(String fileName) {
		SAXReader reader = new SAXReader();
		File file = new File(this.getClass().getResource("/").getPath() + fileName);
		
		log.info(file.getAbsolutePath());
		try {
			document = reader.read(file);
			//root = document.getRootElement();
			
			
		} catch (DocumentException e) {
			
			e.printStackTrace();
		}
	}
	
	public String getAttribute(String path,String attrName) {
		return ((Element)document.selectSingleNode(path)).attribute(attrName).getValue();
	}

	
	@SuppressWarnings("unchecked")
	public <T> List<T> getObjectList(String path,Class<T> cls) {
		List<T> result = new ArrayList<T>();
		for(Object node: document.selectNodes(path)){
			Object instance = null;
			try {
				instance = cls.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			for (Field field : ReflectUtils.getFields(cls)) {
				ReflectUtils.setField(instance, field.getName(), ((Element)node).attributeValue(field.getName()));
			}
			result.add((T) instance);
		}
		
		return result;
	}
	
	public <T> T getSingleObject(String path,Class<T> cls) {
		List<T> list = getObjectList(path, cls);
		return list.size() > 0 ? list.get(0) : null;
	}
	
	@SuppressWarnings("unchecked")
	public <T1,T2> Map<T1, T2> getMap(String path,Class<T1> keyCls,Class<T2> ValueCls) {
		Map<T1, T2> resultMap = new HashMap<T1, T2>();
		
		for(Object node: document.selectNodes(path)){
			T1 key = null;
			T2 item = null;
			
			key = (T1) ReflectUtils.toObject(((Element)node).attributeValue("key"), keyCls);
			item = (T2) ReflectUtils.toObject(((Element)node).attributeValue("value"), ValueCls);
			
			resultMap.put(key, item);
		}
		
		return resultMap;
	}
	
	public Map<String, String> getStringMap(String path) {
		return getMap(path, String.class, String.class);
	}

}
