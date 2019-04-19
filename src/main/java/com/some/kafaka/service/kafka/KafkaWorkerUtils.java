package com.some.kafaka.service.kafka;

import com.google.protobuf.Timestamp;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KafkaWorkerUtils {

  private final static int DEBUG_LOG_WIDTH = 80;
  private final static String DEBUG_LOG_SEPARATOR = "\n" + StringUtils.center("", DEBUG_LOG_WIDTH, "~") + "\n";

  public static String stepToString(@NonNull String applicationId,
                                    @NonNull String step,
                                    Object stepOutput,
                                    Object... stepInputs) {
    final String abbreviatedStepName = StringUtils.abbreviate(step, DEBUG_LOG_WIDTH - 16);

    // summary headline
    StringBuilder summary = new StringBuilder("[DEBUG:")
      .append(applicationId)
      .append("] Step '")
      .append(step)
      .append("' execution summary:\n");

    // step output
    summary
      .append(DEBUG_LOG_SEPARATOR)
      .append(StringUtils.center(" OUTPUT (" + abbreviatedStepName + ") ", DEBUG_LOG_WIDTH, "~"))
      .append(DEBUG_LOG_SEPARATOR)
      .append(stepOutput);

    // step inputs
    if (stepInputs != null) {
      for (int i = 0; i < stepInputs.length; i++) {
        summary
          .append(DEBUG_LOG_SEPARATOR)
          .append(StringUtils.center(" INPUT #" + (i + 1) + " (" + abbreviatedStepName + ") ", DEBUG_LOG_WIDTH, "~"))
          .append(DEBUG_LOG_SEPARATOR)
          .append(stepInputs[i]);
      }
    }

    // end of output
    summary.append(DEBUG_LOG_SEPARATOR);

    // step execution summary
    return summary.toString();
  }

  public static Timestamp instantToTimestamp(Instant instant) {
    return instant != null ? Timestamp
      .newBuilder()
      .setSeconds(instant.getEpochSecond())
      .setNanos(instant.getNano())
      .build() : null;
  }

  public static Instant timestampToInstant(Timestamp timestamp) {
    return timestamp != null ? Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()) : null;
  }

}
