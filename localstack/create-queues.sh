#!/bin/bash

echo "[init] Creating SQS queues..."
awslocal sqs create-queue --queue-name message-analysis-queue || true
awslocal sqs list-queues || true
echo "[init] done."