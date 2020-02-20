package com.shidengke.cms.service.impI;

import javax.annotation.Resource;

import org.junit.Test;

import com.shidengke.cms.domain.User;
import com.shidengke.cms.service.UserService;
import com.github.pagehelper.PageInfo;


public class UserServiceImplTest extends JunitParent {
	
	@Resource
	private UserService userService; 

	@Test
	public void testSelects() {
		
		PageInfo<User> info = userService.selects(null, 1, 100);
		System.out.println(info);
	}
	
	

}
