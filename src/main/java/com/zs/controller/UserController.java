package com.zs.controller;

import com.zs.enums.OperatorFriendRequestTypeEnum;
import com.zs.enums.SearchFriendsStatusEnum;
import com.zs.pojo.ChatMsg;
import com.zs.pojo.Users;
import com.zs.pojo.bo.UsersBO;
import com.zs.pojo.vo.MyFriendsVO;
import com.zs.pojo.vo.UsersVO;
import com.zs.service.UserService;
import com.zs.utils.FastDFSClient;
import com.zs.utils.FileUtils;
import com.zs.utils.IMoocJSONResult;
import com.zs.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.zs.utils.FileUtils.fileToMultipart;

/**
 * @auther: madison
 * @date: 2018-10-04 17:50
 * @description:
 */
@RestController
@RequestMapping("u")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FastDFSClient fastDFSClient;

    @PostMapping("registOrLogin")
    public IMoocJSONResult registOrLogin(@RequestBody Users user) throws Exception {
        // 0. 判断用户名和密码不能为空
        if (StringUtils.isBlank(user.getUsername())
                || StringUtils.isBlank(user.getPassword())) {
            return IMoocJSONResult.errorMsg("用户名或密码不能为空");
        }

        // 1.判断用户是否存在,如果存在就登陆，如果不存在则注册
        boolean usernameIsExist = userService.queryUsernameIsExist(user.getUsername());
        Users userResult = null;
        if (usernameIsExist) {
            // 1.1登录
            userResult = userService.queryUserForLogin(user.getUsername()
                    , MD5Utils.getMD5Str(user.getPassword()));
            if (userResult == null) {
                return IMoocJSONResult.errorMsg("用户名或密码不正确...");
            }
        } else {
            // 1.2注册
            user.setNickname(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            userResult = userService.saveUser(user);
        }

        UsersVO userVO = new UsersVO();
        BeanUtils.copyProperties(userResult, userVO);
        return IMoocJSONResult.ok(userVO);
    }

    @PostMapping("uploadFaceBase64")
    public IMoocJSONResult uploadFaceBase64(@RequestBody UsersBO usersBO) {
        // 获取前端传过来的base64字符串，然后转换为文件对象再上传
        String base64Data = usersBO.getFaceData();
        String userFacePath = "D:\\" + usersBO.getUserId() + "userface64.png";
        Users users = null;
        try {
            FileUtils.base64ToFile(userFacePath, base64Data);

            //上传文件到Fastdfs
            MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
            String path = fastDFSClient.uploadFace(faceFile);
            System.out.println(path);

            // 获取缩略图的url
            String thump = "_80x80.";
            String arr[] = path.split("\\.");
            String thumpImgUrl = arr[0] + thump + arr[1];

            // 更新用户头像
            users = new Users();
            users.setId(usersBO.getUserId());
            users.setFaceImage(thumpImgUrl);
            users.setFaceImageBig(path);

            users = userService.updateUserInfo(users);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return IMoocJSONResult.ok(users);
    }

    @PostMapping("setNickName")
    public IMoocJSONResult setNickName(@RequestBody UsersBO userBO) {
        Users users = new Users();
        users.setId(userBO.getUserId());
        users.setNickname(userBO.getNickName());

        Users result = userService.updateUserInfo(users);
        return IMoocJSONResult.ok(result);
    }

    /**
     * 搜索好友
     *
     * @param myUserId
     * @param friendUsername
     * @return
     */
    @PostMapping("search")
    public IMoocJSONResult searchUser(String myUserId, String friendUsername) {
        // 0. 判断myUserId friendUsername 不能为空
        if (StringUtils.isBlank(myUserId)
                || StringUtils.isBlank(friendUsername)) {
            return IMoocJSONResult.errorMsg("");
        }

        // 前置条件 - 1. 搜索的用户如果不存在，返回[无此用户]
        // 前置条件 - 2. 搜索账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Integer status = userService.preconditionSearchFriend(myUserId, friendUsername);
        if (status == SearchFriendsStatusEnum.SUCCESS.status) {
            Users friend = userService.queryUserInfoByUsername(friendUsername);
            UsersVO usersVO = new UsersVO();
            BeanUtils.copyProperties(friend, usersVO);
            return IMoocJSONResult.ok(usersVO);
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IMoocJSONResult.errorMsg(errorMsg);
        }
    }

    /**
     * 添加好友请求
     * @param myUserId
     * @param friendUsername
     * @return
     */
    @PostMapping("addFriendRequest")
    public IMoocJSONResult addFriendRequest(String myUserId, String friendUsername){
        // 0. 判断myUserId friendUsername 不能为空
        if (StringUtils.isBlank(myUserId)
                || StringUtils.isBlank(friendUsername)) {
            return IMoocJSONResult.errorMsg("");
        }

        // 前置条件 - 1. 搜索的用户如果不存在，返回[无此用户]
        // 前置条件 - 2. 搜索账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Integer status = userService.preconditionSearchFriend(myUserId, friendUsername);
        if (status == SearchFriendsStatusEnum.SUCCESS.status) {
            userService.sendFriendRequest(myUserId, friendUsername);
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IMoocJSONResult.errorMsg(errorMsg);
        }
        return IMoocJSONResult.ok();
    }

    /**
     * 接收到的朋友申请
     * @param userId
     * @return
     */
    @PostMapping("queryFriednRequests")
    public IMoocJSONResult queryFriednRequests(String userId){
        // 0. 判断userId 不能为空
        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("");
        }
        // 1. 查询用户接收到的朋友申请
        return IMoocJSONResult.ok(userService.queryFriednRequestList(userId));
    }

    /**
     * 接收方 忽略或者通过好友请求
     * @param acceptUserId
     * @param sendUserId
     * @param operType
     * @return
     */
    @PostMapping("operFriednRequest")
    public IMoocJSONResult operFriednRequest(String acceptUserId, String sendUserId, Integer operType){
        // 0. acceptUserId,sendUserId 不能为空
        if (StringUtils.isBlank(acceptUserId)
                || StringUtils.isBlank(sendUserId)
                || operType == null) {
            return IMoocJSONResult.errorMsg("");
        }

        // 1. 如果operType 没有对应的枚举值，则直接抛出空错误信息
        if(StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))){
            return IMoocJSONResult.errorMsg("");
        }

        if(operType == OperatorFriendRequestTypeEnum.IGNORE.type){
            // 2. 判断如果忽略好友请求，则直接删除好友请求的数据库记录
            userService.deleteFriendRequest(sendUserId, acceptUserId);
        }else if(operType == OperatorFriendRequestTypeEnum.PASS.type){
            // 3. 判断如果通过好友请求，则互相增加好友记录到数据库对应的表
            //    然后删除好友请求的数据库表记录
            userService.passFriendRequest(sendUserId, acceptUserId);
        }
        // 4. 数据库查询好友列表
        List<MyFriendsVO> myFriends = userService.queryMyFriends(acceptUserId);
        return IMoocJSONResult.ok(myFriends);
    }

    /**
     * 查询我的好友列表
     * @param userId
     * @return
     */
    @PostMapping("myFriends")
    public IMoocJSONResult operFriednRequest(String userId){
        // 0. userId 不能为空
        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("");
        }

        // 1. 数据库查询好友列表
        List<MyFriendsVO> myFriends = userService.queryMyFriends(userId);
        return IMoocJSONResult.ok(myFriends);
    }

    /**
     * 用户手机端获取未签收的消息列表
     * @param acceptUserId
     * @return
     */
    @PostMapping("getUnReadMsgList")
    public IMoocJSONResult getUnReadMsgList(String acceptUserId){
        // 0. acceptUserId 不能为空
        if (StringUtils.isBlank(acceptUserId)) {
            return IMoocJSONResult.errorMsg("");
        }

        // 1. 查询列表
        List<ChatMsg> unReadMsgList = userService.getUnReadMsgList(acceptUserId);
        return IMoocJSONResult.ok(unReadMsgList);
    }


}
