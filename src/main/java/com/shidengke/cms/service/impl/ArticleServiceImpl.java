package com.shidengke.cms.service.impl;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.shidengke.cms.dao.ArticleMapper;
import com.shidengke.cms.domain.Article;
import com.shidengke.cms.domain.ArticleWithBLOBs;
import com.shidengke.cms.domain.Category;
import com.shidengke.cms.domain.Channel;
import com.shidengke.cms.domain.User;
import com.shidengke.cms.service.ArticleService;
import com.shidengke.cms.utils.ESUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service
public class ArticleServiceImpl implements ArticleService {
	@Resource
	
	private ArticleMapper articleMapper;

	@Resource
	private RedisTemplate<String, Article> redisTemplate;
	
	@Resource
	private ElasticsearchTemplate elasticsearchTemplate;
	
	/**
	 * 最新文章
	 */
	public PageInfo<Article> selectLast(Article article, Integer page, Integer pageSize) {
		//第一次访问的时候，redis中没有数据，从mysql数据库中获取数据
		//怎么判断是第一次访问？
		//直接查看redis中有没有对应的数据，如果没有，则为第一次访问
		//之后再次访问时，直接从redis中获取数据
		
		//获取List类型的操作对象
		ListOperations<String, Article> opsForList = redisTemplate.opsForList();
		
		List<Article> articles = null;
		
		if(redisTemplate.hasKey("last_article")) {
			//如果有对应的键，则直接从redis中获取数据
			
			//获取数据
			articles = opsForList.range("last_article", 0, -1);
			
		}else {
			//如果没有对应的键
			//从mysql中获取数据
			PageHelper.startPage(page, pageSize);
			articles = articleMapper.selects(article);
			
			//获取完数据以后，存入redis中
			opsForList.rightPushAll("last_article", articles);
		}
		
		return new PageInfo<Article>(articles);
	}
	
	/**
	 * 热门文章
	 * @param article
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public PageInfo<Article> selectHot(Article article, Integer page, Integer pageSize) {
		//第一次访问的时候，redis中没有数据，从mysql数据库中获取数据
		//怎么判断是第一次访问？
		//直接查看redis中有没有对应的数据，如果没有，则为第一次访问
		//之后再次访问时，直接从redis中获取数据
		
		//获取List类型的操作对象
		ListOperations<String, Article> opsForList = redisTemplate.opsForList();
		
		PageInfo<Article> pageInfo = null;
		
		if(redisTemplate.hasKey("hot_article")) {
			//如果有对应的键，则直接从redis中获取数据
			
			//获取数据
			List<Article> articles = opsForList.range("hot_article", (page - 1) * pageSize, page * pageSize - 1);
			
			//获取总条数
			Long size = opsForList.size("hot_article");
			
			//创建数据
			Page<Article> page_list = new Page<Article>(page,pageSize);
			
			//设置数据
			page_list.addAll(articles);
			
			//设置总条数
			page_list.setTotal(size);
			
			pageInfo = new PageInfo<Article>(page_list, 3);
			
		/*	//设置数据
			pageInfo = new PageInfo<Article>(articles,3);
			//上一页
			pageInfo.setPrePage(page > 1 ? page-1:1);
			//当前页
			pageInfo.setPageNum(page);
			
			//获取总页数
			int pages = (int) ((size + pageSize - 1) / pageSize);
			
			//下一页
			pageInfo.setNextPage(page >= pages ? pages : (page + 1) );*/
			
			
			
		}else {
			//如果没有对应的键
			//从mysql中获取所有热门文章的数据
			List<Article> all_articles = articleMapper.selects(article);
			
			//获取全部数据以后，存入redis中
			opsForList.rightPushAll("hot_article", all_articles);
			
			//获取分页数据，显示出来
			PageHelper.startPage(page, pageSize);
			//设置数据
			pageInfo = new PageInfo<Article>( articleMapper.selects(article));
			
		}
		
		return pageInfo;
	}

	@Override
	public PageInfo<Article> selects(Article article, Integer page, Integer pageSize) {
		PageHelper.startPage(page, pageSize);
		List<Article> articles = articleMapper.selects(article);

		return new PageInfo<Article>(articles);
	}
	@Override
	public boolean update(ArticleWithBLOBs article) {
		try {

			int result = articleMapper.updateByPrimaryKeySelective(article);
			//修改文章成功
			if (result > 0 ) {
				//判断当前要审核文章
				//审核文章通过以后，要清空redis对应的数据
				//清空redis中最新文章的缓存
				redisTemplate.delete("last_article");
				
				//清空redis热门文章的缓存
				redisTemplate.delete("hot_article");
				
				//如果删除文章
				if(article.getDeleted() != null && article.getDeleted() == 1) {
					//删除es中数据
					elasticsearchTemplate.delete(Article.class, article.getId().toString());
					
				}else {
					//存入es中
					IndexQuery query = new IndexQuery();
					//根据子类中id的值，获取到Article对象的数据
					Article art = articleMapper.selectByPrimaryKey(article.getId());
					query.setObject(art);
					
					elasticsearchTemplate.index(query);
				}
				
				
			}
			 
			return result>0;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("修改失败");
		}
	}

	/**
	 * 
	 * 查看文章详情页
	 */
	
	@Override
	public ArticleWithBLOBs selectByPrimaryKey(Integer id) {
		
		ArticleWithBLOBs awb = articleMapper.selectByPrimaryKey(id);
		
		/*//获取原本的访问量
		Integer hits = awb.getHits();
		
		if (hits == null) {
			 hits =0;
		}
		
		//访问量增加1
		
		awb.setHits(hits+1);
		
		//设置数据库
		articleMapper.updateByPrimaryKey(awb);		*/
		
		return awb;
	}

	@Override
	public boolean insertSelective(ArticleWithBLOBs record) {
		try {
			
			return articleMapper.insertSelective(record)>0;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("发布失败");
		}
	}

	@Override
	public PageInfo<Article> selectES(Integer page, Integer pageSize, String key) {
		  //实体类中成员变量如果实体类类型，则将其类对象，存入clazzs中
		  Class [] clazzs =new Class[] {User.class,Category.class,Channel.class};
		  
		  AggregatedPage<Article> selectObjects = ESUtils.selectObjects(elasticsearchTemplate, Article.class, Arrays.asList(clazzs), page -1, pageSize, "id", new String[] {"title"}, key);
		  
		  //获取高亮后的结果
		  List<Article> content = selectObjects.getContent();
		  
		 //创建数据
			Page<Article> page_list = new Page<Article>(page,pageSize);
			
			//设置数据
			page_list.addAll(content);
			
			//设置总条数
			page_list.setTotal(selectObjects.getTotalElements());
			
		  
		  return new PageInfo<Article>(page_list, 3);
	}

}
