package com.shidengke.cms.service;

import com.shidengke.cms.domain.Article;
import com.shidengke.cms.domain.ArticleWithBLOBs;
import com.github.pagehelper.PageInfo;

public interface ArticleService {

	/**
	 * 
	 * @Title: selects 
	 * @Description: 文章列表
	 * @param article
	 * @return
	 * @return: List<Article>
	 */
	PageInfo<Article> selects(Article article,Integer page,Integer pageSize);

	
	/**
	 * 最新文章
	 * @param article
	 * @param page
	 * @param pageSize
	 * @return
	 */
	PageInfo<Article> selectLast(Article article,Integer page,Integer pageSize);
	/**
	 * 热门文章
	 * @param article
	 * @param page
	 * @param pageSize
	 * @return
	 */
	PageInfo<Article> selectHot(Article article,Integer page,Integer pageSize);
	boolean  update(ArticleWithBLOBs article);
	
	ArticleWithBLOBs selectByPrimaryKey(Integer id);
	
	/**
	 * 
	 * @Title: insertSelective 
	 * @Description: 发布文章
	 * @param record
	 * @return
	 * @return: boolean
	 */
	 boolean insertSelective(ArticleWithBLOBs record);

    //高亮查询
	PageInfo<Article> selectES(Integer page, Integer pageSize, String key);
}
