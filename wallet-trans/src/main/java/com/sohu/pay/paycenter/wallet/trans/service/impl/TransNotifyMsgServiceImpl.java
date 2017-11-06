package com.sohu.pay.paycenter.wallet.trans.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sohu.pay.paycenter.common.exception.RequestParamException;
import com.sohu.pay.paycenter.common.http.HttpUtils;
import com.sohu.pay.paycenter.common.security.DigestUtils;
import com.sohu.pay.paycenter.wallet.core.bean.TransInfo;
import com.sohu.pay.paycenter.wallet.core.dao.TransInfoMapper;
import com.sohu.pay.paycenter.wallet.trans.service.TransNotifyMsgService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Service
public class TransNotifyMsgServiceImpl implements TransNotifyMsgService{
	private static final Logger logger = LogManager.getLogger(TransNotifyMsgServiceImpl.class);
	
	@Autowired
	private TransInfoMapper transInfoMapper;
	 //异步通知补单地址
	 @Value("${wallet.transNotifyMsg.url}")
	private String transNotifyMsgUrl;
	 
	private final static String respStatus="status"; //钱包返回状态
	private final static String respErrDesc="failTransid";//钱包返回失败描述

	protected static final int RESP_SUCCESS = 0;//钱包执行成功
	protected static final int RESP_FAIL = 1; //钱包执行
	 
	//异步通知补单   向业务发送确认消息
	public void transNotifyMsg(TransInfo transInfo) throws RequestParamException,Exception{
		transInfo = getTransInfo(transInfo);
		
		if(transInfo == null){
			throw new RequestParamException("交易记录为空");
		}
		if(transInfo.getNotityStatus()== null){
			throw new RequestParamException("通知状态为空,不能进行补发");
		}

		if("0".equals(String.valueOf(transInfo.getNotityStatus()))){
			throw new RequestParamException("通知状态为通知成功,不能进行补发");
		}
		
				
		Map<String, String> params = new HashMap<String, String>();
		params.put("transids", transInfo.getTransId());
		
		try {
			
			String sign = DigestUtils.md5(transInfo.getTransId());
			params.put("sign", sign);
			
			logger.info("[异步通知补单]transId:"+transInfo.getTransId()+"发往钱包补发异步通知URL:"+transNotifyMsgUrl+" 参数:"+params.toString());
			
			String result = HttpUtils.doPost(transNotifyMsgUrl, null, params, "UTF-8");
			
			logger.info("[异步通知补单]transId:"+transInfo.getTransId()+"钱包返回结果:"+result);
			
			JSONObject resultJSON = JSONObject.parseObject(result);
			if(resultJSON == null){
				throw new RequestParamException("钱包处理异常");
			}
			
			if(!(RESP_SUCCESS == resultJSON.getIntValue(respStatus))){
				throw new RequestParamException(""+resultJSON.get(respErrDesc));
			}
		}catch (RequestParamException e) {
			logger.error(e);
			throw e;
		}catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}
	
	private TransInfo getTransInfo(TransInfo transInfo) {
		SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
		Long start = Long.parseLong(format.format(transInfo.getStartTime()));
		transInfo.setStartPartition(start);
		transInfo.setVersionOptimizedLock(null);
		return transInfoMapper.selectFieldsForObject(transInfo);
	}
}
