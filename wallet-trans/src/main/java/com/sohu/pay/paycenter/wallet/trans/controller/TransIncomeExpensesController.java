package com.sohu.pay.paycenter.wallet.trans.controller;

import com.github.pagehelper.Page;
import com.sohu.pay.paycenter.auth.bean.OutSystemKey;
import com.sohu.pay.paycenter.auth.contexts.AuthSessionKey;
import com.sohu.pay.paycenter.common.excel.ExportExcel;
import com.sohu.pay.paycenter.common.page.JQGridPage;
import com.sohu.pay.paycenter.wallet.core.vo.IncomeExpensesInfo;
import com.sohu.pay.paycenter.wallet.trans.service.TransIncomeExpensesService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by wenjiexu on 2016/6/15.
 */
@Controller
@RequestMapping("/wallet/trans")
public class TransIncomeExpensesController {

    private static final Logger log = LogManager.getLogger(TransIncomeExpensesController.class);

    @Autowired
    private TransIncomeExpensesService transIncomeExpensesService;

    @RequestMapping("/getMerchantTotalDiff")
    @ResponseBody
    public BigDecimal getMerchantTotalDiff(Date startTime, Date endTime, Long merchantId) {
        if (merchantId == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalDiff = transIncomeExpensesService.selectMerchantIncomeAndExpensesTotalDiff(startTime, endTime, merchantId);
        return totalDiff != null ? totalDiff : BigDecimal.ZERO;
    }

    @RequestMapping("/getMerchantIncomeAndExpenses")
    @ResponseBody
    public JQGridPage<IncomeExpensesInfo> getMerchantIncomeAndExpenses(HttpSession session, Date startTime, Date endTime, Long merchantId, Integer page, Integer rows) {
        List<OutSystemKey> outSystems = (List<OutSystemKey>) session.getAttribute(AuthSessionKey.OUTSYS);
        List<IncomeExpensesInfo> infos = transIncomeExpensesService.selectMerchantIncomeAndExpenses(outSystems, startTime, endTime, merchantId, page, rows);
        return new JQGridPage((Page<IncomeExpensesInfo>)infos);
    }

    @RequestMapping("/exportMerchantIncomeAndExpenses")
    public void exportMerchantIncomeAndExpenses(HttpSession session, Date startTime, Date endTime, Long merchantId, HttpServletResponse response) {
        List<OutSystemKey> outSystems = (List<OutSystemKey>) session.getAttribute(AuthSessionKey.OUTSYS);
        List<IncomeExpensesInfo> infos = transIncomeExpensesService.selectMerchantIncomeAndExpenses(outSystems, startTime, endTime, merchantId, 0, 0);
        try {
            XSSFWorkbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("业务线收支情况");
            sheet.addMergedRegion(new CellRangeAddress(0,1,0,0));
            sheet.addMergedRegion(new CellRangeAddress(0,1,1,1));
            sheet.addMergedRegion(new CellRangeAddress(0,1,2,2));
            sheet.addMergedRegion(new CellRangeAddress(0,0,3,7));
            sheet.addMergedRegion(new CellRangeAddress(0,0,8,11));
            sheet.addMergedRegion(new CellRangeAddress(0,1,12,12));

            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

            Row head;
            Cell headc;
            head = sheet.createRow(0);

            headc = head.createCell(0, Cell.CELL_TYPE_STRING);
            headc.setCellValue("日期");
            headc.setCellStyle(cellStyle);

            headc = head.createCell(1, Cell.CELL_TYPE_STRING);
            headc.setCellValue("业务线");
            headc.setCellStyle(cellStyle);

            headc = head.createCell(2, Cell.CELL_TYPE_STRING);
            headc.setCellValue("业务线ID");
            headc.setCellStyle(cellStyle);

            headc = head.createCell(3, Cell.CELL_TYPE_STRING);
            headc.setCellValue("入款类交易");
            headc.setCellStyle(cellStyle);

            headc = head.createCell(8, Cell.CELL_TYPE_STRING);
            headc.setCellValue("出款类交易");
            headc.setCellStyle(cellStyle);

            headc = head.createCell(12, Cell.CELL_TYPE_STRING);
            headc.setCellValue("差异金额");
            headc.setCellStyle(cellStyle);

            head = sheet.createRow(1);

            headc = head.createCell(3, Cell.CELL_TYPE_STRING);
            headc.setCellValue("用户充值");
            headc.setCellStyle(cellStyle);

            headc = head.createCell(4, Cell.CELL_TYPE_STRING);
            headc.setCellValue("补贴");
            headc.setCellStyle(cellStyle);

            headc = head.createCell(5, Cell.CELL_TYPE_STRING);
            headc.setCellValue("非余额支付");
            headc.setCellStyle(cellStyle);

            headc = head.createCell(6, Cell.CELL_TYPE_STRING);
            headc.setCellValue("调账（+）");
            headc.setCellStyle(cellStyle);

            headc = head.createCell(7, Cell.CELL_TYPE_STRING);
            headc.setCellValue("合计");
            headc.setCellStyle(cellStyle);

            headc = head.createCell(8, Cell.CELL_TYPE_STRING);
            headc.setCellValue("用户提现");


            headc = head.createCell(9, Cell.CELL_TYPE_STRING);
            headc.setCellValue("原路退款");
            headc.setCellStyle(cellStyle);

            headc = head.createCell(10, Cell.CELL_TYPE_STRING);
            headc.setCellValue("调账（-）");
            headc.setCellStyle(cellStyle);

            headc = head.createCell(11, Cell.CELL_TYPE_STRING);
            headc.setCellValue("合计");
            headc.setCellStyle(cellStyle);

            int rownum = 2;
            for (IncomeExpensesInfo info : infos) {
                int cellnum = 0;
                Row row = sheet.createRow(rownum++);
                Cell cell;

                cell = row.createCell(cellnum++, Cell.CELL_TYPE_STRING);
                cell.setCellValue(info.getDate());

                cell = row.createCell(cellnum++, Cell.CELL_TYPE_STRING);
                cell.setCellValue(info.getMerchantName());

                cell =  row.createCell(cellnum++, Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(info.getMerchantId());


                cell =  row.createCell(cellnum++, Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(info.getCz().doubleValue());

                cell =  row.createCell(cellnum++, Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(info.getBt().doubleValue());

                cell =  row.createCell(cellnum++, Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(info.getZf().doubleValue());

                cell =  row.createCell(cellnum++, Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(info.getTz().doubleValue());

                cell =  row.createCell(cellnum++, Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(info.getIncomeTotal().doubleValue());

                cell =  row.createCell(cellnum++, Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(info.getTx().doubleValue());

                cell =  row.createCell(cellnum++, Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(info.getTk().doubleValue());

                cell =  row.createCell(cellnum++, Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(info.getTz2().doubleValue());

                cell =  row.createCell(cellnum++, Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(info.getExpensesTotal().doubleValue());

                cell =  row.createCell(cellnum++, Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(info.getCyTotal().doubleValue());

            }
            ExportExcel.exportExcelWithContent(response, wb, "业务线收支情况.xlsx");
            wb.close();
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }
    }
}
