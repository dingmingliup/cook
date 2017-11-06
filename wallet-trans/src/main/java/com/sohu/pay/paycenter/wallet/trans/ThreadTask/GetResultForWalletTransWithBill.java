package com.sohu.pay.paycenter.wallet.trans.ThreadTask;

import com.sohu.pay.paycenter.wallet.core.bean.TransInfo;
import com.sohu.pay.paycenter.wallet.trans.service.TransInfoService;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by J on 2017/5/4
 */
public class GetResultForWalletTransWithBill implements Callable<List<TransInfo>> {
    private final TransInfoService transInfoService;

    private TransInfo transInfo;

    public GetResultForWalletTransWithBill(TransInfo transInfo, TransInfoService transInfoService) {
        this.transInfo = transInfo;
        this.transInfoService = transInfoService;
    }
    @Override
    public List<TransInfo> call() throws Exception {
        return transInfoService.listWalletTransWithBill(transInfo);
    }
}
