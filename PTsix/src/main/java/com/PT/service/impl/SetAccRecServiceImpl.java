package com.PT.service.impl;

import com.PT.dao.DriverMapper;
import com.PT.dao.SetAccRecInfoMapper;
import com.PT.dao.SettleAccRecordMapper;
import com.PT.entity.Driver;
import com.PT.entity.DriverExample;
import com.PT.entity.SettleAccRecord;
import com.PT.entity.SettleAccRecordExample;
import com.PT.service.LogService;
import com.PT.service.SetAccRecService;
import com.PT.tools.ToStrings;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * created by yxhuang
 * 结算管理具体业务逻辑实现
 */
@Service
public class SetAccRecServiceImpl implements SetAccRecService{

    @Autowired
    SetAccRecInfoMapper setAccRecInfoMapper;
    @Autowired
    SettleAccRecordMapper settleAccRecordMapper;
    @Autowired
    LogService logService;
    @Autowired
    DriverMapper driverMapper;

    @Override
    public Map<String, Object> listSetAccRec(int page, int ipp, int userId, String query) {
        Map<String, Object> map = new HashMap<>();
        PageHelper.startPage(page, ipp);
        Map factors = new HashMap<String, Object>();
        factors.put("str", query);
        factors.put("userId", userId);
        List list = setAccRecInfoMapper.selectByFactors(factors);
        int maxPage = (setAccRecInfoMapper.countByFactors(factors)-1)/ipp+1;
        map.put("maxPage", maxPage);
        map.put("records", list);
        return map;
    }

    @Transactional
    @Override
    public Boolean deleteSetAccRec(List<String> setAccIds, int userId) {
        SettleAccRecordExample example = new SettleAccRecordExample();
        for(int i = 0; i < setAccIds.size(); i++)
            System.out.println(setAccIds.get(i));
        example.createCriteria().andStatusEqualTo(2).andSetAccIdIn(setAccIds);
        try {
            if(settleAccRecordMapper.deleteByExample(example) > 0) {
                String desc = ToStrings.listToStrings(setAccIds, '&');
                logService.insertLog(userId, "delete", "on table ykat_settle_account_records: "
                +"by ids in ["+desc+"]");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public Boolean updateSetAccState(List<String> setAccIds, int state, int userId) {

        if(state == 1) {
            SettleAccRecordExample example = new SettleAccRecordExample();
            example.createCriteria().andStatusEqualTo(0).andSetAccIdIn(setAccIds);
            SettleAccRecord record = new SettleAccRecord();
            record.setStatus(state);
            if(settleAccRecordMapper.updateByExampleSelective(record, example) > 0) {
                String desc = ToStrings.listToStrings(setAccIds, '&');
                logService.insertLog(userId, "update", "on table ykat_settle_account_records: "
                        +"by ids in ["+desc+"] "+" and status = 0"+". set status to "+state);
            }
            else return false;
        } else {
            return settleAccount(setAccIds, userId);
        }
        return true;
    }

    /**
     * 结算操作，需要扣除司机佣金
     * @param setAccIds
     * @return
     */
    @Transactional
    public Boolean settleAccount(List<String> setAccIds, int userId) {
        int count = setAccRecInfoMapper.updateDriver(setAccIds);
        count += setAccRecInfoMapper.updateSetAccRec(setAccIds);
        DriverExample example = new DriverExample();
        example.createCriteria().andStatusEqualTo(1);
        Driver driver = new Driver();
        driver.setStatus(0);
        driverMapper.updateByExampleSelective(driver, example);
        if(count <= 0) return false;
        String desc = ToStrings.listToStrings(setAccIds, '&');
                logService.insertLog(userId, "update", "on table ykat_settle_account_records: "
                +"by ids in ["+desc+"] "+" and status = 1"+". set status to "+2);
        return true;
    }

}
