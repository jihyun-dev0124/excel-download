package io.github.jihyundev.excel_download.service;

import io.github.jihyundev.excel_download.dto.ExcelJobDto;
import io.github.jihyundev.excel_download.entity.ExcelJob;
import io.github.jihyundev.excel_download.enums.ExcelJobStatus;
import io.github.jihyundev.excel_download.repository.ExcelJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExcelJobService {
    private final ExcelJobRepository excelJobRepository;

    public Long saveExcelJob(String jobType, String requestedBy){
        ExcelJob excelJob = ExcelJob.builder()
                .jobType(jobType)
                .status(ExcelJobStatus.PENDING)
                .fileName(jobType+"_"+dateToString())
                .requestedBy(requestedBy)
                .build();

        ExcelJob save = excelJobRepository.save(excelJob);
        return save.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRunning(Long jobId) {
        ExcelJob job = excelJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("job not found: " + jobId));
        job.markRunning();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProgress(Long jobId, int progress) {
        ExcelJob job = excelJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("job not found: " + jobId));
        job.updateProgress(progress);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long jobId, String fiilePath) {
        ExcelJob job = excelJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("job not found: " + jobId));
        job.completed(fiilePath);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long jobId, Throwable e) {
        ExcelJob job = excelJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("job not found: " + jobId));
        job.fail(e.getMessage());
    }

    public ExcelJobDto getJob(Long jobId){
        ExcelJob job = excelJobRepository.findById(jobId).orElseThrow(() -> new IllegalArgumentException("job not found: " + jobId));
        return new ExcelJobDto(job);
    }

    private String dateToString() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return now.format(formatter);
    }
}
