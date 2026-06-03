from request import *
from langchain.tools import tool,ToolRuntime

def get_user_info_by_phone(phone: str,token) :
    url = "http://localhost:8080/user/info"
    params = {"phone": phone}
    return http_request_get(token, url, params)

def get_friend_apply_list(token):
    url = "http://localhost:8080/conversation/apply/list"
    return http_request_get(token, url, None)

def get_conversation_list(token):
    url = "http://localhost:8080/conversation/list"
    return http_request_get(token, url,None)

@tool
def send_friend_apply_by_phone(phone: str,runtime: ToolRuntime) :
    """
    This is a function for send friend apply by phone
    This tool can only invoke when send apply
    Args:
        phone : phone number
        runtime : runtime
    """
    token = runtime.state["token"]
    user_info = get_user_info_by_phone(phone,token)
    if isinstance(user_info, str):
        return f"用户信息错误：{user_info}"
    user_id = int(user_info.get("id"))
    url = "http://localhost:8080/conversation/apply"
    json = {"receiverId": user_id}
    return http_request_post(token, url, json)

@tool
def get_friend_apply(runtime: ToolRuntime) :
    """
    This is a function for get friend apply
    This tool can only query current user's friend apply but can't query other user's friend apply
    if invoke this tool,you need to claim the result is for current user
    Args:
        runtime : runtime
    """
    token = runtime.state["token"]
    return get_friend_apply_list(token)

@tool
def accept_friend_apply(nickname: str,phone: str,runtime: ToolRuntime) :
    """
    This is a function for accept or agree friend apply through nickname or phone
    Args:
        nickname : the nickname of sender
        phone : the phone of sender
        runtime : runtime
    """
    token = runtime.state["token"]
    friend_apply_list = get_friend_apply_list(token)
    if isinstance(friend_apply_list, str):
        return f"查找好友信息列表失败: {friend_apply_list}"
    for friend_apply in friend_apply_list:
        if (nickname is not None and friend_apply['nickname'] == nickname) or (phone is not None and friend_apply['phone'] == phone):
            url = "http://localhost:8080/conversation/apply/agree"
            json = {
                "friendRequestId": int(friend_apply['friendRequestId']),
                "senderId": int(friend_apply['senderId']),
            }
            return http_request_post(token, url, json)
    return "未查询到对应好友申请"

def send_message_by_nickname(nickname: str,content: str,runtime: ToolRuntime) :
    """
    This is a function for send message
    while invoke this tool,you needn't query the friend relationship
    Args:
        nickname : the nickname of conversation
        content : the message to send
        runtime : runtime
    """
    token = runtime.state["token"]
    conversation_list = get_conversation_list(token)
    if isinstance(conversation_list, str):
        return f"会话查询失败: {conversation_list}"
    for conversation in conversation_list:
        if nickname is not None and conversation['nickname'] == nickname:
            url = "http://localhost:8080/conversation/send/message"
            json = {
                "conversationId": conversation['conversationId'],
                "content": content,
            }
            return http_request_post(token, url, json)
    return "未查询到对应会话"

@tool
def modify_my_user_info(runtime: ToolRuntime,nickname: str | None = None,email: str | None = None) :
    """
    This is a function for modify user info
    This tool can only invoke when modify self user info
    This tool can use for only modify nickname or email
    Args:
        nickname : the nickname to modify
        email : the email to modify
        runtime : runtime
    """
    token = runtime.state["token"]
    user_info = get_user_info_by_phone(None,token)
    url = "http://localhost:8080/user/info/modify"
    if isinstance(user_info, str):
        return f"未查询到个人信息: {user_info}"
    if email is None:
        email = user_info["email"]
    if nickname is None:
        nickname = user_info["nickname"]
    response =  http_request_post(token, url, {"nickname": nickname, "email": email})
    if isinstance(response,str):
        return f"修改个人信息失败{response}"
    return "成功"

@tool
def delete_friend(runtime: ToolRuntime,nickname: str) :
    """
    This is a function for delete friend
    This tool can only invoke when delete friend relationship
    Args:
        nickname : the nickname to delete
        runtime : runtime
    """
    token = runtime.state["token"]
    conversation_list = get_conversation_list(token)
    if isinstance(conversation_list, str):
        return f"会话查询失败: {conversation_list}"
    for conversation in conversation_list:
        if nickname is not None and conversation['nickname'] == nickname:
            url = "http://localhost:8080/conversation/delete"
            json = {"conversationId": conversation['conversationId']}
            return http_request_delete(token, url, json)
    return "未查询到对应好友"

tool_list = [
    send_friend_apply_by_phone,
    get_friend_apply,
    accept_friend_apply,
    send_message_by_nickname,
    modify_my_user_info,
    delete_friend,
]