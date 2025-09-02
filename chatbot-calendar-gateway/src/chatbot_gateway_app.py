from telegram import Update
from telegram.ext import ApplicationBuilder, MessageHandler, CommandHandler, filters, ContextTypes
from confluent_kafka import Producer

from langdetect import detect, DetectorFactory


import os
import json
import logging
import base64
import tempfile


DetectorFactory.seed = 0
TOKEN = os.getenv("TELEGRAM_TOKEN")

KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS")
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC")
SYSTEM_LANGUAGE = os.getenv("SYSTEM_LANGUAGE", "en")
USERS_FILE = "users_seen.json"



logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)

logger = logging.getLogger(__name__)

# Configure Kafka producer
producer = Producer({'bootstrap.servers': KAFKA_BOOTSTRAP_SERVERS})


def delivery_report(err, msg):
    if err is not None:
        logger.error(f"Message delivery failed: {err}")
    else:
        logger.info(f"Message delivered to {msg.topic()} [{msg.partition()}]")


# Load seen users
if os.path.exists(USERS_FILE):
    with open(USERS_FILE, "r") as f:
        users_seen = set(json.load(f))
else:
    users_seen = set()


async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    user_id = update.effective_user.id
    chat_id = update.effective_chat.id

    if user_id not in users_seen:
        welcome_message = (
            "שלום\n\n"
            "ברוך/ה הבא/ה ל-Calendar Bot שלנו! \n\n"
            "כעת ניתן לשלוח:\n"
            " ✍️ הודעת טקסט חופשית\n"
            " 🎙️ הודעה קולית (תישלח ל-Kafka כמו שהיא)\n\n"
            "הבוט ידאג לתעד את האירועים ביומן שלך או להחזיר את לוח הזמנים."
        )
        await context.bot.send_message(chat_id=chat_id, text=welcome_message, parse_mode="Markdown")

        # Save user as seen
        users_seen.add(user_id)
        with open(USERS_FILE, "w") as f:
            json.dump(list(users_seen), f)

def detect_language(text: str) -> str:
    try:
        return detect(text)
    except Exception:
        return "und"


async def handle_message(update: Update, context: ContextTypes.DEFAULT_TYPE):
    logger.info(f"RAW UPDATE: {update.to_dict()}")

    user_id = update.effective_user.id
    chat_id = update.effective_chat.id
    user_text = update.message.text

    first_name = update.effective_user.first_name
    last_name = update.effective_user.last_name
    username = update.effective_user.username
    tz_name = os.getenv("TIME_ZONE", "UTC")

    await context.bot.send_message(chat_id=chat_id, text="הודעת טקסט התקבלה. אנחנו על זה!")

    data = {
        "userId": user_id,
        "text": user_text,
        "languageCode": detect_language(user_text),
        "firstName": first_name,
        "lastName": last_name,
        "username": username,
        "timeZone": tz_name,
        "type": "text"
    }

    producer.produce(KAFKA_TOPIC, json.dumps(data).encode("utf-8"), callback=delivery_report)
    producer.poll(0)

async def handle_voice(update: Update, context: ContextTypes.DEFAULT_TYPE):
    logger.info(f"VOICE UPDATE: {update.to_dict()}")

    user_id = update.effective_user.id
    chat_id = update.effective_chat.id
    voice = update.message.voice

    first_name = update.effective_user.first_name
    last_name = update.effective_user.last_name
    username = update.effective_user.username
    tz_name = os.getenv("TIME_ZONE", "UTC")

    file = await context.bot.get_file(voice.file_id)
    with tempfile.NamedTemporaryFile(delete=False, suffix=".ogg") as tmp_file:
        await file.download_to_drive(tmp_file.name)
        local_path = tmp_file.name

    try:
        with open(local_path, "rb") as f:
            voice_bytes = f.read()
            voice_base64 = base64.b64encode(voice_bytes).decode("utf-8")


        await context.bot.send_message(chat_id=chat_id, text="הודעה קולית התקבלה. אנחנו על זה!")

        data = {
            "userId": user_id,
            "voiceFileId": voice.file_id,
            "voiceDuration": voice.duration,
            "voiceBase64": voice_base64,
            "languageCode": SYSTEM_LANGUAGE,
            "firstName": first_name,
            "lastName": last_name,
            "username": username,
            "timeZone": tz_name,
            "type": "voice"
        }

        producer.produce(KAFKA_TOPIC, json.dumps(data).encode("utf-8"), callback=delivery_report)
        producer.poll(0)
    finally:
        if os.path.exists(local_path):
            os.remove(local_path)
            logger.info(f"Temporary file deleted: {local_path}")


def main():
    app = ApplicationBuilder().token(TOKEN).build()
    app.add_handler(CommandHandler("start", start))
    app.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, handle_message))
    app.add_handler(MessageHandler(filters.VOICE, handle_voice))
    logger.info("Bot is Up ...")
    app.run_polling()


if __name__ == "__main__":
    main()
