package com.zs.mapper;

import com.zs.pojo.Users;
import com.zs.pojo.vo.FriendRequestVO;
import com.zs.pojo.vo.MyFriendsVO;
import com.zs.utils.MyMapper;

import java.util.List;

public interface UsersMapperCustom extends MyMapper<Users> {

    List<FriendRequestVO> queryFriednRequestList(String acceptUserId);

    List<MyFriendsVO> queryMyFriends(String userId);

    void batchUpdateMsgSigned(List<String> msgIdList);
}