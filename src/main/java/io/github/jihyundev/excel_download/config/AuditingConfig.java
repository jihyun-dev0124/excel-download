package io.github.jihyundev.excel_download.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableJpaAuditing
public class AuditingConfig {

    @Bean(name = "excelTaskExecutor")
    public Executor excelTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // 동시에 돌릴 엑셀 작업 수
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("ExcelJob-");
        executor.initialize();

        return executor;
    }
}
