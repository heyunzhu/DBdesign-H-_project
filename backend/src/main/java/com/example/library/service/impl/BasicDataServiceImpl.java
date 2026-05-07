package com.example.library.service.impl;

import com.example.library.mapper.BasicDataMapper;
import com.example.library.service.BasicDataService;
import com.example.library.vo.AuthorVO;
import com.example.library.vo.BookTypeVO;
import com.example.library.vo.RoleVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BasicDataServiceImpl implements BasicDataService {

    private final BasicDataMapper basicDataMapper;

    public BasicDataServiceImpl(BasicDataMapper basicDataMapper) {
        this.basicDataMapper = basicDataMapper;
    }

    @Override
    public List<AuthorVO> listAuthors() {
        return basicDataMapper.selectAuthors();
    }

    @Override
    public List<BookTypeVO> listBookTypes() {
        return basicDataMapper.selectBookTypes();
    }

    @Override
    public List<RoleVO> listRoles() {
        return basicDataMapper.selectRoles();
    }
}
