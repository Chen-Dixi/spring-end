package com.PT.service.impl;

import com.PT.dao.DepositInfoMapper;
import com.PT.dao.DepositRechargeRecordMapper;
import com.PT.dao.StoreMapper;
import com.PT.dao.YkatCommonUtilMapper;
import com.PT.entity.DepositRechargeRecord;
import com.PT.entity.DepositRechargeRecordExample;
import com.PT.entity.Store;
import com.PT.service.DepositService;
import com.PT.service.LogService;
import com.PT.tools.QueryToMap;
import com.PT.tools.ToStrings;
import com.PT.tools.YkatCommonUtil;
import com.PT.tools.YkatConstant;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepositServiceImpl implements DepositService {


    @Autowired
    private DepositRechargeRecordMapper depositRechargeRecordMapper;

    @Autowired
    private DepositInfoMapper depositInfoMapper;

    @Autowired
    private LogService logService;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private YkatCommonUtilMapper ykatCommonUtilMapper;

    /**
     * 查询保证金补足记录
     * @param userId 唯一用户标识
     * @param page
     * @param ipp
     * @param queryCondition
     * @return
     * @throws Exception
     */
    public Map<String,Object> listDepositRecord(int userId, int page, int ipp, String queryCondition) throws Exception
    {
        Map factors = new HashMap();
        if(queryCondition!=null && !"".equals(queryCondition)) { //有搜索条件时
            factors = QueryToMap.stringToMap(queryCondition);
            if (factors.containsKey("time")) {
                YkatCommonUtil.putFromAndToDate(factors, (String) factors.get("time"));
            }
        }

        factors.put("userId",userId);

        PageHelper.startPage(page,ipp);
        List<Map<String, Object> > records = depositInfoMapper.selectByFactor(factors);
        int maxPage = (depositInfoMapper.countByFactor(factors)-1)/ipp+1;

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("records",records);
        resultMap.put("maxPage",maxPage);
        Integer currentDeposit = ykatCommonUtilMapper.getCurrentDepositByUserId(userId);
        resultMap.put("currentDeposit",currentDeposit);
        return resultMap;
    }

    /**
     * 删除保证金补足记录
     * @param userId 唯一用户表示
     * @param ids 一组保证金 id号
     * @throws Exception
     */
    @Transactional
    @Override
    public void deleteDepositRecord(int userId, List<Integer> ids) throws Exception
    {

        if ( ids==null || ids.size() == 0){
            throw new Exception("缺少参数");
        }

        DepositRechargeRecordExample example = new DepositRechargeRecordExample();
        example.createCriteria().andIdIn(ids);
        String descp = ToStrings.integerListToStrings(ids,'&');
        DepositRechargeRecord deleteTo = new DepositRechargeRecord();
        deleteTo.setStatus(YkatConstant.DELETE_DEPOSIT_STATE);
        int deleteResult = depositRechargeRecordMapper.updateByExampleSelective(deleteTo,example);

        logService.insertLog(userId,"delete","on table ykat_deposit_recharge_records " +
                "by ids in ["+descp+"]");
    }

    /**
     * 添加保证金补足记录
     * @param userId
     * @param parameterMap
     * @throws Exception
     */
    @Transactional
    @Override
    public void addDepositRecord(int userId, Map<String, Object> parameterMap) throws Exception{

        String checkMessage = YkatCommonUtil.checkMapHasNull(parameterMap);
        if(!"success".equals(checkMessage)){
            throw new Exception(checkMessage);
        }

        Integer rechargeMoney = (Integer) parameterMap.get("money");

        if(!(rechargeMoney> 0 && rechargeMoney <= 100000)){
            throw new Exception("金额不能超过100000");
        }

        String bankcardId = (String) parameterMap.get("cardId");
        if ("".equals(bankcardId)){
            throw new Exception("缺少银行卡号信息");
        }


        Integer bankcardPrimaryKey = ykatCommonUtilMapper.getBankcardPrimaryKeyByCardId(bankcardId);//获得主键


        if(bankcardId == null){
            throw new Exception("不存在的银行卡");
        }
        Integer currentMoney = ykatCommonUtilMapper.getCurrentDepositByUserId(userId);//当前商店的保证金
        Integer storeId = ykatCommonUtilMapper.getStoreIdByUserId(userId);//商店的id主键
        currentMoney += rechargeMoney;

        DepositRechargeRecord rechargeRecord = new DepositRechargeRecord();
        rechargeRecord.setCreatedAt(new Date());
        rechargeRecord.setRechargeMoney(rechargeMoney);
        rechargeRecord.setCurrentMoney(currentMoney);
        rechargeRecord.setRechargeTime(new Date());
        rechargeRecord.setStoreId(storeId);
        rechargeRecord.setBankcardId(bankcardPrimaryKey);
        Integer status = currentMoney >= YkatConstant.ENOUGH_DEPOSIT ? YkatConstant.ENOUGH_DEPOSIT_STATE:YkatConstant.SHORT_OF_DEPOSIT_STATE;
        rechargeRecord.setStatus(status);
        Store store = new Store();
        store.setId(storeId);
        store.setDeposit(currentMoney);
        if(depositRechargeRecordMapper.insertSelective(rechargeRecord)>0 && storeMapper.updateByPrimaryKeySelective(store)>0){
            logService.insertLog(userId,"insert","recharge deposit of store "+storeId+" "+rechargeMoney+" ¥");
        }else{
            throw new RuntimeException("添加保证金失败");
        }
    }
}
