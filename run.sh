 docker compose up -d kafka node1 node2 kafka-ui



docker compose exec kafka kafka-topics.sh --create --topic jobs --partitions 12 --replication-factor 1 --bootstrap-server localhost:9092


max_retries=10
retry_count=0
success=false

while [ $retry_count -lt $max_retries ]; do
  if docker compose exec node1 cqlsh -f /files/init.sql; then
    success=true
    break
  else
    echo "Connection failed. Retrying in 30 seconds..."
    sleep 30
    retry_count=$((retry_count + 1))
  fi
done

if [ "$success" = true ]; then
  echo "Successfully connected to Cassandra and executed the script."
else
  echo "Failed to connect to Cassandra after $max_retries attempts."
  exit 1
fi