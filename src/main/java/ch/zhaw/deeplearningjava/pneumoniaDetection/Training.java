package ch.zhaw.deeplearningjava.pneumoniaDetection;

import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.inference.Predictor;
import ai.djl.metric.Metrics;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.TrainingResult;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.dataset.Batch;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Training {

    private static final int BATCH_SIZE = 32;
    private static final int EPOCHS = 3;

    public static void main(String[] args) throws IOException, TranslateException {

        Path modelDir = Paths.get("models");

        // === 1. Lade Trainings-, Validierungs- und Testdaten ===
        ImageFolder trainDataset = initDataset("chest_xray/train");
        ImageFolder validateDataset = initDataset("chest_xray/val");
        ImageFolder testDataset = initDataset("chest_xray/test");

        // === 2. Definiere Verlustfunktion ===
        Loss loss = Loss.softmaxCrossEntropyLoss();

        // === 3. Konfiguriere Training ===
        TrainingConfig config = setupTrainingConfig(loss);

        // === 4. Initialisiere Modell ===
        Model model = Models.getModel();
        Trainer trainer = model.newTrainer(config);
        trainer.setMetrics(new Metrics());

        Shape inputShape = new Shape(1, 3, Models.IMAGE_HEIGHT, Models.IMAGE_WIDTH);
        trainer.initialize(inputShape);

        // === 5. Trainiere Modell ===
        EasyTrain.fit(trainer, EPOCHS, trainDataset, validateDataset);

        // === 6. Evaluierung auf Testdaten ===
        try (Predictor<Image, Classifications> predictor = model.newPredictor(Models.getTranslator())) {

            int total = 0;
            int correct = 0;

            for (Batch batch : testDataset.getData(model.getNDManager())) {
                for (int i = 0; i < batch.getSize(); i++) {
                    Image image = (Image) batch.getData().get(i).get(0);
                    Classifications prediction = predictor.predict(image);
                    String predicted = prediction.best().getClassName();
                    String actual = batch.getLabels().get(i).get(0).toIntArray()[0] == 0
                            ? testDataset.getSynset().get(0)
                            : testDataset.getSynset().get(1);

                    if (predicted.equals(actual)) {
                        correct++;
                    }
                    total++;
                }
                batch.close(); // Speicher freigeben
            }

            float testAccuracy = (float) correct / total;
            System.out.printf("Test Accuracy: %.2f%%%n", testAccuracy * 100);

            // === 7. Speichere Modell & Ergebnisse ===
            TrainingResult result = trainer.getTrainingResult();
            model.setProperty("Epoch", String.valueOf(EPOCHS));
            model.setProperty("Accuracy", String.format("%.5f", result.getValidateEvaluation("Accuracy")));
            model.setProperty("Loss", String.format("%.5f", result.getValidateLoss()));
            model.setProperty("TestAccuracy", String.format("%.5f", testAccuracy));
            model.save(modelDir, Models.MODEL_NAME);
        }

        // === 8. Speichere Klassenlabels ===
        Models.saveSynset(modelDir, trainDataset.getSynset());
    }

    private static ImageFolder initDataset(String datasetPath) throws IOException, TranslateException {
        ImageFolder dataset = ImageFolder.builder()
                .setRepositoryPath(Paths.get(datasetPath))
                .optMaxDepth(1)
                .addTransform(new Resize(Models.IMAGE_WIDTH, Models.IMAGE_HEIGHT))
                .addTransform(new ToTensor())
                .setSampling(BATCH_SIZE, true)
                .build();
        dataset.prepare();
        return dataset;
    }

    private static TrainingConfig setupTrainingConfig(Loss loss) {
        return new DefaultTrainingConfig(loss)
                .addEvaluator(new Accuracy())
                .addTrainingListeners(TrainingListener.Defaults.logging());
    }
}
