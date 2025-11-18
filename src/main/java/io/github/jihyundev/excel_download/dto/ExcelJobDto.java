package io.github.jihyundev.excel_download.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.github.jihyundev.excel_download.entity.ExcelJob;
import io.github.jihyundev.excel_download.enums.ExcelJobStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@Data
public class ExcelJobDto {
    private Long id;
    private String jobType;
    private ExcelJobStatus status;
    private int progress;
    private String filePath;
    private String fileName;
    private String errorMessage;
    private String requestedBy;
    private String createdAt;
    private String updatedAt;
    private String completedAt;

    @QueryProjection
    public ExcelJobDto(Long id, String jobType, ExcelJobStatus status, int progress, String filePath, String fileName, String errorMessage, String requestedBy, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime completedAt) {
        this.id = id;
        this.jobType = jobType;
        this.status = status;
        this.progress = progress;
        this.filePath = filePath;
        this.fileName = fileName;
        this.errorMessage = errorMessage;
        this.requestedBy = requestedBy;
        this.createdAt = createdAt != null ? dateToString(createdAt) : null;
        this.updatedAt = updatedAt != null ? dateToString(updatedAt) : null;
        this.completedAt = completedAt != null ? dateToString(completedAt) : null;
    }

    public ExcelJobDto(ExcelJob excelJob) {
        this.id = excelJob.getId();
        this.jobType = excelJob.getJobType();
        this.status = excelJob.getStatus();
        this.progress = excelJob.getProgress();
        this.filePath = excelJob.getFilePath();
        this.fileName = excelJob.getFileName();
        this.errorMessage = excelJob.getErrorMessage();
        this.requestedBy = excelJob.getRequestedBy();
        this.createdAt = excelJob.getCreatedAt() != null ? dateToString(excelJob.getCreatedAt()) : null;
        this.updatedAt = excelJob.getUpdatedAt() != null ? dateToString(excelJob.getUpdatedAt()) : null;
        this.completedAt = excelJob.getCompletedAt() != null ? dateToString(excelJob.getCompletedAt()) : null;
    }

    private String dateToString(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
        return time.format(formatter);
    }
}
