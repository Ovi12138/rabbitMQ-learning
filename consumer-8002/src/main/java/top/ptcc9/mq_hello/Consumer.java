package top.ptcc9.mq_hello;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

/**
 * @Author HE LONG CAN
 * @Description hello 消费者
 * @Date 2021-09-23 15:32:02
 */
public class Consumer {
    private static final Log log = LogFactory.get(Consumer.class);


    @RabbitListener(queues = "hello")
    public void receive(String message) {
        log.info("consumer-8002 just received a message({}) from queue({})",message,"hello");
    }
}
