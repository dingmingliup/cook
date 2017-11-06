package com.sohu.pay.paycenter.wallet.trans.service;

import com.sohu.pay.paycenter.auth.bean.OutSystemKey;
import com.sohu.pay.paycenter.wallet.core.vo.IncomeExpensesInfo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by wenjiexu on 2016/6/15.
 */
public interface TransIncomeExpensesService {
    List<IncomeExpensesInfo> selectMerchantIncomeAndExpenses(List<OutSystemKey> outSystemKeys, Date startTime, Date endTime, Long merchantId, Integer page, Integer rows);

    BigDecimal selectMerchantIncomeAndExpensesTotalDiff(Date startTime, Date endTime, Long merchantId);
}
