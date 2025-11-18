package io.github.jihyundev.excel_download.service;

import io.github.jihyundev.excel_download.repository.ExcelJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExcelService {
    private final ExcelJobAsyncService excelJobAsyncService;
    private final ExcelJobRepository excelJobRepository;

    public void createdMemberExcel(Long jobId){
        excelJobAsyncService.generateMemberExcel(jobId);
    }
}
