package top.ptcc9.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.ptcc9.mq_hello.Consumer;
import top.ptcc9.mq_work.WorkConsumer1;
import top.ptcc9.mq_work.WorkConsumer2;

/**
 * @Author HE LONG CAN
 * @Description 配置类
 * @Date 2021-09-23 15:31:03
 */
@Configuration
public class BeanConfig {

    @Bean
    public Queue getHelloQueue() {
        return new Queue("hello",true);
    }

    @Bean
    public Queue getWorkQueue() {
        return new Queue("work",true);
    }

    @Bean
    public Consumer getHelloConsumer() {
        return new Consumer();
    }

    @Bean
    public WorkConsumer1 getWorkConsumer1() {
        return new WorkConsumer1();
    }

    @Bean
    public WorkConsumer2 getWorkConsumer2() {
        return new WorkConsumer2();
    }
}
