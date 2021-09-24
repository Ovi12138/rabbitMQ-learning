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

<img src="http://typora.ptcc9.top/image-20210923164124264.png?x-oss-process=style/style" alt="image-20210923164124264" style="zoom:80%;" />

该模式的主要思想是为了避免某些任务太过耗时，在提供者将任务已字符串的形式放入队列中后，消费者则是稍后去完成这个任务。

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
@RequestMapping(value = "/sendToWork",method = RequestMethod.POST)public void sendToWork(String message) {    //连续发送 5 条消息，因为消息会被多消费者消费    for (int i = 0; i < 5; i++) {        //调用 workProvider 发送消息        workProvider.send(message);    }}
```



**测试：**

`URL`：`http://127.0.0.1:8001/sendToWork?message=this is a message`

<img src="http://typora.ptcc9.top/image-20210923170056672.png?x-oss-process=style/style" alt="image-20210923170056672" style="zoom:80%;" />



##### ② 编写两个消息消费者`WorkConsumer1和WorkConsumer2`

```java
package top.ptcc9.mq_work;import cn.hutool.log.Log;import cn.hutool.log.LogFactory;import org.springframework.amqp.rabbit.annotation.RabbitListener;import top.ptcc9.mq_hello.Consumer;import java.util.concurrent.TimeUnit;/** * @Author HE LONG CAN * @Description TODO * @Date 2021-09-23 17:08:38 */public class WorkConsumer1 {    private static final Log log = LogFactory.get(WorkConsumer1.class);    @RabbitListener(queues = "work")    public void receive(String message) {        log.info("consumer-1 just received a message({}) from queue({})",message,"work");        //检查字符串中"."，每次检查到阻塞一秒        for (int i = 0; i < message.length(); i++) {            if (message.charAt(i) == '.') {                try {                    TimeUnit.SECONDS.sleep(1);                }catch (InterruptedException e) {                    e.printStackTrace();                }            }        }        log.info("consumer-1 just finished message({})",message);    }}package top.ptcc9.mq_work;import cn.hutool.log.Log;import cn.hutool.log.LogFactory;import org.springframework.amqp.rabbit.annotation.RabbitListener;import top.ptcc9.mq_hello.Consumer;import java.util.concurrent.TimeUnit;/** * @Author HE LONG CAN * @Description TODO * @Date 2021-09-23 17:08:45 */public class WorkConsumer2 {    private static final Log log = LogFactory.get(WorkConsumer2.class);    @RabbitListener(queues = "work")    public void receive(String message) {        log.info("consumer-2 just received a message({}) from queue({})",message,"work");        //检查字符串中"."，每次检查到阻塞一秒        for (int i = 0; i < message.length(); i++) {            if (message.charAt(i) == '.') {                try {                    TimeUnit.SECONDS.sleep(1);                }catch (InterruptedException e) {                    e.printStackTrace();                }            }        }        log.info("consumer-2 just finished message({})",message);    }}
```





