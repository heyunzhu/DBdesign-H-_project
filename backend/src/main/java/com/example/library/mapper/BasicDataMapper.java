package com.example.library.mapper;

import com.example.library.vo.AuthorVO;
import com.example.library.vo.BookTypeVO;
import com.example.library.vo.RoleVO;

import java.util.List;

public interface BasicDataMapper {

    List<AuthorVO> selectAuthors();

    List<BookTypeVO> selectBookTypes();

    List<RoleVO> selectRoles();
}
