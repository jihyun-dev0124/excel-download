package io.github.jihyundev.excel_download.utils;

import io.github.jihyundev.excel_download.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExcelWriter implements AutoCloseable{
    public final static short TYPE_TEXT = 0;
    public final static short TYPE_NUMBER = 1;
    public final static short TYPE_CURRENCY = 2;
    public final static short TYPE_PERCENT = 3;
    public final static short ALIGN_NONE = 0;
    public final static short ALIGN_LEFT = 1;
    public final static short ALIGN_CENTER = 2;
    public final static short ALIGN_RIGHT = 3;

    private int WINDOW_SIZE = 1000; // SXSSF windowSize
    private int FLUSH_KEEP_ROWS = 500; //flushRows 시 메모리에 남길 row 수
    private int FIRST_SHEET_INDEX = 0;
    private short ROW_HEIGHT = 0x150;
    private String FONT_NAME = "맑은고딕";
    private short FONT_SIZE = 9;

    private HttpServletRequest request;

    private SXSSFWorkbook workbook;

    private SXSSFSheet sheet;

    private List<String> mappings;
    private List<Short> cellTypes;
    private List<CellStyle> cellStyles;
    private String[] headers;
    private int rowCurrent = 0;

    /**
     * 생성자
     */
    public ExcelWriter() {
        this.mappings = new ArrayList<>();
        this.cellTypes = new ArrayList<>();
        this.cellStyles = new ArrayList<>();
        this.WINDOW_SIZE = 1000;
        this.FLUSH_KEEP_ROWS = 500;
    }

    public ExcelWriter(int windowSize, int flushKeepRows) {
        this.mappings = new ArrayList<>();
        this.cellTypes = new ArrayList<>();
        this.cellStyles = new ArrayList<>();
        this.WINDOW_SIZE = windowSize;
        this.FLUSH_KEEP_ROWS = flushKeepRows;
    }

    @Override
    public void close(){
        try {
            workbook.dispose();
            workbook.close();
        } catch (IOException e) {
            throw new UncheckedIOException("엑셀 리소스 정리 중 오류", e);
        }
    }

    // 엑셀 템필릿 생성
    public void createTemplate(String[] headers) throws IOException {
        // 엑셀 템플릿파일 생성
        this.workbook = new SXSSFWorkbook(WINDOW_SIZE);
        this.headers = headers;

        // SXSSF 생성
        this.workbook.setCompressTempFiles(true);
    }

    /**
     * 시트 추가
     * @param name
     */
    public void addSheet(String name) {
        Sheet sheet = this.workbook.createSheet(name);
        createHeader(sheet);
        this.rowCurrent = sheet.getLastRowNum();
        this.sheet = workbook.getSheetAt(FIRST_SHEET_INDEX++);
    }

    /**
     * header 생성
     * @param sheet
     */
    public void createHeader(Sheet sheet) {
        // Header Cell 스타일 설정
        CellStyle headerCellStyle = workbook.createCellStyle();

        // 배경색 설정
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 글자색 설정
        Font headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerCellStyle.setFont(headerFont);

        // 정렬 설정 (가운데 정렬)
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < this.headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(this.headers[i]);
            cell.setCellStyle(headerCellStyle);
            // 열 넓이 100px 설정 (Apache POI 단위로 약 2560)
            sheet.setColumnWidth(i, (short)5000);
        }
    }

    /**
     * 엑셀 필드 맵핑 추가
     * @param id 필드 아이디
     * @param type 필드 유형(TYPE_TEXT, TYPE_NUMBER, TYPE_CURRENCY, TYPE_PERCENT)
     * @throws Exception
     */
    public void addMapping(String id, short type) throws Exception {
        short align = ALIGN_NONE;

        switch (type) {
            case TYPE_NUMBER:
            case TYPE_CURRENCY:
            case TYPE_PERCENT:
                break;
            default:
                align = ALIGN_CENTER;
        }
        this.addMapping(id, type, align);
    }

    /**
     * 엑셀 필드 맵핑 추가
     * @param id 필드 아이디
     * @param type 필드 유형(TYPE_TEXT, TYPE_NUMBER, TYPE_CURRENCY, TYPE_PERCENT)
     * @param align 필드 가로정렬(ALIGN_NONE, ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT)
     * @throws Exception
     */
    public void addMapping(String id, short type, short align) throws Exception {
        if (workbook == null) {
            throw new Exception("Workbook is null.");
        }

        Font font = workbook.createFont();
        font.setFontHeightInPoints(FONT_SIZE);
        font.setFontName(FONT_NAME);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        cellStyle.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        cellStyle.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        cellStyle.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setWrapText(true);

        // set data format
        switch (type) {
            case TYPE_NUMBER:
                cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
                break;
            case TYPE_CURRENCY:
                String pattern = "#,##0";
                cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat(pattern));
                break;
            case TYPE_PERCENT:
                cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0%"));
                break;
        }

        // set align
        switch (align) {
            case ALIGN_LEFT:
                cellStyle.setAlignment(HorizontalAlignment.LEFT);
                break;
            case ALIGN_CENTER:
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                break;
            case ALIGN_RIGHT:
                cellStyle.setAlignment(HorizontalAlignment.RIGHT);
                break;
        }

        this.mappings.add(id);
        this.cellTypes.add(type);
        this.cellStyles.add(cellStyle);
    }


    /**
     * 엑셀 로우 데이터 추가
     * @param data 데이터 맵
     */
    public void addRow(Map data)  {
        Cell cell;
        CellStyle cellStyle;

        Row row = sheet.createRow(++rowCurrent);
        row.setHeight(ROW_HEIGHT);

        int index = 0;
        for (String mappingId : mappings) {
            cellStyle = cellStyles.get(index);

            cell = row.createCell(index);
            cell.setCellStyle(cellStyle);

            if (data.get(mappingId) != null) {
                switch (cellTypes.get(index)) {
                    case TYPE_NUMBER:
                    case TYPE_PERCENT:
                        cell.setCellValue(getDouble(data.get(mappingId)));
                        break;
                    case TYPE_CURRENCY:
                        cell.setCellValue(getDecimal(data.get(mappingId)).doubleValue());
                        break;
                    default:
                        cell.setCellValue(getStr(data.get(mappingId)).trim());
                }
            }

            ++index;
        }
    }

    /**
     * Member 전용 메서드
     * mappings: ["id", "username", "real_name", "phone", "email"] 기준
     */
    public void addRow(Member member)  {
        Cell cell;
        CellStyle cellStyle;

        Row row = sheet.createRow(++rowCurrent);
        row.setHeight(ROW_HEIGHT);

        Map<String, Object> data = new HashMap<>();
        data.put("id", member.getId());
        data.put("username", member.getUsername());
        data.put("real_name", member.getRealName());
        data.put("phone", member.getPhone());
        data.put("email", member.getEmail());

        int index = 0;
        for (String mappingId : mappings) {
            cellStyle = cellStyles.get(index);

            cell = row.createCell(index);
            cell.setCellStyle(cellStyle);

            if (data.get(mappingId) != null) {
                switch (cellTypes.get(index)) {
                    case TYPE_NUMBER:
                    case TYPE_PERCENT:
                        cell.setCellValue(getDouble(data.get(mappingId)));
                        break;
                    case TYPE_CURRENCY:
                        cell.setCellValue(getDecimal(data.get(mappingId)).doubleValue());
                        break;
                    default:
                        cell.setCellValue(getStr(data.get(mappingId)).trim());
                }
            }

            ++index;
        }
    }

    public void flushRows(){
        if(sheet == null) return;
        try {
            ((SXSSFSheet) sheet).flushRows(FLUSH_KEEP_ROWS);
        }catch (IOException e) {
            throw new UncheckedIOException("엑셀 flush 중 오류 발생", e);
        }
    }

    /**
     * 엑셀 다운로드 - 즉시 다운로드
     * @param filename 다운로드 파일명
     * @throws IOException
     */
    public void download(String filename) throws IOException {
        try {
            ((SXSSFSheet)sheet).flushRows();

            filename = URLEncoder.encode(filename,"UTF-8").replaceAll("\\+", "%20");

            HttpServletResponse response = null;
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                throw new RuntimeException();
            }
            response = ((ServletRequestAttributes) attributes).getResponse();
            response.setContentType("application/msexcel");
            response.setHeader("Content-Disposition", "attachment; filename=\""+filename+".xlsx\"");

            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }

    }

    /**
     * 다운로드용 파일 생성
     * @param filename
     * @throws IOException
     */
    public String prepareDownloadFile(String filename) {
        String filePath = "/tmp/";

        try {
            flushRows();

            filename = URLEncoder.encode(filename,"UTF-8").replaceAll("\\+", "%20");
            filePath += filename + ".xlsx";

            try(FileOutputStream fos = new FileOutputStream(filePath)){
                workbook.write(fos);
            }

            return filePath;
        } catch (IOException e) {
            throw new UncheckedIOException("다운로드용 엑셀 파일 저장 중 에러 발생", e);
        }
    }

    public double getDouble(Object key) {
        double v = 0;
        Object o = key;
        if (o == null) return v;

        String s = o.toString().trim().replace(",", "");
        try {
            v = Double.valueOf(s).doubleValue();
        } catch (NumberFormatException e) {
        }
        return v;
    }

    /**
     * BigDecimal 반환
     * @param key the key
     * @return String
     */
    public BigDecimal getDecimal(Object key) {
        BigDecimal v = BigDecimal.ZERO;
        Object o = key;
        if (o == null) return v;

        String s = o.toString().trim().replace(",", "");
        try {
            v = new BigDecimal(s);
        } catch (NumberFormatException e) {
        }
        return v;
    }

    /**
     * 문자열 반환
     * @param key the key
     * @return String
     */
    public String getStr(Object key) {
        String v = "";
        Object o = key;
        if (o == null) return v;

        try {
            v = o.toString();
        } catch (Exception e) {
        }
        return v;
    }
}
