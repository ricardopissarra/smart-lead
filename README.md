# Smart Lead Qualification

A Spring Boot application that automatically qualifies sales leads from incoming messages using AI analysis via a HuggingFace model, asynchronous processing through AWS SQS, and persistent storage in PostgreSQL.

## Overview

When a message arrives (e.g., a customer inquiry), the system:

1. Saves the message and publishes it to an SQS queue
2. A listener picks up the message and sends it to an AI model for analysis
3. The AI determines whether the message represents a qualified lead, and if so, extracts its type, urgency, and a summary
4. The lead is persisted and linked back to the originating message
5. A scheduler periodically retries messages that failed or were never processed

## Architecture

```
POST /api/v1/messages
        │
        ▼
  MessageService ──► PostgreSQL (status: CREATED)
        │
        ▼
   SQS Queue
        │
        ▼
 SqsMessageListener
        │
        ▼
HuggingFaceLeadAnalyzerService
        │
   (AI Analysis)
        │
   ┌────┴────┐
   │         │
  Lead    No lead
created   created
   │
   ▼
PostgreSQL
(Message status: PROCESSED, linked to Lead)
```

### Retry & Resilience

A scheduled job (`RetryMessageAnalysisScheduler`) runs every 15 minutes to handle two failure scenarios:

- **Failed messages** — messages that encountered an error during analysis are retried
- **Stuck messages** — messages that remained in `CREATED` status for more than 15 minutes (e.g., lost SQS events) are reprocessed

## Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot |
| AI Integration | Spring AI + HuggingFace |
| Messaging | AWS SQS (via Spring Cloud AWS) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Local Dev | LocalStack (SQS emulation) |
| Containerisation | Docker Compose |

## Data Model

### Message

Represents a raw customer message.

| Field | Type | Description |
|---|---|---|
| `id` | Long | Primary key |
| `content` | String (LOB) | The message text |
| `status` | Enum | `CREATED`, `PROCESSING`, `PROCESSED`, `FAILED` |
| `lead` | Lead | Linked lead if one was created |
| `createdAt` | LocalDateTime | Set on persist |
| `updatedAt` | LocalDateTime | Set on update |

### Lead

A qualified lead extracted from a message.

| Field | Type | Description |
|---|---|---|
| `id` | Long | Primary key |
| `title` | String | Short lead title |
| `type` | Enum | `DEMO_REQUEST`, `PRICING_INQUIRY`, `PARTNERSHIP`, `SUPPORT`, `OTHER` |
| `urgencyLevel` | Enum | `LOW`, `MEDIUM`, `HIGH` |
| `description` | String | AI-generated summary |
| `message` | Message | The originating message |

## API Endpoints

### Messages

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/messages` | List all messages (paginated, sorted by `createdAt` ASC) |
| `POST` | `/api/v1/messages` | Submit a new message for analysis |

**POST body:**
```json
{
  "content": "How much does it cost to upgrade to the pro plan?"
}
```

### Leads

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/leads` | List all leads (paginated, sorted by `createdAt` ASC) |
| `GET` | `/api/v1/leads/{id}` | Get a lead by ID |

Full interactive API documentation is available at `/api/docs` when the application is running.

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- A HuggingFace API key (or enable the fake stub — see below)

### 1. Start infrastructure

```bash
docker compose up -d
```

This starts:
- **LocalStack** on port `4566` (SQS)
- **PostgreSQL** on port `5432`

### 2. Configure the application

Set the following properties in `application.yml` (or as environment variables):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smartlead
    username: myuser
    password: mysecretpassword

aws:
  queque-url: http://localhost:4566/000000000000/<your-queue-name>

app:
  queue:
    name: <your-queue-name>

# Set to true to skip real HuggingFace calls during development
enable:
  fake:
    hf: false
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

### AI Prompt

The prompt sent to the AI model is loaded from `src/main/resources/prompt/lead-prompt.txt`. Edit this file to tune how the model classifies messages and extracts lead data.

The model is expected to return a structured response matching `LeadAnalysisResult`:

```json
{
  "shouldCreateLead": true,
  "title": "Pricing Inquiry",
  "type": "PRICING_INQUIRY",
  "urgencyLevel": "HIGH",
  "description": "User is asking about the cost of the pro plan upgrade."
}
```

## Project Structure

```
src/main/java/com/rpissarra/smartleadqualification/
├── huggingface/        # AI integration (analyzer interface, service, result record)
├── lead/               # Lead entity, repository, service, controller, DTOs, enums
├── message/            # Message entity, repository, service, controller, DTOs, enums
├── scheduler/          # Retry scheduler for failed/stuck messages
└── sqs/                # SQS listener
src/main/resources/
└── prompt/
    └── lead-prompt.txt # System prompt for AI analysis
```