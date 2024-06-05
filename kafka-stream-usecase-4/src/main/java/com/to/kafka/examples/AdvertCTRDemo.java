package com.to.kafka.examples;

import com.to.kafka.examples.serde.AppSerdes;
import com.to.kafka.examples.types.AdClick;
import com.to.kafka.examples.types.AdImpression;
import com.to.kafka.examples.types.CampaignPerformance;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Properties;


public class AdvertCTRDemo {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfigs.applicationID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfigs.bootstrapServers);
        props.put(StreamsConfig.STATE_DIR_CONFIG, AppConfigs.stateStoreName);

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, AdImpression> KS0 = streamsBuilder.stream(AppConfigs.impressionTopic,
            Consumed.with(AppSerdes.String(), AppSerdes.AdImpression()));

        KTable<String, Long> adImpressionCount = KS0.groupBy((k, v) -> v.getCampaigner(),
            Grouped.with(AppSerdes.String(), AppSerdes.AdImpression()))
            .count();

        KStream<String, AdClick> KS1 = streamsBuilder.stream(AppConfigs.clicksTopic,
            Consumed.with(AppSerdes.String(), AppSerdes.AdClick()));

        KTable<String, Long> adClickCount = KS1.groupBy((k, v) -> v.getCampaigner(),
            Grouped.with(AppSerdes.String(), AppSerdes.AdClick()))
            .count();

        KTable<String, CampaignPerformance> campaignPerformance = adImpressionCount.leftJoin(
            adClickCount, (impCount, clkCount) -> new CampaignPerformance()
                .withAdImpressions(impCount)
                .withAdClicks(clkCount))
            .mapValues((k, v) -> v.withCampaigner(k))
            .mapValues((k, v) -> v.withAddCTR( !Objects.isNull(v.getAdClicks()) ? ((v.getAdClicks().doubleValue()/v.getAdImpressions().doubleValue())*100) : 0));

        campaignPerformance.toStream().foreach((k, v) -> logger.info(v));

        logger.info("Starting Stream...");
        KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Stopping Streams...");
            streams.close();
        }));

    }
}
