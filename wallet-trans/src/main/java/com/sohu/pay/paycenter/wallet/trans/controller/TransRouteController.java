package com.sohu.pay.paycenter.wallet.trans.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TransRouteController {

    //运营交易明细查询
	@RequestMapping("wallet/trans/transInfoListPage.htm")
	public String transInfoList() {
		return "wallet/trans/transactionDetail";
	}
	//钱包运营明细查询
	@RequestMapping("wallet/trans/walletTransInfoListPage.htm")
	public String walletTransInfoList() {
		return "wallet/trans/walletDetail";
	}
    //财务交易明细查询
	@RequestMapping("wallet/trans/transactionDetailForFinance.htm")
	public String transInfoListWithBill() {
		return "wallet/trans/transactionDetailForFinance";
	}
    //钱包财务明细查询
	@RequestMapping("wallet/trans/walletTransInfoListPageForFinance.htm")
	public String walletTransInfoListWithBill() {
		return "wallet/trans/walletDetailForFinance";
	}

	//业务线收支查询
	@RequestMapping("/wallet/trans/merchantIncomeAndCost.htm")
	public String merchantIncomeAndCost() {
		return "wallet/trans/transIncomeAndCost";
	}
    //红包交易明细
	@RequestMapping("/wallet/trans/redpacketDetail.htm")
	public String redPackTransInfo(){
		return "/wallet/trans/redpacketDetail";
	}
}
