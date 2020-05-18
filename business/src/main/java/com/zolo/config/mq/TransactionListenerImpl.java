package com.zolo.config.mq;

import com.zolo.entity.CommonDto;
import com.zolo.service.BusinessService;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class TransactionListenerImpl implements TransactionListener {
    /*@Autowired
    private StorageFeignClient storageFeignClient;
    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;
*/
    @Autowired
    BusinessService businessService;

    /**
     *
     * 执行本地事务
     * @param msg
     * @param arg
     * @return
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        log.info("business-transactionListenerImpl全局事务Id{}",RootContext.getXID());
        try {
            Map<String, CommonDto> parameterMap = (Map<String,CommonDto>) arg;
            System.out.println("发送消息到broker成功,执行本地事务");
            //启动购买逻辑
            System.out.println("执行本地事务");
            businessService.purchase(parameterMap.get("dto"));
        }catch (Exception e){
            //执行本地事务失败回滚消息，保证发送消息的事务一致性
            log.error("MQ执行本地事务失败:{}",e);
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
        //执行本地事务成功，提交消息，允许消息被消费
        System.out.println("执行本地事务成功，提交消息，允许消息被消费");
        return LocalTransactionState.COMMIT_MESSAGE;
    }


    /**
     *
     * 回查方法，确认broker上面的消息是否要提交还是回滚
     * @param msg
     * @return
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
