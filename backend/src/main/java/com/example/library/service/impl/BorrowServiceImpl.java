package com.example.library.service.impl;

import com.example.library.auth.AuthContext;
import com.example.library.auth.LoginUser;
import com.example.library.common.BusinessException;
import com.example.library.dto.BorrowCreateRequest;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.SysUser;
import com.example.library.mapper.BookMapper;
import com.example.library.mapper.BorrowMapper;
import com.example.library.mapper.UserMapper;
import com.example.library.service.BorrowService;
import com.example.library.vo.BookDetailVO;
import com.example.library.vo.BorrowRecordVO;
import com.example.library.vo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowServiceImpl implements BorrowService {

    private static final Integer ACCOUNT_STATUS_NORMAL = 1;
    private static final Integer BOOK_STATUS_AVAILABLE = 0;
    private static final Integer BOOK_STATUS_BORROWED = 1;
    private static final Integer BORROW_STATUS_NOT_RETURNED = 0;
    private static final Integer BORROW_STATUS_RETURNED = 1;
    private static final Integer BORROW_STATUS_OVERDUE = 2;
    private static final Integer ROLE_READER = 1;

    private final BorrowMapper borrowMapper;
    private final UserMapper userMapper;
    private final BookMapper bookMapper;

    public BorrowServiceImpl(BorrowMapper borrowMapper, UserMapper userMapper, BookMapper bookMapper) {
        this.borrowMapper = borrowMapper;
        this.userMapper = userMapper;
        this.bookMapper = bookMapper;
    }

    @Override
    public List<BorrowRecordVO> listBorrowRecords(Integer userId, Integer status) {
        return borrowMapper.selectBorrowRecords(resolveReadableUserId(userId), status);
    }

    @Override
    public PageResult<BorrowRecordVO> listBorrowRecordsPage(Integer userId, Integer status, Integer page, Integer pageSize) {
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);
        int offset = (safePage - 1) * safePageSize;
        Integer readableUserId = resolveReadableUserId(userId);
        long total = borrowMapper.countBorrowRecords(readableUserId, status);
        List<BorrowRecordVO> records = borrowMapper.selectBorrowRecordsPage(readableUserId, status, offset, safePageSize);
        return new PageResult<>(safePage, safePageSize, total, records);
    }

    @Override
    @Transactional
    public Integer borrowBook(BorrowCreateRequest request) {
        Integer borrowerId = resolveBorrowerId(request.getUserId());
        SysUser user = userMapper.selectUserEntityById(borrowerId);
        if (user == null) {
            throw new BusinessException("user not found");
        }
        if (!ACCOUNT_STATUS_NORMAL.equals(user.getAccountStatus())) {
            throw new BusinessException("user account is disabled");
        }

        BookDetailVO book = bookMapper.selectBookById(request.getBookId());
        if (book == null) {
            throw new BusinessException("book not found");
        }
        if (!BOOK_STATUS_AVAILABLE.equals(book.getBookStatus())) {
            throw new BusinessException("book is not available");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueReturnTime = request.getDueReturnTime() == null ? now.plusDays(30) : request.getDueReturnTime();
        if (!dueReturnTime.isAfter(now)) {
            throw new BusinessException("due return time must be later than borrow time");
        }

        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setUserId(borrowerId);
        borrowRecord.setBookId(request.getBookId());
        borrowRecord.setBorrowTime(now);
        borrowRecord.setDueReturnTime(dueReturnTime);
        borrowRecord.setBorrowStatus(BORROW_STATUS_NOT_RETURNED);

        borrowMapper.insertBorrowRecord(borrowRecord);
        bookMapper.updateBookStatus(request.getBookId(), BOOK_STATUS_BORROWED);
        return borrowRecord.getBorrowId();
    }

    @Override
    @Transactional
    public void returnBook(Integer borrowId) {
        BorrowRecord borrowRecord = borrowMapper.selectBorrowRecordById(borrowId);
        if (borrowRecord == null) {
            throw new BusinessException("borrow record not found");
        }
        LoginUser loginUser = AuthContext.get();
        if (isReader(loginUser) && !loginUser.getUserId().equals(borrowRecord.getUserId())) {
            throw new BusinessException("borrow record not found");
        }
        Integer status = borrowRecord.getBorrowStatus();
        if (!BORROW_STATUS_NOT_RETURNED.equals(status) && !BORROW_STATUS_OVERDUE.equals(status)) {
            throw new BusinessException("borrow record has already been returned");
        }

        borrowMapper.returnBorrowRecord(borrowId, LocalDateTime.now());
        bookMapper.updateBookStatus(borrowRecord.getBookId(), BOOK_STATUS_AVAILABLE);
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private Integer resolveReadableUserId(Integer requestedUserId) {
        LoginUser loginUser = AuthContext.get();
        if (isReader(loginUser)) {
            return loginUser.getUserId();
        }
        return requestedUserId;
    }

    private Integer resolveBorrowerId(Integer requestedUserId) {
        LoginUser loginUser = AuthContext.get();
        if (isReader(loginUser)) {
            return loginUser.getUserId();
        }
        return requestedUserId;
    }

    private boolean isReader(LoginUser loginUser) {
        return loginUser != null && ROLE_READER.equals(loginUser.getRoleId());
    }
}
