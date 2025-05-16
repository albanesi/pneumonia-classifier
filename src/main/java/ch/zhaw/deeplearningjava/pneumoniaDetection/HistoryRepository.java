package ch.zhaw.deeplearningjava.pneumoniaDetection;

import ch.zhaw.deeplearningjava.pneumoniaDetection.HistoryEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HistoryRepository extends MongoRepository<HistoryEntry, String> {}
