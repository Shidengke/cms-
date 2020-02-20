package com.shidengke.cms.es;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.shidengke.cms.domain.Article;
import com.shidengke.cms.domain.ArticleWithBLOBs;
import com.shidengke.cms.domain.User;
import com.shidengke.cms.utils.ESUtils;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-beans.xml")
public class EsTest {

	@Resource
	private ElasticsearchTemplate elasticsearchTemplate;
	
	//分页模糊
	  @Test 
	  public void testFindPageandMH() {
		  //封装模糊数据
		  QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery("我", "title");
         //封装分页数据
		  Pageable pageable = PageRequest.of(0, 5, Sort.by(Direction.ASC, "id")) ;
		//封装查询条件
		 SearchQuery query = new NativeSearchQueryBuilder().withPageable(pageable).withQuery(queryBuilder).build();
		  
		 AggregatedPage<Article> queryForPage = elasticsearchTemplate.queryForPage(query, Article.class);
		 
		 List<Article> list = queryForPage.getContent();
		 
		   for (Article article : list) {
			System.out.println(article);
		} 
	  }
	
	
	//高亮
	@Test
	public void testHighLight() {
		//实体类对象
		  Class[] clazzs = new Class[] {Article.class,ArticleWithBLOBs.class,User.class};
		  AggregatedPage<Article> selectObjects = ESUtils.selectObjects(elasticsearchTemplate, Article.class, Arrays.asList(clazzs), 0, 5, "id", new String[] {"title"}, "一二三");
		  List<Article> content = selectObjects.getContent();
		   for (Article article : content) {
			   System.out.println(article);
		}
		  
		  
	}
}
