package com.sohu.pay.paycenter.wallet.trans.service.impl;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sohu.pay.paycenter.wallet.core.bean.RedPackInfo;
import com.sohu.pay.paycenter.wallet.core.bean.TransInfo;
import com.sohu.pay.paycenter.wallet.core.dao.RedPackMapper;
import com.sohu.pay.paycenter.wallet.core.dao.TransInfoMapper;
import com.sohu.pay.paycenter.wallet.trans.controller.RedPackController;
import com.sohu.pay.paycenter.wallet.trans.service.RedPackService;
@Service
@Transactional
public class RedPackServiceImpl implements RedPackService {
	private Logger logger = LogManager.getLogger(RedPackServiceImpl.class);
	 @Autowired
	 private RedPackMapper redPackMapper;

	@Override
	public Page<RedPackInfo> selectByFormInfo(RedPackInfo redpactTransBean) {
		if(redpactTransBean.getDealStatus()!=null){
		   if(redpactTransBean.getDealStatus()==25){
			   redpactTransBean.setStatus((short) 0);
		   }else if(redpactTransBean.getDealStatus()==26){
			   redpactTransBean.setStatus((short) 1);
		   }else if(redpactTransBean.getDealStatus()==27){
			   redpactTransBean.setStatus((short) 2);
		   }
			
		}
		Calendar  c=Calendar.getInstance();
		c.setTime(redpactTransBean.getDealFinishTime());
		c.add(Calendar.DATE, 1);
		redpactTransBean.setDealFinishTime(c.getTime());
		PageHelper.startPage(redpactTransBean.getPage(), redpactTransBean.getRows());
		List<RedPackInfo> redpactTransBeans=redPackMapper.selectByFormInfo(redpactTransBean);
		System.out.println(redpactTransBeans.size()+"**********");
		for(int i=0;i<redpactTransBeans.size();i++){
			redpactTransBeans.get(i).setMoneyName("人民币");
			
			if(redpactTransBeans.get(i).getStatus()!=null && redpactTransBeans.get(i).getStatus()==0 ){
				redpactTransBeans.get(i).setDealStatus((short) 25);
			}else if(redpactTransBeans.get(i).getStatus()!=null && redpactTransBeans.get(i).getStatus()==1){
				redpactTransBeans.get(i).setDealStatus((short) 26);
				
			}else if(redpactTransBeans.get(i).getStatus()!=null && redpactTransBeans.get(i).getStatus()==2){
				redpactTransBeans.get(i).setDealStatus((short) 27);
			}
			//System.out.println(redpactTransBeans.get(i).toString());
			
		}
		return (Page<RedPackInfo>) redpactTransBeans;
	}

	@Override
	public Page<RedPackInfo> selectByRedPackId(RedPackInfo redpactTransBean) {
		PageHelper.startPage(redpactTransBean.getPage(), redpactTransBean.getRows());
		List<RedPackInfo> redPackInfos=redPackMapper.selectByRedPackId(redpactTransBean);
		for(int i=0;i<redPackInfos.size();i++){
			if(redPackInfos.get(i).getAmt().compareTo(redPackInfos.get(i).getPlanAmt())==0){
				redPackInfos.get(i).setDealStatus((short) 28);
			}else{
				redPackInfos.get(i).setDealStatus((short) 29);
			}
			redPackInfos.get(i).setMoneyName("人民币");
			//System.out.println("******"+redPackInfos.get(i).toString());
		}
		return (Page<RedPackInfo>) redPackInfos;
	}

}
