package top.ptcc9.manual_ack;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author HE LONG CAN
 * @Description TODO
 * @Date 2021-09-28 16:07:43
 */
public class ManualAckConsumer {
    private static final Log log = LogFactory.get(ManualAckConsumer.class);
    private static final Pattern compile = Pattern.compile("[1,3,5,7,9]");

    @RabbitListener(queues = "manual_ack",ackMode = "MANUAL")
    public void receive(Message source, Channel channel) throws IOException {
        String message = new String(source.getBody());
        long deliveryTag = source.getMessageProperties().getDeliveryTag();
        log.info("manual ack consumer just received a message({}) from queue({})",message,"manual_ack");
        Matcher matcher = compile.matcher(message);
        if (matcher.find()) {
            channel.basicAck(deliveryTag, false);
            log.info("manual ack consumer just [ACK] message({})",message);
        } else {
            channel.basicNack(deliveryTag,false,true);
            log.warn("manual ack consumer just [NACK] message({})",message);
        }
    }
}
