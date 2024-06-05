package com.to.kafka.examples;

class AppConfigs {

    public final static String applicationID = "GKTableJoinDemo";
    public final static String bootstrapServers = "localhost:9092";
    public final static String inventoryTopic = "active-inventories";
    public final static String clicksTopic = "ad-clicks";
    public final static String stateStoreName = "tmp/state-store";
    public final static String top3AggregateKey = "top3NewsTypes";
}
