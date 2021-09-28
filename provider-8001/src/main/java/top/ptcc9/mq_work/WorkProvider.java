package top.ptcc9.mq_work;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;

import javax.annotation.Resource;
import java.util.Random;

/**
 * @Author HE LONG CAN
 * @Description work 提供者
 * @Date 2021-09-23 16:44:20
 */
public class WorkProvider {
    private static final Log log = LogFactory.get(WorkProvider.class);

    /**
     * 消息队列模板
     */
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * @param message 待发送到消息队列中的消息
     */
    public void send(String message) {
        //生成一个随机数s
        int randomInt = new Random().nextInt(7) + 3;
        StringBuilder builder = new StringBuilder(message);
        //循环拼接 .
        for (int i = 0; i < randomInt; i++) {
            builder.append(".");
        }
        message = String.valueOf(builder);
        rabbitTemplate.convertAndSend("work",message);
        log.info("provider-8001 just sent a message({}) to queue({})",message,"work");
    }

    /**
     * 发送一个复杂的任务，将耗时100秒
     */
    public void sendComplexTask() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            builder.append(".");
        }
        String message = String.valueOf(builder);
        rabbitTemplate.convertAndSend("work",message);
        log.info("provider-8001 just sent a message({}) to queue({})",message,"work");
    }


    /**
     * 发送一个请求之前做某件事
     * 前置处理
     */
    public void sendBeforeProcess(String message) {
        //由于是测试消息前置处理，我们这次向hello队列发送消息，因为如果向work队列发送的话，需要修改另外两个监听work队列的消费者，偷个懒。
        rabbitTemplate.convertAndSend("hello",message,o -> {
            String placeHolder = new String(o.getBody());
            //将消息中所有的数字替换成 *
            placeHolder = placeHolder.replaceAll("[0-9]", "*");
            return new Message(placeHolder.getBytes(),o.getMessageProperties());
        });
        log.info("provider-8001 just sent a message({}) to queue({})",message,"hello");
    }
}
