package com.imooc.service;

import java.util.List;

import com.imooc.netty.ChatMsg;
import com.imooc.pojo.Users;
import com.imooc.pojo.vo.FriendRequestVO;
import com.imooc.pojo.vo.MyFriendsVO;

public interface UserService {
	
	/**
	 * 判断用户名是否存在
	 * @param username
	 * @return
	 */
	public boolean queryUsernameIsExist(String username);
	
	/**
	 * 查询用户是否存在
	 * @param username
	 * @param pwd
	 * @return
	 */
	public Users queryUserForLogin(String username, String pwd);
	
	/**
	 * 用户注册
	 * @param user
	 * @return
	 * @throws Exception 
	 */
	public Users saveUser (Users user) throws Exception;
	
	/**
	 * 修改用户记录
	 * @param user
	 */
	public Users updateUserInfo(Users user);
	
	/**
	 * 搜索朋友的前置条件
	 * @param myUserId
	 * @param friendUsername
	 * @return
	 */
	public Integer preconditionSearchFriends(String myUserId, String friendUsername);
	
	
	/**
	 * 根据用户名查询用户对象
	 * @param username
	 * @return
	 */
	public Users queryUserInfoByUsername(String username);
	
	/**
	 * 添加好友请求记录, 保存到数据库
	 * @param myUserId
	 * @param friendUsername
	 */
	public void sendFriendRequest(String myUserId, String friendUsername);
	
	/**
	 * 查询好友请求
	 * @param acceptUserId
	 * @return
	 */
	public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);
	
	/**
	 * 删除好友请求记录
	 * @param sendUserId
	 * @param acceptUserId
	 */
	public void deleteFriendRequest(String sendUserId, String acceptUserId);
	
	/**
	 * 通过好友请求
	 * 	1. 保存好友
	 * 	2. 逆向保存好友
	 * 	3. 删除好友请求记录
	 * @param sendUserId
	 * @param acceptUserId
	 */
	public void passFriendRequest(String sendUserId, String acceptUserId);
	
	/**
	 * 查询好友列表
	 * @param userId
	 * @return
	 */
	public List<MyFriendsVO> queryMyFriends(String userId);
	
	/**
	 * 保存聊天消息到数据库
	 * @param chatMsg
	 * @return
	 */
	public String saveMsg(ChatMsg chatMsg);
	
	/**
	 * 批量签收消息
	 * @param msgIdList
	 */
	public void updateMsgSigned(List<String> msgIdList);
	
	/**
	 * 获取未签收消息列表
	 * @param acceptUserId
	 * @return
	 */
	public List<com.imooc.pojo.ChatMsg> getUnReadMsgList(String acceptUserId);
}
