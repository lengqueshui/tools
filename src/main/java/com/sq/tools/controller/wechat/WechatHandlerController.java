package com.sq.tools.controller.wechat;

import com.sq.tools.utils.HttpUtil;
import com.sq.tools.utils.WechatUtils;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.util.crypto.SHA1;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/msg")
public class WechatHandlerController {
	
	private static Logger logger = LoggerFactory.getLogger(WechatHandlerController.class);
	
	@Value("${wechat.name}")
	private String name;
	
	@Value("${wechat.appid}")
	private String appId;

	@Value("${wechat.secret}")
	private String secret;

	@Value("${wechat.token}")
	private String token;

	@Value("${wechat.aeskey}")
	private String aeskey;
	
	@Value("${wechat.defaultReply}")
	private String defaultReply;

	private WxMpInMemoryConfigStorage config;
	
	@PostConstruct
	public void init(){
		logger.info("===== Controller start success =====");
		//setConfigAndMpServiceAndRouter
		config = new WxMpInMemoryConfigStorage();
		config.setAppId(appId); // 设置微信公众号的appid
		config.setSecret(secret); // 设置微信公众号的app corpSecret
		config.setToken(token); // 设置微信公众号的token
		config.setAesKey(aeskey); // 设置微信公众号的EncodingAESKey
		defaultReply = defaultReply.replace("<br>", "\n");
	}
	
	@RequestMapping("/test")
	@ResponseBody
	public String test(){
		return "success";
	}

	//http://39.106.5.215:8080/tools/msg/entry
	@RequestMapping("/entry")
	public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		String signature = request.getParameter(WechatUtils.SIGNATURE);
		String nonce = request.getParameter(WechatUtils.NONCE);
		String timestamp = request.getParameter(WechatUtils.TIMESTAMP);
		String remoteAddr = HttpUtil.getIpAddress(request);
		// 判断服务器地址有效性
		if (!checkSignature(timestamp, nonce, signature)) {
			// 消息签名不正确，说明不是公众平台发过来的消息
			logger.warn("非法请求，推送方IP={}, nonce={}, timestamp={}, signature={}", 
					remoteAddr, nonce, timestamp, signature);
			response.getWriter().println("非法请求");
			return;
		}

		logger.info("非法请求，推送方IP={}, nonce={}, timestamp={}, signature={}",
				remoteAddr, nonce, timestamp, signature);
		String echostr = request.getParameter(WechatUtils.ECHOSTR);
		if (StringUtils.isNotBlank(echostr)) {
			// 说明是一个仅仅用来验证的请求，回显echostr
			response.getWriter().println(echostr);
			return;
		}

		WxMpXmlMessage inMessage;
		String encryptType = StringUtils.isBlank(request.getParameter(WechatUtils.ENCRYPT_TYPE)) ? "raw"
				: request.getParameter(WechatUtils.ENCRYPT_TYPE);
		switch (encryptType) {
		case "raw": // 明文
			inMessage = WxMpXmlMessage.fromXml(request.getInputStream());
			break;
		case "aes": // aes加密的消息
			String msgSignature = request.getParameter(WechatUtils.MSG_SIGNATURE);
			inMessage = WxMpXmlMessage.fromEncryptedXml(request.getInputStream(), config, timestamp, nonce,
					msgSignature);
			break;
		default:
			logger.warn("不可识别的加密类型:{}", encryptType);
			return;
		}

		if (inMessage == null) {
			return;
		}

		logger.info("微信消息入口---> 推送方IP={}, 消息体{} ", remoteAddr, inMessage.toString());
		// 关注类消息处理
		switch (inMessage.getMsgType()) {
		case WxConsts.XML_MSG_EVENT:// 事件（关注）
			String openId = inMessage.getFromUserName();
			//更新是否关注表
			if (WxConsts.EVT_SUBSCRIBE.equals(inMessage.getEvent())){
				//qrcodeService.subscribe(openId);
			} else if (WxConsts.EVT_UNSUBSCRIBE.equals(inMessage.getEvent())){
				//qrcodeService.updateUnSubscribe(openId);
			}

			break;
		case WxConsts.XML_MSG_TEXT:// 文本

		case WxConsts.XML_MSG_IMAGE:// 图片
		case WxConsts.XML_MSG_VOICE:// 声音
		case WxConsts.XML_MSG_VIDEO:// 视频
		case WxConsts.XML_MSG_NEWS:// 新闻
			// 转发多客服
			response.getWriter().write(WechatUtils.getTCSReturnMsg(inMessage));
			break;
		}

		return;
	}
	
	public boolean checkSignature(String timestamp, String nonce, String signature) {
		try {
			return SHA1.gen(token, timestamp, nonce).equals(signature);
		} catch (Exception e) {
			return false;
		}
	}

}