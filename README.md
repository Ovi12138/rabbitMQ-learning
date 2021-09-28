## `RabbitMQ`

> 码字不易，未完将持续更新。
>
> `GitHub`项目地址：`https://github.com/Ovi12138/rabbitmq-learning.git`

[TOC]

### 一、简介

#### 1. 是什么

`RabbitMQ`是一款由`Erlang`语言开发的消息队列，是一种应用间通讯的方式。消息发送方和消息接受方可能是不同的应用且在不同的`ip`地址下。

在工作时，消息发送者无需关心将消息发送给谁，仅需要将消息发布到`MQ`中，而消息接收者也无需关心是谁发送了消息，只需要从`MQ`中取出消息。所以他们都不需要对方的存在。



#### 2. 消息模型

- 简单消息模型`Hello World`
- 竞争消费模型`work`
- 订阅发布
  - 广播模型`Fanout`
  - 定向模型`Direct`
  - 话题模型`Topic`



#### 3. 应用场景

- 异步处理

  想象一个注册的场景，假定新用户在注册时需要同时验证手机号和邮箱，假设每部操作耗时`50ms`，则共耗时`150ms`。

  **在普通单机情况下：**

  <img src="http://typora.ptcc9.top/image-20210917092724226.png?x-oss-process=style/style" alt="image-20210917092724226" style="zoom:80%;" />

  **当使用并行处理，并发处理发送手机验证码和邮件验证码：**

  <img src="http://typora.ptcc9.top/image-20210917094538900.png?x-oss-process=style/style" alt="image-20210917094538900" style="zoom:80%;" />

  这样由于发送手机验证码和邮件验证码是并行的，所以两个同时仅需要`50ms`，这时总耗时被缩减到了`100ms`。

  **使用消息队列：**

  <img src="http://typora.ptcc9.top/image-20210917094804527.png?x-oss-process=style/style" alt="image-20210917094804527" style="zoom:80%;" />

  引入消息队列后，所有耗时仅需`60ms`，在任务写入消息队列后可以直接对用户返回成功信息。而真正处理发送验证码业务的可能是其他微服务，所以耗时可以忽略。

- 流量削风

​       例如`12306`春运前的购票，大量的下单请求到达后端服务器，很有可能服务器无法承受这样的压力，假设就算服务器能够承受，那么数据库也无法承受这样的并发量。所以可以先放入消息队列里，缓速执行后续逻辑。也可以设置队列的长度，当任务超过这个长度时，则直接抛弃这个任务，返回给用户一个提示。

- 解耦

​      应用间协作，例如有三个微服务，分别为下单、减库存、发货。用户下单后，将消息写入消息队列中，减库存微服务收到消息后进行减库存操作之后再写入消息队列，发货微服务收到消息后执行发货业务逻辑。这时，就算发货微服务宕机，未完成的任务也依旧在队列中未被消费，也不会丢失。当发货微服务重新顶上来的时候，便可以继续消费。



### 二、`Docker`部署和配置

#### 1. 拉取镜像

```shell
docker pull rabbitmq:management
```



#### 2. 跑

```shell
# 5672 client访问端口
# 15672 管理界面访问端口
docker run -d --name rabbitmq-learning -p 5672:5672 -p 15672:15672 rabbitmq:management
```



#### 3. 访问

```http
ip + 15672
```

默认账号密码：guest  guest

该账号拥有最高权限。



#### 4. 创建新用户

使用默认账号密码登陆后：

![](http://typora.ptcc9.top/image-20210917101207395.png?x-oss-process=style/style)

创建新用户时有四种身份可以选择：

- `management`普通管理员

可以管理控制台，无法看到节点的各项信息，不能管理策略。

- `policymaker`策略制定者

可以管理控制台，可以管理策略，但无法查看节点信息。

- `monitoring`监控者

可以管理控制台，可以查看节点信息。

- `administrator`最高权限

可以查看所有信息。



输入用户名，密码，选择身份后点击添加用户按钮。

<img src="http://typora.ptcc9.top/image-20210917103117022.png?x-oss-process=style/style" alt="image-20210917103117022" style="zoom:80%;" />

点击刚刚新建的用户进入该用户的权限配置界面。

<img src="http://typora.ptcc9.top/image-20210917103722464.png?x-oss-process=style/style" alt="image-20210917103722464" style="zoom:80%;" />



#### 5. 创建`virtual host`

`virtual host`即虚拟消息服务器，每一个`virtual host`相当于对应了一个独立的`RabbitMQ`服务器，其相互隔离故`exchange,queue,message`不可互通。

**`rabbitMQ`有一个默认的`virtual host`名为`/`，当然，你也可以自定义自己的`virtual host`。**

<img src="http://typora.ptcc9.top/image-20210917112214684.png?x-oss-process=style/style" alt="image-20210917112214684" style="zoom:80%;" />

创建了新的`virtual host`之后，可以为用户添加这个虚拟消息服务器的使用权限。

<img src="http://typora.ptcc9.top/image-20210917112337295.png?x-oss-process=style/style" alt="image-20210917112337295" style="zoom:80%;" />

### 三、`Springboot`整合`rabbitMQ`

 引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
    <version>2.3.5.RELEASE</version>   <!-- 版本和J2E保持一致 -->
</dependency>
```



配置`application.yml`

```properties
server:
  port: 8001
spring:
  application:
    name: provider-8001
  rabbitmq:
    host: 8.140.171.57  #你的rabbitmq服务器地址
    port: 5672
    username: root
    password: H03089611.
    virtual-host: test
```



#### 1. `Hello World`

> `RabbitMQ`是一个消息代理者，它接收和转发消息。你可以把它当作一个邮局：当你把你想要寄出去的邮件放进邮箱，你可以完全放心你的邮件最终会被邮递员送到收件人的手里。在这个过程中，`RabbitMQ`是邮箱、邮局也是邮递员。
>
> `RabbitMQ`和邮局最大的区别就是，`RabbitMQ`并不关心你的邮件是什么。相反，它只关心接收，存储和转发工作。



**消息提供者：**

<img src="http://typora.ptcc9.top/image-20210923143423297.png?x-oss-process=style/style" alt="image-20210923143423297" style="zoom:80%;" />

消息提供者是专门向消息队列中输送元素的角色。



**消息消费者：**

<img src="http://typora.ptcc9.top/image-20210923143546539.png?x-oss-process=style/style" alt="image-20210923143546539" style="zoom:80%;" />

消息消费者是专门负责消费消息队列中元素的角色。



**队列：**

<img src="http://typora.ptcc9.top/image-20210923164040203.png?x-oss-process=style/style" style="zoom:80%;" />

队列是专门存放消息提供者提供且尚未被消息消费者消费的消息。



##### ① 编写一个消息提供者`Provider`：

```java
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

```



**编写`BeanConfig`将该`Bean`注入到`IOC`中：**

这一步可以省略，更换`@Component`的方式注入也行。

```java
package top.ptcc9.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.ptcc9.mq_hello.Provider;

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
    public Provider getHelloProvider() {
        return new Provider();
    }
}
```



**编写`Controller`对外暴漏接口用于测试：**

```java
package top.ptcc9.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import top.ptcc9.mq_hello.Provider;

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

    @RequestMapping(value = "/sendToHello",method = RequestMethod.POST)
    public void sendToHello(String message) {
        //调用provider发送消息
        provider.send(message);
    }
}
```



**测试：**

`URL`：`http://127.0.0.1:8001/sendToHello?message=this is a message`

建议使用`PostMan`，连续执行两次。

<img src="http://typora.ptcc9.top/image-20210923161448767.png?x-oss-process=style/style" alt="image-20210923161448767" style="zoom:80%;" />

控制台成功打印。

打开`RabbitMQ`控制台查看该队列。

<img src="http://typora.ptcc9.top/image-20210923161534362.png?x-oss-process=style/style" alt="image-20210923161534362" style="zoom:80%;" />

可以看到成功向`Hello`队列添加了两条消息，都已准备就绪且未被消费。



##### ② 编写一个消息消费者`Consumer`：

```java
package top.ptcc9.mq_hello;import cn.hutool.log.Log;import cn.hutool.log.LogFactory;import org.springframework.amqp.rabbit.annotation.RabbitListener;/** * @Author HE LONG CAN * @Description hello 消费者 * @Date 2021-09-23 15:32:02 */public class Consumer {    private static final Log log = LogFactory.get(Consumer.class);    @RabbitListener(queues = "hello")    public void receive(String message) {        log.info("consumer-8002 just received a message({}) from queue({})",message,"hello");    }}
```

`@RabbitListener`注解标注表名该方法将监听指定队列。



**测试：**

启动后，可以看到控制台打印。

<img src="http://typora.ptcc9.top/image-20210923162411883.png?x-oss-process=style/style" alt="image-20210923162411883" style="zoom:80%;" />

此时`RabbitMQ`控制台中该队列的信息如下：

**这里要注意的是，只有消息被消费，才可以在这里看到队列被创建，只有提供者的情况下不会自动创建队列。**

<img src="http://typora.ptcc9.top/image-20210923162444604.png?x-oss-process=style/style" alt="image-20210923162444604" style="zoom:80%;" />

可见刚刚加入的消息都已经被消费。



#### 2. `Work`队列

在上一节中我们讲到的是一个简单的`1v1`模型，这一节我们将创建一个`Work`队列，用于应对复杂任务的耗时问题。

这个模式不同于上一节，它拥有多个消费者。这几个消费者同时消费队列中的任务，且任务不会被重复消费。

<img src="http://typora.ptcc9.top/image-20210923164124264.png?x-oss-process=style/style" alt="image-20210923164124264" style="zoom:80%;" />

该模式的主要思想是为了避免某些任务太过耗时，在提供者将任务以字符串的形式放入队列中后，消费者则是稍后去完成这个任务。

这样的模型被广泛应用于`Web`项目中，因为一个短暂的`HTTP`请求根本不允许我们在后台执行太过耗时的任务。



在本节的`Work`模型示例中，为了保证任务耗时，我们不打算使用真正的耗时任务，而是使用另一种方式。

用`.`表示耗时操作，并且每个`.`将耗时`1s`。

例如：

```java
String message = "hello...";  //这条消息将耗时3秒
```

那么我们只需要在消息提供者中随机生成不同个数`.`的消息，并且在消费者中判断`.`的个数，然后使用`Thread.sleep()`来阻塞线程以达到模拟耗时的目的。



##### ① 编写一个消息提供者`WorkProvider`

这次要编写的`Provider`和上一节类似，我们只需要添加随机生成`.`即可。

```java
package top.ptcc9.mq_work;import cn.hutool.log.Log;import cn.hutool.log.LogFactory;import org.springframework.amqp.rabbit.core.RabbitTemplate;import javax.annotation.Resource;import java.util.Random;/** * @Author HE LONG CAN * @Description work 提供者 * @Date 2021-09-23 16:44:20 */public class WorkProvider {    private static final Log log = LogFactory.get(WorkProvider.class);    /**     * 消息队列模板     */    @Resource    private RabbitTemplate rabbitTemplate;    /**     * @param message 待发送到消息队列中的消息     */    public void send(String message) {        //生成一个随机数        int randomInt = new Random().nextInt(7) + 3;        StringBuilder builder = new StringBuilder(message);        //循环拼接 .         for (int i = 0; i < randomInt; i++) {            builder.append(".");        }        message = String.valueOf(builder);        rabbitTemplate.convertAndSend("work",message);        log.info("provider-8001 just sent a message({}) to queue({})",message,"work");    }}
```



**在`Controller`对外暴露一个接口：**

```java
@RequestMapping(value = "/sendToWork",method = RequestMethod.POST)public void sendToWork(String message) {    //连续发送 5 条消息，因为消息会被多消费者消费    for (int i = 0; i < 5; i++) {        //调用 workProvider 发送消息        workProvider.send(message + (i + 1));    }}
```



**测试：**

`URL`：`http://127.0.0.1:8001/sendToWork?message=this is a message`

<img src="http://typora.ptcc9.top/image-20210923170056672.png?x-oss-process=style/style" alt="image-20210923170056672" style="zoom:80%;" />



##### ② 编写两个消息消费者`WorkConsumer1和WorkConsumer2`

```java
package top.ptcc9.mq_work;import cn.hutool.log.Log;import cn.hutool.log.LogFactory;import org.springframework.amqp.rabbit.annotation.RabbitListener;import top.ptcc9.mq_hello.Consumer;import java.util.concurrent.TimeUnit;/** * @Author HE LONG CAN * @Description TODO * @Date 2021-09-23 17:08:38 */public class WorkConsumer1 {    private static final Log log = LogFactory.get(WorkConsumer1.class);    @RabbitListener(queues = "work")    public void receive(String message) {        log.info("consumer-1 just received a message({}) from queue({})",message,"work");        //检查字符串中"."，每次检查到阻塞一秒        for (int i = 0; i < message.length(); i++) {            if (message.charAt(i) == '.') {                try {                    TimeUnit.SECONDS.sleep(1);                }catch (InterruptedException e) {                    e.printStackTrace();                }            }        }        log.info("consumer-1 just finished message({})",message);    }}package top.ptcc9.mq_work;import cn.hutool.log.Log;import cn.hutool.log.LogFactory;import org.springframework.amqp.rabbit.annotation.RabbitListener;import top.ptcc9.mq_hello.Consumer;import java.util.concurrent.TimeUnit;/** * @Author HE LONG CAN * @Description TODO * @Date 2021-09-23 17:08:45 */public class WorkConsumer2 {    private static final Log log = LogFactory.get(WorkConsumer2.class);    @RabbitListener(queues = "work")    public void receive(String message) {        log.info("consumer-2 just received a message({}) from queue({})",message,"work");        //检查字符串中"."，每次检查到阻塞一秒        for (int i = 0; i < message.length(); i++) {            if (message.charAt(i) == '.') {                try {                    TimeUnit.SECONDS.sleep(1);                }catch (InterruptedException e) {                    e.printStackTrace();                }            }        }        log.info("consumer-2 just finished message({})",message);    }}
```



#### 3. 消息确认机制

你可能在担心有些耗时任务执行的过程中`Consumer`消费者仅仅完成了一半就发生异常。

`Spring AMQP`采用了比较保守的消息确认机制，如果在任务的过程中抛出异常，则会自动调用`channel.basicReject(deliveryTag, requeue);`并且将未完成的任务重新入队。

这样的消息确认机制是默认开启的，除非你显式的关闭它。

```properties
defaultRequeueRejected=false   #关闭消息确认
```

**还有一个例外情况**，并不是所有的`Exception`都会触发`Reject`，当你的任务在执行的过程中抛出了`AmqpRejectAndDontRequeueException`，那么你的任务仍然会被确认消费。



**测试：**

我们先将消息确认关闭。

```java
    listener:      simple:        default-requeue-rejected: false
```

重新在`WorkProvider`中编写一个新的方法，用于发送一个巨耗时的任务到消息队列中。

```java
/** * 发送一个复杂的任务，将耗时100秒 */public void sendComplexTask() {    StringBuilder builder = new StringBuilder();    for (int i = 0; i < 100; i++) {        builder.append(".");    }    String message = String.valueOf(builder);    rabbitTemplate.convertAndSend("work",message);    log.info("provider-8001 just sent a message({}) to queue({})",message,"work");}//MyController.java@RequestMapping(value = "/sendComplexTaskToWork",method = RequestMethod.POST)public void sendComplexTaskToWork() {    workProvider.sendComplexTask();}
```

再修改`WorkConsumer1`和`WorkConsumer2`的监听方法。

让其休眠一段时间后抛出异常，模拟执行过程中的出错。

```java
@RabbitListener(queues = "work")public void receive(String message) {    log.info("consumer-1 just received a message({}) from queue({})",message,"work");    /*        休眠5秒后抛出异常         */    try {        TimeUnit.SECONDS.sleep(5);    }catch (InterruptedException e) {        e.printStackTrace();    }    throw new IllegalArgumentException();    //        //检查字符串中"."，每次检查到阻塞一秒    //        for (int i = 0; i < message.length(); i++) {    //            if (message.charAt(i) == '.') {    //                try {    //                    TimeUnit.SECONDS.sleep(1);    //                }catch (InterruptedException e) {    //                    e.printStackTrace();    //                }    //            }    //        }    //        log.info("consumer-1 just finished message({})",message);}
```

这样就满足了测试要求，我们调用接口让`WorkProvider`发送一个耗时任务到队列中。

`WorkProvider`发送成功。

![image-20210924095958121](http://typora.ptcc9.top/image-20210924095958121.png?x-oss-process=style/style)

`WorkConsumer`接收任务。

![image-20210924095914845](http://typora.ptcc9.top/image-20210924095914845.png?x-oss-process=style/style)

`RabbitMQ`控制台内变化。

![image-20210924100229275](http://typora.ptcc9.top/image-20210924100229275.png?x-oss-process=style/style)

- `Ready`：队列内尚未被取出的任务。
- `Unacked`：已取出，但是尚未被确认消费的任务。
- `Total`：合计。

可见，在执行任务过程中发生了异常，但消息还是被消费掉了。这样很可能造成数据丢失。



**其他测试：**

以上的测试是验证了关闭默认消息确认情况下的消息消费情况。

**除此之外，还需要测试两种情况，这里仅说明，请自行测试：**

- 关闭消息确认，抛出`AmqpRejectAndDontRequeueException`，查看消息状态。
- 打开消息确认，抛出异常，查看消息状态。



#### 4. 消息手动确认

`RabbitMQ`默认使用自动确认，但很多时候并不满足我们的需求。注解`@RabbitListener`中可以修改消息确认模式。

```java
/**	 * Override the container factory	 * {@link org.springframework.amqp.core.AcknowledgeMode} property. Must be one of the	 * valid enumerations. If a SpEL expression is provided, it must evaluate to a	 * {@link String} or {@link org.springframework.amqp.core.AcknowledgeMode}.	 * @return the acknowledgement mode.	 * @since 2.2	 */String ackMode() default "";
```

通过传入一个`String`类型的`AcknowledgeMode`枚举来切换确认模式。共有下列三种模式，手动模式为`MANUAL`。

```java
/**	 * No acks - {@code autoAck=true} in {@code Channel.basicConsume()}.	 */NONE,/**	 * Manual acks - user must ack/nack via a channel aware listener.	 */MANUAL,/**	 * Auto - the container will issue the ack/nack based on whether	 * the listener returns normally, or throws an exception.	 * <p><em>Do not confuse with RabbitMQ {@code autoAck} which is	 * represented by {@link #NONE} here</em>.	 */AUTO;
```

所以，我们可以通过下面的方式修改消费者的消息确认模式。

```java
@RabbitListener(queues = "***",ackMode = "MANUAL")
```

接下来，我们可以在程序中灵活的使用`channel.basicAck()`确认消息，和`channel.basicNack()`拒绝消息。

- `chanel.basicAck(long,boolean)`

确认消息，第一个参数为`deliveryTag`，表示当前消息的投递序号，每次投递消息，该参数都会增加。第二个参数`multiple`表示是否支持多消息同时确认。

- `channel.basicNack(long,boolean,boolean)`

拒绝消息，前两个参数与`basicAck`相同，第三个参数表示是否重新入队。



**测试：**

编写一个`ManualAckConsumer.java`，确认所有含有**奇数**的消息，拒绝所有**非奇数**的消息。

```java
public class ManualAckConsumer {    private static final Log log = LogFactory.get(ManualAckConsumer.class);    private static final Pattern compile = Pattern.compile("[1,3,5,7,9]");    @RabbitListener(queues = "manual_ack",ackMode = "MANUAL")    public void receive(Message source, Channel channel) throws IOException {        String message = new String(source.getBody());        long deliveryTag = source.getMessageProperties().getDeliveryTag();        log.info("manual ack consumer just received a message({}) from queue({})",message,"manual_ack");        Matcher matcher = compile.matcher(message);        if (matcher.find()) {            channel.basicAck(deliveryTag, false);            log.info("manual ack consumer just [ACK] message({})",message);        } else {            channel.basicNack(deliveryTag,false,true);            log.warn("manual ack consumer just [NACK] message({})",message);        }    }}
```

编写一个`DefaultProvider`用于向指定队列发送指定消息。

```java
public class DefaultProvider {    private static final Log log = LogFactory.get(DefaultProvider.class);    @Resource    private RabbitTemplate rabbitTemplate;    public void send(String message,String queueName) {        rabbitTemplate.convertAndSend(queueName,message);        log.info("default provider just sent a message({}) to queue({})",message,queueName);    }}//MyController.java@RequestMapping(value = "/sendToManualAckQueue",method = RequestMethod.POST)public void sendToManualAckQueue() {    for (int i = 1; i <= 9; i++) {        defaultProvider.send(String.valueOf(i),"manual_ack");    }}
```

调用接口发送`[1,9]`消息，查看消费情况：

**`default provider`正常发送了`[1,9]`消息。**

![image-20210928171500619](http://typora.ptcc9.top/image-20210928171500619.png?x-oss-process=style/style)

`ManualAckConsumer`正常消费且`ACK`了五条消息，分别为`1,3,5,7,9`。**但是消息`2,4,6,8`被`NACK`且重新入队，重新入队后重新消费，之后再`NACK`再重新入队，陷入了死循环。**

<img src="http://typora.ptcc9.top/image-20210928171740742.png?x-oss-process=style/style" alt="image-20210928171740742" style="zoom:80%;" />

此时`RabbitMQ`控制台也表现出相同的信息。

![image-20210928171812308](http://typora.ptcc9.top/image-20210928171812308.png?x-oss-process=style/style)

永远有`4`条消息未被消费。



#### 5. 死信队列





#### 6. 消息前置处理

你可以选择使用`convertAndSent`方法并且传入一个`MessagePostProcessor `接口的实现类作为参数来达到消息前置处理的目的。

该接口类中提供一个在消息发送之前的回调方法，所以这里是一个修改信息或者头的好地方。

**附上源码：**

```java
package org.springframework.amqp.core;import org.springframework.amqp.AmqpException;@FunctionalInterfacepublic interface MessagePostProcessor {	Message postProcessMessage(Message message) throws AmqpException;	default Message postProcessMessage(Message message, Correlation correlation) {		return postProcessMessage(message);	}}
```

它是一个函数式接口，由`@FunctionalInterface`可见。

所以可以通过`Lambda`方式调用。

**测试：**

编写一个发送消息的方法。

```java
/**     * 发送一个请求之前做某件事     */public void sendBeforeProcess(String message) {    //由于是测试消息前置处理，我们这次向hello队列发送消息，因为如果向work队列发送的话，需要修改另外两个监听work队列的消费者，偷个懒。    rabbitTemplate.convertAndSend("hello",message,o -> {        String placeHolder = new String(o.getBody());        //将消息中所有的数字替换成 *        placeHolder = placeHolder.replaceAll("[0-9]", "*");        return new Message(placeHolder.getBytes(),o.getMessageProperties());    });    log.info("provider-8001 just sent a message({}) to queue({})",message,"hello");}//MyController.java  添加@RequestMapping(value = "/sendBeforeProcess",method = RequestMethod.POST)public void sendBeforeProcess(String message) {    workProvider.sendBeforeProcess(message);}
```

![image-20210924163816981](http://typora.ptcc9.top/image-20210924163816981.png?x-oss-process=style/style)

![image-20210924163834298](http://typora.ptcc9.top/image-20210924163834298.png?x-oss-process=style/style)

可见信息已得到了处理.



#### 7. 公平调度

`RabbitMQ`在默认情况下使用的就是循环调度，这样的模式通常并不能完全按照我们的意愿来执行。

因为无论任务繁重与否，都将被平分给每个消费者。**这样很可能导致一些消费者任务压得喘不过气，而其他消费者则几乎无事可做。**

`RabbitMQ`之所以这样，是因为他只是盲目的将到来的任务分配给消费者们，而不关心对方是否还有尚未`ACK`的任务。

**例如：**

**`Queue`中有`20`个任务，有两个消费者。那么`RabbitMQ`将默认把第奇数个任务分给`consumer-1`并把第偶数个任务分给`consumer-2`。**



#### 8. `DEFAULT_PREFETCH_COUNT`

该变量在`AbstractMessageListenerContainer`中定义，表示当前消费者最大能够接受的任务数，也是`RabbitMQ`最大能够给予某消费者未完成的任务数。该变量默认值为`250`，也就是说，当消费者手中有`250`个没有`ACK`的任务时，`RabbitMQ`将不再继续给消费者推送新任务。

