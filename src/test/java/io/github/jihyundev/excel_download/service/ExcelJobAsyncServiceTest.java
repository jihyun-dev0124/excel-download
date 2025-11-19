package io.github.jihyundev.excel_download.service;

import io.github.jihyundev.excel_download.domain.excel.ExcelJob;
import io.github.jihyundev.excel_download.domain.excel.ExcelJobStatus;
import io.github.jihyundev.excel_download.infra.ExcelGenerator;
import io.github.jihyundev.excel_download.repository.ExcelJobRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@DisplayName("비동기 엑셀 다운로드 테스트")
class ExcelJobAsyncServiceTest {
    @Autowired
    private ExcelJobAsyncService excelJobAsyncService;

    @Autowired
    private ExcelJobService excelJobService;

    @MockBean
    private ExcelGenerator excelGenerator;

    @Autowired
    private ExcelJobRepository excelJobRepository;


    @Test
    void 회원_엑셀_다운로드_성공() throws Exception {
        // given
        ExcelJob job = excelJobService.saveExcelJob("MEMBERS_EXCEL_TEST", "testByJihyun");
        Long jobId = job.getId();

        // Mock: 생성기 호출되면 s3Key 리턴 (실제 파일을 생성하지 않음)
        when(excelGenerator.generateMemberExcelFile(jobId))
                .thenReturn("excel/member/"+job.getFileName()+".xlsx");

        // when
        excelJobAsyncService.generateMemberExcel(jobId);

        //wait: 비동기 기다림
        Thread.sleep(1500);

        // then
        ExcelJob updated = excelJobRepository.findById(jobId).get();

        assertEquals(ExcelJobStatus.COMPLETED, updated.getStatus());
        assertEquals("excel/member/"+job.getFileName()+".xlsx", updated.getFilePath());
    }

    @Test
    void 회원_엑셀_다운로드_실패() throws Exception {
        // given
        ExcelJob job = excelJobService.saveExcelJob("MEMBERS_EXCEL_TEST", "testByJihyun");
        Long jobId = job.getId();

        //Mock : 실패 던지기
        when(excelGenerator.generateMemberExcelFile(jobId))
                .thenThrow(new RuntimeException("테스트 에러"));

        //when
        excelJobAsyncService.generateMemberExcel(jobId);

        //wait
        Thread.sleep(500);

        //then
        ExcelJob updated = excelJobRepository.findById(jobId).get();

        assertEquals(ExcelJobStatus.FAILED, updated.getStatus());
        assertTrue(updated.getErrorMessage().contains("테스트 에러"));
    }

}