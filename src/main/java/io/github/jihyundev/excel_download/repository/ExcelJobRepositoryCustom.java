package io.github.jihyundev.excel_download.repository;

import io.github.jihyundev.excel_download.dto.ExcelJobDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ExcelJobRepositoryCustom {
    Page<ExcelJobDto> excelJobPagination(Pageable pageable);
}
