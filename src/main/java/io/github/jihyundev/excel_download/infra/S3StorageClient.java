package io.github.jihyundev.excel_download.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;

@Service
@RequiredArgsConstructor
public class S3StorageClient {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    /**
     * 파일 업로드
     * @param key
     * @param file
     * @return
     */
    public String uploadFile(String key, File file) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));

        // 보통 key를 반환해서 나중에 다운로드 때 사용
        return key;
    }

    /**
     * s3 파일 다운로드 해서 바이트 배열로 반환
     * -> 바로 HTTP 응답으로 내보낼 때 사용 가능
     * @param key : s3의 파일 경로 + 이름
     * @return
     */
    public byte[] downloadFile(String key){
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

        return objectBytes.asByteArray();
    }

}
