package com.PT.service.impl;

import com.PT.dao.ReceiveRecordMapper;
import com.PT.dao.SettleAccRecordMapper;
import com.PT.entity.SettleAccRecord;
import com.PT.entity.SettleAccRecordExample;
import com.PT.service.ReceiveRecordService;
import com.PT.tools.QueryToMap;
import com.PT.tools.YkatCommonUtil;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReceiveRecordDServiceImpl implements ReceiveRecordService{

    @Autowired
    private ReceiveRecordMapper receiveRecordMapper;

    /**
     * 查询收账记录
     * @param userId 唯一用户请求
     * @param page 页码
     * @param ipp 没有数据量
     * @param queryCondition 查询条件
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> listReceiveRecord(int userId, int page, int ipp, String queryCondition) throws Exception{

        SettleAccRecordExample settleAccRecordExample = new SettleAccRecordExample();
        settleAccRecordExample.createCriteria().andCreatedAtIsNotNull();

        Map factors = new HashMap();
        if(queryCondition!=null && !"".equals(queryCondition)) { //有搜索条件时
            factors = QueryToMap.stringToMap(queryCondition);
            if (factors.containsKey("time")) {
                YkatCommonUtil.putFromAndToDate(factors, (String) factors.get("time"));
            }
        }

        factors.put("userId", userId);

        PageHelper.startPage(page,ipp);
        List<Map<String, Object> > receiveRecords = receiveRecordMapper.selectByFactors(factors);
        int maxPage = ( receiveRecordMapper.countByFactors(factors) -1)/ipp+1;

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("records",receiveRecords);
        map.put("maxPage",maxPage);

        return map;
    }

    /**
     * 删除收账记录
     * @param userId
     * @param ids
     * @throws Exception
     */
    @Transactional
    @Override
    public void deleteReceiveRecord(int userId, List<Integer> ids) throws Exception{


    }


}
