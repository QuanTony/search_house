package com.project.search.config.interceptor;

import com.alibaba.fastjson.JSON;
import com.project.search.common.utils.JsonMapper;
import com.project.search.common.utils.JwtUtil;
import com.project.search.common.utils.RedisUtil;
import com.project.search.common.utils.ResultHelper;
import com.project.search.config.interf.PassPermission;
import com.project.search.config.interf.RequiresPermission;
import com.project.search.config.interf.UrlPermission;
import com.project.search.constants.JwtConstants;
import com.project.search.dao.model.User;
import com.project.search.service.SysPermissionService;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;


@Component
public class JWTInterceptor implements HandlerInterceptor {

	private final static Logger logger = LoggerFactory.getLogger(JWTInterceptor.class);

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private SysPermissionService sysPermissionService;

	@Value("${jwt.anonUrl}")
	private String[] anonUrlArr;

    @Value("${jwt.rejectUrl}")
    private String[] rejectUrlArr;

    public String[] getAnonUrlArr() {
        return anonUrlArr;
    }

    public void setAnonUrlArr(String[] anonUrlArr) {
        this.anonUrlArr = anonUrlArr;
    }

    public String[] getRejectUrlArr() {
        return rejectUrlArr;
    }

    public void setRejectUrlArr(String[] rejectUrlArr) {
        this.rejectUrlArr = rejectUrlArr;
    }

	/**
	 *
	 * @param request
	 * @param response
	 * @param handler
	 * @return
	 * @throws Exception
	 */
    @Override
	public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception{
		ResultHelper resultHelper = new ResultHelper();
        //获取请求的url后缀
		String requestUri = request.getRequestURI();
		String contextPath = request.getContextPath();
		String url = requestUri.substring(contextPath.length());
        //获取请求的token
		String token = JwtUtil.extractJwtTokenFromAuthorizationHeader(request.getHeader("authorization"));
		logger.info("本次请求url:"+url+",请求中的token:"+token);

		//白名单放行
		if(anonUrlArr!=null && anonUrlArr.length>0){
			for(String anonUrl : anonUrlArr){
				if(url.contains(anonUrl)){
					return true;
				}
			}
		}

		//拒绝访问
		if(rejectUrlArr!=null && rejectUrlArr.length>0){
			for(String rejectUrl : rejectUrlArr){
				if(url.contains(rejectUrl)){
					doResponse(response, resultHelper.newFailResult("维护中，请联系管理员"));
					return false;
				}
			}
		}

		//判断需不需要跳过验证
		HandlerMethod handlerMethod = (HandlerMethod) handler;
		PassPermission passPermission = handlerMethod.getBeanType().getAnnotation(PassPermission.class);
		if(null == passPermission){
			passPermission = handlerMethod.getMethodAnnotation(PassPermission.class);
			if(null != passPermission){
				return true;
			}
		} else {
			return true;
		}

		//其它url 验证token
		if(StringUtils.isBlank(token)){
			doResponse(response, resultHelper.newFailResult("无效的token"));
			return false;
		}
		User user = null;
		try{
			//把token解密成cliam获取用户的subject Json
			Claims claims = JwtUtil.parseJWT(token);
			String subject = claims.getSubject();

			if(StringUtils.isBlank(subject)){
				doResponse(response, resultHelper.newFailResult("无效的token"));
				return false;
			}else{
				user = JsonMapper.fromJsonString(subject, User.class);
				//获取用户存在redis里的token
				String serverToken = (String)redisUtil.get(JwtConstants.JWT_TOKEN_USER_ID + user.getId());
				if(StringUtils.isBlank(serverToken)){
					doResponse(response, resultHelper.newFailResult("token超时|无效"));
					return false;
				}
				if(!serverToken.equals(token)){
					doResponse(response, resultHelper.newFailResult("已在另一处地方登录"));
					return false;
				}
				//鉴权
				//首先判断controller有没有注解，没有在判断方法上有没有注解
				RequiresPermission requiresPermission = handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);
				if(null == requiresPermission){
					requiresPermission = handlerMethod.getMethodAnnotation(RequiresPermission.class);
					if(null == requiresPermission){
						//更新缓存时间//重新设定token在Redis中的时间
						//CacheUtils.set(CacheConstant.KEY_PREFIX_JWT_USER_ID+operator.getId(), token, ExpireTime.HALF_AN_HOUR);
						return true;
					}
				}
				//此处判断的是菜单级别的
				if(StringUtils.isNotBlank(requiresPermission.value())){
					//查询数据库中是否有权限用户ID和菜单权限标识
					Long l = sysPermissionService.checkPermission(user.getId(), requiresPermission.value());
					if(l == null){
						doResponse(response, resultHelper.newFailResult("无操作权限"));
						return false;
					}
				}
				//此处判断的是具体controller方法级别的
				UrlPermission urlPermission = handlerMethod.getMethodAnnotation(UrlPermission.class);
				if (null != urlPermission && urlPermission.isNeed()){
					List<String> actionMaps = sysPermissionService.getUserActionUrls(user.getId());
					if (!actionMaps.contains(url)){
						doResponse(response, resultHelper.newFailResult("无操作接口权限"));
						return false;
					}
				}
				//更新缓存时间,重新设定token在Redis中的时间
				redisUtil.set(JwtConstants.JWT_TOKEN_USER_ID + user.getId(), token, JwtConstants.JWT_EXPIRE_TIME);
				return true;
			}

		}catch (Exception e){
			logger.error("token处理失败", e);
			doResponse(response,resultHelper.newFailResult("token处理失败"));

		}

		return false;
	}


	private void doResponse(HttpServletResponse response, Map map) throws Exception{
//		response.setContentType("application/json;charset=UTF-8");
//		PrintWriter out = response.getWriter();
//		out.print(JSON.toJSON(map));

		response.setContentType("text/html; charset=UTF-8");//注意text/html，和application/html
		response.getWriter().print("<!DOCTYPE html>\n" +
				"<html lang=\"en\">\n" +
				"<head>\n" +
				"    <meta charset=\"UTF-8\">\n" +
				"    <title>Forbidden</title>\n" +
				"</head>\n" +
				"<body>\n" +
				"<h1>Oops!!! Access Denied</h1>\n" +
				"<h2>你无权访问该页面</h2>\n" +
				"<a href=\"/index\">返回首页</a>\n" +
				"</body>\n" +
				"</html>");
		response.getWriter().close();
	}


	@Override
	public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {

	}

	@Override
	public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex) {
	}
}
