package top.ptcc9.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import top.ptcc9.mq_hello.Provider;
import top.ptcc9.mq_work.WorkProvider;

import javax.annotation.Resource;

/**
 * @Author HE LONG CAN
 * @Description 用于测试
 * @Date 2021-09-23 15:39:12
 */
@RestController
public class MyController {

    @Resource
    private Provider provider;

    @Resource
    private WorkProvider workProvider;

    @RequestMapping(value = "/sendToHello",method = RequestMethod.POST)
    public void sendToHello(String message) {
        //调用provider发送消息
        provider.send(message);
    }


    @RequestMapping(value = "/sendToWork",method = RequestMethod.POST)
    public void sendToWork(String message) {
        //连续发送 5 条消息，因为消息会被多消费者消费
        for (int i = 0; i < 5; i++) {
            //调用 workProvider 发送消息
            workProvider.send(message);
        }
    }
}
