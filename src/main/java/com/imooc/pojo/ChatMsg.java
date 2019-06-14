package com.imooc.pojo;

import java.util.Date;
import javax.persistence.*;

@Table(name = "chat_msg")
public class ChatMsg {
    @Id
    private String id;

    /**
     * 发送用户的id
     */
    @Column(name = "send_user_id")
    private String sendUserId;

    /**
     * 接收用户的id
     */
    @Column(name = "accept_user_id")
    private String acceptUserId;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 签收的状态，1已读或 0未读
     */
    @Column(name = "sign_flag")
    private Integer signFlag;

    /**
     * 记录创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

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
     * 获取发送用户的id
     *
     * @return send_user_id - 发送用户的id
     */
    public String getSendUserId() {
        return sendUserId;
    }

    /**
     * 设置发送用户的id
     *
     * @param sendUserId 发送用户的id
     */
    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    /**
     * 获取接收用户的id
     *
     * @return accept_user_id - 接收用户的id
     */
    public String getAcceptUserId() {
        return acceptUserId;
    }

    /**
     * 设置接收用户的id
     *
     * @param acceptUserId 接收用户的id
     */
    public void setAcceptUserId(String acceptUserId) {
        this.acceptUserId = acceptUserId;
    }

    /**
     * 获取消息内容
     *
     * @return msg - 消息内容
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置消息内容
     *
     * @param msg 消息内容
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 获取签收的状态，1已读或 0未读
     *
     * @return sign_flag - 签收的状态，1已读或 0未读
     */
    public Integer getSignFlag() {
        return signFlag;
    }

    /**
     * 设置签收的状态，1已读或 0未读
     *
     * @param signFlag 签收的状态，1已读或 0未读
     */
    public void setSignFlag(Integer signFlag) {
        this.signFlag = signFlag;
    }

    /**
     * 获取记录创建时间
     *
     * @return create_time - 记录创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置记录创建时间
     *
     * @param createTime 记录创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}