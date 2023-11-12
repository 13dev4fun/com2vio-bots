# FT-Model Service
A fine-tuned SBERT model service as a kafka consumer

## How to run it
1. download fine-tuned sbert model from [here](https://drive.google.com/file/d/1dBksMlJt7tbUc8Orpc3MtTiV-dzrZYES/view?usp=sharing), extract it to `./ft-model` (This model is fine-tuned using `dataset.csv` and script `ft_sbert.py`)
2. run `pip install -r requirements.txt`
3. rename `config.tmpl` as `config.yml`, update with your database config
4. run `python main.py`, a kafka consumer is listening on topic `label-event`
5. you can run command shown below to send a message to the kafka consumer
    ```bash
    # use kafka-console-producer command to connect to the kafka cluster and specify the topic
    kafka-console-producer --broker-list localhost:9092 --topic label-event

    # Once connected, provide message payload in json like shown below,
    # please ensure violation_comment_match records for this repo already exist in your database
    {"owner":"google","repo":"gson"}
    ```