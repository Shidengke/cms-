package com.shidengke.cms.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.elasticsearch.http.HttpTransportSettings;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.shidengke.cms.domain.Article;
import com.shidengke.cms.domain.ArticleWithBLOBs;
import com.shidengke.cms.domain.Category;
import com.shidengke.cms.domain.Channel;
import com.shidengke.cms.domain.Collect;
import com.shidengke.cms.domain.Links;
import com.shidengke.cms.domain.User;
import com.shidengke.cms.service.ArticleService;
import com.shidengke.cms.service.CategoryService;
import com.shidengke.cms.service.ChannelService;
import com.shidengke.cms.service.CollectService;
import com.shidengke.cms.service.LinksService;
import com.shidengke.cms.utils.ArticleEnum;
import com.shidengke.cms.utils.Result;
import com.shidengke.cms.utils.ResultUtil;
import com.shidengke.cms.vo.ArticleVO;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@Controller
public class IndexController {
	@Resource
	private ChannelService channelService;// 栏目

	@Resource
	private ArticleService articleService;// 文章

	@Resource
	private CategoryService categoryService;// 分类
	@Resource
	private LinksService linksService;// 分类
	@Resource
	private CollectService collectService;//收藏	
	@Resource
	private KafkaTemplate<String, String> kafkaTemplate;
	@Resource
	private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private ThreadPoolTaskExecutor executor;//线程池
	/**
	 * 
	 * @Title: index
	 * @Description: 首页
	 * @param article
	 * @param model
	 * @param page
	 * @param pageSize
	 * @return
	 * @return: String
	 */
	@RequestMapping(value = { "", "/", "index" })
	public String index(Article article, Model model, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "5") Integer pageSize,String key) {
		// 访问方法开始时间
		long s1 = System.currentTimeMillis();

		article.setStatus(1);// 显示审审核过的文章
		article.setDeleted(0);// 查询未删除的
		article.setContentType(ArticleEnum.HTML.getCode());
		Thread t1 = null;
		Thread t2 = null;
		Thread t3 = null;
		Thread t4 = null;
		Thread t5 = null;
		Thread t6 = null;

		// 查询出左侧栏目
		t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				List<Channel> channels = channelService.selects();
				model.addAttribute("channels", channels);

			}
		});

		t2 = new Thread(new Runnable() {

			@Override
			public void run() {

				if (key!=null &&!key.trim().equals("")) {
					//如果搜索条件不为空，则查询es,进行高亮显示
					PageInfo<Article> info = articleService.selectES(page, pageSize,key);
					model.addAttribute("info", info);
					model.addAttribute("key", key);
				}else {
					//如果搜索条件为空，则显示热门文章			
				// 如果栏目为空则默认显示热点
				if (article.getChannelId() == null) {
					// 查询热点文章的列表
					Article hot = new Article();
					hot.setStatus(1);// 审核过的
					hot.setHot(1);// 热点文章
					hot.setDeleted(0);//
					hot.setContentType(ArticleEnum.HTML.getCode());
					PageInfo<Article> info = articleService.selectHot(hot, page, pageSize);
					model.addAttribute("info", info);
				}
				}
			}
		});

		t3 = new Thread(new Runnable() {

			@Override
			public void run() {
				// 显示分类文章
				if (article.getChannelId() != null) {
					// 1查询出来栏目下分类
					List<Category> categorys = categoryService.selectsByChannelId(article.getChannelId());
					model.addAttribute("categorys", categorys);
					// 2.显示分类下的文章
					PageInfo<Article> info = articleService.selects(article, page, pageSize);
					model.addAttribute("info", info);
				}
			}
		});
		t4 = new Thread(new Runnable() {

			@Override
			public void run() {
				
				// 右侧边栏显示最新的5遍文章
				Article lastArticle = new Article();
				lastArticle.setStatus(1);// 审核通过的
				lastArticle.setDeleted(0);
				lastArticle.setContentType(ArticleEnum.HTML.getCode());
				;//
				PageInfo<Article> lastInfo = articleService.selectLast(lastArticle, 1, 5);
				model.addAttribute("lastInfo", lastInfo);

			}
		});
		
		t5 =new Thread(new Runnable() {
			
			@Override
			public void run() {
				// 右侧边栏显示最新的5遍文章

				Article picArticle = new Article();
				picArticle.setStatus(1);// 审核通过的
				picArticle.setDeleted(0);
				picArticle.setContentType(ArticleEnum.IMAGE.getCode());
				PageInfo<Article> picInfo = articleService.selects(picArticle, 1, 5);
				model.addAttribute("picInfo", picInfo);
				
			}
		});
		
		// 查询出左侧栏目
		t6 = new Thread(new Runnable() {
			@Override
			public void run() {
				PageInfo<Links> info = linksService.selects(1, 10);
				model.addAttribute("linksInfo", info);

			}
		});
		
		// 封装查询条件
		model.addAttribute("article", article);
		//开启线程
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		t6.start();
		
		try {
			//让子线程先运行.主线程最后运行.返回首页
			t1.join();
			t2.join();
			t3.join();
			t4.join();
			t5.join();
			t6.join();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		long s2 = System.currentTimeMillis();
		System.out.println("用时:->" + (s2 - s1));
		
		return "index/index";
/*		long s1=System.currentTimeMillis();
		article.setStatus(1);
		article.setDeleted(0);
		article.setContentType(ArticleEnum.HTML.getCode());
		
		Thread t1 = null;
		Thread t2 = null;
		Thread t3 = null;
		Thread t4 = null;
		Thread t5 = null;
		Thread t6 = null;
		
		t1 =new Thread(new Runnable() {
			@Override
			 public void run() {
				  List<Channel> channels = channelService.selects();
				  model.addAttribute("channels", channels);
				  
			 }
		});
		
		t2 =new Thread(new Runnable() {
			@Override
			 public void run() {
				  if (key!=null&&key.trim().equals("")) {
					  PageInfo<Article> info = articleService.selectES(page, pageSize, key);
					  model.addAttribute("info", info);
					  model.addAttribute("key", key);
					
				}else {
					if (article.getChannelId()!=null) {
						Article hot = new Article();
						hot.setStatus(1);
						hot.setHot(1);
						hot.setDeleted(0);
						PageInfo<Article> info = articleService.selectHot(article, page, pageSize);
						model.addAttribute("info", info);
						
					}
				}
				  
			 }
		});
		
		t3 =new Thread(new Runnable() {
			@Override
			 public void run() {
				 if (article.getChannelId()!=null) {
					List<Category> channelId = categoryService.selectsByChannelId(article.getChannelId());
					model.addAttribute("channelId", channelId);
					PageInfo<Article> info = articleService.selects(article, page, pageSize);
					model.addAttribute("info", info);
					
					
				}
				  
			 }
		});
		
	
		
		
		t4 =new Thread(new Runnable() {
			@Override
			 public void run() {
			     Article lastarticle = new Article();
			     lastarticle.setStatus(1);
			     lastarticle.setDeleted(0);
			     lastarticle.setContentType(ArticleEnum.HTML.getCode());
			     PageInfo<Article> lastinfo = articleService.selects(lastarticle, page, pageSize);
			     model.addAttribute("lastinfo", lastinfo);
			}
		 });
		
		
		t5 =new Thread(new Runnable() {
			@Override
			 public void run() {
			     Article picarticle = new Article();
			     picarticle.setStatus(1);
			     picarticle.setDeleted(0);
			     picarticle.setContentType(ArticleEnum.HTML.getCode());
			     PageInfo<Article> picinfo = articleService.selects(picarticle, page, pageSize);
			     model.addAttribute("picinfo", picinfo);
			}
		 });
		
		
		t6 =new Thread(new Runnable() {
			@Override
			 public void run() {
			     PageInfo<Links> info = linksService.selects(1, 10);
			     model.addAttribute("linkesinfo", info);
			}
		 });
		
		model.addAttribute("article", article);
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		t6.start();
		
		try {
			t1.join();
			t2.join();
			t3.join();
			t4.join();
			t5.join();
			t6.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		
			}
		long s2 = System.currentTimeMillis();
		System.out.println("用时:=============="+(s2-s1));
		return "index/index";*/
	}

	/**
	 * 
	 * @Title: article
	 * @Description: 文章详情
	 * @param model
	 * @return
	 * @return: String
	 */
	@RequestMapping("article")
	public String article(Model model, Integer id,HttpServletRequest request) {
		ArticleWithBLOBs article = articleService.selectByPrimaryKey(id);
		
		//创建key键 转换成json字符串发送到kafka中
		kafkaTemplate.sendDefault("update_hits", id.toString());
		//获取session方法
		HttpSession session = request.getSession();
		if (null!=session) {
			//得到user对象
			User user = (User)session.getAttribute("user");
			//获取文本内容
			int i = collectService.selectByText(article.getTitle(), user);
			model.addAttribute("isCollect", i);
		}
		//传到前台值
	    model.addAttribute("article", article);
		//返回article
		return "index/article";
		
	}

	
	/**
	 * 
	 * @Title: article
	 * @Description: 文章详情
	 * @param model
	 * @return
	 * @return: String
	 */
	@RequestMapping("articlepic")
	public String articlepic(Model model, Integer id) {

		ArticleWithBLOBs article = articleService.selectByPrimaryKey(id);

		String string = article.getContent();

		
		
		ArrayList<ArticleVO> list = new ArrayList<ArticleVO>();
		
		Gson gson = new Gson();
		JsonArray array = new JsonParser().parse(string).getAsJsonArray();
		for (JsonElement jsonElement : array) {
			//把json转为java
			ArticleVO vo = gson.fromJson(jsonElement, ArticleVO.class);
			list.add(vo);
		}
		model.addAttribute("title", article.getTitle());// 标题
		model.addAttribute("list", list);// 标题包含的 图片的地址和描述
		return "index/articlepic";
	}

	
	
	/**
	 * 
	 * @Title: collect 
	 * @Description: 收藏
	 * @param collect
	 * @return
	 * @return: Result<Collect>
	 */
/*	@ResponseBody
	@PostMapping("/collect")
	public  Result<Collect> collect(Collect collect,HttpServletRequest request){
		
		HttpSession session = request.getSession(false);
		if(null==session) {
	    return ResultUtil.error(1, "收藏失败,登录可能过期");
		}
		User user = (User) session.getAttribute("user");
		if(null==user) {
		return	ResultUtil.error(1, "收藏失败,登录可能过期");
		}
		collect.setUser(user);
		collectService.insert(collect);
		return ResultUtil.success();
	}*/
	@ResponseBody
	@PostMapping("/collect")
	public Result<Collect> collect(Collect collect,HttpServletRequest request){
		HttpSession session = request.getSession(false);
		if (null==session) {
			return ResultUtil.error(1, "收藏失败");		
		}
		User user=(User)session.getAttribute("user");
		if (null==user) {
			return ResultUtil.error(1, "收藏失败");
		}
		collect.setUser(user);
		collectService.insert(collect);
		return ResultUtil.success();
	}
}
