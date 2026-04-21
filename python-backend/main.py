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

with open("Comp.txt") as f:
    context_data = f.read()

context_str = json.dumps(context_data, indent=2)

@app.post("/query")
def query(req: QueryRequest):

    if not req.text.strip():
        return {"result": "Please enter a question to begin."}

    prompt = """
    You are a helpful assistant that provides information or gives advice based on the required courses and suggested four-year plan for a certain major.
    When given a question, you should provide an accurate answer based on the information I provide, and be friendly and helpful. If you don't know the answer, say you don't know. Do not make up an answer.
    Don't make anything bold in the answer you return.
    If you're giving a response that includes a list of courses, please format the courses in a clear and organized way, such as using bullet points or numbering. Include newlines. This will make it easier for the user to read and understand the information.
    Here is the course information and four-year plan taken from the major requirements for a computer science major student:\n

    """
    prompt += context_str

    user_prompt = req.text


    response = client.chat.completions.create(
        model="gpt-5.4-nano",
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

