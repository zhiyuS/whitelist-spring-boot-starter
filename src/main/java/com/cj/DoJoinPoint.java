package com.cj;

import com.alibaba.fastjson.JSON;
import com.cj.annotion.DoWhiteList;
import org.apache.commons.beanutils.BeanUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class DoJoinPoint {
    private Logger logger = LoggerFactory.getLogger(DoJoinPoint.class);

    @Resource
    private String whiteListConfig;
    @Pointcut("@annotation(com.cj.annotion.DoWhiteList)")
    public void aopPoint(){};

    @Around("aopPoint()")
    public Object doRouter(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = getMethod(joinPoint);
        DoWhiteList doWhiteList = method.getAnnotation(DoWhiteList.class);
        String filedValue = getFiledValue(doWhiteList.key(), joinPoint.getArgs());
        if(filedValue == null || "".equals(filedValue)) return joinPoint.proceed();
        String[] split = whiteListConfig.split(",");
        for (String s : split) {
            if(s.equals(filedValue)) return joinPoint.proceed();
        }
        return returnJson(doWhiteList,method);
    }
    public Object returnJson(DoWhiteList doWhiteList,Method method) throws Exception{

        String returnJson = doWhiteList.returnJson();
        Class<?> returnType = method.getReturnType();
        if("".equals(returnJson)){
            return returnType.newInstance();
        }
        return JSON.parseObject(returnJson,returnType);
    }
    public Method getMethod(ProceedingJoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method;
    }
    // 获取属性值
    private String getFiledValue(String filed, Object[] args) {
        String filedValue = null;
        for (Object arg : args) {
            try {
                if (null == filedValue || "".equals(filedValue)) {
                    filedValue = BeanUtils.getProperty(arg, filed);
                } else {
                    break;
                }
            } catch (Exception e) {
                if (args.length == 1) {
                    return args[0].toString();
                }
            }
        }
        return filedValue;
    }

}
