package io.github.jihyundev.excel_download.mapper;

import io.github.jihyundev.excel_download.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.cursor.Cursor;

@Mapper
public interface MemberMapper {
    int count();
    Cursor<Member> cursorAll();
}
