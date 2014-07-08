package com.paic.gimap.common.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import com.paic.gimap.common.biz.constant.Constant;
import com.paic.gimap.common.dto.AgentInfo;
import com.paic.gimap.common.dto.CommonMessage;
import com.paic.gimap.common.dto.MsgInfo;

/**
 * @author EX-LIZHISHEN001
 * 
 */
public class CommonUtils {

	private static Logger logger = Logger.getLogger(CommonUtils.class);

	/**
	 * 获取返回消息
	 * 
	 * @param code
	 * @param msg
	 * @return
	 */
	public static String getResultMessage(String code, String msg) {

		CommonMessage sys = new CommonMessage();
		sys.setCode(code);
		sys.setMsg(msg);
		logger.info("发送消息 CommonMessage=" + JsonUtils.toJsonString(sys));
		return JsonUtils.toJsonString(sys);
	}

	

	 

	 

	public static String getUniqueNO() {

		logger.info("获取一个md5码 getUniqueNO=");

		String uniqueString = System.currentTimeMillis() + "" + Math.random() * 100000000;
		try {
			MessageDigest md5Digest = MessageDigest.getInstance("MD5");
			byte[] bs = md5Digest.digest(uniqueString.getBytes("utf-8"));
			StringBuffer buf = new StringBuffer("");
			byte i;
			for (int offset = 0; offset < bs.length; offset++) {
				i = bs[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			uniqueString = buf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return uniqueString;

	}



	// 发送消息给坐席
	@SuppressWarnings("deprecation")
	public static String sendMsgToAgentAllot(String msg, String msgType, String taskId, String extensionNo, String agentId, String serverfrom, String microLetter, String customerName, String source, String mobileNo, String taskstatus, String caseNo, String sip_server_url, String ip, String paImNo) throws Exception {

		String url = sip_server_url + "/sendToAgentServlet?microLetter=" + microLetter + "&msgContext=" + URLEncoder.encode(msg, "UTF-8") + "&msgContextType=" + msgType + "&taskId=" + taskId + "&customerName=" + URLEncoder.encode(null == customerName ? "" : customerName, "UTF-8") + "&source=" + source + "&flag=0&mobileNo=" + mobileNo + "&taskstatus=" + taskstatus + "&caseNo=" + caseNo + "&extensionNo=" + extensionNo + "&ip=" + ip + "&pa_im=" + paImNo;

		String res = Util.sendHttpRequest(url);
		logger.info("发送消息给坐席 sendMsgToAgentAllot=" + url + " 结果：" + res);

		return res;

	}

	public static int sendMessageToClient(String server_url, String microLetter, String message, String messageType, String source, String paImNo, Object extensionNo, Object ip) {

		logger.info("发送消息给客户并保存消息sendMsgToClientAndSaveMsg()");
		String url = null;
		try {
			messageType=messageType2Client(messageType);

			url = server_url + "/sendToCustomerServlet?microLetter=" + microLetter + "&msgContext=" + URLEncoder.encode(null == message ? "" : message, "UTF-8") + "&msgContextType=" + messageType + "&source=" + source + "&pa_im=" + paImNo;
		} catch (UnsupportedEncodingException e) {
			logger.error("发送消息出错！" + e.getMessage(), e);
			e.printStackTrace();
			return 0;
		}
		if (null != extensionNo && null != ip) {
			url = url + "&extensionNo=" + extensionNo + "&ip=" + ip;
		}
		logger.info("发送消息给客户并保存消息URL：" + url);

		String isSucc = null;
		try {
			isSucc = Util.sendHttpRequest(url);
		} catch (IOException e) {
			logger.error("发送消息出错！" + e.getMessage(), e);
			e.printStackTrace();
			return 0;
		}
		// 如果发送给客户信息失败,
		if ("1".equals(isSucc)) {
			return 1;
		} else {

			return 0;
		}

	}

	/**
	 * 发送模板消息给客户
	 * 
	 * @param server_url
	 * @param microLetter
	 * @param message
	 * @param messageType
	 * @param source
	 * @param paImNo
	 * @param extensionNo
	 * @param ip
	 * @return
	 */
	public static int sendTemplateMessageToClient(String server_url, String microLetter, String message, String messageType, String source, String paImNo, String extensionNo, String ip, String templateNo) {

		logger.info("发送消息给客户并保存消息sendMsgToClientAndSaveMsg()");
		String url = null;
		if (templateNo == null)
			templateNo = "";
		try {
			messageType = messageType2Client(messageType);

			url = server_url + "/sendToCustomerServlet?microLetter=" + microLetter + "&msgContext=" + URLEncoder.encode(null == message ? "" : message, "UTF-8") + "&msgContextType=" + messageType + "&source=" + source + "&pa_im=" + paImNo + "&templateNo=" + templateNo;
		} catch (UnsupportedEncodingException e) {
			logger.error("发送消息出错！" + e.getMessage(), e);
			e.printStackTrace();
			return 0;
		}
		if (null != extensionNo && null != ip) {
			url = url + "&extensionNo=" + extensionNo + "&ip=" + ip;
		}
		logger.info("发送消息给客户并保存消息URL：" + url);

		String isSucc = null;
		try {
			isSucc = Util.sendHttpRequest(url);
		} catch (IOException e) {
			logger.error("发送消息出错！" + e.getMessage(), e);
			e.printStackTrace();
			return 0;
		}
		// 如果发送给客户信息失败,
		if ("1".equals(isSucc)) {
			return 1;
		} else {

			return 0;
		}

	}

	public static String messageType2Client(String messageType) {
		if (messageType != null) {
			if (Constant.STATAND_TEXT_CONTENT_TYPE.equals(messageType)) {
				messageType = Constant.TEXT_CONTENT_TYPE;
			}
			if (Constant.STATAND_IMAGE_CONTENT_TYPE.equals(messageType)) {
				messageType = Constant.IMAGE_CONTENT_TYPE;
			}
			if (Constant.STATAND_SDP_CONTENT_TYPE.equals(messageType)) {
				messageType = Constant.VOICE_CONTENT_TYPE;
			}
			if (Constant.STATAND_IMAGE_CONTENT_TYPE2.equals(messageType)) {
				messageType = Constant.IMAGE_CONTENT_TYPE;
			}

		}
		return messageType;
	}
	
	
	public static String messageType2Agent(String messageType) {
		if (messageType != null) {
			if (Constant.TEXT_CONTENT_TYPE.equals(messageType)) {
				messageType = Constant.STATAND_TEXT_CONTENT_TYPE;
			}
			if (Constant.IMAGE_CONTENT_TYPE.equals(messageType)) {
				messageType = Constant.STATAND_IMAGE_CONTENT_TYPE;
			}
			if (Constant.VOICE_CONTENT_TYPE.equals(messageType)) {
				messageType = Constant.STATAND_SDP_CONTENT_TYPE;
			}
			if (Constant.IMAGE_CONTENT_TYPE.equals(messageType)) {
				messageType = Constant.STATAND_IMAGE_CONTENT_TYPE2;
			}

		}
		return messageType;
	}

	/**
	 * 发送模板消息给客户
	 * 
	 * @param server_url
	 * @param microLetter
	 * @param message
	 * @param messageType
	 * @param source
	 * @param paImNo
	 * @param extensionNo
	 * @param ip
	 * @return
	 */
	public static Map sendMessageToWX4Res(String server_url, String microLetter, String message, String messageType, String source, String paImNo, Object extensionNo, Object ip) {

		logger.info("发送消息给客户并保存消息sendMessageToWX4Res()");
		Map resultMap = new HashMap();

		String url = null;
		try {
			messageType=messageType2Client(messageType);

			url = server_url + "/SendToNode4ResServlet?microLetter=" + microLetter + "&msgContext=" + URLEncoder.encode(null == message ? "" : message, "UTF-8") + "&msgContextType=" + messageType + "&source=" + source + "&pa_im=" + paImNo;
		} catch (UnsupportedEncodingException e) {
			logger.error("发送消息出错！" + e.getMessage(), e);
			e.printStackTrace();
			// return 0;
		}
		if (null != extensionNo && null != ip) {
			url = url + "&extensionNo=" + extensionNo + "&ip=" + ip;
		}
		logger.info("发送消息给客户并保存消息URL：" + url);

		String isSucc = null;
		try {
			isSucc = Util.sendHttpRequest4Res(url);
		} catch (IOException e) {
			logger.error("发送消息出错！" + e.getMessage(), e);
			e.printStackTrace();
			resultMap.put("rtnFlag", "FAIL");
			resultMap.put("rtnMsg", "调用sip出错");
			return resultMap;
		}
		logger.info("发送消息给客户返回值：" + isSucc);
		// 如果发送给客户信息失败,
		if ("".equals(isSucc)) {
			resultMap.put("rtnFlag", "FAIL");
			resultMap.put("rtnMsg", "调用sip出错2");
		} else {
			resultMap = jsonToMap(isSucc);

		}
		return resultMap;

	}

	@SuppressWarnings("unchecked")
	public static Object creatBeanObjectByMap(Class c, Map<String, String> data) {
		logger.info("发送消息给坐席 creatBeanObjectByMap map对象转换注入到对象中" + data);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Object bean = null;
		try {
			bean = c.newInstance();

			BeanInfo beanInfo = null;
			beanInfo = Introspector.getBeanInfo(c);

			PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();

			for (PropertyDescriptor pd : descriptors) {
				try {
					String name = pd.getName();

					Class clazz = pd.getPropertyType();
					Method setMethod = pd.getWriteMethod();
					String typeName = clazz.getName();

					String value = data.get(name);

					if (name.equals("class"))
						continue;

					if (value != null)
						if (value.trim().length() != 0) {
							if (typeName.equals("java.lang.String")) {
								setMethod.invoke(bean, new Object[] { value });
							} else {
								java.util.Date tDate;
								if (typeName.equals("java.util.Date")) {
									tDate = sdf.parse(value);
									setMethod.invoke(bean, new Object[] { tDate });
								} else if (typeName.equals("java.sql.Date")) {

									tDate = sdf.parse(value);
									java.sql.Date sqlDate = new java.sql.Date(tDate.getTime());
									setMethod.invoke(bean, new Object[] { sqlDate });
								} else if (typeName.equals("java.sql.Timestamp")) {
									Timestamp timestamp = getDateTime(value);
									if (timestamp != null) {
										setMethod.invoke(bean, new Object[] { timestamp });
									}
								} else if ((typeName.equals("java.lang.Integer")) || (typeName.equals("int"))) {
									Integer val = Integer.valueOf(Integer.parseInt(value));
									setMethod.invoke(bean, new Object[] { val });
								} else if ((typeName.equals("java.lang.Long")) || (typeName.equals("long"))) {
									Long val = Long.valueOf(Long.parseLong(value));
									setMethod.invoke(bean, new Object[] { val });
								} else if ((typeName.equals("java.lang.Short")) || (typeName.equals("short"))) {
									Short val = Short.valueOf(Short.parseShort(value));
									setMethod.invoke(bean, new Object[] { val });
								} else if ((typeName.equals("java.lang.Boolean")) || (typeName.equals("boolean"))) {
									Boolean bo = Boolean.valueOf(Boolean.parseBoolean(value));
									setMethod.invoke(bean, new Object[] { bo });
								}
							}

						}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IntrospectionException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {

			e.printStackTrace();
		} catch (IllegalAccessException e) {

			e.printStackTrace();
		} catch (InstantiationException e) {

			e.printStackTrace();
		}

		return bean;

	}

	@SuppressWarnings("unchecked")
	public static Object updateBeanObjectByMap(Object bean, Map<String, String> data) {

		logger.info(" updateBeanObjectByMap map对象转换注入到对象中" + data);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {

			BeanInfo beanInfo = null;
			beanInfo = Introspector.getBeanInfo(bean.getClass());

			PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();

			for (PropertyDescriptor pd : descriptors) {
				try {
					String name = pd.getName();

					Class clazz = pd.getPropertyType();
					Method setMethod = pd.getWriteMethod();
					String typeName = clazz.getName();

					String value = data.get(name);

					if (name.equals("class"))
						continue;

					if (value != null)
						if (value.trim().length() != 0) {
							if (typeName.equals("java.lang.String")) {
								setMethod.invoke(bean, new Object[] { value });
							} else {
								java.util.Date tDate;
								if (typeName.equals("java.util.Date")) {
									tDate = sdf.parse(value);
									setMethod.invoke(bean, new Object[] { tDate });
								} else if (typeName.equals("java.sql.Date")) {

									tDate = sdf.parse(value);
									java.sql.Date sqlDate = new java.sql.Date(tDate.getTime());
									setMethod.invoke(bean, new Object[] { sqlDate });
								} else if (typeName.equals("java.sql.Timestamp")) {
									Timestamp timestamp = getDateTime(value);
									if (timestamp != null) {
										setMethod.invoke(bean, new Object[] { timestamp });
									}
								} else if ((typeName.equals("java.lang.Integer")) || (typeName.equals("int"))) {
									Integer val = Integer.valueOf(Integer.parseInt(value));
									setMethod.invoke(bean, new Object[] { val });
								} else if ((typeName.equals("java.lang.Long")) || (typeName.equals("long"))) {
									Long val = Long.valueOf(Long.parseLong(value));
									setMethod.invoke(bean, new Object[] { val });
								} else if ((typeName.equals("java.lang.Short")) || (typeName.equals("short"))) {
									Short val = Short.valueOf(Short.parseShort(value));
									setMethod.invoke(bean, new Object[] { val });
								} else if ((typeName.equals("java.lang.Boolean")) || (typeName.equals("boolean"))) {
									Boolean bo = Boolean.valueOf(Boolean.parseBoolean(value));
									setMethod.invoke(bean, new Object[] { bo });
								}
							}

						}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IntrospectionException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {

			e.printStackTrace();
		}

		return bean;

	}

	public static Timestamp getDateTime(String text) {

		logger.info(" getDateTime 日期转换" + text);
		SimpleDateFormat sdf = null;

		if (text == null || text.trim().length() == 0 || !text.contains("-") || !text.contains(":"))
			return null;

		text = text.trim();

		try {

			String regexString = null;
			if (text.length() > 10) {
				regexString = "[\\d]{4}-[\\d]{1,2}-[\\d]{1,2} [\\d]{1,2}:[\\d]{1,2}:[\\d]{1,2}";
				sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			} else {
				regexString = "[\\d]{4}-[\\d]{1,2}-[\\d]{1,2}";
				sdf = new SimpleDateFormat("yyyy-MM-dd");
			}

			Pattern p = Pattern.compile(regexString);
			Matcher m = p.matcher(text);
			if (m.find()) {
				String findStr = m.group();

				java.util.Date date = sdf.parse(findStr);

				if (date != null) {
					Timestamp time = new Timestamp(date.getTime());
					return time;
				}

			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 发送消息给客户并保存消息
	 */
	@SuppressWarnings("deprecation")
	public static String sendMsgToClientMsg(MsgInfo contentInfo, String serverfrom, String microLetter, String source, String server_url, AgentInfo agentInfo, String paImNo) throws Exception {
		logger.info("发送消息给客户并保存消息sendMsgToClientAndSaveMsg()");
		String msgType=messageType2Client(contentInfo.getMsgType());
		contentInfo.setMsgType(msgType);
		String url = server_url + "/sendToCustomerServlet?microLetter=" + microLetter + "&msgContext=" + URLEncoder.encode(null == contentInfo.getMsgContext() ? "" : contentInfo.getMsgContext(), "UTF-8") + "&msgContextType=" + contentInfo.getMsgType() + "&source=" + source + "&pa_im=" + paImNo;
		if (null != agentInfo) {
			url = url + "&extensionNo=" + agentInfo.getExtensionNo() + "&ip=" + agentInfo.getLogonIp();
		}
		logger.info("发送消息给客户并保存消息URL：" + url);

		String isSucc = Util.sendHttpRequest(url);
		return isSucc;

	}

	@SuppressWarnings("deprecation")
	public static String sendMsgToAgent(AgentInfo newAnInfo, MsgInfo msgInfo, String serverfrom, String microLetter, String customerName, String source, String flag, String mobileNo, String taskstatus, String caseNo, String sip_server_url, String paImNo) throws Exception {
		logger.info("发送消息给坐席sendMsgToAgent()");
		String res = null;
		String content = msgInfo.getMsgContext();
		if (content == null || content.trim().length() == 0) {
			content = "0";

		}
		if (content != null) {
			List<String> list = DataUtil.divideLength(content, 300);
			if (list != null && list.size() > 0) {
				for (String c : list) {

					String url = sip_server_url + "/sendToAgentServlet?microLetter=" + microLetter + "&msgContext=" + URLEncoder.encode(null == c ? "" : c, "UTF-8") + "&msgContextType=" + msgInfo.getMsgType() + "&taskId=" + msgInfo.getTaskOid() + "&customerName=" + URLEncoder.encode(null == customerName ? "" : customerName, "UTF-8") + "&source=" + source + "&flag=" + flag + "&mobileNo=" + mobileNo + "&taskstatus=" + taskstatus + "&caseNo=" + caseNo + "&extensionNo=" + newAnInfo.getExtensionNo() + "&ip=" + newAnInfo.getLogonIp() + "&pa_im=" + paImNo;
					logger.info("发送消息给坐席调sip的httpServletURL：" + url);
					res = Util.sendHttpRequest(url);

					if (flag != null && flag.trim().equals("0")) {
						return "1";
					}
				}
			}
		}
		return res;

	}

	// 转发消息给目的坐席
	@SuppressWarnings("deprecation")
	public static String sendConnectionMsgToAgent(String extensionNo, String ip, String jsonObject) throws Exception {
		ServiceRequestID serviceRequestID = ServiceRequestID.getInstance();
		jsonObject = URLEncoder.encode(jsonObject, "UTF-8");
		String sip_server_url = serviceRequestID.getSIP_SERVER_URL();
		String url = sip_server_url + "/sendConnectionMsgToAgentServlet?extensionNo=" + extensionNo + "&ip=" + ip + "&jsonObject=" + jsonObject;
		String res = Util.sendHttpRequest(url);
		logger.info("发送消息给坐席 sendConnectionMsgToAgent=" + url + " 结果：" + res);
		return res;
	}

	public static String sendMsgToClient(String content, String microLetter, String source, String paImNo, String server_url) throws Exception {
		logger.info("发送消息给客户sendMsgToClient()");
		String url = server_url + "/sendToCustomerServlet?microLetter=" + microLetter + "&msgContext=" + URLEncoder.encode(null == content ? "" : content, "UTF-8") + "&msgContextType=" + Constant.TEXT_CONTENT_TYPE + "&source=" + source + "&pa_im=" + paImNo;
		logger.info("发送消息给客户并保存消息URL：" + url);
		String isSucc = Util.sendHttpRequest(url);
		logger.info("发送消息返回的状态：" + isSucc);
		return isSucc;

	}

	public static String sendMessageToNode(String server_url,String json) {
		
		logger.info("发送消息给客户并保存消息sendMessageToNode()");
		
		
		String response = "";
		try {
			String url = server_url + "?message="
					+ URLEncoder.encode(json.toString(), "utf-8");
					logger.info("sendMessageToNode url is:"+url);
					
					response = Util.sendHttpRequest(url);
					logger.info("直接发送消息给NODE，返回:"+response);
					
		} catch (Exception e) {
			logger.error("直接发送消息给NODE出错！" + e.getMessage(), e);
			e.printStackTrace();
//			return 0;
		}
		
		return response;
		
	}
	
	/**
	 * json转换为map
	 * 
	 * @param o
	 * @return
	 */
	public static Map jsonToMap(Object o) {
		JSONObject jsonObject = JSONObject.fromObject(o);
		Iterator keyIter = jsonObject.keys();
		String key;
		Object value;
		Map valueMap = new HashMap();
		while (keyIter.hasNext()) {
			key = (String) keyIter.next();
			value = jsonObject.get(key);
			valueMap.put(key, value);
		}
		return valueMap;

	}

	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String formatTimestampWithYMDHMS(Timestamp t) {
		return format.format(t);
	}

	public static String formatTimestampWithYMDHMS(java.util.Date t) {
		return format.format(t);
	}

	public static String formatTimestampWithYMDHMS(java.sql.Date t) {
		return format.format(t);
	}

	@SuppressWarnings("static-access")
	public static String sendMessageToNodeClient(String clientImNo, String msgContext, String msgType, String source, String paImNo, String extensionNo, String ip, String imTemplateNo) {
				
		String reqMsg = "clientImNo=" + (clientImNo == null ? "" : clientImNo) + ";msgContext=" + (msgContext == null ? "" : msgContext) + ";msgType=" + (msgType == null ? "" : msgType) + ";source=" + (source == null ? "" : source) + ";paImNo=" + (paImNo == null ? "" : paImNo) + ";extensionNo=" + (extensionNo == null ? "" : extensionNo) + ";ip=" + (ip == null ? "" : ip);
		logger.info("sendMessageToNodeClient " + reqMsg);
		try {
			
			msgType=messageType2Client(msgType);
			
			
			String NODE_SERVER_URL = null;
			if (paImNo != null && clientImNo != null && msgContext != null && msgType != null && source != null) {
				NODE_SERVER_URL = ConfigTools.getInstance().getNodeURL(paImNo);

				if (NODE_SERVER_URL != null) {
					JSONObject obj = new JSONObject();
					obj.put("toMicroLetter", clientImNo);

					try {
						msgContext = URLEncoder.encode(msgContext, "utf-8");
					} catch (Exception e) {
					}
					obj.put("type", msgType);
					obj.put("source", source);
					obj.put("pa_im", paImNo);

					String url = NODE_SERVER_URL + "?message=" + URLEncoder.encode(obj.toString(), "utf-8") + "&content=" + msgContext + "&code=1" + "&templateNo=" + imTemplateNo;

					String response = Util.sendHttpRequest4Res(url);
					logger.warn(reqMsg + ";send to node url:" + (url == null ? "" : url));
					logger.warn(reqMsg + ";send to node result:" + (response == null ? "" : response));

					return response;

				} else {
					logger.warn(reqMsg + ";get nodeURL fail,can not send message");
				}
			} else {
				logger.warn(reqMsg + ";args hava null, can not send message");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(reqMsg + ";can not send message;" + e.getMessage(), e);
		} catch (IOException e) {
			logger.error(reqMsg + ";can not send message;" + e.getMessage(), e);
		}

		return null;
	}

	/**
	 * 发送消息给客户并保存消息
	 * @param msgContext
	 * @param msgType
	 * @param microLetter
	 * @param source
	 * @param paImNo
	 * @param extensionNo坐席登陆分机号，用于发送消息失败提醒坐席
	 * @param logonIp坐席登陆IP，可为空，用于发送消息失败提醒坐席
	 * @return
	 * @throws Exception
	 */
	public static String sendMsgToClient(String msgContext, String msgType,String microLetter, String source, String paImNo,String extensionNo, String logonIp){
		String url = null;
        try {
        	
        	msgType=messageType2Client(msgType);
        	
			String server_url = ServiceRequestID.getInstance().getSIP_SERVER_URL();
			logger.info("发送消息给客户并保存消息sendMsgToClientAndSaveMsg()");
			url = server_url + "/sendToCustomerServlet?microLetter=" + microLetter + "&msgContext=" + URLEncoder.encode(null == msgContext ? "" : msgContext, "UTF-8") + "&msgContextType=" + msgType + "&source=" + source + "&pa_im=" + paImNo+ "&extensionNo=" + extensionNo + "&ip=" + logonIp;
			logger.info("发送消息给客户并保存消息URL：" + url);
			return Util.sendHttpRequest(url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.error("sendMsgToClient url为"+url + ";can not send message;" + e.getMessage(), e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("sendMsgToClient url为"+url + ";can not send message;" + e.getMessage(), e);
		}    
		return null;
	}
	
	public static int sendMessageToNode(String server_url, String microLetter, String message, String messageType, String source, String paImNo, Object extensionNo, Object ip) {

		// String microLetter = request.getParameter("microLetter");
		// String msgContext = URLDecoder.decode(request.getParameter("msgContext"),"utf-8");
		// String msgContextType = request.getParameter("msgContextType");

		// String errorCode = request.getParameter("errorCode");
		// String source = request.getParameter("source");
		// String extensionNo = request.getParameter("extensionNo");
		// String ip = request.getParameter("ip");
		// String paIm = request.getParameter("pa_im");
		// String templateNo = request.getParameter("templateNo");

		// param.put("templateNo", templateNo);

		logger.info("发送消息给客户并保存消息sendMsgToClientAndSaveMsg()");
		String url = null;
		String isSucc = null;
		try {
			
			messageType=messageType2Client(messageType);
			 
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setMsgContext(message);
			msgInfo.setMsgType(messageType);
			msgInfo.setPaImNo(paImNo);
			// 添加一个官方微信号
			AgentInfo agentInfo = null;
			if (null == ip && null == extensionNo) {
				agentInfo = new AgentInfo();
				agentInfo.setExtensionNo((String) extensionNo);
				agentInfo.setIp((String) ip);
			}
			logger.info("sendMessageToNode参数获取完毕：" + paImNo);
			// 增加参数map，renpeng162 2013-8-13
			if (msgInfo == null || msgInfo.getPaImNo() == null || msgInfo.getPaImNo().trim().length() == 0) {
				logger.error("发送给客户的消息中没有包含平安im号");
				return 0;
			}
			Map<String, Object> param = new HashMap<String, Object>();

			JSONObject obj = new JSONObject();
			String content = msgInfo.getMsgContext();
			String templateNo = (String) param.get("templateNo");
			logger.debug("客户ID是：" + microLetter + "，转义前的消息内容为：" + content);

			content = content == null ? "" : content;
			templateNo = templateNo == null ? "" : templateNo;

			if ("BANK_TALK_WEB".equals(msgInfo.getPaImNo().trim())) {
				content = content.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>");

				while (content.indexOf("&lt;a") > -1 && content.indexOf("&lt;/a&gt;") > -1 && content.indexOf("&lt;/a&gt;") > content.indexOf("&lt;a")) {
					String aPreStr = content.substring(0, content.indexOf("&lt;a"));
					String aNextStr = content.substring(content.indexOf("&lt;a"));
					String aMidStr = aNextStr.substring(0, aNextStr.indexOf("&lt;/a&gt;"));

					if (aMidStr.indexOf("&gt;") > -1) {
						aNextStr = aNextStr.replaceFirst("&lt;a", "<a").replaceFirst("&gt;", ">").replaceFirst("&lt;/a&gt;", "</a>");
						content = aPreStr + aNextStr;
					}
				}
			}
			content = content.replaceAll("\\\\", "\\\\\\\\").replace("\\\\", "\\");
			logger.debug("客户ID是：" + microLetter + "URL为：" + url + "，转义后的消息内容为：" + content);
			obj.put("toMicroLetter", microLetter);
			try {
				// obj.put("content", content);
				content = URLEncoder.encode(content, "utf-8");
				obj.put("type", msgInfo.getMsgType());
				obj.put("source", source);
				obj.put("pa_im", msgInfo.getPaImNo());
				logger.debug("客户发送消息的官方号：" + msgInfo.getPaImNo() + "，转发消息给客户：" + microLetter + "，转发消息给客户封装成json格式的消息为：" + obj.toString());

				String NODE_SERVER_URL = server_url;
				if (NODE_SERVER_URL == null) {
					logger.error("系统配置文件中没有配置平安im号的node服务链接=\"" + msgInfo.getPaImNo().trim().toUpperCase() + "\"");
					return 0;
				}
				url = NODE_SERVER_URL + "?message=" + URLEncoder.encode(obj.toString(), "utf-8") + "&content=" + content + "&code=1" + "&templateNo=" + templateNo;// 这里多加了一个code来区分，在线客服列队消息
				// String url = NODE_SERVER_URL+"/"+microLetter+"/"+content;
				// logger.debug("转发消息给客户：" + microLetter + "，请求的HttpURL为：" + url);
				String response = Util.sendToNodeHttpRequest(url);
				// logger.info("坐席发消息给客户，node返回结果：" + response);
				logger.debug("转发消息给客户[" + microLetter + "]，请求的HttpURL为：[" + url + "];node返回结果：[" + response + "]");
				if (!"200".equals(response)) {
					logger.info("客户ID是：" + microLetter + "，转发消息给客户失败");
					return 0;
				} else {
					return 1;
				}
				// url = server_url + "/sendToCustomerServlet?microLetter=" + microLetter + "&msgContext=" + URLEncoder.encode(null == message ? "" : message, "UTF-8") + "&msgContextType=" + messageType + "&source=" + source + "&pa_im=" + paImNo;
			} catch (IOException e) {
				logger.error("客户ID是：" + microLetter + "，转发消息给客户请求发生IO异常：" + e.getMessage(), e);
				e.printStackTrace();
				return 0;
			}
		} catch (Exception e) {
			logger.error("发送消息出错！" + e.getMessage(), e);
			e.printStackTrace();
			return 0;
		}
		// if (null != extensionNo && null != ip) {
		// url = url + "&extensionNo=" + extensionNo + "&ip=" + ip;
		// }
		// logger.info("发送消息给客户并保存消息URL：" + url);
		//
		// try {
		// isSucc = Util.sendHttpRequest(url);
		// } catch (IOException e) {
		// logger.error("发送消息出错！" + e.getMessage(), e);
		// e.printStackTrace();
		// return 0;
		// }
		// // 如果发送给客户信息失败,
		// if ("1".equals(isSucc)) {
		// return 1;
		// } else {
		//
		// return 0;
		// }

	}
	
	public static String replaceDymic(String content,Map<String,Object> argMap){
		if(content!=null){		 
			if(argMap!=null&&argMap.size()>0){
				for(Entry<String,Object> entry : argMap.entrySet()){
					String key = entry.getKey();
					Object value = entry.getValue();					
					if(key!=null&&value!=null&&(value instanceof String||value instanceof Integer||value instanceof Long)){
						key = key.toUpperCase();
						content=content.replace("${"+key+"}", String.valueOf(value));						
					}
					
					
				}
			}
			
			
		}
		
		return content;
	}
}
