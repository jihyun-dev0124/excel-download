package io.github.jihyundev.excel_download.domain.excel;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name="tb_excel_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Builder
public class ExcelJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String jobType;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ExcelJobStatus status;

    @Column(nullable = false, length = 11)
    private int progress;

    @Column(length = 500)
    private String filePath;

    @Column(length = 100)
    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(length = 100, nullable = false)
    private String requestedBy;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;


    //진행중으로 변경
    public void markRunning(){
        this.status = ExcelJobStatus.RUNNING;
        this.progress = 0;
    }

    public void updateProgress(int progress){
        this.progress = progress;
    }

    //완료 처리
    public void completed(String filePath){
        this.status = ExcelJobStatus.COMPLETED;
        this.progress = 100;
        this.filePath = filePath;
        this.completedAt = LocalDateTime.now();
    }

    //실패 처리
    public void fail(String errorMessage) {
        this.status = ExcelJobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.filePath = null;
    }

}
