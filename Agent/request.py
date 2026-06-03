import requests

def http_request_get(token: str,url: str,param: dict) :
    response = requests.get(
        url = url,
        params = param,
        headers = {
            "token": token,
            "Content-Type": "application/json"
        },
    )
    data = response.json()
    if data["code"] != 1000:
        return data["msg"]
    return data["data"]

def http_request_post(token: str,url: str,json: dict) :
    response = requests.post(
        url = url,
        headers = {
            "token": token,
            "Content-Type": "application/json"
        },
        json = json
    )
    data = response.json()
    if data["code"] != 1000:
        return data["msg"]
    return data["data"]

def http_request_delete(token: str,url: str,param: dict) :
    response = requests.delete(
        url = url,
        params = param,
        headers = {
            "token": token,
            "Content-Type": "application/json"
        }
    )
    data = response.json()
    if data["code"] != 1000:
        return data["msg"]
    return data["data"]