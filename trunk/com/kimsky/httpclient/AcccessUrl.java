package com.kimsky.httpclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.CoreProtocolPNames;

public class AcccessUrl {
	public final Log log = LogFactory.getLog(getClass());
	
	private XmlHelper configHelper;
	
	public static void main(String[] args) {
		try {
			AcccessUrl aGeturl = new AcccessUrl("http://www.wandafilm.com/");
			aGeturl.getResponseToFile(aGeturl.getHttp(), "d:\\film.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public AcccessUrl(String url) throws Exception{
		this.url = url;
		parseDomain(url);
		configHelper = new XmlHelper("config.xml");
	}
	
	public void parseDomain(String url) throws Exception {
		String regx = "\\.(.+)\\.(com)?(net)?(cn)?(org)?";
	
		Pattern p=Pattern.compile(regx); 
		Matcher matcher = p.matcher(url);
		
		if (matcher.find()){
			domain=matcher.group();
			log.info(matcher.group());
		}else {
			throw new Exception("url ´íÎó");
		}
		
	}
	private String domain;
	
	private String url;
	
	private boolean isHttps = false;
	
	private String getUrl;
	
	public String getGetUrl() {
		return getUrl;
	}
	public void setGetUrl(String getUrl) {
		this.getUrl = getUrl;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public InputStream getHttp() {
		AbstractHttpClient httpclient = new DefaultHttpClient();
		HttpRequestBase httpget = new HttpGet(url);
		
		
		httpget.setHeader(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
//		System.out.println("executing request " + httpget.getURI());
//		Header[] headers = httpget.getAllHeaders();
//		for(int i=0;i<headers.length;i++)
//		{
//			System.out.println(headers[i].getName() + "::" + headers[i].getValue());
//		}
		
		ProxyBean proxy = configHelper.getSingleObject("/config/proxy/item", ProxyBean.class);
		if (proxy != null)
		{
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					new HttpHost(proxy.getHost(), Integer.parseInt(proxy.getPort()), "http"));
		}

		
		CookieStore cookieStore = new BasicCookieStore();
		Map<String, String> cookieMap =  configHelper.getStringMap("/config/cookies/mapitem");
		for(String key:cookieMap.keySet()){
			cookieStore.addCookie(addCookie(key, cookieMap.get(key)));
		}
		httpget.getParams().setParameter(
				ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		httpclient.setCookieStore(cookieStore);
		
		
		String paramstr = getUrlParam(configHelper.getStringMap("/config/param/mapitem"));
		if (!StringUtils.isBlank(paramstr)){
			if (!(url.endsWith("/") || url.endsWith("?")))
			{
				url += "/?";
			}else if (url.endsWith("/")) {
				url += "?";
			}
		}
		getUrl = url + paramstr;
		
		try {
			httpget.setURI(new URI(getUrl));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HttpEntity entity = response.getEntity();
		
		try {
			return entity.getContent();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	public String getResponseToString(InputStream responStream) {
		StringBuilder sb = new StringBuilder();
		try {
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(responStream,"UTF-8"));
			String line = "";
			while ((line = reader.readLine())!=null) {
				sb.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info(sb.toString());
		return sb.toString();
	}
	
	public void getResponseToFile(InputStream responStream,String filePath) {
		final int BUFFER = 1024;
		try {
			FileOutputStream out = new FileOutputStream(new File(filePath));
			byte[] b = new byte[BUFFER];  
            int len = 0;  
            while((len=responStream.read(b))!= -1){  
                out.write(b,0,len);
            }  
            responStream.close();  
            out.close();  
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public String getUrlParam(Map<String, String> map) {
		StringBuffer result = new StringBuffer();
		for(String key:map.keySet()){
			result.append("&").append(key).append("=").append(map.get(key));
		}
		if (result.length()>0){
			result.deleteCharAt(0);
		}
		
		return result.toString();
	}
	
	@SuppressWarnings("deprecation")
	public void wrapHttps(AbstractHttpClient httpclient) {
		SSLSocketFactory socketFactory = null;
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] xcs,
						String string) {
				}

				public void checkServerTrusted(X509Certificate[] xcs,
						String string) {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			ctx.init(null, new TrustManager[] { tm }, null);
			socketFactory = new SSLSocketFactory(ctx);
			socketFactory
					.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			isHttps = true;
		} catch (KeyManagementException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Scheme sch = new Scheme("https", 443,
				(SchemeSocketFactory) socketFactory);

		httpclient.getConnectionManager().getSchemeRegistry().register(sch);
	}
	
	
	
	public BasicClientCookie addCookie(String cookieName,String cookieValue) {
		BasicClientCookie jSessionCookie = new BasicClientCookie(cookieName, cookieValue);
		
		jSessionCookie.setDomain(domain);
//		jSessionCookie.setPath("");
		jSessionCookie.setSecure(isHttps);
		jSessionCookie.setVersion(1);
//		jSessionCookie.setPorts(new int[] {80,443});
		jSessionCookie.setAttribute(ClientCookie.VERSION_ATTR, "1");
		jSessionCookie.setAttribute(ClientCookie.DOMAIN_ATTR, domain);
		return jSessionCookie;
	}
}
