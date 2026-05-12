package com.example.library.mapper;

import com.example.library.entity.SysUser;
import com.example.library.vo.UserVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    List<UserVO> selectUsers(@Param("keyword") String keyword,
                             @Param("roleId") Integer roleId,
                             @Param("status") Integer status);

    List<UserVO> selectUsersPage(@Param("keyword") String keyword,
                                 @Param("roleId") Integer roleId,
                                 @Param("status") Integer status,
                                 @Param("offset") Integer offset,
                                 @Param("pageSize") Integer pageSize);

    long countUsers(@Param("keyword") String keyword,
                    @Param("roleId") Integer roleId,
                    @Param("status") Integer status);

    UserVO selectUserById(@Param("userId") Integer userId);

    SysUser selectUserEntityById(@Param("userId") Integer userId);

    int insertUser(SysUser user);

    int updateUser(SysUser user);

    int updateUserStatus(@Param("userId") Integer userId, @Param("accountStatus") Integer accountStatus);
}
