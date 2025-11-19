package io.github.jihyundev.excel_download.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.jihyundev.excel_download.dto.MemberDto;
import io.github.jihyundev.excel_download.dto.QMemberDto;
import io.github.jihyundev.excel_download.domain.member.Member;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static io.github.jihyundev.excel_download.domain.member.QMember.*;

public class MemberRepositoryImpl extends QuerydslRepositorySupport implements MemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        super(Member.class);
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<MemberDto> memberPagination(Pageable pageable) {

        List<MemberDto> content = queryFactory
                .select(new QMemberDto(
                        member.id,
                        member.username,
                        member.realName,
                        member.status,
                        member.phone,
                        member.email))
                .from(member)
                .orderBy(member.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }
}
