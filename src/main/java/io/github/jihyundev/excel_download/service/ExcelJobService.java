package io.github.jihyundev.excel_download.service;

import io.github.jihyundev.excel_download.entity.ExcelJob;
import io.github.jihyundev.excel_download.repository.ExcelJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExcelJobService {
    private final ExcelJobRepository excelJobRepository;

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


}
