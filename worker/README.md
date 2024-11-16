# distributed-scheduler
docker-compose exec kafka kafka-topics.sh --create --topic my-topic --partitions 100 --replication-factor 3 --bootstrap-server localhost:9092