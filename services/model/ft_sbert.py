#!/usr/bin/env python
# coding: utf-8

import pandas as pd
import numpy as np

import random
import re
import math
from datetime import datetime

import torch
from torch import nn
from torch.utils.data import DataLoader, Dataset
from torch.nn.functional import pairwise_distance

from sentence_transformers import SentenceTransformer, models, InputExample, losses, evaluation, util

from utils import clean_texts, get_head_words

# Set the seed value all over the place to make this reproducible.
seed_val = 42

random.seed(seed_val)
np.random.seed(seed_val)
torch.manual_seed(seed_val)
torch.cuda.manual_seed_all(seed_val)

# model config
max_length = 350 # ok, 352 oom 
batch_size = 16
epochs = 2
learning_rate = 2e-5
pretrained_model = 'sentence-transformers/all-mpnet-base-v2'
out_features = 256

dataset_path = './dataset.csv'

df = pd.read_csv(dataset_path)
df = df.fillna('')
df = clean_texts(df)
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
print(device)

data = []
for index, row in df.iterrows():
    data.append(InputExample(texts=[get_head_words(row['clean_type_detail'] + " </s> " + row['raw_code'], max_length), 
                                        get_head_words(row['clean_comment'] + " </s> " + row['raw_code'], max_length)],label=float(row['label'])))

print(len(data))
print(data[0])


start_time = datetime.now().strftime('%y-%m-%d-%H_%M')
output_path=model_save_path = './models/'+ start_time
print(output_path)

train_data = data
word_embedding_model = models.Transformer(pretrained_model, max_seq_length=max_length)
pooling_model = models.Pooling(768)
dense_model = models.Dense(in_features=pooling_model.get_sentence_embedding_dimension(), out_features=out_features, activation_function=nn.Tanh())
model = SentenceTransformer(modules=[word_embedding_model, pooling_model, dense_model])
train_loss = losses.ContrastiveLoss(model=model, distance_metric=losses.SiameseDistanceMetric.COSINE_DISTANCE, margin=0.9)

train_dataloader = DataLoader(train_data, shuffle=True, batch_size=batch_size, pin_memory=False)
warmup_steps = math.ceil(len(train_dataloader) * epochs * 0.1) #10% of train data for warm-up
model.fit(train_objectives=[(train_dataloader, train_loss)], epochs=epochs, warmup_steps=warmup_steps, output_path=model_save_path)

