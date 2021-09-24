package top.ptcc9.mq_work;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
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
        //生成一个随机数
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
}
