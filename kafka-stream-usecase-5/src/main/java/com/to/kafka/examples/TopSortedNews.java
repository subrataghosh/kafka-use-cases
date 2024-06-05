package com.to.kafka.examples;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.to.kafka.examples.serde.AppSerdes;
import com.to.kafka.examples.types.AdClick;
import com.to.kafka.examples.types.AdInventories;
import com.to.kafka.examples.types.ClicksByNewsType;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Properties;


public class TopSortedNews {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, AppConfigs.applicationID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfigs.bootstrapServers);
        props.put(StreamsConfig.STATE_DIR_CONFIG, AppConfigs.stateStoreName);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0);

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        GlobalKTable<String, AdInventories> GKT0 = streamsBuilder.globalTable(AppConfigs.inventoryTopic,
                Consumed.with(AppSerdes.String(), AppSerdes.AdInventories())
        );

        KStream<String, AdClick> KS1 = streamsBuilder.stream(AppConfigs.clicksTopic,
                Consumed.with(AppSerdes.String(), AppSerdes.AdClick())
        );

        KTable<String, Long> KT1 = KS1.join(GKT0, (k, v) -> k, (v1, v2) -> v2)
                .groupBy((k, v) -> v.getNewsType(), Grouped.with(AppSerdes.String(), AppSerdes.AdInventories()))
                .count();

        KT1.groupBy((k,v) ->{
                ClicksByNewsType cb = new ClicksByNewsType();
                cb.setNewsType(k);
                cb.setClicks(v);
                return KeyValue.pair(AppConfigs.top3AggregateKey,cb);
            },Grouped.with(AppSerdes.String(),AppSerdes.ClicksByNewsType())
        ).aggregate(() -> new Top3NewsType(),
            (k,newV,aggV) -> {
                aggV.add(newV);
                return aggV;
            },
            (k,oldV,aggV) -> {
                aggV.remove(oldV);
                return aggV;
            },
             Materialized.<String,Top3NewsType, KeyValueStore<Bytes,byte[]>>
                 as("top3-clicks")
                 .withKeySerde(AppSerdes.String())
                 .withValueSerde(AppSerdes.Top3NewsType()))
        .toStream().foreach((k,v) ->{
            try {
                logger.info("Key= " + k + "Value= "+ v.getTop3sorted());
            } catch( JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        logger.info("Starting Stream...");
        KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Stopping Streams...");
            streams.close();
        }));

    }
}
