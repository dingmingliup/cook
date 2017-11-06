package com.sohu.pay.paycenter.wallet.trans.service;

import java.util.List;

import com.sohu.pay.paycenter.wallet.core.bean.RedPackInfo;
import com.sohu.pay.paycenter.wallet.core.bean.TransInfo;

public interface RedPackService {
	List<RedPackInfo> selectByFormInfo(RedPackInfo redpactTransBean);
	
	List<RedPackInfo> selectByRedPackId(RedPackInfo redpactTransBean);
}
