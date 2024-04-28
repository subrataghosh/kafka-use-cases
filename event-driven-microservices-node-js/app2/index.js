const express = require('express');
const kafka = require('kafka-node');
const mongoose =  require('mongoose');

const app = express();
app.use(express.json);

const isKafkaUp = async () => {
    mongoose.connect(process.env.MONGO_URL);
    const User = mongoose.model('user',{
        name: String,
        email: String,
        password: String
    });

    const client = new kafka.KafkaClient({kafkaHost: process.env.KAFKA_BOOTSTRAP_SERVER});
    const consumer = new kafka.Consumer(client,[{topic:process.env.KAFKA_TOPIC}],{
        autoCommit: false
    });

    consumer.on('message', async (message) =>{
        const user = new User(JSON.parse(message.value));
        await user.save(user);
    });

    consumer.on('error', (err) =>{
        console.log(err);
    });

};

setTimeout(isKafkaUp,10000);


app.listen(process.env.PORT);