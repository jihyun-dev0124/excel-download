package io.github.jihyundev.excel_download.repository;

import io.github.jihyundev.excel_download.entity.ExcelJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExcelJobRepository extends JpaRepository<ExcelJob, Long> {

}
