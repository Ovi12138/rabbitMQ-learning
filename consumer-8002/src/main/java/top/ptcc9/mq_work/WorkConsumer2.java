package top.ptcc9.mq_work;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.util.concurrent.TimeUnit;

/**
 * @Author HE LONG CAN
 * @Description TODO
 * @Date 2021-09-23 17:08:45
 */
public class WorkConsumer2 {
    private static final Log log = LogFactory.get(WorkConsumer2.class);

    @RabbitListener(queues = "work")
    public void receive(String message) {
        log.info("consumer-2 just received a message({}) from queue({})",message,"work");
        /*
        休眠5秒后抛出异常
         */
        try {
            TimeUnit.SECONDS.sleep(5);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException();
//        //检查字符串中"."，每次检查到阻塞一秒
//        for (int i = 0; i < message.length(); i++) {
//            if (message.charAt(i) == '.') {
//                try {
//                    TimeUnit.SECONDS.sleep(1);
//                }catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        log.info("consumer-2 just finished message({})",message);
    }
}
