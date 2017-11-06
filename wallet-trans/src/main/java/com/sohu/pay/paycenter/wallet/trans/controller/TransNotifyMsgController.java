package com.sohu.pay.paycenter.wallet.trans.controller;


import com.sohu.pay.paycenter.common.exception.RequestParamException;
import com.sohu.pay.paycenter.common.vo.ExecInfo;
import com.sohu.pay.paycenter.wallet.core.bean.TransInfo;
import com.sohu.pay.paycenter.wallet.trans.service.TransNotifyMsgService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 异步通知补单   向业务发送确认消息
 * @author Administrator
 *
 */

@Controller
@RequestMapping("/wallet/trans")
public class TransNotifyMsgController {
	 private static final Logger logger = LogManager.getLogger(TransNotifyMsgController.class);
	 
	 @Autowired
	 private TransNotifyMsgService transNotifyMsgService;
	 
	//异步通知补单   向业务发送确认消息
	@RequestMapping("/transNotifyMsg")
	@ResponseBody
	public ExecInfo transNotifyMsg(TransInfo transInfo) {
		ExecInfo execInfo = new ExecInfo();
		
		if (StringUtils.isBlank(transInfo.getTransId())) {
			logger.error("[异步通知补单]请求参数为空");
			execInfo.setResult(ExecInfo.ERRPR);
			execInfo.setErrorDesc("请求参数为空");
			return execInfo;
		}
	
		try {
			
			transNotifyMsgService.transNotifyMsg(transInfo);
			execInfo.setResult(ExecInfo.SUCCESS);		
			
		}catch (RequestParamException e) {
			logger.error("异步通知补单",e);
			execInfo.setResult(ExecInfo.ERRPR);
			execInfo.setErrorDesc(e.getMessage());
		}catch (Exception e) {
			logger.error("异步通知补单",e);
			execInfo.setResult(ExecInfo.ERRPR);
			execInfo.setErrorDesc(e.getMessage());
			
		}
		
		return execInfo;	
	}
	 
}
