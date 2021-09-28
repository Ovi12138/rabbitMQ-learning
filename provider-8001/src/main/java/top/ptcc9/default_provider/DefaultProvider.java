package top.ptcc9.default_provider;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.annotation.Resource;

/**
 * @Author HE LONG CAN
 * @Description TODO
 * @Date 2021-09-28 17:03:21
 */
public class DefaultProvider {
    private static final Log log = LogFactory.get(DefaultProvider.class);

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void send(String message,String queueName) {
        rabbitTemplate.convertAndSend(queueName,message);
        log.info("default provider just sent a message({}) to queue({})",message,queueName);
    }
}
