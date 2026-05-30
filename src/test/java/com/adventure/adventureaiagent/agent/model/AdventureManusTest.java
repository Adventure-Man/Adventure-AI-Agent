package com.adventure.adventureaiagent.agent.model;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AdventureManusTest {
    @Resource
    private AdventureManus adventureManus;
    @Test
    void test() {
        // 创建一个 AdventureManus 实例
//        String run = adventureManus.run("单身如何提升自己？");
        String run = adventureManus.run("我和对象在上海浦东新区,帮我推荐5公里内合适的约会地点," +
                "并结合网络图片,制定详细的约会计划,输出到pdf,pdf名称:约会计划.pdf");
        System.out.println(run);
        Assertions.assertNotNull(run);
    }

}