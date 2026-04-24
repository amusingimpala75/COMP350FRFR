FROM eclipse-temurin:21.0.10_7-jre
WORKDIR /app
# Copy in the Java backend
COPY backend/build/libs/backend-1.0-SNAPSHOT-all.jar app.jar
# Install python
RUN apt-get update && apt-get install -y python3 python3-pip python3-venv
# Copy in the python backend
COPY python-backend/ ./python-backend/
# Create venv
RUN python3 -m venv python-backend/venv
# Install the python backend deps
RUN ./python-backend/venv/bin/pip install -r python-backend/requirements.txt
EXPOSE 7070 8000
CMD ["sh", "-c", "java -jar app.jar & cd python-backend && /app/python-backend/venv/bin/uvicorn main:app --reload --host 0.0.0.0"]
