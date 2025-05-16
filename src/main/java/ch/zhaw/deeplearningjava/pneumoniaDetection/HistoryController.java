package ch.zhaw.deeplearningjava.pneumoniaDetection;

import ch.zhaw.deeplearningjava.pneumoniaDetection.HistoryEntry;
import ch.zhaw.deeplearningjava.pneumoniaDetection.HistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@RestController
public class HistoryController {

    @Autowired
    private HistoryRepository historyRepository;

    @PostMapping("/history/store")
    public void store(@RequestParam("image") MultipartFile image, @RequestParam("diagnosis") String diagnosis) throws Exception {
        String base64 = Base64.getEncoder().encodeToString(image.getBytes());
        HistoryEntry entry = new HistoryEntry(image.getOriginalFilename(), diagnosis, base64);
        historyRepository.save(entry);
    }

    @GetMapping("/history/all")
    public List<HistoryEntry> getAll() {
        return historyRepository.findAll();
    }
}
