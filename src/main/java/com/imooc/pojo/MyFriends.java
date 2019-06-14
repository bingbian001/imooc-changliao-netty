package com.imooc.pojo;

import javax.persistence.*;

@Table(name = "my_friends")
public class MyFriends {
    @Id
    private String id;

    /**
     * 用户id
     */
    @Column(name = "my_user_id")
    private String myUserId;

    /**
     * 朋友用户Id
     */
    @Column(name = "my_friend_user_id")
    private String myFriendUserId;

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取用户id
     *
     * @return my_user_id - 用户id
     */
    public String getMyUserId() {
        return myUserId;
    }

    /**
     * 设置用户id
     *
     * @param myUserId 用户id
     */
    public void setMyUserId(String myUserId) {
        this.myUserId = myUserId;
    }

    /**
     * 获取朋友用户Id
     *
     * @return my_friend_user_id - 朋友用户Id
     */
    public String getMyFriendUserId() {
        return myFriendUserId;
    }

    /**
     * 设置朋友用户Id
     *
     * @param myFriendUserId 朋友用户Id
     */
    public void setMyFriendUserId(String myFriendUserId) {
        this.myFriendUserId = myFriendUserId;
    }
}