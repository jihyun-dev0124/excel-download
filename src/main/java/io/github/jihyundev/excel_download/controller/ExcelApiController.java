package io.github.jihyundev.excel_download.controller;

import io.github.jihyundev.excel_download.domain.excel.ExcelJob;
import io.github.jihyundev.excel_download.dto.ExcelJobDto;
import io.github.jihyundev.excel_download.domain.excel.ExcelJobStatus;
import io.github.jihyundev.excel_download.repository.ExcelJobRepository;
import io.github.jihyundev.excel_download.service.ExcelJobAsyncService;
import io.github.jihyundev.excel_download.service.ExcelJobService;
import io.github.jihyundev.excel_download.infra.S3StorageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/excel")
public class ExcelApiController {
    private final ExcelJobRepository excelJobRepository;
    private final ExcelJobAsyncService excelJobAsyncService;
    private final ExcelJobService excelJobService;
    private final S3StorageClient s3StorageClient;

    /**
     * 회원 엑셀 다운로드 - JPA
     * @param requestedBy
     * @return
     */
    @PostMapping("/members")
    public ResponseEntity<Long> requestMemberExcel(@RequestParam String requestedBy){
        ExcelJob job = excelJobService.saveExcelJob("MEMBERS_JPA", requestedBy);

        // 비동기 엑셀 다운로드
        excelJobAsyncService.generateMemberExcel(job.getId());

        return ResponseEntity.ok(job.getId());
    }

    /**
     * 회원 엑셀 다운로드 - Mybatis
     * @param requestedBy
     * @return
     */
    @PostMapping("/members/ver2")
    public ResponseEntity<Long> requestMemberExcelByMybatis(@RequestParam String requestedBy){
        ExcelJob job = excelJobService.saveExcelJob("MEMBERS_Mybatis", requestedBy);

        // 비동기 엑셀 다운로드
        excelJobAsyncService.generateMemberExcelByMybatis(job.getId());

        return ResponseEntity.ok(job.getId());
    }

    /**
     * 엑셀 다운로드 상태 조회
     * @param jobId
     * @return
     */
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ExcelJobDto> getExcelJobStatus(@PathVariable Long jobId){
        ExcelJobDto excelJob = excelJobService.getJob(jobId);
        return ResponseEntity.ok(excelJob);
    }

    /**
     * 엑셀 다운로드 목록
     * @param pageable
     * @return
     */
    @GetMapping("/list")
    public Page<ExcelJobDto> excelList(@PageableDefault(size = 20) Pageable pageable) {
        return excelJobRepository.excelJobPagination(pageable);
    }

    @GetMapping("/{jobId}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long jobId) {
        try {
            ExcelJobDto job = excelJobService.getJob(jobId);
            if(!job.getStatus().equals(ExcelJobStatus.COMPLETED)){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일 생성이 완료되지 않았습니다.");
            }
            String key = job.getFilePath();
            byte[] fileBytes = s3StorageClient.downloadFile(key);
            String fileName = job.getFileName()+".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileBytes);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, !e.getMessage().isEmpty() ? e.getMessage() :  "파일이 존재하지 않습니다.");
        }
    }

}
