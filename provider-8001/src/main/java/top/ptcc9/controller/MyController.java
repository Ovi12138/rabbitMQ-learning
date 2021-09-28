package top.ptcc9.controller;

import cn.hutool.core.date.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import top.ptcc9.default_provider.DefaultProvider;
import top.ptcc9.fire_dispatch.DispatchProvider;
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

    @Resource
    DispatchProvider dispatchProvider;

    @Resource
    DefaultProvider defaultProvider;

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
            workProvider.send(message + (i + 1));
        }
    }

    @RequestMapping(value = "/sendComplexTaskToWork",method = RequestMethod.POST)
    public void sendComplexTaskToWork() {
        workProvider.sendComplexTask();
    }


    @RequestMapping(value = "/sendBeforeProcess",method = RequestMethod.POST)
    public void sendBeforeProcess(String message) {
        workProvider.sendBeforeProcess(message);
    }

    @RequestMapping(value = "/sendDispatch",method = RequestMethod.POST)
    public void sendDispatch(String message) {
        dispatchProvider.send(message);
    }

    @RequestMapping(value = "/sendToManualAckQueue",method = RequestMethod.POST)
    public void sendToManualAckQueue() {
        for (int i = 1; i <= 9; i++) {
            defaultProvider.send(String.valueOf(i),"manual_ack");
        }
    }
}
