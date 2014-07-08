package com.paic.gimap.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.paic.gimap.common.biz.constant.Constant;
import com.paic.gimap.common.dto.AgentInfo;
import com.paic.gimap.common.dto.MsgInfo;
import com.paic.gimap.common.dto.OnlineDTO;

/**
 * 此类提供一些通用的公共方法
 * 
 * @author yuhan024
 * 
 */
public class Util {

	private static Logger logger = Logger.getLogger(Util.class);
	private static int seq = 0;
	/**
	 * 是否是忽略大小写
	 * 
	 * @param one
	 *            ，指定开头
	 * @param anotherString
	 *            ，是消息内容
	 * @return
	 */
	public static boolean isSimilar(String one, String anotherString) {
		int length = one.length();
		if (length > anotherString.length()) {
			// 如果被期待为开头的字符串的长度大于anotherString的长度
			return false;
		}
		// 忽略大小写
		if (one.equalsIgnoreCase(anotherString.substring(0, length))) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * // json格式的数据组装和解析实例 public static void main(String[] args) {
	 * 
	 * List tasks = new ArrayList(); Map task = new HashMap();
	 * task.put("taskid", "123"); task.put("taskstatus", "01");
	 * task.put("source", "wx"); Map customers = new HashMap();
	 * customers.put("name", "yuhan"); customers.put("mobileno", "mobileno");
	 * customers.put("clientid", "clientid"); task.put("customer", customers);
	 * Map cases = new HashMap(); cases.put("caseno", "case1"); task.put("case",
	 * cases); Map taskMap = new HashMap(); taskMap.put("task", task);
	 * tasks.add(taskMap); Map map1 = new HashMap(); map1.put("tasks", tasks);
	 * map1.put("operationtype", "dispatchtask"); JSONObject json1 =
	 * JSONObject.fromObject(map1); System.out.println(json1);
	 * 
	 * String paheader =
	 * "\"{\"operationtype\":\"dispatchtask\",\"wxid\":[\"oMFLfjviJr-LzMIYflfgxoarakxY\"],\"taskid\":[\"C51DCD5FDBBA148EE04018AC880233D8\"]}\"";
	 * System.out.println(paheader); if (paheader.startsWith("\"")) { paheader =
	 * paheader.substring(1, paheader.length() - 1); }
	 * System.out.println(paheader); // 封装成json格式,包括数组 Map map = new HashMap();
	 * map.put("name", "json"); map.put("bool", Boolean.TRUE);
	 * 
	 * map.put("int", new Integer(1)); // map.put( "arr", new String[]{"b"} );
	 * String[] wxids = new String[1]; wxids[0] = "a"; map.put("arr", wxids);
	 * JSONObject json = JSONObject.fromObject(map); System.out.println(json); //
	 * 解析json格式数据 String name = json.getString("name"); boolean bool =
	 * json.getBoolean("bool"); int intI = json.getInt("int");
	 * System.out.println(name); System.out.println(bool);
	 * System.out.println(intI); JSONArray arr = json.getJSONArray("arr"); for
	 * (int i = 0; i < arr.size(); i++) { System.out.println(arr.get(i)); }
	 * String jsonStr = "abc"; String s_tr = "\"" + jsonStr + "\"";
	 * System.out.println(s_tr);
	 * 
	 * System.out.println("***********"); System.out.println(isSimilar("@CXLP",
	 * "@cxlp1244444444444")); System.out.println(isSimilar("@CXLP3",
	 * "@cxlp1244444444444")); StringBuffer sfStr = new StringBuffer(); String
	 * str = sfStr.toString(); System.out.println("".equals(str));
	 * System.out.println(str.getBytes()); }
	 */

	// 发送http请求，app调sip区的httpServlet
	public static String sendHttpRequest(String url) throws IOException {
		logger.info("sendHttpRequest()方法开始，URL：" + url);
		if(url!=null&&url.indexOf("&msgContextType=event")!=-1&&url.indexOf("sendToAgentServlet")!=-1){
			logger.warn("发送微菜单消息发送给了坐席 url"+(url==null?"":url));
			return "0";
		}
		int status = -1;
		String response = "0";
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = connectionManager.getParams();
		params.setConnectionTimeout(5000);
		params.setSoTimeout(20000);
		params.setDefaultMaxConnectionsPerHost(32);// very important!!
		params.setMaxTotalConnections(256);// very important!!

		HttpClient httpClient = new HttpClient(connectionManager);
		// 设置编码
		httpClient.getParams().setContentCharset("utf-8");
		httpClient.getParams().setHttpElementCharset("utf-8");

		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		// 一定要有，否则会生成多个Cookie header送给web server
		httpClient.getParams().setParameter(
				"http.protocol.single-cookie-header", true);
		httpClient.getParams().setParameter("http.protocol.content-charset",
				"utf-8");
		ArrayList<Header> headerList = new ArrayList<Header>();
		Header accept = new Header(
				"Accept",
				"image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/x-silverlight, */*");
		headerList.add(accept);
		httpClient.getParams().setParameter("http.default-headers", headerList);
		httpClient.getParams().setParameter("http.protocol.version",
				HttpVersion.HTTP_1_1);
		httpClient.getParams().setParameter("http.method.retry-handler",
				new DefaultHttpMethodRetryHandler());

		PostMethod postMethod = new PostMethod(url);
		// httpClient.getParams().setParameter("http.socket.timeout", 1); //
		// 为HttpClient设置参数
		// httpClient.getHttpConnectionManager().getParams().setParameter("http.socket.timeout",
		// 1); // 为HttpConnetionManager设置参数
		// postMethod.getParams().setParameter("http.socket.timeout", 10000); //
		// 为HttpMethod设置参数

		try {
			postMethod.getParams().setUriCharset("utf-8");
			postMethod.setURI(new URI(url, false, "utf-8"));
			status = httpClient.executeMethod(postMethod);
			// logger.info("随机数："+randNm+"http请求返回的状态： " + status);
			if (status == 200) {
				logger.info("sendHttpRequest()方法调用返回响应状态OK，URL：" + url);
				response = postMethod.getResponseBodyAsString();
			}

		} catch (Exception e) {
			logger.error("sendHttpRequest()方法调用异常，URL：" + url + "，异常信息："
					+ e.getMessage());
			e.printStackTrace();
			status = -1;
		} finally {
			if (response != null && response.equals("0")) {
				seq++;
				logger.warn("发送消息给sip出错次数 " + seq);

			}
			postMethod.releaseConnection();
			logger.info("sendHttpRequest()方法，关闭连接，URL：" + url);
		}
		logger.info("sendHttpRequest()方法调用完毕，URL：" + url + "，返回值：" + response);
		return response;
	}
	
	
	/**
	 * 需要返回值
	 */
	// 发送http请求，app调sip区的httpServlet
	public static String sendHttpRequest4Res(String url) throws IOException {
		logger.info("sendHttpRequest()方法开始，URL：" + url);
		
		int status = -1;
		String response = "";
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = connectionManager.getParams();
		params.setConnectionTimeout(5000);
		params.setSoTimeout(20000);
		params.setDefaultMaxConnectionsPerHost(32);// very important!!
		params.setMaxTotalConnections(256);// very important!!
		
		HttpClient httpClient = new HttpClient(connectionManager);
		// 设置编码
		httpClient.getParams().setContentCharset("utf-8");
		httpClient.getParams().setHttpElementCharset("utf-8");
		
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		// 一定要有，否则会生成多个Cookie header送给web server
		httpClient.getParams().setParameter(
				"http.protocol.single-cookie-header", true);
		httpClient.getParams().setParameter("http.protocol.content-charset",
		"utf-8");
		ArrayList<Header> headerList = new ArrayList<Header>();
		Header accept = new Header(
				"Accept",
		"image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/x-silverlight, */*");
		headerList.add(accept);
		httpClient.getParams().setParameter("http.default-headers", headerList);
		httpClient.getParams().setParameter("http.protocol.version",
				HttpVersion.HTTP_1_1);
		httpClient.getParams().setParameter("http.method.retry-handler",
				new DefaultHttpMethodRetryHandler());
		
		PostMethod postMethod = new PostMethod(url);
		// httpClient.getParams().setParameter("http.socket.timeout", 1); //
		// 为HttpClient设置参数
		// httpClient.getHttpConnectionManager().getParams().setParameter("http.socket.timeout",
		// 1); // 为HttpConnetionManager设置参数
		// postMethod.getParams().setParameter("http.socket.timeout", 10000); //
		// 为HttpMethod设置参数
		
		try {
			postMethod.getParams().setUriCharset("utf-8");
			postMethod.setURI(new URI(url, false, "utf-8"));
			status = httpClient.executeMethod(postMethod);
			// logger.info("随机数："+randNm+"http请求返回的状态： " + status);
			if (status == 200) {
				logger.info("sendHttpRequest4Res()方法调用返回响应状态OK，URL：" + url);
				response = postMethod.getResponseBodyAsString();
			}
			
		} catch (Exception e) {
			logger.error("sendHttpRequest4Res()方法调用异常，URL：" + url + "，异常信息："
					+ e.getMessage());
			e.printStackTrace();
			status = -1;
		} finally {
			if (response != null && response.equals("")) {
				seq++;
				logger.warn("发送消息给sip出错次数 " + seq);
				
			}
			postMethod.releaseConnection();
			logger.info("sendHttpRequest()方法，关闭连接，URL：" + url);
		}
		logger.info("sendHttpRequest4Res()方法调用完毕，URL：" + url + "，返回值：" + response);
		return response;
	}

	/**
	 * 组装JSon格式的数据
	 * 
	 * @param map
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject assembleJsonData(Map map) {
		JSONObject jsonObj = JSONObject.fromObject(map);
		return jsonObj;
	}

	/**
	 * 解析JSON格式的数据
	 * 
	 * @param jsonObj
	 * @param type
	 * @return
	 */
	public static Object analyticJsonData(JSONObject jsonObj, String type,
			String key) {
		Object returnStr = "";
		if ("String".equals(type)) {
			returnStr = jsonObj.getString((String) key);
		}
		if ("Boolean".equals(type)) {
			returnStr = jsonObj.getBoolean((String) key);
		}
		if ("Array".equals(type)) {
			returnStr = jsonObj.getJSONArray(key);
		}
		return returnStr;
	}

	/**
	 * 将o序列化成json格式
	 * 
	 * @param bean
	 * @return
	 */
	public static String bean2Json(Object bean) {
		JSONObject jsonobj = JSONObject.fromObject(bean);
		String jsonStr = jsonobj.toString();
		return jsonStr;
	}

	/**
	 * 将list序列化成json格式
	 * 
	 * @param list
	 * @return
	 */
	public static String list2Json(List list) {
		JSONArray json = new JSONArray();
		if (null != list) {
			json.addAll(list);
		}
		return json.toString();
	}

	/**
	 * 半角转全角：
	 * 
	 * @param str
	 * @return
	 */
	public static String toSBC(String str) {
		char[] c = str.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 32) {
				c[i] = (char) 12288;
				continue;
			}
			if (c[i] < 127)
				c[i] = (char) (c[i] + 65248);
		}
		return new String(c);
	}

	/**
	 * 全角转半角
	 * 
	 * @param str
	 * @return
	 */
	public static String toDBC(String str) {
		char[] c = str.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)

				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}

	/**
	 * 表示式解析方法 expstr: 待解析的表达式 parameters : 传入的条件变量值
	 */
	public static boolean evalute(String expStr, Map<String, Object> parameters)
			throws Exception {
		if ("nvl".equalsIgnoreCase(expStr)) {
			return true;
		}
		String[] expAry = spliteExp(expStr);
		for (int i = 0; i < expAry.length; i++) {
			if (expAry[i].equals("=") || expAry[i].equals("!")) {
				String key = expAry[i - 1];
				Object value = parameters.get(key);
				if (value == null) {
					value = "^_^";
				}

				String entValue = _2String(value);
				if (entValue.indexOf("(") >= 0 || entValue.indexOf(")") >= 0
						|| entValue.indexOf("&") >= 0
						|| entValue.indexOf("|") >= 0
						|| entValue.indexOf("!") >= 0
						|| entValue.indexOf("=") >= 0) {
					throw new Exception();
				}

				expAry[i - 1] = entValue;
			}
		}

		StringBuffer exp = new StringBuffer();
		for (int i = 0; i < expAry.length; i++) {
			exp.append(expAry[i]);
		}

		String result = parse(exp.toString());
		return Boolean.parseBoolean(result);
	}

	/*
	 * 分离表达式，置换条件变量
	 */
	private static String[] spliteExp(String expStr) {
		ArrayList<String> splited = new ArrayList<String>();
		int start = 0;
		int idx = 0;
		for (int i = 0; i < expStr.length(); i++) {
			char chr = expStr.charAt(i);
			if (chr == '=' || chr == '(' || chr == ')' || chr == '&'
					|| chr == '|' || chr == '!') {
				String t = expStr.substring(start, i);
				if (t.length() > 0) {
					splited.add(idx++, t);
				}
				start = i;

				t = expStr.substring(start, i + 1);
				if (t.length() > 0) {
					splited.add(idx++, t);
				}
				start = i + 1;
			}

			if (i == expStr.length() - 1) {
				String t = expStr.substring(start, i + 1);
				if (t.length() > 0) {
					splited.add(idx++, t);
				}
			}
		}

		String[] rtn = new String[idx];
		return splited.toArray(rtn);
	}

	/**
	 * 分离表达式各组成单元，嵌套解析
	 * 
	 * @param exp
	 * @return
	 * @throws JadeWorkFlowException
	 */
	public static String parse(String exp) throws Exception {
		if (exp.indexOf("(") < 0 && exp.indexOf(")") < 0) {
			return String.valueOf(parseOr(exp));
		} else {
			int start = exp.indexOf("(");
			int end = 0;
			int count = 0;
			for (int i = start; i < exp.length(); i++) {
				if (exp.charAt(i) == '(') {
					count++;
				} else if (exp.charAt(i) == ')') {
					count--;
				}

				if (count == 0) {
					end = i + 1;
					break;
				}
			}

			String nowStr = exp.substring(start + 1, end - 1);
			String before = exp.substring(0, start);
			String after = "";
			if (end < exp.length()) {
				after = exp.substring(end, exp.length());
			}

			String newStr = before + parse(nowStr) + after;

			return parse(newStr);
		}
	}

	/**
	 * 解析 或 关系
	 * 
	 * @param exp
	 * @return
	 * @throws JadeWorkFlowException
	 */
	public static boolean parseOr(String exp) throws Exception {
		if (exp.indexOf("(") >= 0 || exp.indexOf(")") >= 0) {
			throw new Exception();
		}

		String[] x = exp.split("\\|");

		for (int i = 0; i < x.length; i++) {
			if (parseAnd(x[i]) == true) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 解析 与 关系
	 * 
	 * @param exp
	 * @return
	 * @throws JadeWorkFlowException
	 */
	public static boolean parseAnd(String exp) throws Exception {
		if (exp.indexOf("(") >= 0 || exp.indexOf(")") >= 0
				|| exp.indexOf("|") >= 0) {
			throw new Exception();
		}

		String[] x = exp.split("&");
		for (int i = 0; i < x.length; i++) {
			if (parseEquals(x[i]) == false) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 解析最小单元：等于 或 不等于
	 * 
	 * @param exp
	 * @return
	 * @throws JadeWorkFlowException
	 */
	public static boolean parseEquals(String exp) throws Exception {
		if (exp.indexOf("(") >= 0 || exp.indexOf(")") >= 0
				|| exp.indexOf("|") >= 0 || exp.indexOf("&") >= 0) {
			throw new Exception();
		}

		int eI = exp.indexOf("=");
		int nI = exp.indexOf("!");
		if (eI < 0 && nI < 0) {
			if (exp.equals("true"))
				return true;
			else if (exp.equals("false"))
				return false;
		} else {
			boolean flag = nI < 0;
			String[] sa = exp.split(flag ? "=" : "!");
			String nv = sa[0];
			String ov = sa.length == 2 ? sa[1] : "";
			return flag ? nv.equals(ov) : !nv.equals(ov);
		}

		/*
		 * if(exp.indexOf("=")>0) { String[] x = exp.split("=");
		 * if(x[0].equals(x[1])) { return true; } else { return false; } } else
		 * if(exp.indexOf("!")>0) { String[] x = exp.split("!");
		 * if(x[0].equals(x[1])) { return false; } else { return true; } } else
		 * if(exp.equals("true")) { return true; } else if(exp.equals("false")) {
		 * return false; }
		 */

		throw new Exception();
	}

	/**
	 * 将传入参数转成字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String _2String(Object obj) {
		if (obj instanceof Date) {
			return (new SimpleDateFormat("yyyy-MM-dd")).format((Date) obj);
		} else if (obj instanceof Timestamp) {
			return ((Timestamp) obj).toString();
		} else {
			return String.valueOf(obj);
		}
	}

	/*
	 * public static void main(String[] arg) { HashMap<String,Object> map = new
	 * HashMap<String,Object>(); map.put("_a", "1"); map.put("B", "0");
	 * map.put("C", "0"); map.put("D", "1"); map.put("_E", "2009-09-09");
	 * map.put("_x", "x");
	 * 
	 * String exp = "(_A=1 || B=0) & (C=1 || D=1 ) && _E=2009-09-09";
	 * 
	 * String exp ="_A=1"; try{ Util parse = new Util(); exp = exp.trim();
	 * exp=exp.replace("&&", "&").replace("||", "|").replace("!=",
	 * "!").replace(" ", "");
	 * 
	 * int c1=0,c2=0; for(int i=0;i<exp.length();i++) { if(exp.charAt(i)=='(')
	 * c1++; else if(exp.charAt(i)==')') c2++; } if(c1!=c2) { throw new
	 * Exception(); }
	 * 
	 * System.out.println(parse.evalute(exp,map)); } catch(Exception e) {
	 * e.printStackTrace(); } }
	 */

	/**
	 * 用MAP中的KEY值，替换str中的标识符
	 */
	public static String replaceStrWithMap(Map params, String str) {
		if (null == params) {
			return str;
		}
		Iterator it = (Iterator) params.keySet().iterator();
		String key = null;
		while (it.hasNext()) {
			key = (String) it.next();
			str = str.replace("${" + key + "}", (String) params.get(key));
		}
		return str;
	}

	public static int getWordCount(String s) {
		int length = 0;
		for (int i = 0; i < s.length(); i++) {
			int ascii = Character.codePointAt(s, i);
			if (ascii >= 0 && ascii <= 255)
				length++;
			else
				length += 2;
		}
		return length;
	}

	/**
	 * 排队提示
	 * 
	 * @param lineDto
	 * @return
	 */
	public static OnlineDTO queueRemind(OnlineDTO lineDto, String flag) {
		if (flag.equals("N")) {
			if (lineDto.getQueueNum() >= Constant.ONLINE_QUEUE_SUM_NUM) {
				lineDto.setCode(Constant.ONLINE_DISPATCH_BUSY);// 提示排队
				// "您好！目前客服人员全忙，请您稍后再与我们联系，或选择页面右上角的“我要留言”，留言给我们，谢谢！"
			} else {
				lineDto.setCode(Constant.ONLINE_DISPATCH_FAILLING);// 提示排队Constant.ONLINE_QUEUE_SUM_NUM
				lineDto.setQueueRemind("您好，现服务坐席全忙，已有" + lineDto.getQueueNum()
						+ "人在排队，是否要继续等待？");// 这里可能从数据库取
			}
		} else if (flag.equals("Y")) {// 排队中返回信息
			lineDto.setCode(Constant.ONLINE_YES_QUEUE);// 已排队
			lineDto.setQueueRemind("您好，您之前还有" + lineDto.getQueueNum()
					+ "人在排队，请稍等！");
		} else if (flag.equals("E")) {// 排队的客户当进行分配时，没有一个坐席在线或忙
			lineDto.setCode(Constant.ONLINE_ERROR_EX);// 派工失败
			lineDto.setQueueRemind("您好，现服务坐席不在线，如需留言请点击右下方“在线留言”！");
		}
		return lineDto;
	}

	public static String objctToString(Object obj) throws Throwable {
		if (obj instanceof Serializable) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			os.close();
			String ss = os.toString("ISO8859-1");
			return ss;
		}

		return null;

	}

	public static String base64Encoder(String str, String charset)
			throws UnsupportedEncodingException {
		BASE64Encoder encoder = new BASE64Encoder();
		String rs = encoder.encode(str.getBytes(Charset.defaultCharset()));
		return rs;
	}

	public static String base64Decoder(String str, String charset)
			throws IOException {
		BASE64Decoder decoder = new BASE64Decoder();
		String rs = null;
		byte[] buffer = decoder.decodeBuffer(str);
		rs = new String(buffer, charset);
		return rs;
	}

	public static Object stringToObject(String str) throws Throwable {
		if (str != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(str
					.getBytes("ISO8859-1"));
			ObjectInputStream ois = new ObjectInputStream(bis);
			Object obj = ois.readObject();
			ois.close();
			bis.close();
			return obj;

		}
		return null;

	}

	// <?xml version="1.0" encoding="utf-8"?>
	// <Data version="1.0">
	// <Msg>
	// <PPID><![CDATA[4018983514]]></PPID>
	// <UserURI><![CDATA[1305266145]]></UserURI>
	// <MsgType><![CDATA[PublicPlatformMsg]]></MsgType>
	// <Content><![CDATA[\u4f60\u53d1\u9001\u7684\u6d88\u606fshifsdf---useruri:1305266145---ppid4018983514]]></Content>
	// <CallID><![CDATA[48]]></CallID>
	// <CseqValue><![CDATA[1]]></CseqValue>
	// <MsgID><![CDATA[GZ04171244636086]]></MsgID>
	// <ClientType><![CDATA[PCV4]]></ClientType>
	// <PackageID><![CDATA[13]]></PackageID>
	// <UserType><![CDATA[CUCC/1:L0]]></UserType>
	// </Msg>
	// </Data>
	@SuppressWarnings("unchecked")
	public static Map<String, String> parserXml(String doc)
			throws DocumentException {
		Document document = null;
		Map<String, String> data = new HashMap<String, String>();

		document = DocumentHelper.parseText(doc);
		if (document != null) {

			Element msgNode = (Element) document.selectSingleNode("/Data/Msg");

			// System.out.println(msgNode.asXML());

			if (msgNode != null) {
				Iterator<Element> it = msgNode.elementIterator();
				while (it.hasNext()) {
					Element e = it.next();

					String name = e.getName();
					String value = e.getText();
					data.put(name, value);
				}

			}

		}

		// System.out.println(data);

		return data;
	}

	@SuppressWarnings("unchecked")
	public static String parserDocument(Map<String, String> data)
			throws DocumentException {
		StringBuilder document = new StringBuilder();
		document.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		document.append("<Data version=\"1.0\">");
		document.append("<Msg>");
		if (data != null && data.size() > 0)
			for (Entry<String, String> entry : data.entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue();
				if (name == null) {
					name = "";
				}
				if (value == null) {
					value = "";
				}

				document.append("<" + name + ">");
				document.append("<![CDATA[" + value + "]]>");
				document.append("</" + name + ">");

			}

		document.append("</Msg>");
		document.append("</Data>");

		// System.out.println(document.toString());

		return document.toString();
	}

	public static boolean checkStr(String inputStr, String regex) {
		if (null == inputStr || "".equals(inputStr)) {
			return false;
		}
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(inputStr);
		return m.matches();
	}

	public static void sendMsgToAgent(AgentInfo agentInfo, MsgInfo msgInfo,
			String microLetter, String customerName, String source,
			String flag, String mobileNo, String taskstatus, String caseNo,
			String paImNo,String clientStatus) throws Exception {
		String sip_server_url = ServiceRequestID.getInstance()
				.getSIP_SERVER_URL();
		logger.info("发送消息给坐席sendMsgToAgent()");

		String content = msgInfo.getMsgContext();
		if(content==null||content.trim().length()==0){
			content="0";
			
		}
		if (content != null) {
			List<String> list = DataUtil.divideLength(content, 300);
			if (list != null && list.size() > 0) {
				for (String c : list) {

					String url = sip_server_url
							+ "/sendToAgentServlet?microLetter="
							+ microLetter
							+ "&msgContext="
							+ URLEncoder.encode(
									null == c ? ""
											: c, "UTF-8")
							+ "&msgContextType="
							+ msgInfo.getMsgType()
							+ "&taskId="
							+ msgInfo.getTaskOid()
							+ "&customerName="
							+ URLEncoder.encode(null == customerName ? ""
									: customerName, "UTF-8") + "&source="
							+ source + "&flag=" + flag + "&mobileNo="
							+ mobileNo + "&taskstatus=" + taskstatus
							+ "&caseNo=" + caseNo + "&extensionNo="
							+ agentInfo.getExtensionNo() + "&ip="
							+ agentInfo.getLogonIp() + "&pa_im=" + paImNo
							+ "&clientStatus="+clientStatus;
					logger.info("发送消息给坐席调sip的httpServletURL：" + url);
					Util.sendHttpRequest(url);
					
					if(flag!=null&&flag.trim().equals("0")){
						return;
					}
				}
			}
		}

	}
    public static String printValidCode(int validLength) {
        // 初始化随机数对象
        Random rd = new Random();
        long seed = new Date().getTime();
        rd.setSeed(seed);

        // 生成随机数
        int ran[] = new int[1024];
        for (int i = 0; i < 1024; i++) {
            ran[i] = rd.nextInt(10);
        }

        // 生成6位验证码
        StringBuffer validCodeBuff = new StringBuffer("");
        rd.setSeed(seed);
        int index = 0;

        for (int i = 0; i < validLength; i++) {
            index = rd.nextInt(1024 - index);
            validCodeBuff.append(Integer.toString(ran[index]));
        }

        String validCode = validCodeBuff.toString();

        return validCode;
     }
    
    public static Map<String ,Object> json2map(String jsonstr){
    	
    	JSONObject jsonObject = JSONObject.fromObject(jsonstr);
    	Iterator<String> keyIter = jsonObject.keys();
    	String key;
    	Object value;
    	Map<String,Object> valueMap = new HashMap<String,Object>();
    	
    	while (keyIter.hasNext()) {
	    	key = (String) keyIter.next();
	    	value = jsonObject.get(key);
	    	valueMap.put(key, value);
    	}
    	return valueMap;
    }

	public static void main(String arg[]) {
		Boolean a = Util.checkStr("123456789361", "([0-9]{11})*");
		System.out.println("a:" + a);
	}
	
	@SuppressWarnings("unchecked")
	public static String sendToNodeHttpRequest(String url) throws IOException {
		int status = -1;
		String response = "0";
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = connectionManager.getParams();
		params.setConnectionTimeout(5000);
		params.setSoTimeout(60000);
		params.setDefaultMaxConnectionsPerHost(32);// very important!!
		params.setMaxTotalConnections(256);// very important!!

		HttpClient httpClient = new HttpClient(connectionManager);
		// 设置编码
		httpClient.getParams().setContentCharset("utf-8");
		httpClient.getParams().setHttpElementCharset("utf-8");

		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		// 一定要有，否则会生成多个Cookie header送给web server
		httpClient.getParams().setParameter(
				"http.protocol.single-cookie-header", true);
		httpClient.getParams().setParameter("http.protocol.content-charset",
				"utf-8");
		ArrayList headerList = new ArrayList();
		Header accept = new Header(
				"Accept",
				"image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/x-silverlight, */*");
		headerList.add(accept);
		httpClient.getParams().setParameter("http.default-headers", headerList);
		httpClient.getParams().setParameter("http.protocol.version",
				HttpVersion.HTTP_1_1);
		httpClient.getParams().setParameter("http.method.retry-handler",
				new DefaultHttpMethodRetryHandler());

		PostMethod postMethod = new PostMethod(url);
		// httpClient.getParams().setParameter("http.socket.timeout", 1); //
		// 为HttpClient设置参数
		// httpClient.getHttpConnectionManager().getParams().setParameter("http.socket.timeout",
		// 1); // 为HttpConnetionManager设置参数
		// postMethod.getParams().setParameter("http.socket.timeout", 10000); //
		// 为HttpMethod设置参数

		try {
			postMethod.getParams().setUriCharset("utf-8");
			postMethod.setURI(new URI(url, false, "utf-8"));
			status = httpClient.executeMethod(postMethod);
			// logger.info("随机数："+randNm+"http请求返回的状态： " + status);
			if (status == HttpStatus.SC_OK) {
				response = postMethod.getResponseBodyAsString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			status = -1;
		} finally {
			if(response!=null&&response.equals("0")){
			seq++;
			logger.warn("发送消息给node出错次数 "+seq);
			
		}
			postMethod.releaseConnection();
		}
		return response;
	}
}
