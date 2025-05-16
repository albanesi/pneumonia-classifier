package ch.zhaw.deeplearningjava.pneumoniaDetection;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("uploadHistory")
public class HistoryEntry {

    @Id
    private String id;
    private String filename;
    private String diagnosis;
    private String imageBase64;
    private LocalDateTime timestamp;

    public HistoryEntry() {}

    public HistoryEntry(String filename, String diagnosis, String imageBase64) {
        this.filename = filename;
        this.diagnosis = diagnosis;
        this.imageBase64 = imageBase64;
        this.timestamp = LocalDateTime.now();
    }

    // Getter & Setter ...
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public String getDiagnosis() {
        return diagnosis;
    }
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    public String getImageBase64() {
        return imageBase64;
    }
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    

}
