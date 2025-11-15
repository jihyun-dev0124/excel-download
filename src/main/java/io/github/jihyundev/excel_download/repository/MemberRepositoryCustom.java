package io.github.jihyundev.excel_download.repository;

import io.github.jihyundev.excel_download.dto.MemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepositoryCustom {
    Page<MemberDto> memberPagination(Pageable pageable);
}
