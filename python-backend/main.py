from fastapi import FastAPI
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware



app = FastAPI()

class QueryRequest(BaseModel):
    text: str

@app.post("/query")
def query(req: QueryRequest):
    return {"result": f"Received: {req.text}"}


# browser permission rule that lets the React frontend talk to the Python 
# backend during development.

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:7070"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

