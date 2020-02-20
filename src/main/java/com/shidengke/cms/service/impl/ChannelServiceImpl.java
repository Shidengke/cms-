package com.shidengke.cms.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shidengke.cms.dao.ChannelMapper;
import com.shidengke.cms.domain.Channel;
import com.shidengke.cms.service.ChannelService;

@Service
public class ChannelServiceImpl implements ChannelService {
	@Resource
	private ChannelMapper channelMapper;

	@Override
	public List<Channel> selects() {
		return channelMapper.selects();
	}

}
