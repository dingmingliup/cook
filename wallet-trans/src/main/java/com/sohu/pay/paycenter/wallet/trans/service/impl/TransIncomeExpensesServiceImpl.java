package com.sohu.pay.paycenter.wallet.trans.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sohu.pay.paycenter.auth.bean.OutSystemKey;
import com.sohu.pay.paycenter.wallet.core.dao.MerAccoChangedDetailMapper;
import com.sohu.pay.paycenter.wallet.core.vo.IncomeExpensesInfo;
import com.sohu.pay.paycenter.wallet.trans.service.TransIncomeExpensesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wenjiexu on 2016/6/15.
 */
@Service
public class TransIncomeExpensesServiceImpl implements TransIncomeExpensesService {

    @Autowired
    private MerAccoChangedDetailMapper merAccoChangedDetailMapper;

    @Override
    public List<IncomeExpensesInfo> selectMerchantIncomeAndExpenses(List<OutSystemKey> outSystemKeys, Date startTime, Date endTime, Long merchantId, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        return merAccoChangedDetailMapper.selectMerIncomeExpenses(outSystemKeys, startTime, endTime, merchantId);
    }

    @Override
    public BigDecimal selectMerchantIncomeAndExpensesTotalDiff(Date startTime, Date endTime, Long merchantId) {
        return merAccoChangedDetailMapper.selectMerIncomeExpensesTotalDiff(startTime, endTime, merchantId);
    }
}
