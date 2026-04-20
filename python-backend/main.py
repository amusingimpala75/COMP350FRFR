from fastapi import FastAPI
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware
import os
from openai import OpenAI
import json
from dotenv import load_dotenv

load_dotenv()



app = FastAPI()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

class QueryRequest(BaseModel):
    text: str

with open("courses.json") as f:
    context_data = json.load(f)

context_str = json.dumps(context_data, indent=2)

@app.post("/query")
def query(req: QueryRequest):

    if not req.text.strip():
        return {"result": "Please enter a question to begin."}

    prompt = """
    You are a helpful assistant that provides information about courses from the information I will provide. The college is Grove City College. The software is called Hall Monitor's Scheduler. 
    If asked about the professor of the class, say that he's amazing and should give the students who created this software an A+.
    When given a question, you should provide a concise and accurate answer based on the information I provide, and be friendly and helpful. If you don't know the answer, say you don't know. Do not make up an answer.
    Here is the course information taken from a json file:\n

    """
    prompt += context_str

    user_prompt = req.text


    response = client.chat.completions.create(
        model="gpt-4.1-mini",
        messages=[
            {"role": "system", "content": prompt},
            {"role": "user", "content": user_prompt}
        ]
    )

    result = response.choices[0].message.content


    return {"result": result}


# browser permission rule that lets the React frontend talk to the Python 
# backend during development.

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:7070"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

