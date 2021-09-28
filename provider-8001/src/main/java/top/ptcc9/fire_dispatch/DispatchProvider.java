package top.ptcc9.fire_dispatch;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * @Author HE LONG CAN
 * @Description TODO
 * @Date 2021-09-26 09:38:52
 */
public class DispatchProvider {
    private static final Log log = LogFactory.get(DispatchProvider.class);

    @Resource
    RabbitTemplate rabbitTemplate;

    public void send(String message) {
        for (int i = 0; i < 10; i++) {
            rabbitTemplate.convertAndSend("dispatch_test",message + i);
            log.info("provider-8001 just sent a message({}) to queue({})",message + i,"dispatch_test");
        }
    }
}
