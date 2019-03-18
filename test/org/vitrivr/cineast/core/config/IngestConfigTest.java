package org.vitrivr.cineast.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.vitrivr.cineast.core.data.MediaType;

class IngestConfigTest {

  private static Stream<FileArguments> fileProvider() {
    return Stream.of(
        new FileArguments(
            "extraction_3d.json", MediaType.MODEL3D,
            Arrays.asList(
                "LightfieldFourier", "LightfieldZernike", "SphericalHarmonicsDefault",
                "SphericalHarmonicsLow", "SphericalHarmonicsHigh")),
        new FileArguments(
            "extraction_video.json", MediaType.VIDEO,
            Arrays.asList(
                "AverageColor", "AverageColorGrid8Reduced11", "AverageColorGrid8Reduced15",
                "AverageColorRaster", "AverageColorRasterReduced11",
                "AverageColorRasterReduced15", "AverageFuzzyHist",
                "AverageFuzzyHistNormalized", "CLDReduced11", "CLDReduced15", "EdgeARP88",
                "EdgeGrid16", "EHD", "DominantEdgeGrid16", "DominantEdgeGrid8",
                "MedianColor", "MedianFuzzyHist", "SubDivMotionHistogram3",
                "SubDivMotionHistogram5", "SubDivMotionHistogramBackground3",
                "SubDivMotionHistogramBackground5", "HOGMirflickr25K512",
                "SURFMirflickr25K512", "AudioFingerprint", "AverageHPCP20F36B",
                "AverageHPCP30F36B", "CENS12Shingle", "HPCP12Shingle", "MFCCShingle")),
        new FileArguments(
            "extraction_audio.json", MediaType.AUDIO,
            Arrays.asList(
                "AudioFingerprint", "AverageHPCP20F36B", "AverageHPCP30F36B",
                "CENS12Shingle", "HPCP12Shingle", "MFCCShingle")),
        new FileArguments(
            "extraction_images.json", MediaType.IMAGE,
            Arrays.asList(
                "AverageColor", "AverageColorARP44", "AverageColorCLD", "AverageColorGrid8",
                "AverageColorRaster", "AverageFuzzyHist", "CLD", "EdgeARP88", "EdgeGrid16",
                "EHD", "MedianColor", "MedianColorARP44", "MedianColorGrid8",
                "MedianColorRaster", "MotionHistogram", "HOGMirflickr25K512",
                "SURFMirflickr25K512")));
  }

  @ParameterizedTest
  @MethodSource("fileProvider")
  void filesCanBeParsedWithoutFail(FileArguments args) throws URISyntaxException {
    // GIVEN - A config file
    File file = new File(getClass().getResource("/cineast/config/" + args.getUrl()).toURI());

    // WHEN - The file is loaded
    ObjectMapper mapper = new ObjectMapper();
    IngestConfig ingestConfig;
    try {
      ingestConfig = mapper.readValue(file, IngestConfig.class);

      // THEN - The created object reflects the data in the file
      assertEquals(args.getExpectedType(), ingestConfig.getType());
      assertEquals(args.getExtractors().size(), ingestConfig.getExtractors().size());
      List<String> expectedExtractors = args.getExtractors();
      List<String> parsedExtractors = ingestConfig.getExtractors().stream()
          .map(MetadataConfig::getName).collect(
              Collectors.toList());
      for (String extractor : expectedExtractors) {
        assertTrue(parsedExtractors.contains(extractor));
      }
      for (String extractor : parsedExtractors) {
        assertTrue(expectedExtractors.contains(extractor));
      }
    } catch (IOException e) {
      fail();
    }
  }

  private static class FileArguments {

    private final String url;
    private final MediaType expectedType;
    private final List<String> extractors;

    private FileArguments(
        String url,
        MediaType expectedType,
        List<String> extractors) {
      this.url = url;
      this.expectedType = expectedType;
      this.extractors = extractors;
    }

    String getUrl() {
      return url;
    }

    MediaType getExpectedType() {
      return expectedType;
    }

    List<String> getExtractors() {
      return extractors;
    }
  }
}