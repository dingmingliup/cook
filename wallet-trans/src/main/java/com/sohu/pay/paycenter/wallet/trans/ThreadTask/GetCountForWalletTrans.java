package com.sohu.pay.paycenter.wallet.trans.ThreadTask;

import com.sohu.pay.paycenter.wallet.core.bean.TransInfo;
import com.sohu.pay.paycenter.wallet.trans.service.TransInfoService;

import java.util.concurrent.Callable;

/**
 * Created by J on 2017/5/4
 */
public class GetCountForWalletTrans implements Callable<Integer> {
    private final TransInfoService transInfoService;

    private TransInfo transInfo;

    public GetCountForWalletTrans(TransInfo transInfo, TransInfoService transInfoService) {
        this.transInfo = transInfo;
        this.transInfoService = transInfoService;
    }

    @Override
    public Integer call() throws Exception {
        return transInfoService.CountForWalletTrans(transInfo);
    }
}
