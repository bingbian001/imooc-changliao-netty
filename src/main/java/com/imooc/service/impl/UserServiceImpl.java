package com.imooc.service.impl;

import java.util.Date;
import java.util.List;

import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.imooc.enums.MsgActionEnum;
import com.imooc.enums.MsgSignFlagEnum;
import com.imooc.enums.SearchFriendsStatusEnum;
import com.imooc.mapper.ChatMsgMapper;
import com.imooc.mapper.FriendsRequestMapper;
import com.imooc.mapper.MyFriendsMapper;
import com.imooc.mapper.UsersMapper;
import com.imooc.mapper.UsersMapperCustom;
import com.imooc.netty.ChatMsg;
import com.imooc.netty.DataContent;
import com.imooc.netty.UserChannelRel;
import com.imooc.pojo.FriendsRequest;
import com.imooc.pojo.MyFriends;
import com.imooc.pojo.Users;
import com.imooc.pojo.vo.FriendRequestVO;
import com.imooc.pojo.vo.MyFriendsVO;
import com.imooc.service.UserService;
import com.imooc.utils.FastDFSClient;
import com.imooc.utils.FileUtils;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.MD5Utils;
import com.imooc.utils.QRCodeUtils;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UsersMapper userMapper;
	
	@Autowired
	private UsersMapperCustom userMapperCustom;
	
	@Autowired
	private MyFriendsMapper myFriendsMapper;
	
	@Autowired
	private FriendsRequestMapper friendsRequestMapper;
	
	@Autowired
	private ChatMsgMapper chatMsgMapper;
	
	@Autowired
	private Sid sid;
	
	@Autowired
	private QRCodeUtils qrCodeUtils;
	
	@Autowired
	private FastDFSClient fastDFSClient;
	
	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public boolean queryUsernameIsExist(String username) {
		
		Users user = new Users();
		user.setUsername(username);
		
		Users result = userMapper.selectOne(user); //查询用户名是否存在
		
		return result != null ?  true : false;
	}

	@Transactional(propagation = Propagation.SUPPORTS) // 事务管理
	@Override
	public Users queryUserForLogin(String username, String pwd) {
		
		Example userExample = new Example(Users.class); // 逆向工具的工具类， 通过example拿到相应的条件
		
		Criteria criteria = userExample.createCriteria(); // 创建一个条件， 可以去做额外的条件查询， 不用去自定义sql查询
		criteria.andEqualTo("username", username);
		criteria.andEqualTo("password", pwd);
		
		Users result = userMapper.selectOneByExample(userExample);
		
		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED) // 事务管理
	@Override
	public Users saveUser(Users user) throws Exception {

		String userId = sid.nextShort(); // 生成唯一的id
		user.setId(userId);
		user.setNickname(user.getUsername());
		user.setFaceImage("");
		user.setFaceImageBig("");
		user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
		
		
		// 每一个用户生成一个二维码
		String qrCodePath = "F://user" + userId + "qrcode.png";
		// changliao_qrcode:[username]
		qrCodeUtils.createQRCode(qrCodePath, "changliao_qrcode:" + user.getUsername());
		MultipartFile qrcodeFile = FileUtils.fileToMultipart(qrCodePath);
		String qrCodeUrl = fastDFSClient.uploadQRCode(qrcodeFile);
		user.setQrcode(qrCodeUrl); 
		
		userMapper.insert(user);
		
		return user;
	}

	@Transactional(propagation = Propagation.REQUIRED) // 事务管理
	@Override
	public Users updateUserInfo(Users user) {
		userMapper.updateByPrimaryKeySelective(user);
		return queryUserById(user.getId());
	}
	
	@Transactional(propagation = Propagation.SUPPORTS) // 事务管理
	private Users queryUserById(String userId){
		return userMapper.selectByPrimaryKey(userId);
	}

	@Transactional(propagation = Propagation.SUPPORTS) // 事务管理
	@Override
	public Integer preconditionSearchFriends(String myUserId, String friendUsername) {
		
		//1. 搜索的用户如果不存在， 返回[无此用户]
		Users user = queryUserInfoByUsername(friendUsername);
		if(user == null){
			return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
		}
		
		//2. 搜索的账号是你自己， 返回[不能添加自己]
		if(user.getId().equals(myUserId)){
			return SearchFriendsStatusEnum.NOT_YOURSELF.status;
		}
		
		//3. 搜索的用户已经是你的好友， 返回[该用户已经是你的好友]
		Example myFriendExample = new Example(MyFriends.class); // 逆向工具的工具类， 通过example拿到相应的条件
		Criteria criteria = myFriendExample.createCriteria();
		criteria.andEqualTo("myUserId", myUserId);
		criteria.andEqualTo("myFriendUserId", user.getId());
		MyFriends myFriendsRel = myFriendsMapper.selectOneByExample(myFriendExample);
		if(myFriendsRel != null){
			return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
		}
		
		return SearchFriendsStatusEnum.SUCCESS.status;
	}
	
	@Transactional(propagation = Propagation.SUPPORTS) // 事务管理
	@Override
	public Users queryUserInfoByUsername(String username){
		Example userExample = new Example(Users.class); // 逆向工具的工具类， 通过example拿到相应的条件
		Criteria criteria = userExample.createCriteria();
		criteria.andEqualTo("username", username);
		return userMapper.selectOneByExample(userExample);
	}

	@Transactional(propagation = Propagation.REQUIRED) // 事务管理
	@Override
	public void sendFriendRequest(String myUserId, String friendUsername) {
		
		// 根据用户名把朋友信息查询出来
		Users friend = queryUserInfoByUsername(friendUsername);
		
		// 查询发送好友请求记录表
		Example friendsExample = new Example(FriendsRequest.class); // 逆向工具的工具类， 通过example拿到相应的条件
		Criteria criteria = friendsExample.createCriteria();
		criteria.andEqualTo("sendUserId", myUserId);
		criteria.andEqualTo("acceptUserId", friend.getId());
		
		FriendsRequest friendsRequest = friendsRequestMapper.selectOneByExample(friendsExample);
		if(friendsRequest == null){
			// 如果不是你的好友， 并且好友记录没有添加， 则新增好友请求记录
			String requestId = sid.nextShort();
			
			FriendsRequest request = new FriendsRequest();
			request.setId(requestId);
			request.setSendUserId(myUserId);
			request.setAcceptUserId(friend.getId());
			request.setRequestDateTime(new Date());
			friendsRequestMapper.insert(request);
		}
	}

	@Transactional(propagation = Propagation.SUPPORTS) // 事务管理
	@Override
	public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
		return userMapperCustom.queryFriendRequestList(acceptUserId);
	}

	@Transactional(propagation = Propagation.REQUIRED) // 事务管理
	@Override
	public void deleteFriendRequest(String sendUserId, String acceptUserId) {
		
		// 删除好友请求记录
		Example friendsExample = new Example(FriendsRequest.class); // 逆向工具的工具类， 通过example拿到相应的条件
		Criteria criteria = friendsExample.createCriteria();
		criteria.andEqualTo("sendUserId", sendUserId);
		criteria.andEqualTo("acceptUserId", acceptUserId);
		friendsRequestMapper.deleteByExample(friendsExample);
	}

	@Transactional(propagation = Propagation.REQUIRED) // 事务管理
	@Override
	public void passFriendRequest(String sendUserId, String acceptUserId) {
		saveFriends(sendUserId, acceptUserId);
		saveFriends(acceptUserId, sendUserId);
		deleteFriendRequest(sendUserId, acceptUserId);
		
		Channel sendChannel = UserChannelRel.get(sendUserId);
		if(sendChannel != null){
			// 使用websocket主动推送消息到请求发起者， 更新他的通讯录列表为最新
			DataContent dataContent = new DataContent();
			dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
			sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED) // 事务管理
	private void saveFriends(String sendUserId, String acceptUserId){
		MyFriends myFriends = new MyFriends();
		String recordId = sid.nextShort();
		myFriends.setId(recordId);
		myFriends.setMyFriendUserId(acceptUserId);
		myFriends.setMyUserId(sendUserId);
		myFriendsMapper.insert(myFriends);
		
	}

	@Transactional(propagation = Propagation.SUPPORTS) // 事务管理
	@Override
	public List<MyFriendsVO> queryMyFriends(String userId) {
		return userMapperCustom.queryMyFriends(userId);
	}

	@Transactional(propagation = Propagation.REQUIRED) // 事务管理
	@Override
	public String saveMsg(ChatMsg chatMsg) {
		
		com.imooc.pojo.ChatMsg msgDB = new com.imooc.pojo.ChatMsg();
		String msgId = sid.nextShort();
		msgDB.setId(msgId);
		msgDB.setAcceptUserId(chatMsg.getReceiverId());
		msgDB.setSendUserId(chatMsg.getSenderId());
		msgDB.setCreateTime(new Date());
		msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
		msgDB.setMsg(chatMsg.getMsg());
		
		chatMsgMapper.insert(msgDB);
		
		return msgId;
	}

	@Transactional(propagation = Propagation.REQUIRED) // 事务管理
	@Override
	public void updateMsgSigned(List<String> msgIdList) {
		userMapperCustom.batchUpdateMsgSigned(msgIdList);
	}

	@Transactional(propagation = Propagation.SUPPORTS) // 事务管理
	@Override
	public List<com.imooc.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {
		
		Example chatExample = new Example(com.imooc.pojo.ChatMsg.class); // 逆向工具的工具类， 通过example拿到相应的条件
		Criteria criteria = chatExample.createCriteria();
		criteria.andEqualTo("signFlag", 0);
		criteria.andEqualTo("acceptUserId", acceptUserId);
		
		List<com.imooc.pojo.ChatMsg> result = chatMsgMapper.selectByExample(chatExample);
		
		return result;
	}
}
