package com.example.acfun.permissionstudy;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by acfun on 2016/12/1.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private HashMap<Integer,RequestPermissionCallback> permissionCallbackHashMap=new HashMap<>();
    private List<Integer> requestCodes=new ArrayList<>();

    /**
     * 申请成功的回调接口
     */
    public interface RequestPermissionCallback{
        void onRequestCallback();
    }

    private String[] findDeniedPermissions(String...permissions){
        List<String> permissionList=new ArrayList<>();
        for (String perm:permissions){
            if (ContextCompat.checkSelfPermission(this,perm)!=
                    PackageManager.PERMISSION_GRANTED){
                permissionList.add(perm);
            }
        }
        return permissionList.toArray(new String[permissionList.size()]);
    }

    protected  void hasPermissions(int requestCode,RequestPermissionCallback callback,String... permissions){
        permissionCallbackHashMap.put(requestCode,callback);
        requestCodes.add(requestCode);
        String[] deniedPermissions=findDeniedPermissions(permissions);
        if (deniedPermissions.length>0){
            ActivityCompat.requestPermissions(this,permissions,requestCode);
        }else {
            callback.onRequestCallback();
        }
    }

    private boolean verifyPermissions(int [] grantResults){
        for (int result:grantResults){
            if (result!=PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean grandResult=verifyPermissions(grantResults);
        for (int code:requestCodes){
            if (code==requestCode){
                if (grandResult){
                    permissionCallbackHashMap.get(code).onRequestCallback();
                }else {
                    ActivityCompat.requestPermissions(this,permissions, requestCode);
                }
            }
        }
    }
}
