from telegram import Update
from telegram.ext import ApplicationBuilder, MessageHandler, filters, ContextTypes
from confluent_kafka import Producer
from dotenv import load_dotenv

import os
import json
import logging

load_dotenv() # Load .env file

TOKEN = os.getenv("TOKEN")
KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS")
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC")

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)

logger = logging.getLogger(__name__)

# Configure Kafka producer
producer = Producer({'bootstrap.servers': KAFKA_BOOTSTRAP_SERVERS})

def delivery_report(err, msg):
    """Called once for each message produced to indicate delivery result."""
    if err is not None:
        logger.error(f"Message delivery failed: {err}")
    else:
        logger.info(f"Message delivered to {msg.topic()} [{msg.partition()}]")

async def handle_message(update: Update, context: ContextTypes.DEFAULT_TYPE):
    logger.info(f"RAW UPDATE: {update.to_dict()}")
    user_id = update.effective_user.id
    chat_id = update.effective_chat.id
    user_text = update.message.text

    first_name = update.effective_user.first_name
    last_name = update.effective_user.last_name
    username = update.effective_user.username

    # 1. Immediate reply to user
    await context.bot.send_message(chat_id=chat_id, text="Message received! We'll process it soon.")

    # 2. Push the message to Kafka (non-blocking)
    data = {
        "userId": user_id,
        "chatId": chat_id,
        "text": user_text,
        "firstName": first_name,
        "lastName": last_name,
        "username": username
    }

    # Serialize as string (or JSON)
   
    producer.produce(KAFKA_TOPIC, json.dumps(data).encode('utf-8'), callback=delivery_report)
    producer.flush(1)

    # The bot does NOT do heavy work here, just queues the message

def main():
    app = ApplicationBuilder().token(TOKEN).build()
    app.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, handle_message))
    #logger.info("Yaniv TEST !!!!!")
    logger.info("Bot is running...")
    app.run_polling()

if __name__ == "__main__":
    main()
