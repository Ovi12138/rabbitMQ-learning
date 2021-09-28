package top.ptcc9.config;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.ptcc9.default_provider.DefaultProvider;
import top.ptcc9.fire_dispatch.DispatchProvider;
import top.ptcc9.mq_hello.Provider;
import top.ptcc9.mq_work.WorkProvider;

/**
 * @Author HE LONG CAN
 * @Description rabbitmq 配置类
 * @Date 2021-09-23 14:50:09
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
    public Queue getManualAckQueue() {
        return new Queue("manual_ack",true);
    }

    /**
     * 用于测试 dispatch 的队列
     * @return Queue
     */
    @Bean
    public Queue getDispatchQueue() {
        return new Queue("dispatch_test",true);
    }

    @Bean
    public Provider getHelloProvider() {
        return new Provider();
    }

    @Bean
    public WorkProvider getWorkProvider() {
        return new WorkProvider();
    }

    @Bean
    public DispatchProvider getDispatchProvider() {
        return new DispatchProvider();
    }

    @Bean
    public DefaultProvider getDefaultProvider() {
        return new DefaultProvider();
    }
}
