package com.sohu.pay.paycenter.wallet.trans.controller;

import com.github.pagehelper.Page;
import com.sohu.pay.paycenter.auth.bean.OutSystem;
import com.sohu.pay.paycenter.auth.contexts.AuthSessionKey;
import com.sohu.pay.paycenter.common.page.JQGridPage;
import com.sohu.pay.paycenter.dic.bean.Dic;
import com.sohu.pay.paycenter.dic.service.DicService;
import com.sohu.pay.paycenter.thr.bean.ThrAccount;
import com.sohu.pay.paycenter.thr.helper.CommonHelper;
import com.sohu.pay.paycenter.thr.service.ThrAccountService;
import com.sohu.pay.paycenter.wallet.core.bean.TransInfo;
import com.sohu.pay.paycenter.wallet.trans.ThreadTask.GetCountForWalletTrans;
import com.sohu.pay.paycenter.wallet.trans.ThreadTask.GetCountForWalletTransWithBill;
import com.sohu.pay.paycenter.wallet.trans.ThreadTask.GetResultForWalletTrans;
import com.sohu.pay.paycenter.wallet.trans.ThreadTask.GetResultForWalletTransWithBill;
import com.sohu.pay.paycenter.wallet.trans.service.TransInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;


/**
 * created by hengsun 2016-04-18
 */
@Controller
@RequestMapping("/transInfo")
public class TransInfoController {

    private Logger logger = LogManager.getLogger(TransInfoController.class);
    @Autowired
    private TransInfoService transInfoService;

    @Autowired
    private DicService dicService;

    @Autowired
    private ThrAccountService thrAccountService;

    private Map<String, String> payChannelMapDic;//渠道字典值
    private Map<String, String> businessTypeMapDic; //业务类型
    private Map<String, String> dealStatusMapDic; //处理状态
    private Map<String, String> notifyStatusMapDic; //通知状态
    private Map<String, String> channelTypeMapDic; //支付方式
    private Map<Long, String> thrMerIdMapDic; //渠道商户号

    /**
     * 查询交易明细query表
     * transInfo 交易信息实体
     */
    @RequestMapping(value = "/transInfoList")
    @ResponseBody
    public JQGridPage<TransInfo> listThrRecord(HttpSession session, TransInfo transInfo) {
        //校验渠道商户订单号、订单ID、支付渠道、业务线流水ID、钱包流水号、用户名页面传""
        if (StringUtils.isBlank(transInfo.getPgTransId())) {
            transInfo.setPgTransId(null);
        }
        if (StringUtils.isBlank(transInfo.getOrderId())) {
            transInfo.setOrderId(null);
        }
        if (StringUtils.isBlank(transInfo.getPayChannel())) {
            transInfo.setPayChannel(null);
        }
        if (StringUtils.isBlank(transInfo.getPsTransId())) {
            transInfo.setPsTransId(null);
        }
        if (StringUtils.isBlank(transInfo.getTransId())) {
            transInfo.setTransId(null);
        }
        if (StringUtils.isBlank(transInfo.getUserName())) {
            transInfo.setUserName(null);
        }

        //数据权限
        List<OutSystem> outSystems = (List<OutSystem>) session.getAttribute(AuthSessionKey.OUTSYS);
        transInfo.setOutSystems(outSystems);
        //查询交易明细
        Page<TransInfo> page = transInfoService.listTransRecordByPage(transInfo);
        JQGridPage<TransInfo> jqgrid = new JQGridPage<TransInfo>();
        jqgrid.setTotal(page.getPages());
        jqgrid.setPage(page.getPageNum());
        jqgrid.setRecords(page.getTotal());
        jqgrid.setRows(page.getResult());
        return jqgrid;
    }


    /**
     * 导出查询交易明细 query
     * pageNum  页数
     * pageSize 每页条目数量
     */

    @RequestMapping(value = "/transInfoExport")
    public void exportTransRecord(HttpSession session, HttpServletRequest request, HttpServletResponse response, TransInfo transInfo) {
        try {
            //校验渠道商户订单号、订单ID、支付渠道、钱包流水号、用户名页面传""
            if (StringUtils.isBlank(transInfo.getPgTransId())) {
                transInfo.setPgTransId(null);
            }
            if (StringUtils.isBlank(transInfo.getOrderId())) {
                transInfo.setOrderId(null);
            }
            if (StringUtils.isBlank(transInfo.getPayChannel())) {
                transInfo.setPayChannel(null);
            }
            if (StringUtils.isBlank(transInfo.getPsTransId())) {
                transInfo.setPsTransId(null);
            }
            if (StringUtils.isBlank(transInfo.getTransId())) {
                transInfo.setTransId(null);
            }
            if (StringUtils.isBlank(transInfo.getUserName())) {
                transInfo.setUserName(null);
            }
            //更新结束时间+1
            getFinishTime(transInfo);//角色数据权限
            List<OutSystem> outSystems = (List<OutSystem>) session.getAttribute(AuthSessionKey.OUTSYS);
            transInfo.setOutSystems(outSystems);
            //查询交易明细
            List<TransInfo> jyList = transInfoService.selectByFormInfo(transInfo);

            queryDic();
            // 创建一个webbook，对应一个Excel文件
            SXSSFWorkbook wb = new SXSSFWorkbook();
            // 在webbook中添加一个sheet,对应Excel文件中的sheet
            Sheet sheet = wb.createSheet("交易明细表");
            // 在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            Row row = sheet.createRow(0);
            // 创建单元格，并设置值表头 设置表头居中
            CellStyle style = wb.createCellStyle();
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
            Cell cell = row.createCell(0);
            cell.setCellValue("序号");
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue("业务线");
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue("业务类型");
            cell.setCellStyle(style);

            cell = row.createCell(3);
            cell.setCellValue("业务线产品");
            cell.setCellStyle(style);

            cell = row.createCell(4);
            cell.setCellValue("用户名");
            cell.setCellStyle(style);

            cell = row.createCell(5);
            cell.setCellValue("业务订单号");
            cell.setCellStyle(style);

            cell = row.createCell(6);
            cell.setCellValue("钱包流水号");
            cell.setCellStyle(style);

            cell = row.createCell(7);
            cell.setCellValue("业务线流水ID");
            cell.setCellStyle(style);

            cell = row.createCell(8);
            cell.setCellValue("渠道名称");
            cell.setCellStyle(style);

            cell = row.createCell(9);
            cell.setCellValue("渠道商户号");
            cell.setCellStyle(style);

            cell = row.createCell(10);
            cell.setCellValue("支付方式");
            cell.setCellStyle(style);

            cell = row.createCell(11);
            cell.setCellValue("paygateID");
            cell.setCellStyle(style);

            cell = row.createCell(12);
            cell.setCellValue("paygate名称");
            cell.setCellStyle(style);

            cell = row.createCell(13);
            cell.setCellValue("渠道商户订单号");
            cell.setCellStyle(style);

            cell = row.createCell(14);
            cell.setCellValue("金额");
            cell.setCellStyle(style);

            cell = row.createCell(15);
            cell.setCellValue("手续费");
            cell.setCellStyle(style);

            cell = row.createCell(16);
            cell.setCellValue("币种");
            cell.setCellStyle(style);

            cell = row.createCell(17);
            cell.setCellValue("处理状态");
            cell.setCellStyle(style);

            cell = row.createCell(18);
            cell.setCellValue("通知状态");
            cell.setCellStyle(style);

            cell = row.createCell(19);
            cell.setCellValue("备注");
            cell.setCellStyle(style);

            cell = row.createCell(20);
            cell.setCellValue("提交时间");
            cell.setCellStyle(style);

            cell = row.createCell(21);
            cell.setCellValue("交易完成时间");
            cell.setCellStyle(style);


            for (int i = 0; i < jyList.size(); i++) {
                row = sheet.createRow(i + 1);
                TransInfo trans = jyList.get(i);
                // ，创建单元格，并设置值
                row.createCell(0).setCellValue(
                        i + 1);//序号
                row.createCell(1).setCellValue(
                        nullValueStr(trans.getMerchantName()));//业务线
                row.createCell(2).setCellValue(
                        getBusinessTypeDic(nullValueShort(trans.getType())));//业务类型
                row.createCell(3).setCellValue(
                        nullValueStr(trans.getChildMerchant()));//业务线产品
                row.createCell(4).setCellValue(
                        nullValueStr(trans.getUserName()));//用户名
                row.createCell(5).setCellValue(
                        nullValueStr(trans.getOrderId()));//业务订单号
                row.createCell(6).setCellValue(
                        nullValueStr(trans.getTransId()));//钱包流水号
                row.createCell(7).setCellValue(
                        nullValueStr(trans.getPsTransId()));//业务线流水ID
                row.createCell(8).setCellValue(
                        getPayChannelDic(trans.getPayChannel()));//渠道名称
                row.createCell(9).setCellValue(
                        getMerIdDic(trans.getThrId()));//渠道商户号
                row.createCell(10).setCellValue(
                        getChannelTypDic(nullValueShort(trans.getChannelType())));//支付方式
                row.createCell(11).setCellValue(
                        nullValueStr(trans.getPaygate().toString()));//paygateID
                row.createCell(12).setCellValue(
                        nullValueStr(trans.getAcntName()));//paygate名称
                row.createCell(13).setCellValue(
                        nullValueStr(trans.getPgTransId()));//渠道商户订单号
                row.createCell(14).setCellValue(
                        trans.getAmt().toString());//金额
                row.createCell(15).setCellValue(
                        nullValueDouble(trans.getPoundage()));//手续费
                row.createCell(16).setCellValue(
                        trans.getMoneyName());//币种
                row.createCell(17).setCellValue(
                        getDealStatusDic(nullValueShort(trans.getStatus())));//处理状态
                row.createCell(18).setCellValue(
                        getNotifyStatusDic(nullValueShort(trans.getNotityStatus())));//通知状态
                row.createCell(19).setCellValue(
                        nullValueStr(trans.getErrDesc()));//备注
                row.createCell(20).setCellValue(
                        CommonHelper.getTimeStrByDate(trans.getCreatedOn()));//提交时间
                if (trans.getDealFinishTime() != null) {
                    row.createCell(21).setCellValue(
                            CommonHelper.getTimeStrByDate(trans.getDealFinishTime()));//交易完成时间
                } else {
                    row.createCell(21).setCellValue("");//交易完成时间
                }

            }

            String fileName = "运营交易明细.xlsx";
            String fileNameTra = new String(fileName.getBytes("gbk"), "iso-8859-1");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileNameTra + "\"");
            OutputStream out = response.getOutputStream();
            wb.write(out);
            wb.close();
            out.close();
        } catch (Exception e) {
            logger.error("导出查询交易明细", e);
        }
    }

    /**
     * 查询交易明细 Bill
     *
     * @param transInfo 交易信息实体
     */
    @RequestMapping(value = "/transInfoListWithBill")
    @ResponseBody
    public JQGridPage<TransInfo> listThrRecordWithBill(HttpSession session, TransInfo transInfo) {
        //校验渠道商户订单号、订单ID、支付渠道、业务线流水ID、钱包流水号、用户名页面传""
        if (StringUtils.isBlank(transInfo.getPgTransId())) {
            transInfo.setPgTransId(null);
        }
        if (StringUtils.isBlank(transInfo.getOrderId())) {
            transInfo.setOrderId(null);
        }
        if (StringUtils.isBlank(transInfo.getPayChannel())) {
            transInfo.setPayChannel(null);
        }
        if (StringUtils.isBlank(transInfo.getPsTransId())) {
            transInfo.setPsTransId(null);
        }
        if (StringUtils.isBlank(transInfo.getTransId())) {
            transInfo.setTransId(null);
        }
        if (StringUtils.isBlank(transInfo.getUserName())) {
            transInfo.setUserName(null);
        }
        //更新结束时间+1
        getFinishTime(transInfo);
        //数据权限
        List<OutSystem> outSystems = (List<OutSystem>) session.getAttribute(AuthSessionKey.OUTSYS);
        transInfo.setOutSystems(outSystems);
        //查询交易明细
        Page<TransInfo> page = transInfoService.listTransRecordByPageWithBill(transInfo);
        JQGridPage<TransInfo> jqgrid = new JQGridPage<>();
        jqgrid.setTotal(page.getPages());
        jqgrid.setPage(page.getPageNum());
        jqgrid.setRecords(page.getTotal());
        jqgrid.setRows(page.getResult());
        return jqgrid;
    }

    /**
     * 导出查询交易明细 bill
     * pageNum  页数
     * pageSize 每页条目数量
     */

    @RequestMapping(value = "/transInfoExportWithBill")
    public void exportTransRecordWithBill(HttpSession session, HttpServletRequest request, HttpServletResponse response, TransInfo transInfo) {
        try {
            //校验渠道商户订单号、订单ID、支付渠道、钱包流水号、用户名页面传""
            if (StringUtils.isBlank(transInfo.getPgTransId())) {
                transInfo.setPgTransId(null);
            }
            if (StringUtils.isBlank(transInfo.getOrderId())) {
                transInfo.setOrderId(null);
            }
            if (StringUtils.isBlank(transInfo.getPayChannel())) {
                transInfo.setPayChannel(null);
            }
            if (StringUtils.isBlank(transInfo.getPsTransId())) {
                transInfo.setPsTransId(null);
            }
            if (StringUtils.isBlank(transInfo.getTransId())) {
                transInfo.setTransId(null);
            }
            if (StringUtils.isBlank(transInfo.getUserName())) {
                transInfo.setUserName(null);
            }
            //更新结束时间+1
            getFinishTime(transInfo);
            //角色数据权限
            List<OutSystem> outSystems = (List<OutSystem>) session.getAttribute(AuthSessionKey.OUTSYS);
            transInfo.setOutSystems(outSystems);
            //查询交易明细
            List<TransInfo> jyList = transInfoService.selectByFormInfoWithBill(transInfo);

            queryDic();
            // 创建一个webbook，对应一个Excel文件
            SXSSFWorkbook wb = new SXSSFWorkbook();
            // webbook中添加一个sheet,对应Excel文件中的sheet
            Sheet sheet = wb.createSheet("财务交易明细表");
            // 在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            Row row = sheet.createRow(0);
            // 创建单元格，并设置值表头 设置表头居中
            CellStyle style = wb.createCellStyle();
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
            Cell cell = row.createCell(0);
            cell.setCellValue("序号");
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue("业务线");
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue("业务类型");
            cell.setCellStyle(style);

            cell = row.createCell(3);
            cell.setCellValue("业务线产品");
            cell.setCellStyle(style);

            cell = row.createCell(4);
            cell.setCellValue("用户名");
            cell.setCellStyle(style);

            cell = row.createCell(5);
            cell.setCellValue("业务订单号");
            cell.setCellStyle(style);

            cell = row.createCell(6);
            cell.setCellValue("钱包流水号");
            cell.setCellStyle(style);

            cell = row.createCell(7);
            cell.setCellValue("业务线流水ID");
            cell.setCellStyle(style);

            cell = row.createCell(8);
            cell.setCellValue("渠道名称");
            cell.setCellStyle(style);

            cell = row.createCell(9);
            cell.setCellValue("渠道商户号");
            cell.setCellStyle(style);

            cell = row.createCell(10);
            cell.setCellValue("支付方式");
            cell.setCellStyle(style);

            cell = row.createCell(11);
            cell.setCellValue("paygateID");
            cell.setCellStyle(style);

            cell = row.createCell(12);
            cell.setCellValue("paygate名称");
            cell.setCellStyle(style);

            cell = row.createCell(13);
            cell.setCellValue("渠道商户订单号");
            cell.setCellStyle(style);

            cell = row.createCell(14);
            cell.setCellValue("金额");
            cell.setCellStyle(style);

            cell = row.createCell(15);
            cell.setCellValue("手续费");
            cell.setCellStyle(style);

            cell = row.createCell(16);
            cell.setCellValue("币种");
            cell.setCellStyle(style);

            cell = row.createCell(17);
            cell.setCellValue("处理状态");
            cell.setCellStyle(style);

            cell = row.createCell(18);
            cell.setCellValue("通知状态");
            cell.setCellStyle(style);

            cell = row.createCell(19);
            cell.setCellValue("备注");
            cell.setCellStyle(style);

            cell = row.createCell(20);
            cell.setCellValue("提交时间");
            cell.setCellStyle(style);

            cell = row.createCell(21);
            cell.setCellValue("交易完成时间");
            cell.setCellStyle(style);

            for (int i = 0; i < jyList.size(); i++) {
                row = sheet.createRow(i + 1);
                TransInfo trans = jyList.get(i);
                // ，创建单元格，并设置值
                row.createCell(0).setCellValue(
                        i + 1);//序号
                row.createCell(1).setCellValue(
                        nullValueStr(trans.getMerchantName()));//业务线
                row.createCell(2).setCellValue(
                        getBusinessTypeDic(nullValueShort(trans.getType())));//业务类型
                row.createCell(3).setCellValue(
                        nullValueStr(trans.getChildMerchant()));//业务线产品
                row.createCell(4).setCellValue(
                        nullValueStr(trans.getUserName()));//用户名
                row.createCell(5).setCellValue(
                        nullValueStr(trans.getOrderId()));//业务订单号
                row.createCell(6).setCellValue(
                        nullValueStr(trans.getTransId()));//钱包流水号
                row.createCell(7).setCellValue(
                        nullValueStr(trans.getPsTransId()));//业务线流水ID
                row.createCell(8).setCellValue(
                        getPayChannelDic(trans.getPayChannel()));//渠道名称
                row.createCell(9).setCellValue(
                        getMerIdDic(trans.getThrId()));//渠道商户号
                row.createCell(10).setCellValue(
                        getChannelTypDic(nullValueShort(trans.getChannelType())));//支付方式
                row.createCell(11).setCellValue(
                        nullValueStr(trans.getPaygate().toString()));//paygateID
                row.createCell(12).setCellValue(
                        nullValueStr(trans.getAcntName()));//paygate名称
                row.createCell(13).setCellValue(
                        nullValueStr(trans.getPgTransId()));//渠道商户订单号
                row.createCell(14).setCellValue(
                        trans.getAmt().toString());//金额
                row.createCell(15).setCellValue(
                        nullValueDouble(trans.getPoundage()));//手续费
                row.createCell(16).setCellValue(
                        trans.getMoneyName());//币种
                row.createCell(17).setCellValue(
                        getDealStatusDic(nullValueShort(trans.getStatus())));//处理状态
                row.createCell(18).setCellValue(
                        getNotifyStatusDic(nullValueShort(trans.getNotityStatus())));//通知状态
                row.createCell(19).setCellValue(
                        nullValueStr(trans.getErrDesc()));//备注
                row.createCell(20).setCellValue(
                        CommonHelper.getTimeStrByDate(trans.getCreatedOn()));//提交时间
                if (trans.getDealFinishTime() != null) {
                    row.createCell(21).setCellValue(
                            CommonHelper.getTimeStrByDate(trans.getDealFinishTime()));//交易完成时间
                } else {
                    row.createCell(21).setCellValue("");//交易完成时间
                }
            }

            String fileName = "财务交易明细.xlsx";
            String fileNameTra = new String(fileName.getBytes("gbk"), "iso-8859-1");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileNameTra + "\"");
            OutputStream out = response.getOutputStream();
            wb.write(out);
            wb.close();
            out.close();
        } catch (Exception e) {
            logger.error("导出财务查询交易明细", e);
        }
    }

    /**
     * 查询钱包交易明细 query
     *
     * @param transInfo 交易信息实体
     */
    @RequestMapping(value = "/walletTransInfoList")
    @ResponseBody
    public JQGridPage<TransInfo> listTWalletTransRecord(HttpSession session, TransInfo transInfo) {
        JQGridPage<TransInfo> jqgrid = new JQGridPage<>();
        try {
            if (StringUtils.isBlank(transInfo.getOrderId())) {
                transInfo.setOrderId(null);
            }
            if (StringUtils.isBlank(transInfo.getPgTransId())) {
                transInfo.setPgTransId(null);
            }
            if (StringUtils.isBlank(transInfo.getUserName())) {
                transInfo.setUserName(null);
            }
            if (StringUtils.isBlank(transInfo.getTransId())) {
                transInfo.setTransId(null);
            }
            //更新结束时间+1
            getFinishTime(transInfo);
            //角色数据权限
            List<OutSystem> outSystems = (List<OutSystem>) session.getAttribute(AuthSessionKey.OUTSYS);
            transInfo.setOutSystems(outSystems);

            if ("1".equals(transInfo.getFlag())) {
                GetCountForWalletTrans getCount = new GetCountForWalletTrans(transInfo, transInfoService);
                GetResultForWalletTrans getResult = new GetResultForWalletTrans(transInfo, transInfoService);
                FutureTask<Integer> future = new FutureTask<>(getCount);
                Thread t = new Thread(future);
                t.start();
                FutureTask<List<TransInfo>> listFuture = new FutureTask<>(getResult);
                Thread thread = new Thread(listFuture);
                thread.start();
                int count = 0;
                while (!future.isDone()) {
                    count = future.get();
                }
                List<TransInfo> list = null;
                while (!listFuture.isDone()) {
                    list = listFuture.get();
                }
                jqgrid.setTotal(count % transInfo.getRows() == 0 ? count % transInfo.getRows() : (count / transInfo.getRows() + 1));
                jqgrid.setPage(transInfo.getPage());
                jqgrid.setRecords(count);
                jqgrid.setRows(list);
            } else {
                GetResultForWalletTrans getResult = new GetResultForWalletTrans(transInfo, transInfoService);
                FutureTask<List<TransInfo>> listFuture = new FutureTask<>(getResult);
                Thread t = new Thread(listFuture);
                t.start();
                List<TransInfo> list = null;
                while (!listFuture.isDone()) {
                    list = listFuture.get();
                }
                jqgrid.setPage(transInfo.getPage());
                jqgrid.setRows(list);
            }
        } catch (Exception e) {
            logger.error("获取线程执行结果错误: " + e);
        }
        return jqgrid;
    }


    /**
     * 导出查询钱包交易明细 query
     *
     * @param request 请求
     */
    @RequestMapping(value = "/walletTransInfoExport")
    @ResponseBody
    public void exportWalletTransRecord(HttpSession session, HttpServletRequest request, HttpServletResponse response, TransInfo transInfo) {
        try {

            if (StringUtils.isBlank(transInfo.getOrderId())) {
                transInfo.setOrderId(null);
            }
            if (StringUtils.isBlank(transInfo.getPgTransId())) {
                transInfo.setPgTransId(null);
            }
            if (StringUtils.isBlank(transInfo.getUserName())) {
                transInfo.setUserName(null);
            }
            if (StringUtils.isBlank(transInfo.getTransId())) {
                transInfo.setTransId(null);
            }
            //更新结束时间+1
            getFinishTime(transInfo);
            //角色数据权限
            List<OutSystem> outSystems = (List<OutSystem>) session.getAttribute(AuthSessionKey.OUTSYS);
            transInfo.setOutSystems(outSystems);
            //查询交易明细
            List<TransInfo> jyList = transInfoService.selectByWalletFormInfo(transInfo);

            queryDic();
            // 创建一个webbook，对应一个Excel文件
            SXSSFWorkbook wb = new SXSSFWorkbook();
            // 在webbook中添加一个sheet,对应Excel文件中的sheet
            Sheet sheet = wb.createSheet("交易明细表");
            // 在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            Row row = sheet.createRow(0);
            // 创建单元格，并设置值表头 设置表头居中
            CellStyle style = wb.createCellStyle();
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
            Cell cell = row.createCell(0);
            cell.setCellValue("序号");
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue("业务线");
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue("业务类型");
            cell.setCellStyle(style);

            cell = row.createCell(3);
            cell.setCellValue("业务线产品");
            cell.setCellStyle(style);

            cell = row.createCell(4);
            cell.setCellValue("用户名");
            cell.setCellStyle(style);

            cell = row.createCell(5);
            cell.setCellValue("业务订单号");
            cell.setCellStyle(style);

            cell = row.createCell(6);
            cell.setCellValue("业务线流水ID");
            cell.setCellStyle(style);

            cell = row.createCell(7);
            cell.setCellValue("钱包流水号");
            cell.setCellStyle(style);

            cell = row.createCell(8);
            cell.setCellValue("渠道商户订单号");
            cell.setCellStyle(style);

            cell = row.createCell(9);
            cell.setCellValue("交易金额");
            cell.setCellStyle(style);

            cell = row.createCell(10);
            cell.setCellValue("币种");
            cell.setCellStyle(style);

            cell = row.createCell(11);
            cell.setCellValue("处理状态");
            cell.setCellStyle(style);

            cell = row.createCell(12);
            cell.setCellValue("备注");
            cell.setCellStyle(style);

            cell = row.createCell(13);
            cell.setCellValue("提交时间");
            cell.setCellStyle(style);

            cell = row.createCell(14);
            cell.setCellValue("交易完成时间");
            cell.setCellStyle(style);

            for (int i = 0; i < jyList.size(); i++) {
                row = sheet.createRow(i + 1);
                TransInfo trans = jyList.get(i);
                // ，创建单元格，并设置值
                row.createCell(0).setCellValue(
                        i + 1);//序号
                row.createCell(1).setCellValue(
                        nullValueStr(trans.getMerchantName()));//业务线
                row.createCell(2).setCellValue(
                        getBusinessTypeDic(nullValueShort(trans.getType())));//业务类型
                row.createCell(3).setCellValue(
                        nullValueStr(trans.getChildMerchant()));//业务线产品
                row.createCell(4).setCellValue(
                        nullValueStr(trans.getUserName()));//用户名
                row.createCell(5).setCellValue(
                        nullValueStr(trans.getOrderId()));//业务订单号
                row.createCell(6).setCellValue(
                        nullValueStr(trans.getPsTransId()));//业务线流水ID
                row.createCell(7).setCellValue(
                        nullValueStr(trans.getTransId()));//钱包流水号
                row.createCell(8).setCellValue(
                        nullValueStr(trans.getPgTransId()));//渠道商户订单号
                row.createCell(9).setCellValue(
                        nullValueDouble(trans.getChangedAmt()));//交易金额
                row.createCell(10).setCellValue(
                        trans.getMoneyName());//币种
                row.createCell(11).setCellValue(
                        getDealStatusDic(nullValueShort(trans.getStatus())));//处理状态
                row.createCell(12).setCellValue(
                        nullValueStr(trans.getErrDesc()));//备注
                row.createCell(13).setCellValue(
                        CommonHelper.getTimeStrByDate(trans.getCreatedOn()));//提交时间
                if (trans.getDealFinishTime() != null) {
                    row.createCell(14).setCellValue(
                            CommonHelper.getTimeStrByDate(trans.getDealFinishTime()));//交易完成时间
                } else {
                    row.createCell(14).setCellValue("");//交易完成时间
                }
            }

            String fileName = "运营钱包交易明细.xlsx";
            String fileNameTra = new String(fileName.getBytes("gbk"), "iso-8859-1");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileNameTra + "\"");
            OutputStream out = response.getOutputStream();
            wb.write(out);
            wb.close();
            out.close();
        } catch (Exception e) {
            logger.error("导出查询钱包交易明细", e);
        }
    }

    /**
     * 查询钱包交易明细BILL
     *
     * @param transInfo 交易信息实体
     */
    @RequestMapping(value = "/walletTransInfoListWithBill")
    @ResponseBody
    public JQGridPage<TransInfo> listTWalletTransRecordWithBill(HttpSession session, TransInfo transInfo) {
        JQGridPage<TransInfo> jqgrid = new JQGridPage<>();
        try {
            if (StringUtils.isBlank(transInfo.getOrderId())) {
                transInfo.setOrderId(null);
            }
            if (StringUtils.isBlank(transInfo.getPgTransId())) {
                transInfo.setPgTransId(null);
            }
            if (StringUtils.isBlank(transInfo.getUserName())) {
                transInfo.setUserName(null);
            }
            if (StringUtils.isBlank(transInfo.getTransId())) {
                transInfo.setTransId(null);
            }
            //更新结束时间+1
            getFinishTime(transInfo);
            //角色数据权限
            List<OutSystem> outSystems = (List<OutSystem>) session.getAttribute(AuthSessionKey.OUTSYS);
            transInfo.setOutSystems(outSystems);
            //查询交易明细
//        List<TransInfo> page = transInfoService.listWalletTransWithBill(transInfo);

            if ("1".equals(transInfo.getFlag())) {
                GetCountForWalletTransWithBill getCount = new GetCountForWalletTransWithBill(transInfo, transInfoService);
                GetResultForWalletTransWithBill getResult = new GetResultForWalletTransWithBill(transInfo, transInfoService);
                FutureTask<Integer> future = new FutureTask<>(getCount);
                Thread t = new Thread(future);
                t.start();
                FutureTask<List<TransInfo>> listFuture = new FutureTask<>(getResult);
                Thread thread = new Thread(listFuture);
                thread.start();
                int count = 0;
                while (!future.isDone()) {
                    count = future.get();
                }
                List<TransInfo> list = null;
                while (!listFuture.isDone()) {
                    list = listFuture.get();
                }
                jqgrid.setTotal(count % transInfo.getRows() == 0 ? count % transInfo.getRows() : (count / transInfo.getRows() + 1));
                jqgrid.setPage(transInfo.getPage());
                jqgrid.setRecords(count);
                jqgrid.setRows(list);
            } else {
                GetResultForWalletTransWithBill getResult = new GetResultForWalletTransWithBill(transInfo, transInfoService);
                FutureTask<List<TransInfo>> listFuture = new FutureTask<>(getResult);
                Thread t = new Thread(listFuture);
                t.start();
                List<TransInfo> list = null;
                while (!listFuture.isDone()) {
                    list = listFuture.get();
                }
                jqgrid.setPage(transInfo.getPage());
                jqgrid.setRows(list);
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
        return jqgrid;
    }

    /**
     * 导出查询钱包交易明细 bill
     *
     * @param request 请求
     */
    @RequestMapping(value = "/walletTransInfoExportWithBill")
    @ResponseBody
    public void exportWalletTransRecordWithBill(HttpSession session, HttpServletRequest request, HttpServletResponse response, TransInfo transInfo) {
        try {

            if (StringUtils.isBlank(transInfo.getOrderId())) {
                transInfo.setOrderId(null);
            }
            if (StringUtils.isBlank(transInfo.getPgTransId())) {
                transInfo.setPgTransId(null);
            }
            if (StringUtils.isBlank(transInfo.getUserName())) {
                transInfo.setUserName(null);
            }
            if (StringUtils.isBlank(transInfo.getTransId())) {
                transInfo.setTransId(null);
            }
            //更新结束时间+1
            getFinishTime(transInfo);

            //角色数据权限
            List<OutSystem> outSystems = (List<OutSystem>) session.getAttribute(AuthSessionKey.OUTSYS);
            transInfo.setOutSystems(outSystems);
            //查询交易明细
            List<TransInfo> jyList = transInfoService.selectByWalletFormInfoWithBill(transInfo);

            queryDic();
            // 创建一个webbook，对应一个Excel文件
            SXSSFWorkbook wb = new SXSSFWorkbook();
            // 在webbook中添加一个sheet,对应Excel文件中的sheet
            Sheet sheet = wb.createSheet("财务交易明细表");
            // 在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            Row row = sheet.createRow(0);
            // 创建单元格，并设置值表头 设置表头居中
            CellStyle style = wb.createCellStyle();
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
            Cell cell = row.createCell(0);
            cell.setCellValue("序号");
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue("业务线");
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue("业务类型");
            cell.setCellStyle(style);

            cell = row.createCell(3);
            cell.setCellValue("业务线产品");
            cell.setCellStyle(style);

            cell = row.createCell(4);
            cell.setCellValue("用户名");
            cell.setCellStyle(style);

            cell = row.createCell(5);
            cell.setCellValue("业务订单号");
            cell.setCellStyle(style);

            cell = row.createCell(6);
            cell.setCellValue("业务线流水ID");
            cell.setCellStyle(style);

            cell = row.createCell(7);
            cell.setCellValue("钱包流水号");
            cell.setCellStyle(style);

            cell = row.createCell(8);
            cell.setCellValue("渠道商户订单号");
            cell.setCellStyle(style);

            cell = row.createCell(9);
            cell.setCellValue("交易金额");
            cell.setCellStyle(style);

            cell = row.createCell(10);
            cell.setCellValue("币种");
            cell.setCellStyle(style);

            cell = row.createCell(11);
            cell.setCellValue("处理状态");
            cell.setCellStyle(style);

            cell = row.createCell(12);
            cell.setCellValue("备注");
            cell.setCellStyle(style);

            cell = row.createCell(13);
            cell.setCellValue("提交时间");
            cell.setCellStyle(style);

            cell = row.createCell(14);
            cell.setCellValue("交易完成时间");
            cell.setCellStyle(style);

            for (int i = 0; i < jyList.size(); i++) {
                row = sheet.createRow(i + 1);
                TransInfo trans = jyList.get(i);
                // ，创建单元格，并设置值
                row.createCell(0).setCellValue(
                        i + 1);//序号
                row.createCell(1).setCellValue(
                        nullValueStr(trans.getMerchantName()));//业务线
                row.createCell(2).setCellValue(
                        getBusinessTypeDic(nullValueShort(trans.getType())));//业务类型
                row.createCell(3).setCellValue(
                        nullValueStr(trans.getChildMerchant()));//业务线产品
                row.createCell(4).setCellValue(
                        nullValueStr(trans.getUserName()));//用户名
                row.createCell(5).setCellValue(
                        nullValueStr(trans.getOrderId()));//业务订单号
                row.createCell(6).setCellValue(
                        nullValueStr(trans.getPsTransId()));//业务线流水ID
                row.createCell(7).setCellValue(
                        nullValueStr(trans.getTransId()));//钱包流水号
                row.createCell(8).setCellValue(
                        nullValueStr(trans.getPgTransId()));//渠道商户订单号
                row.createCell(9).setCellValue(
                        nullValueDouble(trans.getChangedAmt()));//交易金额
                row.createCell(10).setCellValue(
                        trans.getMoneyName());//币种
                row.createCell(11).setCellValue(
                        getDealStatusDic(nullValueShort(trans.getStatus())));//处理状态
                row.createCell(12).setCellValue(
                        nullValueStr(trans.getErrDesc()));//备注
                row.createCell(13).setCellValue(
                        CommonHelper.getTimeStrByDate(trans.getCreatedOn()));//提交时间
                if (trans.getDealFinishTime() != null) {
                    row.createCell(14).setCellValue(
                            CommonHelper.getTimeStrByDate(trans.getDealFinishTime()));//交易完成时间
                } else {
                    row.createCell(14).setCellValue("");//交易完成时间
                }
            }

            String fileName = "财务钱包交易明细.xlsx";
            String fileNameTra = new String(fileName.getBytes("gbk"), "iso-8859-1");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileNameTra + "\"");
            OutputStream out = response.getOutputStream();
            wb.write(out);
            wb.close();
            out.close();
        } catch (Exception e) {
            logger.error("导出查询钱包交易明细", e);
        }
    }

    public String nullValueStr(String value) {
        if (value == null)
            return "";
        return value;
    }

    public String nullValueShort(Short value) {
        if (value == null)
            return "";
        return value.toString();
    }

    public String nullValueDouble(BigDecimal bigDecimal) {
        if (bigDecimal == null)
            return "";
        return bigDecimal.toString();
    }

    //获取渠道的字典值
    private String getPayChannelDic(String payChannelKey) {
        if (payChannelKey == null) {
            return "";
        }
        return payChannelMapDic.get(payChannelKey);
    }


    //获取业务类型的字典值
    private String getBusinessTypeDic(String businessType) {
        if (businessType == null) {
            return "";
        }
        return businessTypeMapDic.get(businessType);
    }

    //获取处理状态的字典值
    private String getDealStatusDic(String dealStatus) {
        if (dealStatus == null) {
            return "";
        }
        return dealStatusMapDic.get(dealStatus);
    }

    //获取通知状态的字典值
    private String getNotifyStatusDic(String notifyStatus) {
        if (notifyStatus == null) {
            return "";
        }
        return notifyStatusMapDic.get(notifyStatus);
    }


    //获取支付方式的字典值
    private String getChannelTypDic(String channelType) {
        if (channelType == null) {
            return "";
        }
        return channelTypeMapDic.get(channelType);
    }

    //获取渠道商户号
    private String getMerIdDic(Long thrId) {
        if (thrId == null) {
            return "";
        }
        return thrMerIdMapDic.get(thrId);
    }

    //获取所需的字典值
    private void queryDic() {
        payChannelMapDic = new HashMap<String, String>();//支付渠道
        businessTypeMapDic = new HashMap<String, String>();//业务类型
        notifyStatusMapDic = new HashMap<String, String>();//通知状态
        dealStatusMapDic = new HashMap<String, String>();//处理状态
        channelTypeMapDic = new HashMap<String, String>();//支付方式
        thrMerIdMapDic = new HashMap<Long, String>();//渠道商户号

        Dic dic = new Dic();
        List<Dic> dicList = null;
        //渠道字典值
        dic.setGroup("zfqd");
        dicList = dicService.getDic(dic);
        if (dicList != null) {
            for (Dic d : dicList) {
                payChannelMapDic.put(d.getKey(), d.getValue());
            }
        }

        //业务类型
        dic.setGroup("ywlx");
        dicList = dicService.getDic(dic);
        if (dicList != null) {
            for (Dic d : dicList) {
                businessTypeMapDic.put(d.getKey(), d.getValue());
            }
        }

        //通知状态
        dic.setGroup("tzzt");
        dicList = dicService.getDic(dic);
        if (dicList != null) {
            for (Dic d : dicList) {
                notifyStatusMapDic.put(d.getKey(), d.getValue());
            }
        }


        //处理状态
        dic.setGroup("clzt");
        dicList = dicService.getDic(dic);
        if (dicList != null) {
            for (Dic d : dicList) {
                dealStatusMapDic.put(d.getKey(), d.getValue());
            }
        }


        //支付方式
        dic.setGroup("zffs");
        dicList = dicService.getDic(dic);
        if (dicList != null) {
            for (Dic d : dicList) {
                channelTypeMapDic.put(d.getKey(), d.getValue());
            }
        }

        //渠道商户号
        List<ThrAccount> thrList = thrAccountService.selectAll();
        if (thrList != null) {
            for (ThrAccount t : thrList) {
                thrMerIdMapDic.put(t.getThrId(), t.getThrChannelMerId());
            }
        }

    }

    private void getFinishTime(TransInfo transInfo) {
        //更新结束时间+1
        if (transInfo.getFinishEndTime() != null) {
            Calendar cld = Calendar.getInstance();
            cld.setTime(transInfo.getFinishEndTime());
            cld.add(Calendar.DATE, 1);
            transInfo.setFinishEndTime(cld.getTime());
        }
    }
}

