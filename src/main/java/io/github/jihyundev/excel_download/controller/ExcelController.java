package io.github.jihyundev.excel_download.controller;

import io.github.jihyundev.excel_download.entity.ExcelJob;
import io.github.jihyundev.excel_download.enums.ExcelJobStatus;
import io.github.jihyundev.excel_download.repository.ExcelJobRepository;
import io.github.jihyundev.excel_download.service.ExcelJobAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/excel")
public class ExcelController {
    private final ExcelJobRepository excelJobRepository;
    private final ExcelJobAsyncService excelJobAsyncService;


    @PostMapping("/members")
    public ResponseEntity<Long> requestMemberExcel(@RequestParam String requestedBy){
        ExcelJob excelJob = ExcelJob.builder()
                .jobType("MEMBERS")
                .status(ExcelJobStatus.PENDING)
                .requestedBy(requestedBy)
                .build();

        ExcelJob save = excelJobRepository.save(excelJob);

        // 비동기 엑셀 다운로드
        excelJobAsyncService.generateMemberExcel(save.getId());

        return ResponseEntity.ok(save.getId());
    }
}
