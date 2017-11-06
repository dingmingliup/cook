package com.sohu.pay.paycenter.wallet.trans.service;

import com.github.pagehelper.Page;
import com.sohu.pay.paycenter.wallet.core.bean.TransInfo;

import java.util.List;

public interface TransInfoService {
	List<TransInfo> selectByFormInfo(TransInfo transInfo);

	List<TransInfo> selectByFormInfoWithBill(TransInfo transInfo);

	Page<TransInfo> listTransRecordByPage(TransInfo transInfo);
	
	List<TransInfo> selectByWalletFormInfo(TransInfo transInfo);

	List<TransInfo> selectByWalletFormInfoWithBill(TransInfo transInfo);
	
	List<TransInfo> listWalletTrans(TransInfo transInfo);

    List<TransInfo> listWalletTransWithBill(TransInfo transInfo);

	Page<TransInfo> listTransRecordByPageWithBill(TransInfo transInfo);

	Integer CountForWalletTrans(TransInfo transInfo);

	Integer CountForWalletTransWithBill(TransInfo transInfo);



}
