package io.github.jihyundev.excel_download.service;

import io.github.jihyundev.excel_download.domain.excel.ExcelJob;
import io.github.jihyundev.excel_download.domain.member.Member;
import io.github.jihyundev.excel_download.infra.S3StorageClient;
import io.github.jihyundev.excel_download.repository.ExcelJobRepository;
import io.github.jihyundev.excel_download.repository.MemberRepository;
import io.github.jihyundev.excel_download.repository.mybatis.MemberRepositoryByMybatis;
import io.github.jihyundev.excel_download.infra.ExcelGenerator;
import io.github.jihyundev.excel_download.infra.ExcelWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExcelJobAsyncService {
    private final ExcelJobRepository excelJobRepository;
    private final ExcelJobService excelJobService;
    private final ExcelGenerator excelGenerator;

    /**
     * JPA Stream
     * @param jobId
     */
    @Async("excelTaskExecutor")
    public void generateMemberExcel(Long jobId){
        ExcelJob job = excelJobRepository.findById(jobId).orElse(null);
        if(job == null){
            log.error("[generateMemberExcel] 존재하지 않는 jobId={} 로 요청 들어옴", jobId);
            return;
        }

        try {
            excelJobService.markRunning(jobId);
            String s3Key = excelGenerator.generateMemberExcelFile(jobId);
            excelJobService.complete(jobId, s3Key);
        } catch (Exception e) {
            excelJobService.fail(jobId, e);
        }
    }

    /**
     * Mybatis Cursor
     */
    @Async("excelTaskExecutor")
    @Transactional(readOnly = true)
    public void generateMemberExcelByMybatis(Long jobId) {
        ExcelJob job = excelJobRepository.findById(jobId).orElse(null);
        if(job == null){
            log.error("[generateMemberExcel] 존재하지 않는 jobId={} 로 요청 들어옴", jobId);
            return;
        }

        try {
            excelJobService.markRunning(jobId);
            String s3Key = excelGenerator.generateMemberExcelFileByMybatis(jobId);
            excelJobService.complete(jobId, s3Key);
        } catch (Exception e) {
            excelJobService.fail(jobId, e);
        }
    }

}
