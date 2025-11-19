package io.github.jihyundev.excel_download.infra;

import io.github.jihyundev.excel_download.domain.excel.ExcelJob;
import io.github.jihyundev.excel_download.domain.excel.ExcelJobStatus;
import io.github.jihyundev.excel_download.domain.member.Member;
import io.github.jihyundev.excel_download.domain.member.MemberStatus;
import io.github.jihyundev.excel_download.repository.ExcelJobRepository;
import io.github.jihyundev.excel_download.repository.MemberRepository;
import io.github.jihyundev.excel_download.service.ExcelJobAsyncService;
import io.github.jihyundev.excel_download.service.ExcelJobService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.File;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("파일 생성 로직 테스트")
class ExcelGeneratorTest {
    @Autowired
    private ExcelGenerator excelGenerator;

    @Autowired
    private ExcelJobService excelJobService;

    @Autowired
    private ExcelJobAsyncService excelJobAsyncService;

    @Autowired
    private ExcelJobRepository excelJobRepository;

    @MockBean
    private S3StorageClient s3StorageClient;

    @MockBean
    private MemberRepository memberRepository;

    @Test
    void 회원엑셀파일생성_성공(){
        // given
        ExcelJob job = excelJobService.saveExcelJob("MEMBERS_EXCEL_TEST", "testByJihyun");

        // Mock
        when(memberRepository.streamAll())
                .thenReturn(Stream.of(
                        new Member(1L, "user1", "test123", "김개굴", MemberStatus.ACTIVE, "010123456678", "a@a.com"),
                        new Member(2L, "user2", "test123", "멘토스", MemberStatus.ACTIVE, "01099994444", "b@b.com")
                ));

        when(memberRepository.count()).thenReturn(2L);

        //when
        String s3Key = excelGenerator.generateMemberExcelFile(job.getId());

        // then
        assertTrue(s3Key.startsWith("excel/member"));
        verify(s3StorageClient, times(1)).uploadFile(anyString(), any(File.class));
    }

    @Test
    void 회원엑셀파일생성_실패() throws Exception{
        // given
        ExcelJob job = excelJobService.saveExcelJob("MEMBERS_EXCEL_TEST", "testByJihyun");

        //Mock : 실패 던지기
        when(excelGenerator.generateMemberExcelFile(job.getId()))
                .thenThrow(new RuntimeException());

        // when
        excelJobAsyncService.generateMemberExcel(job.getId());

        //wait
        Thread.sleep(500);

        // then
        ExcelJob updated = excelJobRepository.findById(job.getId()).get();

        assertEquals(ExcelJobStatus.FAILED, updated.getStatus());
        assertTrue(updated.getErrorMessage().contains("엑셀 작업 중 에러 발생"));
    }
}