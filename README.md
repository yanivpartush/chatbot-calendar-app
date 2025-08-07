# Chatbot Calendar Application - Dev system properties


## chatbot-calendar-gateway

```
1. put valid telegram TOKEN in .env file

```

## chatbot-calendar-service ( Not executed as port of docker-compose )

```
1. define tokens in application.yaml file :  api-key ( Open-AI ) and bot-token ( Telegram )

2. Add to config folder jwt for google calendar connection : google-calendar-credentials.json

3. need to cofigure CONFIG_DIR in Run configuration with full path  - > ..\chatbot-calendar-app\chatbot-calendar-service\config

4. need to add the argument : spring.config.location = file:${CONFIG_DIR}/application.yaml

```

## chatbot-calendar-app

```

1. under ..\chatbot-calendar-app\ run the following cmd : docker-compose -p chatbot-calendar-app up -d --build

```




