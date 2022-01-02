package com.wmr.community;

import com.wmr.community.dao.AlphaDAO;
import com.wmr.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {
	private ApplicationContext applicationContext;

	@Autowired
	@Qualifier("alphaDAOHibernate")
	private AlphaDAO alphaDAO;

	@Autowired
	private AlphaService alphaService;

	@Autowired
	private SimpleDateFormat simpleDateFormat;

	@Test
	void testDI() {
		System.out.println(alphaDAO);
		System.out.println(alphaService);
		System.out.println(simpleDateFormat.format(new Date()));
	}

	@Test
	void testApplicationContext() {
		System.out.println(applicationContext);
		AlphaDAO bean = applicationContext.getBean(AlphaDAO.class);
		System.out.println(bean.select());
		bean = applicationContext.getBean("alphaDAOHibernate", AlphaDAO.class);
		System.out.println(bean.select());
	}

	@Test
	void testBeanManagement() {
		AlphaService bean = applicationContext.getBean(AlphaService.class);
		AlphaService bean1 = applicationContext.getBean(AlphaService.class);
		System.out.println(bean);
		System.out.println(bean1);
	}

	@Test
	void testBeanConfig() {
		SimpleDateFormat simpleDateFormat = applicationContext.getBean("simpleDateFormat", SimpleDateFormat.class);
		System.out.println(simpleDateFormat.format(new Date()));
	}


	// ApplicationContext就是Spring容器
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
