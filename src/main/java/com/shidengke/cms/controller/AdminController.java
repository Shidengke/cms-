package com.shidengke.cms.controller;

import javax.annotation.Resource;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.shidengke.cms.domain.Article;
import com.shidengke.cms.domain.ArticleWithBLOBs;
import com.shidengke.cms.domain.Links;
import com.shidengke.cms.domain.User;
import com.shidengke.cms.service.ArticleService;
import com.shidengke.cms.service.LinksService;
import com.shidengke.cms.service.UserService;
import com.shidengke.cms.utils.Result;
import com.shidengke.cms.utils.ResultUtil;
import com.github.pagehelper.PageInfo;

@RequestMapping("admin")
@Controller
public class AdminController {

	@Resource
	private ArticleService articleService;

	@Resource
	private UserService userService;
	
	@Resource
	private LinksService linksService;
	/**
	 * 
	 * @Title: selects 
	 * @Description: 维护友情链接--列表
	 * @return
	 * @return: String
	 */
	@GetMapping("links/selects")
	public String selects(Model model,@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "3") Integer pageSize) {
		PageInfo<Links> info = linksService.selects(page, pageSize);	
		model.addAttribute("info", info);
		return "admin/links/links";
	}
	/**
	 * 
	 * @Title: add 
	 * @Description: 跳转到增加友情链接页面
	 * @return
	 * @return: String
	 */
    @GetMapping("links/add")
	public String add() {
		return "admin/links/add";
		
	}
	
	
	

	@SuppressWarnings("unchecked")
	@ResponseBody
	@PostMapping("links/add")
	public Result<Links> add(Links links){
		linksService.insert(links);
		return ResultUtil.success();
	}

	@RequestMapping("user/users")
	public String users(Model model, User user, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "3") Integer pageSize) {

		PageInfo<User> info = userService.selects(user, page, pageSize);

		model.addAttribute("users", info.getList());
		model.addAttribute("user", user);
		model.addAttribute("info", info);
		/*
		 * String url="/users"; if(user.getUsername()!=null) {
		 * url+="?username="+user.getUsername(); }
		 */
		// String pages = PageUtil.page(page, info.getPages(), url, pageSize);

		// model.addAttribute("pages", pages);
		int[] nums = info.getNavigatepageNums();
		model.addAttribute("nums", nums);
		return "admin/user/users";

	}

	/**
	 * 
	 * @Title: update
	 * @Description: 修改改用
	 * @return
	 * @return: boolean
	 */
	@RequestMapping("user/update")
	@ResponseBody
	public Result<User> update(User user) {
		userService.update(user);//执行修改
		return ResultUtil.success();

	}

	/**
	 * 
	 * 
	 * @Title: detail
	 * @Description: 查询文章详情
	 * @param id
	 * @return
	 * @return: String
	 */
	@RequestMapping("article/article")
	public String detail(Model model, Integer id) {
		ArticleWithBLOBs article = articleService.selectByPrimaryKey(id);
		model.addAttribute("article", article);
		return "/admin/article/article";
	}

	/**
	 * 文章列表
	 * 
	 * @Title: articles
	 * @Description: TODO
	 * @param model
	 * @param article
	 * @param page
	 * @param pageSize
	 * @return
	 * @return: String
	 */
	@RequestMapping("article/articles")
	public String articles(Model model, Article article, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "3") Integer pageSize) {
	
		PageInfo<Article> info = articleService.selects(article, page, pageSize);
		model.addAttribute("info", info);// 封装的查询结国
		model.addAttribute("article", article);// 封装的查询

		return "admin/article/articles";
	}

	/**
	 * 修改文章
	 * 
	 * @Title: update
	 * @Description: TODO
	 * @param article
	 * @return
	 * @return: boolean
	 */
	@ResponseBody
	@RequestMapping("article/update")
	public Result<Article> update(ArticleWithBLOBs article) {
		articleService.update(article);
		return  ResultUtil.success();
	}

	/**
	 * 
	 * @Title: index
	 * @Description: 后台首页
	 * @return
	 * @return: String
	 */
	@RequestMapping(value = { "", "/", "index" })
	public String index() {

		return "admin/index";

	}

}
