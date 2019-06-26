package com.sq.tools.utils;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {

	private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	
	public static final int HTTP_TYPE = 0;

	private static PoolingHttpClientConnectionManager cm;

	static {
		if (cm == null) {
			cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(10);// 整个连接池最大连接数
			cm.setDefaultMaxPerRoute(5);// 每路由最大连接数，默认值是2
		}
	}

	private static CloseableHttpClient getHttpClient() {
		return HttpClients.custom().setConnectionManager(cm).build();
	}
	
	public static String doPost(String url, Map<String, String> params) {
		logger.info(url + "|" + params.toString());
		String result = "";
		HttpEntity entity = null;
		try {

			CloseableHttpClient httpclient = getHttpClient();
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(buildNameValuePair(params));
			CloseableHttpResponse response = httpclient.execute(httpPost);
			entity = response.getEntity();
			if (response.getStatusLine().getReasonPhrase().equals("OK")
					&& response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				result = EntityUtils.toString(entity, "UTF-8");
			}

		} catch (ClientProtocolException e) {
			logger.info("doPost: e" + e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.info("doPost: e" + e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			if (entity != null) {
				try {
					EntityUtils.consume(entity);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		logger.info("doPost: url = " + url + " | params = " + params + " | result = "
				+ ((result.length() > 500)?result.substring(0, 500): result));
		return result;
	}
	
	@SuppressWarnings("deprecation")
	public static String doSSLPost(String url, String xmlParams, String certPath, String secret){
		logger.info("doPost: url = " + url + " | xmlParams = " + xmlParams);
		String result = "";
		try {
			KeyStore keyStore  = KeyStore.getInstance("PKCS12");
	        FileInputStream instream = new FileInputStream(new File(certPath));
	        try {
	            keyStore.load(instream, secret.toCharArray());
	        } finally {
	            instream.close();
	        }

	        // Trust own CA and all self-signed certs
	        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, secret.toCharArray())
	                .build();
	        // Allow TLSv1 protocol only
	        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,
	                new String[] { "TLSv1" }, null,
	                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
	        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
	        try {
	        	HttpPost httpPost = new HttpPost(url);
	        	StringEntity xmlEntity = new StringEntity(xmlParams, getContentType());
				httpPost.setEntity(xmlEntity);
	            CloseableHttpResponse response = httpclient.execute(httpPost);
	            try {
	                HttpEntity entity = response.getEntity();
	    			if (response.getStatusLine().getReasonPhrase().equals("OK")
	    					&& response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	    				result = EntityUtils.toString(entity, "UTF-8");
	    			}
	    			
	                EntityUtils.consume(entity);
	            } finally {
	                response.close();
	            }
	        } finally {
	            httpclient.close();
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.info("doPost: result = " + result);
		return result;
	}

	/**
	 * MAP类型数组转换成NameValuePair类型
	 * 
	 */
	public static UrlEncodedFormEntity buildNameValuePair(Map<String, String> params) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}

		try {
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nvps, "UTF-8");
			return formEntity;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static ContentType getContentType(){
		return ContentType.create("text/html", Consts.UTF_8);
	}
	
	/**
	 * 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址;
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static String getIpAddress(HttpServletRequest request) throws IOException {
		// 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址

		String ip = request.getHeader("X-Forwarded-For");

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_CLIENT_IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}
		} else if (ip.length() > 15) {
			String[] ips = ip.split(",");
			for (int index = 0; index < ips.length; index++) {
				String strIp = (String) ips[index];
				if (!("unknown".equalsIgnoreCase(strIp))) {
					ip = strIp;
					break;
				}
			}
		}
		return ip;
	}

	private static final String ACCESS_TOKEN_URL = "https://open.ys7.com/api/lapp/token/get";

	private static final String APP_KEY = "ff956e80a03a4535b38a3ec6aaadd397";

	private static final String APP_SECRET = "f721f7e1f89ab1333f4ef4551cb2a7d4";

	private static void getAccessToken() {
		Map<String, String> params = new HashMap<>();
		params.put("appKey", APP_KEY);
		params.put("appSecret", APP_SECRET);
		System.out.println(doPost(ACCESS_TOKEN_URL, params));
		//{"data":{"accessToken":"at.730ydd2619xmyd176dx699wt4buaojnb-3d2nflg7ig-0catjow-1kskkrnks","expireTime":1562036144904},"code":"200","msg":"操作成功!"}
	}

	private static final String ACCESS_TOKEN = "ra.6rmx7nsj9v0kiyme37u9yp9x9er17a74-48z4v45m8a-0z67tpu-6cfjuvurz";//"at.730ydd2619xmyd176dx699wt4buaojnb-3d2nflg7ig-0catjow-1kskkrnks";


	private static final String ALIVE_VIDEO_LIST_URL = "https://open.ys7.com/api/lapp/live/video/list";

	private static void getAliveVideoList() {
		Map<String, String> params = new HashMap<>();
		params.put("accessToken", ACCESS_TOKEN);
		params.put("pageStart", "0");
		params.put("pageSize", "50");
		System.out.println(doPost(ALIVE_VIDEO_LIST_URL, params));
		//{"page":{"total":1,"page":0,"size":50},"data":[{"deviceSerial":"203751922","channelNo":1,"deviceName":"测试设备A","liveAddress":"http://hls01open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b.m3u8","hdAddress":"http://hls01open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b.hd.m3u8","rtmp":"rtmp://rtmp01open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b","rtmpHd":"rtmp://rtmp01open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b.hd","flvAddress":"https://flvopen.ys7.com:9188/openlive/f01018a141094b7fa138b9d0b856507b.flv","hdFlvAddress":"https://flvopen.ys7.com:9188/openlive/f01018a141094b7fa138b9d0b856507b.hd.flv","status":1,"exception":0,"beginTime":1525501260000,"endTime":1561391688000}],"code":"200","msg":"操作成功!"}
	}

	private static final String ALIVE_ADDRESS_LIMITED_URL = "https://open.ys7.com/api/lapp/live/address/limited";

	private static void getAliveVideoLimited() {
		Map<String, String> params = new HashMap<>();
		params.put("accessToken", ACCESS_TOKEN);
		params.put("deviceSerial", "203751922");
		params.put("channelNo", "1");
		params.put("expireTime", "300");
		System.out.println(doPost(ALIVE_ADDRESS_LIMITED_URL, params));
		//{"page":{"total":1,"page":0,"size":50},"data":[{"deviceSerial":"203751922","channelNo":1,"deviceName":"测试设备A","liveAddress":"http://hls01open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b.m3u8","hdAddress":"http://hls01open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b.hd.m3u8","rtmp":"rtmp://rtmp01open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b","rtmpHd":"rtmp://rtmp01open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b.hd","flvAddress":"https://flvopen.ys7.com:9188/openlive/f01018a141094b7fa138b9d0b856507b.flv","hdFlvAddress":"https://flvopen.ys7.com:9188/openlive/f01018a141094b7fa138b9d0b856507b.hd.flv","status":1,"exception":0,"beginTime":1525501260000,"endTime":1561391688000}],"code":"200","msg":"操作成功!"}
	}


	public static void main (String[] args) {
		System.out.println("-----------------");
		getAliveVideoLimited();
		System.out.println("-----------------");

	}

}