package com.h5game.thirdpartycallback;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class ThirdPartyCallback {
    private Class<?> cClass;
    private Object obj;
    private int nCallbackId = -1;

    public Activity mActivity;

    public static final int CALL_SUCCESS = 1;
    public static final int CALL_FAILED = 2;

    public Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper())){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            switch (msg.what){
                case CALL_SUCCESS:
                    callSuccess(msg.obj);
                    break;
                case CALL_FAILED:
                    int callbackId = data.getInt("callbackId");
                    int errCode = data.getInt("errCode");
                    String errInfo = data.getString("errInfo");
                    callErr(callbackId, errCode, errInfo);
                    break;
                default:
                    break;
            }
        }
    };

    public ThirdPartyCallback(String className){
        try{
            cClass = Class.forName(className);
            obj = cClass.newInstance();
        } catch (ClassNotFoundException | IllegalArgumentException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Method GetMethod(String methodName, Class<?>... parameterTypes){
        Method method = null;
        try {
            if(parameterTypes == null)
                method = cClass.getMethod(methodName);
            else
                method = cClass.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }

    private Object ExecuteMethod(Method method, Object... args){
        Object result = null;
        try {
            if(args == null)
                result = method.invoke(obj);
            else
                result = method.invoke(obj, args);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void callbackToJS(int callbackId, Object data, int code, String msg){
        Method method = GetMethod("callbackToJS", int.class, Object.class, int.class, String.class);
        ExecuteMethod(method, callbackId, data, code, msg);
    }

    public void callSuccess(Object obj){
        if(nCallbackId != -1){
            callbackToJS(nCallbackId, obj, 0, "");
            resetCallbackId();
        }
    }

    public void callErr(int callbackId, int errCode, String msg){
        if(callbackId != -1) {
            callbackToJS(callbackId, "",  errCode, msg);
        }else if(nCallbackId != -1) {
            callbackToJS(nCallbackId, "",  errCode, msg);
            resetCallbackId();
        }
    }

    public boolean checkCallbackId(int callbackId){
        if(nCallbackId != -1){
            callErr(callbackId, 1, "当前尚有操作未完成，请稍后再试");
            return false;
        }

        setCallbackId(callbackId);
        return true;
    }

    public void resetCallbackId(){
        nCallbackId = -1;
    }

    public void setCallbackId(int callbackId){
        nCallbackId = callbackId;
    }

    public int getCallbackId() { return nCallbackId; }

    public void log(String str){
        Log.d("H5Game", str);
    }
}
