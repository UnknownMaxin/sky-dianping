package com.maxin.mapper;

import com.maxin.entity.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    @Select("select * from tb_user_info where tb_user_info.user_id = #{userId}")
    UserInfo getById(Long userId);
}
