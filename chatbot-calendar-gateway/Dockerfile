FROM python:3.11-slim

WORKDIR /app

COPY chatbot_gateway_app.py /app/
COPY requirements.txt /app/
COPY .env /app/

RUN pip install -r requirements.txt

CMD ["python","chatbot_gateway_app.py"]