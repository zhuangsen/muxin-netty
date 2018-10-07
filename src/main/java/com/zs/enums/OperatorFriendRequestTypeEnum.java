package com.zs.enums;

/**
 * @auther: madison
 * @date: 2018-10-06 10:51
 * @description:
 */
public enum OperatorFriendRequestTypeEnum {
    IGNORE(0, "忽略"),
    PASS(1, "通过");

    public final Integer type;
    public final String msg;

    OperatorFriendRequestTypeEnum(Integer type, String msg){
        this.type = type;
        this.msg = msg;
    }

    public Integer getType(){
        return type;
    }

    public static String getMsgByType(Integer type){
        for (OperatorFriendRequestTypeEnum operType : OperatorFriendRequestTypeEnum.values()) {
            if(operType.getType() == type){
                return operType.msg;
            }
        }
        return null;
    }
}
