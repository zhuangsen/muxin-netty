package com.zs.service;

import com.zs.netty.ChatMessage;
import com.zs.pojo.ChatMsg;
import com.zs.pojo.Users;
import com.zs.pojo.vo.FriendRequestVO;
import com.zs.pojo.vo.MyFriendsVO;

import java.util.List;

/**
 * @auther: madison
 * @date: 2018-10-04 21:18
 * @description:
 */
public interface UserService {

    /**
     * 判断用户名是否存在
     * @param username
     * @return
     */
    boolean queryUsernameIsExist(String username);

    /**
     * 查询用户是否存在
     * @param username
     * @param pwd
     * @return
     */
    Users queryUserForLogin(String username, String pwd);

    /**
     * 用户注册
     * @param user
     * @return
     */
    Users saveUser(Users user);

    /**
     * 修改用户记录
     */
    Users updateUserInfo(Users user);

    /**
     * 搜索朋友的前置条件
     * @param myUserId
     * @param friendUsername
     * @return
     */
    Integer preconditionSearchFriend(String myUserId, String friendUsername);

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    Users queryUserInfoByUsername(String username);

    /**
     * 添加好友请求记录，保存到数据库
     * @param myUserId
     * @param friendUsername
     */
    void sendFriendRequest(String myUserId, String friendUsername);

    /**
     * 查询好友请求
     * @param acceptUserId
     * @return
     */
    List<FriendRequestVO> queryFriednRequestList(String acceptUserId);

    /**
     * 删除好友请求记录
     * @param sendUserId
     * @param acceptUserId
     */
    void deleteFriendRequest(String sendUserId, String acceptUserId);

    /**
     * 通过好友请求：1.保存好友; 2.逆向保存好友; 3.删除好友请求记录
     * @param sendUserId
     * @param acceptUserId
     */
    void passFriendRequest(String sendUserId, String acceptUserId);

    /**
     * 查询我的好友列表
     * @param userId
     * @return
     */
    List<MyFriendsVO> queryMyFriends(String userId);

    /**
     * 保存聊天消息到数据库
     * @param chatMessage
     * @return
     */
    String saveMsg(ChatMessage chatMessage);

    /**
     * 批量签收消息
     * @param msgIdList
     */
    void updateMsgSigned(List<String> msgIdList);

    /**
     * 获取未签收的消息列表
     * @param acceptUserId
     * @return
     */
    List<ChatMsg> getUnReadMsgList(String acceptUserId);
}
