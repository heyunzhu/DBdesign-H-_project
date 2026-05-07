package com.example.library.mapper;

import com.example.library.entity.BorrowRecord;
import com.example.library.vo.BorrowRecordVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BorrowMapper {

    List<BorrowRecordVO> selectBorrowRecords(@Param("userId") Integer userId, @Param("status") Integer status);

    BorrowRecord selectBorrowRecordById(@Param("borrowId") Integer borrowId);

    int insertBorrowRecord(BorrowRecord borrowRecord);

    int returnBorrowRecord(@Param("borrowId") Integer borrowId,
                           @Param("actualReturnTime") LocalDateTime actualReturnTime);
}
