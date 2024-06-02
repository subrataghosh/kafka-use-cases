package com.to.kafka.examples;

import com.to.kafka.examples.serde.AppSerdes;
import com.to.kafka.examples.types.HeartBeat;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Properties;


public class WindowSuppressApp {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfigs.applicationID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfigs.bootstrapServers);
        props.put(StreamsConfig.STATE_DIR_CONFIG, AppConfigs.stateStoreName);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0);

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, HeartBeat> KS0 = streamsBuilder.stream(AppConfigs.topicName,
            Consumed.with(AppSerdes.String(), AppSerdes.HeartBeat())
                .withTimestampExtractor(new AppTimestampExtractor())
        );

        KTable<Windowed<String>,Long> KT0 = KS0.groupByKey(Grouped.with(AppSerdes.String(),AppSerdes.HeartBeat()))
           .windowedBy(TimeWindows.of(Duration.ofSeconds(60)).grace(Duration.ofSeconds(10)))
           .count()
           .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()));

        KT0.toStream().foreach(
                (wKey , value) -> logger.info(
                       "Store Id" + wKey.key() + " Window Id: " + wKey.window().hashCode() +
                        " Window Start: " + Instant.ofEpochMilli(wKey.window().start()).atOffset(ZoneOffset.UTC) +
                        " Window End: " + Instant.ofEpochMilli(wKey.window().end()).atOffset(ZoneOffset.UTC) +
                        " Count: " + value +
                        (value > 2 ? "App is Alive.." : "App Failed -- sending Alert Email")
                )
        );


        logger.info("Starting Stream...");
        KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Stopping Streams...");
            streams.close();
        }));

    }
}
