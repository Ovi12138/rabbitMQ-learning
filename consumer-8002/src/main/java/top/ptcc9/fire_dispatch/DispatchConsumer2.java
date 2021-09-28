package top.ptcc9.fire_dispatch;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.IOException;

/**
 * @Author HE LONG CAN
 * @Description 用于测试任务调度
 * @Date 2021-09-26 09:39:37
 */

public class DispatchConsumer2 {
    private static final Log log = LogFactory.get(DispatchConsumer2.class);

    @RabbitListener(queues = "dispatch_test",ackMode = "MANUAL")
    public void receive(Message receive, Channel channel) {
        String message = new String(receive.getBody());
        long deliveryTag = receive.getMessageProperties().getDeliveryTag();
        //channel.basicAck(deliveryTag,false);
        log.info("consumer-2 just receive a message({}) from queue({})",message,"dispatch_test");
    }
}