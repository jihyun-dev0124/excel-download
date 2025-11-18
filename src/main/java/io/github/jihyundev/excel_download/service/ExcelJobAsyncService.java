package io.github.jihyundev.excel_download.service;

import io.github.jihyundev.excel_download.entity.ExcelJob;
import io.github.jihyundev.excel_download.entity.Member;
import io.github.jihyundev.excel_download.repository.ExcelJobRepository;
import io.github.jihyundev.excel_download.repository.MemberRepository;
import io.github.jihyundev.excel_download.repository.mybatis.MemberRepositoryByMybatis;
import io.github.jihyundev.excel_download.utils.ExcelWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    //MyBatis
    private final MemberRepositoryByMybatis memberRepositoryByMybatis;

    /**
     * JPA 사용
     * @param jobId
     */
    @Async("excelTaskExecutor")
    @Transactional(readOnly = true)
    public void generateMemberExcel(Long jobId){
        StopWatch sw = new StopWatch("generateMemberExcelJpa-" + jobId);

        ExcelJob job = excelJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("job not found:" + jobId));

        excelJobService.markRunning(jobId);

        long totalCount = memberRepository.count();
        int[] excelNos = {0, 1}; //[0]: rowNo, [1]: sheetNo

        String s3Key = null;
        sw.start("db-read+excel");
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
                    ew.addRow(member);
                    excelNos[0]++;

                    // 1만 건마다 sheet 분리
                    if(excelNos[0] % 10000 == 0){
                        ew.addSheet("member_"+(++excelNos[1]));
                    }

                    //1,000건 단위로 flush
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

            sw.stop(); //db-read+excel end

            // ===== /tmp 에 파일 저장 =====
            sw.start("local-write");
            String localPath = ew.prepareDownloadFile(job.getFileName());
            File file = new File(localPath);
            sw.stop(); //local-write end

            // ===== S3 업로드 =====
            sw.start("s3-upload");
            s3Key = "excel/member/" + file.getName();
            s3Service.uploadFile(s3Key, file);
            sw.stop(); //s3-upload end

        } catch (Exception e) {
            excelJobService.fail(jobId, e);
            log.error("[ExcelJob JPA] jobId={} 작업 실패", jobId, e);
            throw new RuntimeException("엑셀 작업 중 에러 발생 ",e);
        }

        excelJobService.complete(jobId, s3Key);
        log.info("[ExcelJob JPA] jobId = {} timings = \n{}", job, sw.prettyPrint());
    }

    /**
     * Mybatis Cursor + JPA
     */
    @Async("excelTaskExecutor")
    @Transactional(readOnly = true)
    public void generateMemberExcelByMybatis(Long jobId) {
        StopWatch sw = new StopWatch("generateMemberExcelByMybatis-"+jobId);
        ExcelJob job = excelJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("job not found:" + jobId));

        excelJobService.markRunning(jobId);

        int totalCount = memberRepositoryByMybatis.memberCount();
        int rowNo = 0, sheetNo = 1;

        String s3Key = null;

        sw.start("db-read+excel");
        try (ExcelWriter ew = new ExcelWriter(1000, 1000)) {
            String[] headers = {"id", "username", "realName", "phone", "email"};
            ew.createTemplate(headers);
            ew.addMapping("id", ExcelWriter.TYPE_TEXT);
            ew.addMapping("username", ExcelWriter.TYPE_TEXT);
            ew.addMapping("real_name", ExcelWriter.TYPE_TEXT);
            ew.addMapping("phone", ExcelWriter.TYPE_TEXT);
            ew.addMapping("email", ExcelWriter.TYPE_TEXT);
            ew.addSheet("member_"+1);

            try (Cursor<Member> memberAll = memberRepositoryByMybatis.cursorAll()) {
                for (Member member : memberAll) {
                    ew.addRow(member);
                    rowNo++;

                    //1만 건마다 sheet 분리
                    if(rowNo % 10000 == 0) ew.addSheet("member_"+(++sheetNo));

                    //1,000건 단위로 flush
                    if(rowNo % 1000 == 0) ew.flushRows();

                    //5,000건 단위로 progress 저장
                    if (rowNo % 5000 == 0) {
                        int progressed = (int) ((rowNo * 100) / totalCount);
                        excelJobService.updateProgress(jobId, progressed);
                    }
                }
            }
            sw.stop(); //db-read+excel end

            // ===== /tmp 에 파일 저장 =====
            sw.start("local-write");
            String localPath = ew.prepareDownloadFile(job.getFileName());
            File file = new File(localPath);
            sw.stop();//local-write end

            // ===== S3 업로드 =====
            sw.start("s3-upload");
            s3Key = "excel/member/" + file.getName();
            s3Service.uploadFile(s3Key, file);
            sw.stop();//s3-upload end

        } catch (Exception e) {
            excelJobService.fail(jobId, e);
            log.error("[ExcelJob MyBatis] jobId={} 작업 실패", jobId, e);
            throw new RuntimeException(e);
        }

        excelJobService.complete(jobId, s3Key);
        log.info("[ExcelJob MyBatis] jobId = {} timings = \n{}", job, sw.prettyPrint());
    }


}
