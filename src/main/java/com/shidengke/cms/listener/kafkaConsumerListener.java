package com.shidengke.cms.listener;

import javax.annotation.Resource;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shidengke.cms.dao.ArticleMapper;
import com.shidengke.cms.domain.ArticleWithBLOBs;
import com.shidengke.cms.service.ArticleService;

@Component
public class kafkaConsumerListener implements MessageListener<String, String>{

	@Resource 
	private ArticleService articleService;
	
	@Resource
	private ArticleMapper mapper;
	
	@Override
	public void onMessage(ConsumerRecord<String, String> data) {
		  
		String key = data.key();
		   if (key!=null && key.equals("article_add")) {
			   String value = data.value();
			   
			   ArticleWithBLOBs article = JSON.parseObject(value, ArticleWithBLOBs.class);
			   
			   articleService.insertSelective(article);
			   
			   System.out.println("存入数据库");
	
			   
			   
		}
		
	}

}
