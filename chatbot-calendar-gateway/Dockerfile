FROM python:3.11-slim

WORKDIR /app

COPY /src/chatbot_gateway_app.py /app/
COPY src/resources/ /app/resources/

COPY requirements.txt /app/

RUN pip install -r requirements.txt

CMD ["python","chatbot_gateway_app.py"]