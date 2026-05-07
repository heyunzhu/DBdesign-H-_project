package com.example.library.service;

import com.example.library.vo.AuthorVO;
import com.example.library.vo.BookTypeVO;
import com.example.library.vo.RoleVO;

import java.util.List;

public interface BasicDataService {

    List<AuthorVO> listAuthors();

    List<BookTypeVO> listBookTypes();

    List<RoleVO> listRoles();
}
