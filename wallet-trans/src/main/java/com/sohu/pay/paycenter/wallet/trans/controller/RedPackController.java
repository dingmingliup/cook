package com.sohu.pay.paycenter.wallet.trans.controller;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

import com.github.pagehelper.Page;
import com.sohu.pay.paycenter.auth.bean.OutSystem;
import com.sohu.pay.paycenter.auth.contexts.AuthSessionKey;
import com.sohu.pay.paycenter.common.page.JQGridPage;
import com.sohu.pay.paycenter.thr.helper.CommonHelper;
import com.sohu.pay.paycenter.wallet.core.bean.RedPackInfo;
import com.sohu.pay.paycenter.wallet.core.bean.TransInfo;
import com.sohu.pay.paycenter.wallet.trans.service.RedPackService;
import com.sohu.pay.paycenter.wallet.trans.service.TransInfoService;

@Controller
@RequestMapping("/redPack")
public class RedPackController {
	
	private Logger logger = LogManager.getLogger(RedPackController.class);
	@Autowired
    private RedPackService redPackService;
	
	/**
     * 查询发红包交易明细
     * redpactTransBean 交易信息实体
     */
	@RequestMapping(value= "/redPackList")
    @ResponseBody
    public JQGridPage<RedPackInfo> listThrRecord(HttpSession session, RedPackInfo redpactTransBean) {
		System.out.println(redpactTransBean.toString());
        //校验渠道商户订单号、订单ID、支付渠道、业务线流水ID、钱包流水号、用户名页面传""
        if (StringUtils.isBlank(redpactTransBean.getId())) {
        	redpactTransBean.setId(null);
        }
        if (StringUtils.isBlank(redpactTransBean.getTransId())) {
        	redpactTransBean.setTransId(null);
        }
        if (StringUtils.isBlank(redpactTransBean.getUserName())) {
        	redpactTransBean.setUserName(null);
        }

        //数据权限
        List<OutSystem> outSystems = (List<OutSystem>) session.getAttribute(AuthSessionKey.OUTSYS);
        redpactTransBean.setOutSystems(outSystems);
        //查询交易明细
        Page<RedPackInfo> page = (Page<RedPackInfo>) redPackService.selectByFormInfo(redpactTransBean);
        JQGridPage<RedPackInfo> jqgrid = new JQGridPage<RedPackInfo>();
        jqgrid.setTotal(page.getPages());
        jqgrid.setPage(page.getPageNum());
        jqgrid.setRecords(page.getTotal());
        jqgrid.setRows(page.getResult());
        return jqgrid;
    }
	/**
	 * 查询领红包明细
	 */
	@RequestMapping(value = "/redPackReceiveList")
    @ResponseBody
    public JQGridPage<RedPackInfo> redPackReceiveList(HttpSession session, RedPackInfo redpactTransBean){
		//校验渠道商户订单号、订单ID、支付渠道、业务线流水ID、钱包流水号、用户名页面传""
        if (StringUtils.isBlank(redpactTransBean.getId())) {
        	redpactTransBean.setId(null);
        }
        if (StringUtils.isBlank(redpactTransBean.getTransId())) {
        	redpactTransBean.setTransId(null);
        }
        if (StringUtils.isBlank(redpactTransBean.getUserName())) {
        	redpactTransBean.setUserName(null);
        }

        //数据权限
        List<OutSystem> outSystems = (List<OutSystem>) session.getAttribute(AuthSessionKey.OUTSYS);
        redpactTransBean.setOutSystems(outSystems);
        Page<RedPackInfo> page = (Page<RedPackInfo>) redPackService.selectByRedPackId(redpactTransBean);
        logger.info("字表查询的数量"+redPackService.selectByRedPackId(redpactTransBean).size());
        JQGridPage<RedPackInfo> jqgrid = new JQGridPage<RedPackInfo>();
        jqgrid.setTotal(page.getPages());
        jqgrid.setPage(page.getPageNum());
        jqgrid.setRecords(page.getTotal());
        jqgrid.setRows(page.getResult());
        return jqgrid;
	}
	/**
	 * 导出红包交易明细表
	 * 
	 */
	@RequestMapping(value = "/redPackListExport")
    @ResponseBody
	public void exportRedPackList(HttpSession session, HttpServletRequest request, HttpServletResponse response,RedPackInfo redpactTransBean){
		try{
		System.out.println("开始导出表");
		//校验渠道商户订单号、订单ID、支付渠道、业务线流水ID、钱包流水号、用户名页面传""
		if (StringUtils.isBlank(redpactTransBean.getId())) {
        	redpactTransBean.setId(null);
        }
        if (StringUtils.isBlank(redpactTransBean.getTransId())) {
        	redpactTransBean.setTransId(null);
        }
        if (StringUtils.isBlank(redpactTransBean.getUserName())) {
        	redpactTransBean.setUserName(null);
        }
      //更新结束时间+1
       getFinishTime(redpactTransBean);

        //角色数据权限
        List<OutSystem> outSystems = (List<OutSystem>) session.getAttribute(AuthSessionKey.OUTSYS);
        redpactTransBean.setOutSystems(outSystems);
        //查询发红包交易明细
        List<RedPackInfo> redPackInfos=redPackService.selectByFormInfo(redpactTransBean);
        for(int s=0;s<redPackInfos.size();s++){
        	System.out.println(redPackInfos.get(s).toString());
        }
        System.out.println("redPackInfos的长度="+redPackInfos.size());
     // 创建一个webbook，对应一个Excel文件
        SXSSFWorkbook wb = new SXSSFWorkbook();
        // 在webbook中添加一个sheet,对应Excel文件中的sheet
        Sheet sheet = wb.createSheet("发红包交易明细表");
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
        cell.setCellValue("业务线产品");
        cell.setCellStyle(style);

        cell = row.createCell(3);
        cell.setCellValue("用户名");
        cell.setCellStyle(style);


        cell = row.createCell(4);
        cell.setCellValue("钱包流水号");
        cell.setCellStyle(style);

        cell = row.createCell(5);
        cell.setCellValue("红包编号");
        cell.setCellStyle(style);
        
        cell = row.createCell(6);
        cell.setCellValue("业务流水ID");
        cell.setCellStyle(style);
        
        cell = row.createCell(7);
        cell.setCellValue("金额");
        cell.setCellStyle(style);
        
        cell = row.createCell(8);
        cell.setCellValue("退款金额");
        cell.setCellStyle(style);
        
        cell = row.createCell(9);
        cell.setCellValue("红包剩余金额");
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
        
        //int n=1;
        for(int i=0;i<redPackInfos.size();i++){
        	row = sheet.createRow(i+1);
        	RedPackInfo  redPackInfo=redPackInfos.get(i);
        	 //查询领红包交易明细
        	 // ，创建单元格，并设置值
            row.createCell(0).setCellValue(
                    i+1);//序号
            row.createCell(1).setCellValue(
                    nullValueStr(redPackInfo.getMerchantName()));//业务线
            row.createCell(2).setCellValue(
            		nullValueStr(redPackInfo.getProductName()));//业务线产品
            row.createCell(3).setCellValue(
            		nullValueStr(redPackInfo.getUserName()));//用户名
            row.createCell(4).setCellValue(
            		nullValueStr(redPackInfo.getTransId()));//钱包流水号
            row.createCell(5).setCellValue(
            		nullValueStr(redPackInfo.getId()));//红包编号
            row.createCell(7).setCellValue(
            		nullValueDouble(redPackInfo.getAmt()));//金额
            row.createCell(8).setCellValue(
            		nullValueDouble(redPackInfo.getRefundAmt()));//退款金额
            row.createCell(9).setCellValue(
            		nullValueDouble(redPackInfo.getRemainderAmt()));//红包剩余金额
            row.createCell(10).setCellValue(
            		nullValueStr("人民币"));//币种
            if(redPackInfo.getDealStatus()==25){
            row.createCell(11).setCellValue(
                		nullValueStr("可领取"));//处理状态
            }else if(redPackInfo.getDealStatus()==26){
            row.createCell(11).setCellValue(
                		nullValueStr("领取完成"));//处理状态	
            }else if(redPackInfo.getDealStatus()==27){
            row.createCell(11).setCellValue(
                		nullValueStr("已退款"));//处理状态	
            }
            
            row.createCell(12).setCellValue(
            		nullValueStr(redPackInfo.getErrDesc()));//备注
            row.createCell(13).setCellValue(
            		CommonHelper.getTimeStrByDate(redPackInfo.getCreatedOn()));//提交时间
            row.createCell(14).setCellValue(
            		CommonHelper.getTimeStrByDate(redPackInfo.getUpdatedOn()));//交易完成时间
            
        }
        Sheet sheets = wb.createSheet("领红包交易明细表");
        Row rows=sheets.createRow(0);
        CellStyle styles = wb.createCellStyle();
        styles.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
        Cell cells = rows.createCell(0);
        cells.setCellValue("序号");
        cells.setCellStyle(styles);

        cells = rows.createCell(1);
        cells.setCellValue("业务线");
        cells.setCellStyle(styles);


        cells = rows.createCell(2);
        cells.setCellValue("业务线产品");
        cells.setCellStyle(styles);

        cells = rows.createCell(3);
        cells.setCellValue("用户名");
        cells.setCellStyle(styles);
        
        cells = rows.createCell(4);
        cells.setCellValue("红包编号");
        cells.setCellStyle(styles);


        cells = rows.createCell(5);
        cells.setCellValue("钱包流水号");
        cells.setCellStyle(styles);

        cells = rows.createCell(6);
        cells.setCellValue("业务线流水ID");
        cells.setCellStyle(styles);
        
        cells = rows.createCell(7);
        cells.setCellValue("金额");
        cells.setCellStyle(styles);
        
        cells = rows.createCell(8);
        cells.setCellValue("计划领取金额");
        cells.setCellStyle(styles);
        

        cells = rows.createCell(9);
        cells.setCellValue("币种");
        cells.setCellStyle(styles);

        cells = rows.createCell(10);
        cells.setCellValue("处理状态");
        cells.setCellStyle(styles);


        cells = rows.createCell(11);
        cells.setCellValue("备注");
        cells.setCellStyle(styles);

        cells = rows.createCell(12);
        cells.setCellValue("提交时间");
        cells.setCellStyle(styles);
        
        cells = rows.createCell(13);
        cells.setCellValue("交易完成时间");
        cells.setCellStyle(styles);
        int n=1;
        for(int j=0;j<redPackInfos.size();j++){
        	RedPackInfo  r=new RedPackInfo();
            r.setId(redPackInfos.get(j).getId());
            List<RedPackInfo> recePackInfos=redPackService.selectByRedPackId(r);
           
            if(recePackInfos.size()>0){
            for(int z=0;z<recePackInfos.size();z++){
            	RedPackInfo  recePackInfo=recePackInfos.get(z);
            	rows = sheets.createRow(n+z);
            	 // ，创建单元格，并设置值
            	rows.createCell(0).setCellValue(
            			n+z);//序号
            	rows.createCell(1).setCellValue(
                        nullValueStr(recePackInfo.getMerchantName()));//业务线
            	rows.createCell(2).setCellValue(
                		nullValueStr(recePackInfo.getProductName()));//业务线产品
            	rows.createCell(3).setCellValue(
                        nullValueStr(recePackInfo.getUserName()));//用户名
            	rows.createCell(4).setCellValue(
                        nullValueStr(recePackInfo.getId()));//红包编号
            	rows.createCell(5).setCellValue(
                		nullValueStr(recePackInfo.getTransId()));//钱包流水号
            	rows.createCell(6).setCellValue(
                		nullValueStr(recePackInfo.getrPstransId()));//业务线流水ID
            	rows.createCell(7).setCellValue(
                		nullValueDouble(recePackInfo.getAmt()));//金额
            	rows.createCell(8).setCellValue(
                		nullValueDouble(recePackInfo.getPlanAmt()));//计划领取金额
            	rows.createCell(9).setCellValue(
                		nullValueStr("人民币"));//币种
                if(recePackInfo.getAmt().compareTo(recePackInfo.getPlanAmt())==0){
                	rows.createCell(10).setCellValue(
                     		nullValueStr("领取成功"));//处理状态
    			}else{
    				rows.createCell(10).setCellValue(
                     		nullValueStr("领取失败"));//处理状态
    			}
             
                rows.createCell(11).setCellValue(
                		nullValueStr(recePackInfo.getErrDesc()));//备注
                rows.createCell(12).setCellValue(
                		CommonHelper.getTimeStrByDate(recePackInfo.getCreatedOn()));//提交时间
                rows.createCell(13).setCellValue(
                		CommonHelper.getTimeStrByDate(recePackInfo.getUpdatedOn()));//交易完成时间
            }
           
            }
            n=recePackInfos.size()+n;
        }
        String fileName = "红包交易明细.xlsx";
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
	private void getFinishTime(RedPackInfo redpactTransBean) {
        //更新结束时间+1
        if (redpactTransBean.getEndTime() != null) {
            Calendar cld = Calendar.getInstance();
            cld.setTime(redpactTransBean.getEndTime());
            cld.add(Calendar.DATE, 1);
            redpactTransBean.setEndTime(cld.getTime());
        }
    }
	public String nullValueStr(String value) {
        if (value == null)
            return "";
        return value;
    }
	public String nullValueDouble(BigDecimal bigDecimal) {
        if (bigDecimal == null)
            return "";
        return bigDecimal.toString();
    }
	public String nullValueLong(Long l) {
        if (l == null)
            return "";
        return l.toString();
    }
	public String nullValueShort(Short value) {
        if (value == null)
            return "";
        return value.toString();
    }
}
