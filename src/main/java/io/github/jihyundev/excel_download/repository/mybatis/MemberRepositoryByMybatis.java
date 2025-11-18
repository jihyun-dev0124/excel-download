package io.github.jihyundev.excel_download.repository.mybatis;

import io.github.jihyundev.excel_download.entity.Member;
import io.github.jihyundev.excel_download.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryByMybatis {
    private final MemberMapper memberMapper;

    public int memberCount(){
        return memberMapper.count();
    }

    public Cursor<Member> cursorAll() {
        return memberMapper.cursorAll();
    }
}
