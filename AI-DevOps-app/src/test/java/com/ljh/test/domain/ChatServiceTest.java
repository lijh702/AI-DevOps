package com.ljh.test.domain;

import com.alibaba.fastjson.JSON;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.ljh.domain.agent.model.valobj.AiAgentRegisterVO;
import com.ljh.domain.agent.service.IChatService;
import com.ljh.domain.agent.service.armory.factory.DefaultArmoryFactory;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author ljh studio
 * @description
 * @create 2026/7/8 10:22
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ChatServiceTest {


    @Value("classpath:file/dog.png")
    private org.springframework.core.io.Resource resource;

    @Resource
    private IChatService chatService;

    private final String agentId = "100003";
    private final String userId = "ljh";



    @Test
    public void test_agent() throws InterruptedException {

        String session = chatService.createSession(agentId, userId);
        log.info("创建会话ID,{}", session);


        new CountDownLatch(1).await();
    }
}
