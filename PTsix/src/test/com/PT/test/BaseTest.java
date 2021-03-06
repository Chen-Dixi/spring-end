package com.PT.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * 配置spring和junit整合，junit启动时加载springIOC容器 spring-test,junit
 */

//@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
// 告诉junit spring配置文件
@ContextConfiguration(locations = {"classpath:spring/spring-*"})
public class BaseTest {

}