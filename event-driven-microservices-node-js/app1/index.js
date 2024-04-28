const express = require('express');
const kafka = require('kafka-node');
const sequelize =  require('sequelize');

const app = express();
app.use(express.json());

const isKafkaUp = async () => {

    const db = new sequelize(process.env.POSTGRES_URL);
    const User = db.define('user',{
        name: sequelize.STRING,
        email: sequelize.STRING,
        password: sequelize.STRING
    });
    db.sync({ force : true});

    const client = new kafka.KafkaClient({kafkaHost: process.env.KAFKA_BOOTSTRAP_SERVER});
    const producer = new kafka.Producer(client);

    producer.on('ready', async () =>{
        console.log("Kafka is ready...");
        app.post('/', async (req,res) =>{
            console.log(JSON.stringify(req.body));
            producer.send([{topic:process.env.KAFKA_TOPIC,
                messages: JSON.stringify(req.body)}], async (err, data) =>{
                    if(err){
                        console.log(err);
                    } 
                    else{
                        await User.create(req.body);
                        res.send(req.body);
                    }
            })
        });
    });

    producer.on('error', (err) =>{
        console.log(err);
    });

};

setTimeout(isKafkaUp,10000);


app.listen(process.env.PORT);

