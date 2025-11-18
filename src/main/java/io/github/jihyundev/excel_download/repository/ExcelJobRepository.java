package io.github.jihyundev.excel_download.repository;

import io.github.jihyundev.excel_download.entity.ExcelJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ExcelJobRepository extends JpaRepository<ExcelJob, Long>, ExcelJobRepositoryCustom, QuerydslPredicateExecutor<ExcelJob> {

}
