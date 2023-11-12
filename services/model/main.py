import json
import yaml
import pandas as pd
import mysql.connector
from kafka import KafkaProducer, KafkaConsumer
from sentence_transformers import SentenceTransformer, util

from utils import clean_texts

# Set up Kafka consumer configuration
# Load the configuration from the YAML file
with open('config.yml', 'r') as f:
    config = yaml.safe_load(f)

# Load fine-tuned model
model_path = config['model']['path']
threshold = config['model']['threshold']
model = SentenceTransformer(model_path)

# Load database config
datasource = config['datasource']
print(datasource)

# Create Kafka consumer instance
bootstrap_servers = config['kafka']['server']
consume_topic = config['kafka']['topic']
group_id = config['kafka']['group_id']

print(f'Kafka bootstrap-servers {bootstrap_servers} and topic {consume_topic}')

consumer = KafkaConsumer(consume_topic,
                        group_id=group_id,
                        bootstrap_servers=bootstrap_servers,
                        auto_offset_reset='earliest',
                        value_deserializer=lambda m: json.loads(m.decode('utf-8')))

# Set up a Kafka producer instance
producer = KafkaProducer(bootstrap_servers=bootstrap_servers,
                         value_serializer=lambda x: json.dumps(x).encode('utf-8'))

print(f'Kafka consumer created! Listening for {bootstrap_servers} on topic {consume_topic}')

# Continuously listen for new messages
try:
    for message in consumer:
        # Parse the message as a JSON object
        message_dict = message.value
        owner = message_dict['owner']
        repo = message_dict['repo']
        
        # Process the values of the 'owner' and 'repo' fields
        print(f"Received message: owner={owner}, repo={repo}")
        
        # Do action
        # Connect to the MySQL database using the configuration options
        cnx = mysql.connector.connect(
            user=datasource['user'],
            password=datasource['password'],
            host=datasource['host'],
            port=datasource['port'],
            database=datasource['database']
        )
        cur = cnx.cursor()

        # Define the SQL query to select the data
        query = 'SELECT * FROM violation_comment_match WHERE owner=%s AND repo=%s AND label is null'

        # Execute the query and store the results in a DataFrame
        df = pd.read_sql(query, params=(owner, repo), con=cnx)
        if df.empty:
            print(f"No results found: {owner}/{repo}")
        else:
            df = df.fillna('')
            df = clean_texts(df)

            print(f"Fetched rows: {len(df)}")

            # Iterate over each row in the DataFrame and update the data in the database
            for index, row in df.iterrows():
                # Define the SQL query to update the row
                emb1 = model.encode(row['clean_type_detail'] + row['vio_code'])
                emb2 = model.encode(row['clean_comment'] + row['cmt_code'])
                sim = util.cos_sim(emb1, emb2)
                label = 1 if sim >= threshold else 0
                update_query = "UPDATE violation_comment_match SET label = %s WHERE id = %s"
                # Execute the update query with the values from the current row
                cur.execute(update_query, (label, row['id']))
                # Commit the changes to the database
                cnx.commit()

        # Close the database connection
        cur.close()
        cnx.close()

        # Update repo status
        repo_dict = {'owner': owner, 'repo': repo, 'labelled': True}
        producer.send('status', value=repo_dict)
        producer.send('postprocessing-event', value=repo_dict)
        # Wait for any outstanding messages to be transmitted and delivery reports received
        producer.flush()
        print(f"Labeling completed: owner={owner}, repo={repo}")
except Exception as e:
    print(f"Exception occurred: {str(e)}")
finally:
    consumer.close()
    producer.close()