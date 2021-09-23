package top.ptcc9.mq_hello;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.annotation.Resource;

/**
 * @Author HE LONG CAN
 * @Description hello 提供者
 * @Date 2021-09-23 15:16:55
 */
public class Provider {
    private static final Log log = LogFactory.get(Provider.class);

    /**
     * 消息队列模板
     */
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * @param message 待发送到消息队列中的消息
     */
    public void send(String message) {
        //convertAndSend => 在不整合springboot的场景下，要发送一个消息必须将消息转换成 byte[] 类型,在这里的convert表示转换，该方法已默认将传入message => byte[]
        //参数1：队列名  参数2：待发送消息
        rabbitTemplate.convertAndSend("hello",message);
        log.info("provider-8001 just sent a message({}) to queue({})",message,"hello");
    }
}
