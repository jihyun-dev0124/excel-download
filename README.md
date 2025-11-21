# Cursor + SXSSF로 대용량 엑셀 다운로드 구현하기 (번외. JPA Stream + SXSSF)
<br>
JVM 메모리가 제한된 운영 환경에서 16만 건 이상의 회원 엑셀 다운로드 시 OutOfMemoryError(OOM)가 발생했던 문제를 해결하고자<br>
<br>


- type1. MyBatis와 Cursor를 사용한 스트리밍 조회 방식과 Apache POI SXSSF의 flushRows() 호출 최적화를 통한 엑셀 생성 방식과
- type2. JPA의 Stream을 통한 스트리밍 조회 방식과 Apache POI SXSSF의 flushRows() 호출 최적화를 통한 엑셀 생성 방식
- @Async 기반 비동기 작업 + 상태 테이블로 엑셀 생성 과정을 기록하고
- S3 업로드 기반 다운로드 분리 구조로 개선한 예제 프로젝트입니다.


<br><br>
## 1. 기능 개요
- 엑셀 생성 요청 시 즉시 응답 (비동기 Job 생성)
- 백그라운드에서 대용량 데이터를 스트리밍으로 읽어 엑셀 생성
- SXSSF flushRows()로 메모리 사용을 일정 수준으로 유지
- /tmp에 임시 파일 생성 후 S3 업로드
- ExcelJob 테이블로 상태(PENDING/RUNNING/COMPLETED/FAILED) 및 진행률(progress) 관리
- 프론트에서는 Polling API로 상태 조회 후 다운로드 버튼 활성화

<br><br>
## 2. 기술 스택
- java17, Spring Boot 3.x
- JPA, Mybatis
- Apache POI SXSSF
- Spring @Async + ThreadPoolTaskExecutor
- AWS S3
  
<br><br>
## 3. Cursor를 사용한 스트리밍 조회 방식 흐름
<img width="584" height="400" alt="스크린샷 2025-11-19 오후 11 09 57" src="https://github.com/user-attachments/assets/25063de0-3f48-4e5e-850a-ca1d7fc52819" />

<br><br>
## 4. 요약
1. 클라이언트가 엑셀 생성 요청
2. ExcelJob 생성 (PENDING) 후 즉시 응답
3. @Async 서비스에서 ExcelGenerator 호출
4. DB 스트리밍 + SXSSF flush 처리로 엑셀 생성
5. /tmp에 저장 후 S3 업로드
6. ExcelJob 상태 COMPLETED 및 다운로드 경로 저장
7. 클라이언트는 상태 조회 API로 진행률/완료 여부 확인


https://github.com/user-attachments/assets/a7d17554-541d-4729-bf63-72478a50de51







