package com.sq.tools.utils;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutTextMessage;
import org.apache.commons.lang.StringUtils;

public class WechatUtils {
	
	private static final String QRSCENE = "qrscene_";
	
	public static final String SIGNATURE = "signature";
	
	public static final String NONCE = "nonce";
	
	public static final String TIMESTAMP = "timestamp";
	
	public static final String ECHOSTR = "echostr";
	
	public static final String ENCRYPT_TYPE = "encrypt_type";
	
	public static final String MSG_SIGNATURE = "msg_signature";
	
	
	public static String getTextMsg(WxMpXmlMessage wxMessage, String content){
		WxMpXmlOutTextMessage m = new WxMpXmlOutTextMessage();
		m.setToUserName(wxMessage.getFromUserName());
		m.setFromUserName(wxMessage.getToUserName());
		m.setMsgType(WxConsts.XML_MSG_TEXT);
		m.setContent(content);
		m.setCreateTime(System.currentTimeMillis());
		return m.toXml();
	}
	
	//多客服消息
	public static String getTCSReturnMsg(WxMpXmlMessage wxMessage){
		WxMpXmlOutTextMessage m = new WxMpXmlOutTextMessage();
		m.setToUserName(wxMessage.getFromUserName());
		m.setFromUserName(wxMessage.getToUserName());
		m.setMsgType(WxConsts.XML_TRANSFER_CUSTOMER_SERVICE);
		m.setContent(wxMessage.getContent());
		m.setCreateTime(System.currentTimeMillis());
		return m.toXml();
	}
	
	
	
	public static String getSceneId(String eventKey){
		if(StringUtils.isEmpty(eventKey)){
			return "";
		}
		
		if (eventKey.startsWith(QRSCENE)) {
			return eventKey.split("\\_")[1];
		}
		
		return eventKey;
	}
	

}