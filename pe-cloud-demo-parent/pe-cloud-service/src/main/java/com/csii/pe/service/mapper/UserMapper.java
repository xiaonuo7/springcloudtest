package com.csii.pe.service.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.csii.pe.service.bean.POCUser;

@Mapper
public interface UserMapper {
	
	public List<POCUser> queryUser(@Param("userId")String userId);
	
}
