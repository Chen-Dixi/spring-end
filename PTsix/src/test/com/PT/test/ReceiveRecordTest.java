package com.PT.test;

import com.PT.service.ReceiveRecordService;
import com.PT.tools.OutputMessage;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class ReceiveRecordTest extends BaseTest{

    @Autowired
    private ReceiveRecordService receiveRecordService;

    @Test
    public void testList(){
        try {

            Map<String, Object>  res = receiveRecordService.listReceiveRecord(1,1,10,"content:201803+time:1521820800000-1521993600000");
            List<Map<String,Object> > records =(List<Map<String, Object> >) res.get("records");
            OutputMessage.outputListOfMap(records);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    

}
