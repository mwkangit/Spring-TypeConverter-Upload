package hello.upload.domain;

import lombok.Data;

@Data
public class UploadFile {

    // 업로드한 파일명
    private String uploadFileName;
    // 시스템에 저장한 파일명
    private String storeFileName;

    public UploadFile(String uploadFileName, String storeFileName) {
        this.uploadFileName = uploadFileName;
        this.storeFileName = storeFileName;
    }
}
