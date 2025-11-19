package io.github.jihyundev.excel_download.repository;

import io.github.jihyundev.excel_download.domain.member.Member;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.stream.Stream;


public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, QuerydslPredicateExecutor<Member> {
    @QueryHints(value={
            @QueryHint(name=org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, value = "1000"),
            @QueryHint(name=org.hibernate.jpa.QueryHints.HINT_READONLY, value = "true")
    })
    @Query("SELECT m FROM Member m")
    Stream<Member> streamAll();
}
