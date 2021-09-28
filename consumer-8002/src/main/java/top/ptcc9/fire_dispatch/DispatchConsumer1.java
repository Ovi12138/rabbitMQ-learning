package top.ptcc9.fire_dispatch;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.IOException;


/**
 * @Author HE LONG CAN
 * @Description 用于测试任务调度
 * @Date 2021-09-26 09:39:28
 */
public class DispatchConsumer1 {
    private static final Log log = LogFactory.get(DispatchConsumer1.class);

    @RabbitListener(queues = "dispatch_test",ackMode = "MANUAL")
    public void receive(Message receive,Channel channel) throws IOException {
        String message = new String(receive.getBody());
        long deliveryTag = receive.getMessageProperties().getDeliveryTag();
        channel.basicQos(1);
        //channel.basicAck(deliveryTag,false);
        log.info("consumer-1 just receive a message({}) from queue({})",message,"dispatch_test");
    }
}
