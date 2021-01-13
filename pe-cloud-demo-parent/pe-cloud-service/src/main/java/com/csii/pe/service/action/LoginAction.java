package com.csii.pe.service.action;

import com.csii.pe.action.Action;
import com.csii.pe.action.Executable;
import com.csii.pe.core.Context;
import com.csii.pe.core.PeException;
import com.csii.pe.service.common.BaseQueryAction;
import com.csii.pe.service.common.ConfigFields;
import com.csii.pe.service.mapper.UserMapper;
import com.csii.pe.validation.ValidationException;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class LoginAction extends BaseQueryAction {
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private ConfigFields configFields;

	public void execute(Context context) throws PeException {
//		if (1==1) {
//		throw new ValidationException("role.invalid_user");
//	}
		HashMap jsonMap = new HashMap();
		List list = userMapper.queryUser("");
		jsonMap.put("List", list);
		jsonMap.put("configTest", configFields.getConfigTest()+"士大夫但是");
		context.setData("json", jsonMap);
	}
}
