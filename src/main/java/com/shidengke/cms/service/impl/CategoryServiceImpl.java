package com.shidengke.cms.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shidengke.cms.dao.CategoryMapper;
import com.shidengke.cms.domain.Category;
import com.shidengke.cms.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {
	@Resource
	private CategoryMapper categoryMapper;

	@Override
	public List<Category> selectsByChannelId(Integer channelId) {
		return categoryMapper.selectsByChannelId(channelId);
	}

}
