package com.csii.pe.gateway.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.csii.pe.gateway.model.LoginUser;


@Mapper
public interface UserMapper {
	
	public List<LoginUser> queryUser(@Param("userName")String userName,@Param("password")String password);
	
}
