package com.sohu.pay.paycenter.wallet.trans.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sohu.pay.paycenter.wallet.core.bean.TransInfo;
import com.sohu.pay.paycenter.wallet.core.dao.TransInfoMapper;
import com.sohu.pay.paycenter.wallet.trans.service.TransInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Service
@Transactional
public class TransInfoServiceImpl implements TransInfoService {


    @Autowired
    private TransInfoMapper transInfoMapper;

    @Override
    public List<TransInfo> selectByFormInfo(TransInfo transInfo) {
        SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
        Long start = Long.parseLong(format.format(transInfo.getStartTime()));
        Long end = Long.parseLong(format.format(transInfo.getEndTime()));
        transInfo.setStartPartition(start);
        transInfo.setEndPartition(end);
        return transInfoMapper.selectByFormInfo(transInfo);
    }

    @Override
    public List<TransInfo> selectByWalletFormInfo(TransInfo transInfo) {
        SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
        Long start = Long.parseLong(format.format(transInfo.getStartTime()));
        Long end = Long.parseLong(format.format(transInfo.getEndTime()));
        transInfo.setStartPartition(start);
        transInfo.setEndPartition(end);
        return transInfoMapper.selectWalletTransByFormInfo(transInfo);
    }

    @Override
    public Page<TransInfo> listTransRecordByPage(TransInfo transInfo) {
        SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
        Long start = Long.parseLong(format.format(transInfo.getStartTime()));
        Long end = Long.parseLong(format.format(transInfo.getEndTime()));
        transInfo.setStartPartition(start);
        transInfo.setEndPartition(end);
        PageHelper.startPage(transInfo.getPage(), transInfo.getRows());
        List<TransInfo> transInfoList = transInfoMapper.selectByFormInfo(transInfo);
        return (Page<TransInfo>) transInfoList;
    }

    @Override
    public List<TransInfo> listWalletTrans(TransInfo transInfo) {
        SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
        Calendar c = Calendar.getInstance();
        c.setTime(transInfo.getStartTime());
        c.add(Calendar.MONTH, -1);
        Long start = Long.parseLong(format.format(c.getTime()));

        c.setTime(transInfo.getEndTime());
        c.add(Calendar.MONTH, 1);
        Long end = Long.parseLong(format.format(c.getTime()));

        transInfo.setStartPartition(start);
        transInfo.setEndPartition(end);
        int i = transInfo.getRows() * (transInfo.getPage() - 1);
        transInfo.setStartNum(i);
        int j = transInfo.getRows() * transInfo.getPage();
        transInfo.setEndNum(j);
        return transInfoMapper.resultWalletTransByFormInfo(transInfo);
    }

    @Override
    public List<TransInfo> listWalletTransWithBill(TransInfo transInfo) {
        SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
        Calendar c = Calendar.getInstance();
        c.setTime(transInfo.getFinishBeginTime());
        c.add(Calendar.MONTH, -1);
        Long start = Long.parseLong(format.format(c.getTime()));

        c.setTime(transInfo.getFinishEndTime());
        c.add(Calendar.MONTH, 1);
        Long end = Long.parseLong(format.format(c.getTime()));

        transInfo.setStartPartition(start);
        transInfo.setEndPartition(end);
        int i = transInfo.getRows() * (transInfo.getPage() - 1);
        transInfo.setStartNum(i);
        int j = transInfo.getRows() * transInfo.getPage();
        transInfo.setEndNum(j);
        return transInfoMapper.resultWalletTransByFormInfoWithBill(transInfo);
    }

    @Override
    public List<TransInfo> selectByWalletFormInfoWithBill(TransInfo transInfo) {
        SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
        Calendar c = Calendar.getInstance();
        c.setTime(transInfo.getFinishBeginTime());
        c.add(Calendar.MONTH, -1);
        Long start = Long.parseLong(format.format(c.getTime()));

        c.setTime(transInfo.getFinishEndTime());
        c.add(Calendar.MONTH, 1);
        Long end = Long.parseLong(format.format(c.getTime()));

        transInfo.setStartPartition(start);
        transInfo.setEndPartition(end);
        return transInfoMapper.selectWalletTransByFormInfoWithBill(transInfo);
    }

    @Override
    public Page<TransInfo> listTransRecordByPageWithBill(TransInfo transInfo) {
        SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
        Calendar c = Calendar.getInstance();
        c.setTime(transInfo.getFinishBeginTime());
        c.add(Calendar.MONTH, -1);
        Long start = Long.parseLong(format.format(c.getTime()));

        c.setTime(transInfo.getFinishEndTime());
        c.add(Calendar.MONTH, 1);
        Long end = Long.parseLong(format.format(c.getTime()));

        transInfo.setStartPartition(start);
        transInfo.setEndPartition(end);
        PageHelper.startPage(transInfo.getPage(), transInfo.getRows());
        List<TransInfo> transInfoList = transInfoMapper.selectByFormInfoByBill(transInfo);
        return (Page<TransInfo>) transInfoList;
    }

    @Override
    public Integer CountForWalletTrans(TransInfo transInfo) {
        SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
        Calendar c = Calendar.getInstance();
        c.setTime(transInfo.getStartTime());
        c.add(Calendar.MONTH, -1);
        Long start = Long.parseLong(format.format(c.getTime()));

        c.setTime(transInfo.getEndTime());
        c.add(Calendar.MONTH, 1);
        Long end = Long.parseLong(format.format(c.getTime()));

        transInfo.setStartPartition(start);
        transInfo.setEndPartition(end);
        return transInfoMapper.countWalletTrans(transInfo);
    }

    @Override
    public Integer CountForWalletTransWithBill(TransInfo transInfo) {
        SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
        Calendar c = Calendar.getInstance();
        c.setTime(transInfo.getFinishBeginTime());
        c.add(Calendar.MONTH, -1);
        Long start = Long.parseLong(format.format(c.getTime()));

        c.setTime(transInfo.getFinishEndTime());
        c.add(Calendar.MONTH, 1);
        Long end = Long.parseLong(format.format(c.getTime()));

        transInfo.setStartPartition(start);
        transInfo.setEndPartition(end);
        return transInfoMapper.countWalletTransWithBill(transInfo);
    }

    @Override
    public List<TransInfo> selectByFormInfoWithBill(TransInfo transInfo) {
        SimpleDateFormat format = new SimpleDateFormat("YYYYMM");
        Calendar c = Calendar.getInstance();
        c.setTime(transInfo.getFinishBeginTime());
        c.add(Calendar.MONTH, -1);
        Long start = Long.parseLong(format.format(c.getTime()));

        c.setTime(transInfo.getFinishEndTime());
        c.add(Calendar.MONTH, 1);
        Long end = Long.parseLong(format.format(c.getTime()));

        transInfo.setStartPartition(start);
        transInfo.setEndPartition(end);
        return transInfoMapper.selectByFormInfoByBill(transInfo);
    }
}
