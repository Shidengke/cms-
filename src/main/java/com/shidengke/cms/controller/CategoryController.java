package com.shidengke.cms.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.shidengke.cms.domain.Category;
import com.shidengke.cms.service.CategoryService;
import com.shidengke.cms.utils.Result;
import com.shidengke.cms.utils.ResultUtil;

@RequestMapping("category")
@Controller
public class CategoryController {
	@Resource
	private CategoryService categoryService;
	
	/**
	 * 
	 * @Title: selects 
	 * @Description: 根据栏目查询分类
	 * @param channelId
	 * @return
	 * @return: List<Category>
	 */
	@ResponseBody
	@RequestMapping("selects")
	private Result<Category> selects(Integer channelId){
		return ResultUtil.success(categoryService.selectsByChannelId(channelId));
	}

}
