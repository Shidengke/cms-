package com.shidengke.cms.service.impI;

import javax.annotation.Resource;

import org.junit.Test;

import com.shidengke.cms.domain.Collect;
import com.shidengke.cms.domain.User;
import com.shidengke.cms.service.CollectService;


public class CollectServiceImplTest extends JunitParent {

	@Resource
	CollectService collectService;
	@Test
	public void testInsert() {
		//
		Collect c=new Collect();
	User user = new User();
	user.setId(160);
		//错误的
	//	c.setUrl("aaa");
		//正确的
		c.setUrl("http://127.0.0.1/article?id=2");
		c.setUser(user);
		collectService.insert(c);
		
		
	}

	@Test
	public void testSelects() {
	}

	@Test
	public void testDeleteById() {
	}

}
