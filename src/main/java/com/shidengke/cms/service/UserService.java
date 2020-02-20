package com.shidengke.cms.service;

import com.shidengke.cms.domain.User;
import com.shidengke.cms.vo.UserVO;
import com.github.pagehelper.PageInfo;

public interface UserService {

	/**
	 * 
	 * @Title: selects 
	 * @Description: 用户列表
	 * @param user
	 * @return
	 * @return: List<User>
	 */
	 PageInfo<User> selects(User user,Integer page,Integer pageSize);

	  boolean update(User user);
	  //注册
	  boolean  insertSelective(UserVO userVO);
    //登录
	  User login(User user);
	  
		
}
