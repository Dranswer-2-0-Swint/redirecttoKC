package com.t3q.dranswer.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.t3q.dranswer.dto.db.LoginHistory;

@Mapper
@Repository
public interface LoginHistoryMapper {
    public List<LoginHistory> getLoginHistoryByUserId(@Param("userId") String userId);
    public int setLoginHistory(@Param("obj") LoginHistory obj);
}
