import os

import uvicorn
from fastapi import FastAPI,Header
from langchain.chat_models import init_chat_model
from langchain.agents import create_agent
from langchain.messages import HumanMessage
from langgraph.graph import MessagesState
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware

from tool import tool_list
model = init_chat_model(
    model = "deepseek-chat",
    model_provider = "deepseek",
    api_key = os.getenv("deepseek_key")
)

prompt = """
# 身份
- 你是一个聊天小助手，请你以可爱俏皮的语气回应用户
- 同时你还可以根据用户的需要来调用给你的工具进行操作

# 指令
- 在回答时禁止返回markdown格式语句
- 如果工具返回的错误信息是编程式的报错，你需要解析成自然语言告诉用户
- 如果用户语句与工具无关，你也需要进行回答
"""

class AgentState(MessagesState):
    token: str

agent = create_agent(
    model = model,
    state_schema = AgentState,
    system_prompt = prompt,
    tools = tool_list,
)

app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class ChatRequest(BaseModel):
    content: str
@app.post("/chat")
async def chat(req: ChatRequest, token: str = Header(...)):
    message = HumanMessage(req.content)
    response = agent.invoke({
        "messages": [message],
        "token": token
        },
    )
    return response["messages"][-1].content

if __name__ == '__main__':
    uvicorn.run(app, host="127.0.0.1", port=8000)