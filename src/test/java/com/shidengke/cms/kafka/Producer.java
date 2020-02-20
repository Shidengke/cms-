package com.shidengke.cms.kafka;

import java.io.File;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import com.shidengke.cms.domain.ArticleWithBLOBs;
import com.shidengke.cms.domain.Category;
import com.shidengke.cms.domain.Channel;
import com.shidengke.cms.service.CategoryService;
import com.shidengke.cms.service.ChannelService;
import com.shidengke.common.utils.DateUtil;
import com.shidengke.common.utils.RandomUtil;
import com.shidengke.common.utils.StreamUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-beans.xml")
public class Producer {

	@Resource
	private KafkaTemplate<String, String> kafkaTemplate;
	@Resource
	private ChannelService channelService;
	@Resource
	private CategoryService categoryService;
	
	@Test
	public void titleTest() {
		//读取文章
				//文件夹
				File dir = new File("D:\\1707EJsoup");
				
				//获取文件的数组
				File[] listFiles = dir.listFiles();
				
				//循环遍历
				for (File file : listFiles) {
					//读取file数据，获取文章内容
					String content = StreamUtil.readTextFile(file);
					
					//获取标题
					String name = file.getName().replace(".txt", "");
					
					//封装数据
					ArticleWithBLOBs article = new ArticleWithBLOBs();
					
					article.setTitle(name);
					article.setContent(content);
					
                   //截取前140个字作为摘要
					String summary = content;
					if(content.length() > 140) {
						summary = content.substring(0, 140);
					}
					
					article.setSummary(summary);
					
					//点击量
					article.setHits(RandomUtil.random(0, 10000));
					
					//是否热门
					article.setHot(RandomUtil.random(0, 1));
					
					//栏目
					List<Channel> channels = channelService.selects();
					//随机获取一个下标
					int random = RandomUtil.random(0, channels.size() - 1);
					//再根据下标取出对应的值
					Channel channel = channels.get(random);
					
					article.setChannelId(channel.getId());
					
					//获取指定栏目下的类别
					List<Category> categories = categoryService.selectsByChannelId(channel.getId());
					
					if(categories != null ) {
						
						//随机下标
						int random2 = RandomUtil.random(0, categories.size() - 1);
						
						//获取类别的id
						Category category = categories.get(random2);
						
						article.setCategoryId(category.getId());
					}
					//文章发布日期从2019年1月1日模拟到今天
			
					
					article.setCreated(DateUtil.randomDateString("2019-01-01", "2019-12-18"));

					article.setStatus(0);
					article.setDeleted(0);
					article.setContentType(0);
					
				System.out.println(article);
					
					//转换成json字符串
					String json = JSON.toJSONString(article);
					
					//发送到kafka
					kafkaTemplate.sendDefault("article_add", json);
				}
		   
	}
}
