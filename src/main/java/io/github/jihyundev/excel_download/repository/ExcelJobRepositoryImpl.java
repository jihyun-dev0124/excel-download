package io.github.jihyundev.excel_download.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.jihyundev.excel_download.dto.ExcelJobDto;
import io.github.jihyundev.excel_download.dto.QExcelJobDto;
import io.github.jihyundev.excel_download.entity.ExcelJob;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static io.github.jihyundev.excel_download.entity.QExcelJob.*;

public class ExcelJobRepositoryImpl extends QuerydslRepositorySupport implements ExcelJobRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public ExcelJobRepositoryImpl(EntityManager em) {
        super(ExcelJob.class);
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<ExcelJobDto> excelJobPagination(Pageable pageable) {
        List<ExcelJobDto> content = queryFactory
                .select(new QExcelJobDto(
                        excelJob.id,
                        excelJob.jobType,
                        excelJob.status,
                        excelJob.progress,
                        excelJob.filePath,
                        excelJob.fileName,
                        excelJob.errorMessage,
                        excelJob.requestedBy,
                        excelJob.createdAt,
                        excelJob.updatedAt,
                        excelJob.completedAt))
                .from(excelJob)
                .orderBy(excelJob.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<ExcelJob> countQuery = queryFactory
                .select(excelJob)
                .from(excelJob);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }
}
