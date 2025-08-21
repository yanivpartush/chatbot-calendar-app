# Chatbot Calendar Application - Dev system properties

## IntelliJ IDEA Configuration

```
1. --spring.config.location=file:${CONFIG_DIR}/application.yaml --logging.config=file:${CONFIG_DIR}/logback-spring.xml

2. Need to define env vars : 
    - DB_USERNAME
    - DB_PASSWORD
    - OPENAI_API_KEY
    - TELEGRAM_TOKEN
    - KEY_STORE_PASSWORD

```

## chatbot-calendar-gateway

```
1. put valid telegram TOKEN in .env file

```

## chatbot-calendar-service ( Not executed as part of docker-compose )

```
1. define tokens in application.yaml file :  api-key ( Open-AI ) and bot-token ( Telegram )

2. Add to config folder jwt for google calendar connection : google-calendar-credentials.json

3. need to cofigure CONFIG_DIR in Run configuration with full path  - > ..\chatbot-calendar-app\chatbot-calendar-service\config

4. need to add the argument : spring.config.location = file:${CONFIG_DIR}/application.yaml

```

## chatbot-calendar-app

```

1. under ..\chatbot-calendar-app\ run the following cmd : docker-compose -p chatbot-calendar-app up -d --build

* In case you wanna restart the system please run : docker-compose -p chatbot-calendar-app down


SELECT * FROM telegramUserDetails.user;

SELECT * FROM telegramUserDetails.user_tokens;

SELECT * FROM telegramUserDetails.user_messages;

```
## Telegram Bot

```

Click here for bot -> https://t.me/yaniv_calendar_bot

```