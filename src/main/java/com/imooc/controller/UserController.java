package com.imooc.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.imooc.enums.OperatorFriendRequestTypeEnum;
import com.imooc.enums.SearchFriendsStatusEnum;
import com.imooc.pojo.ChatMsg;
import com.imooc.pojo.Users;
import com.imooc.pojo.po.UsersBO;
import com.imooc.pojo.vo.MyFriendsVO;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.UserService;
import com.imooc.utils.FastDFSClient;
import com.imooc.utils.FileUtils;
import com.imooc.utils.JSONResult;
import com.imooc.utils.MD5Utils;

@RestController
@RequestMapping("u")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private FastDFSClient fastDFSClient;
	
	private UsersVO userVO = new UsersVO();
	
	/**
	 * 登陆/注册
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/registOrLogin")
	public JSONResult registOrLogin(@RequestBody Users user) throws Exception{
		
		if(StringUtils.isBlank(user.getUsername())|| StringUtils.isBlank(user.getPassword())){ // 判断用户名和密码不能为空
			
			return JSONResult.errorMsg("用户名或密码不能为空...");
		}
		
		boolean usernameIsExits = userService.queryUsernameIsExist(user.getUsername()); // 判断用户名是否存在, 如果存在就登陆， 如果不存在则注册

		Users userResult = null;
		
		if(usernameIsExits){ // 登陆
			
			userResult = userService.queryUserForLogin(user.getUsername(), MD5Utils.getMD5Str(user.getPassword())); 
			
			if(userResult == null){
				return JSONResult.errorMsg("用户名或密码不正确...");
			}
			
		}else{ // 注册
			userResult = userService.saveUser(user);
		}
		
		BeanUtils.copyProperties(userResult, userVO); // 工具类方法， 把两个相似的类的属性做一个copy， 主要用于把想返回的属性返回， 比getset要来得快
		
		return JSONResult.ok(userVO);
	}
	
	/**
	 * 上传头像
	 * @param userBO
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/uploadFaceBase64")
	public JSONResult uploadFaceBase64(@RequestBody UsersBO userBO) throws Exception{
		// 获取前端传过来的base64字符串, 然后转换为文件对象再上传
		String base64Data = userBO.getFaceData();
		String userFacePath = "F:\\" + userBO.getUserId() + "userface64.png";
		FileUtils.base64ToFile(userFacePath, base64Data);
		
		// 上传文件到fastdfs
		MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
		String url = fastDFSClient.uploadBase64(faceFile);
		System.out.println(url);
		
		// 获取缩略图的url
		String thump = "_80x80.";
		String arr[] = url.split("\\.");
		String thumpImgUrl = arr[0] + thump + arr[1];
		
		// 更新用户头像
		Users user = new Users();
		user.setId(userBO.getUserId());
		user.setFaceImage(thumpImgUrl);
		user.setFaceImageBig(url);
		
		user = userService.updateUserInfo(user);
		
		BeanUtils.copyProperties(user, userVO);
		
		return JSONResult.ok(userVO);
	}
	
	/**
	 * 设置用户昵称
	 * @param userBO
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/setNickname")
	public JSONResult setNickname(@RequestBody UsersBO userBO) throws Exception{
		
		Users user = new Users();
		user.setId(userBO.getUserId());
		user.setNickname(userBO.getNickname());
		
		user = userService.updateUserInfo(user);
		
		BeanUtils.copyProperties(user, userVO);
		
		return JSONResult.ok(userVO);
	}
	
	/**
	 * 搜索好友接口, 根据账号做匹配查询而不是模糊查询
	 * @param userBO
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/search")
	public JSONResult searchUser(String myUserId, String friendUsername) throws Exception{
		
		if(StringUtils.isBlank(myUserId)|| StringUtils.isBlank(friendUsername)){ // 判断myUserId和friendUsername不能为空
			return JSONResult.errorMsg("");
		}
		
		// 前置条件 - 1. 搜索的用户如果不存在， 返回[无此用户]
		// 前置条件 - 2. 搜索的账号是你自己， 返回[不能添加自己]
		// 前置条件 - 3. 搜索的用户已经是你的好友， 返回[该用户已经是你的好友]
		Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
		
		if(status == SearchFriendsStatusEnum.SUCCESS.status){
			Users user = userService.queryUserInfoByUsername(friendUsername);
			BeanUtils.copyProperties(user, userVO);
			return JSONResult.ok(userVO);
		}else{
			String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
			return JSONResult.errorMsg(errorMsg);
		}
	}
	
	
	/**
	 * 发送添加好友的请求
	 * @param userBO
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/addFriendRequest")
	public JSONResult addFriendRequest(String myUserId, String friendUsername) throws Exception{
		
		if(StringUtils.isBlank(myUserId)|| StringUtils.isBlank(friendUsername)){ // 判断myUserId和friendUsername不能为空
			return JSONResult.errorMsg("");
		}
		// 前置条件 - 1. 搜索的用户如果不存在， 返回[无此用户]
		// 前置条件 - 2. 搜索的账号是你自己， 返回[不能添加自己]
		// 前置条件 - 3. 搜索的用户已经是你的好友， 返回[该用户已经是你的好友]
		Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
		if(status == SearchFriendsStatusEnum.SUCCESS.status){
			userService.sendFriendRequest(myUserId, friendUsername);
		}else{
			String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
			return JSONResult.errorMsg(errorMsg);
		}
		
		return JSONResult.ok();
	}
	
	/**
	 * 发送添加好友请求
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/queryFriendRequests")
	public JSONResult queryFriendRequests(String userId) throws Exception{
		
		if(StringUtils.isBlank(userId)){ // 判断userId不能为空
			return JSONResult.errorMsg("");
		}
		
		//查询用户接受到的朋友申请
		return JSONResult.ok(userService.queryFriendRequestList(userId));
	}
	
	/**
	 * 接收方 通过或者忽略朋友请求
	 * @param acceptUserId
	 * @param sendUserId
	 * @param operType
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/operFriendRequest")
	public JSONResult operFriendRequest(String acceptUserId, String sendUserId, Integer operType) throws Exception{
		
		if(StringUtils.isBlank(acceptUserId) || StringUtils.isBlank(sendUserId) || operType == null){ // 判断acceptUserId,sendUserId,operType 不能为空
			return JSONResult.errorMsg("");
		}
		
		// 如果operType 没有对应的枚举值， 则直接抛出空错误信息
		if(StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))){
			return JSONResult.errorMsg("");
		}
		
		if(operType == OperatorFriendRequestTypeEnum.IGNORE.type){
			// 判断如果忽略好友请求， 则直接删除好友请求的数据库表记录
			userService.deleteFriendRequest(sendUserId, acceptUserId);
		}else if(operType == OperatorFriendRequestTypeEnum.PASS.type){
			// 判断如果是通过好友请求， 则互相增加好友记录到数据库对应的表
			// 然后删除好友请求的数据库表记录
			userService.passFriendRequest(sendUserId, acceptUserId);
		}
		
		// 数据库查询好友列表
		List<MyFriendsVO> myFriends = userService.queryMyFriends(acceptUserId);
		
		return JSONResult.ok(myFriends);
	}
	
	/**
	 * 查询我的好友列表
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/myFriends")
	public JSONResult myFriends(String userId) throws Exception{
		
		if(StringUtils.isBlank(userId)){ // 判断userId不能为空
			return JSONResult.errorMsg("");
		}
		
		// 数据库查询好友列表
		List<MyFriendsVO> myFriends = userService.queryMyFriends(userId);
		return JSONResult.ok(myFriends);
	}
	
	/**
	 * 用户手机端获取未签收的消息列表
	 * @param acceptUserId
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/getUnReadMsgList")
	public JSONResult getUnReadMsgList(String acceptUserId) throws Exception{
		
		if(StringUtils.isBlank(acceptUserId)){ // 判断userId不能为空
			return JSONResult.errorMsg("");
		}
		
		// 查询列表
		List<ChatMsg> unreadMsgList = userService.getUnReadMsgList(acceptUserId);
		
		return JSONResult.ok(unreadMsgList);
	}
}
