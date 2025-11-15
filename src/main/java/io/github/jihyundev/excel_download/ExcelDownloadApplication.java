package io.github.jihyundev.excel_download;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ExcelDownloadApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExcelDownloadApplication.class, args);
	}

}
