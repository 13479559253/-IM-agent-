package com.example.demo.constant;

public class WebSocketConstant {

    //非发送方收到新消息（需要更改会话列表的最新消息和会话框内的消息【如果没在当前会话框则不更新】加未读数加1）
    public static final String WEBSOCKET_RECEIVE_MESSAGE = "receiveMessage";

    //发送方发送完新消息后（需要更换会话列表的最新消息和会话框内消息【如果没在当前会话框不更新】）
    public static final String WEBSOCKET_SEND_MESSAGE = "sendMessage";

    //通知对方有新的好友申请
    public static final String WEBSOCKET_SEND_APPLY = "sendApply";

    //通过好友申请后通知对方增加会话数
    public static final String WEBSOCKET_RECEIVE_APPLY = "receiveApply";

    //点开会话框后清除前端展示的未读数
    public static final String WEBSOCKET_CLEAR_UNREAD = "clearUnread";

    //更改个人消息，前端导航栏更新
    public static final String WEBSOCKET_INFO_MODIFY = "modifyInfo";

    //删除好友信号，通知前端更新会话列表
    public static final String WEBSOCKET_FRIEND_DELETE = "friendDelete";
}
