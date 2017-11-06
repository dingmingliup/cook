package com.sohu.pay.paycenter.wallet.trans.service;


import com.sohu.pay.paycenter.wallet.core.bean.TransInfo;

public interface TransNotifyMsgService {

	//异步通知补单   向业务发送确认消息
	public void transNotifyMsg(TransInfo transInfo) throws Exception;
}
