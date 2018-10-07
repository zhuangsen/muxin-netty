package com.zs.netty;

import com.zs.SpringUtil;
import com.zs.enums.MsgActionEnum;
import com.zs.service.UserService;
import com.zs.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @auther: madison
 * @date: 2018-10-04 10:31
 * @description: 处理消息的handler
 * TextWebSocketFrame： 在netty中，是用于为websocket专门处理文本的对象，frame是消息的载体
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    // 用于记录和管理所有客户端的channel
    public static ChannelGroup users =
            new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 获取客户端传输过来的信息
        String content = msg.text();

        Channel currenChannel = ctx.channel();

        // 1. 获取客户端发来的消息
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        Integer action = dataContent.getAction();
        // 2. 判断消息类型
        if(action == MsgActionEnum.CONNECT.type){
            // 2.1 当websocket第一次open的时候，初始化channel,把用户的channel和userid关联起来
            String senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(senderId, currenChannel);

            // 测试
            for (Channel c : users) {
                System.out.println(c.id().asLongText());
            }

            UserChannelRel.output();
        }else if(action == MsgActionEnum.CHAT.type){
            // 2.2 聊天类型的消息，把聊天记录保存到数据库, 同时标记消息的签收状态[未签收]
            ChatMessage chatMessage = dataContent.getChatMsg();
            String msgText = chatMessage.getMsg();
            String receiverId = chatMessage.getReceiverId();
            String senderId = chatMessage.getSenderId();

            // 保存消息到数据库，并且标记为 未签收
            UserService userService = (UserService)SpringUtil.getBean("userServiceImpl");
            String msgId = userService.saveMsg(chatMessage);
            chatMessage.setMsgId(msgId);

            DataContent dataContentMessage = new DataContent();
            dataContentMessage.setChatMsg(chatMessage);

            // 发送消息
            // 从全局用户Channel关系中获取接收方的channel
            Channel receiverChannel = UserChannelRel.get(receiverId);
            if(receiverChannel == null){
                // TODO channel 为空代表用户离线，推送信息(JPush, 个推，小米推送)
            }else{
                // 当receiverChannel不为空的时候，从ChannelGroup去查找对应的channel是否存在
                Channel findChannel = users.find(receiverChannel.id());
                if(findChannel != null){
                    // 用户在线
                    receiverChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContentMessage)));
                }else {
                    // 用户离线 TODO 推送消息
                }
            }
        }else if(action == MsgActionEnum.SIGNED.type){
            // 2.3 签收消息类型，针对具体的消息进行签收，修改数据库中对应消息的签收状态[已签收]
            UserService userService = (UserService)SpringUtil.getBean("userServiceImpl");
            // 扩展字段在signed类型的消息中，代表需要去签收的消息id，逗号分隔
            String msgIdsStr = dataContent.getExtand();
            String msgIds[] = msgIdsStr.split(",");

            List<String> msgIdList = new ArrayList<>();
            for (String mid : msgIds) {
                if(StringUtils.isNoneBlank(mid)){
                    msgIdList.add(mid);
                }
            }
            System.out.println(msgIdList.toString());
            if(msgIdList != null && !msgIdList.isEmpty() && msgIdList.size() >0){
                // 批量签收
                userService.updateMsgSigned(msgIdList);
            }
        }else if(action == MsgActionEnum.KEEPALIVE.type){
            // 2.4 心跳类型的消息
            System.out.println("收到来自channel为["+currenChannel+"]的心跳包");
        }
    }

    /**
     * 当客户端连接服务端之后(打开连接)
     * 获取客户端的channel，并且放到ChannelGroup中去进行管理
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        users.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 当触发handlerRemoved，ChannelGrouop会自动移除对应客户端的channel
        users.remove(ctx.channel());

        System.out.println("客户端断开, channel对应的长id为：" + ctx.channel().id().asLongText());
//        System.out.println("客户端断开, channel对应的短id为：" + ctx.channel().id().asShortText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生异常之后关闭连接(关闭channel)，随后从ChannelGroup移除
        ctx.channel().close();
        users.remove(ctx.channel());
    }
}
