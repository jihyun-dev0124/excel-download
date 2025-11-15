package io.github.jihyundev.excel_download.service;

import io.github.jihyundev.excel_download.entity.ExcelJob;
import io.github.jihyundev.excel_download.entity.Member;
import io.github.jihyundev.excel_download.enums.ExcelJobStatus;
import io.github.jihyundev.excel_download.repository.ExcelJobRepository;
import io.github.jihyundev.excel_download.repository.MemberRepository;
import io.github.jihyundev.excel_download.utils.ExcelWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExcelJobAsyncService {
    private final ExcelJobRepository excelJobRepository;
    private final MemberRepository memberRepository;
    private final ExcelJobService excelJobService;
    private final S3Service s3Service;

    @Async("excelTaskExecutor")
    @Transactional(readOnly = true)
    public void generateMemberExcel(Long jobId){
        ExcelJob job = excelJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("job not found:" + jobId));

        log.info("excel donwload 비동기 시작");
        excelJobService.markRunning(jobId);

        long totalCount = memberRepository.count();
        int[] excelNos = {0, 1}; //[0]: rowNo, [1]: sheetNo

        String s3Key = null;

        try(ExcelWriter ew = new ExcelWriter(1000, 1000)){
            String[] headers = {"id", "username", "realName", "phone", "email"};
            ew.createTemplate(headers);
            ew.addMapping("id", ExcelWriter.TYPE_TEXT);
            ew.addMapping("username", ExcelWriter.TYPE_TEXT);
            ew.addMapping("real_name", ExcelWriter.TYPE_TEXT);
            ew.addMapping("phone", ExcelWriter.TYPE_TEXT);
            ew.addMapping("email", ExcelWriter.TYPE_TEXT);
            ew.addSheet("member_"+1);

            try (Stream<Member> memberAll = memberRepository.streamAll()) {
                memberAll.forEach(member -> {
                    // 1만 건마다 sheet 분리
                    if(excelNos[0] % 10000 == 0){
                        ew.addSheet("member_"+(++excelNos[1]));
                    }

                    ew.addRow(member);
                    excelNos[0]++;

                    if(excelNos[0] % 1000 == 0){
                        ew.flushRows();
                    }

                    //5,000건 단위로 progress 저장
                    if(excelNos[0] % 5000 == 0){
                        int progressed = (int)((excelNos[0] * 100) / totalCount);
                        excelJobService.updateProgress(jobId, progressed);
                    }
                });
            }

            LocalDateTime dateTime = LocalDateTime.now();
            String localPath = ew.prepareDownloadFile("member_" + dateTime.toString());

            // 로컬 /tmp에 파일 저장
            File file = new File(localPath);

            // s3에 업로드
            s3Key = "excel/member/" + file.getName();
            s3Service.uploadFile(s3Key, file);

        } catch (Exception e) {
            excelJobService.fail(jobId, e);
            throw new RuntimeException("엑셀 작업 중 에러 발생 ",e);
        }

        excelJobService.complete(jobId, s3Key);
    }


}
