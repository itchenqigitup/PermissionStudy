package com.example.acfun.permissionstudy.special;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.example.acfun.permissionstudy.R;

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
     * 防止一次申请多个权限时屏幕闪烁，多次弹窗
     */
    private HashMap<Integer,Boolean> needChecks=new HashMap<>();

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

    protected  void hasPermissions(int requestCode, RequestPermissionCallback callback, String... permissions){
        permissionCallbackHashMap.put(requestCode,callback);
        requestCodes.add(requestCode);
        needChecks.put(requestCode,true);
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

    /**
     * 当被用户拒绝授权并且出现不再提示时（小米在第一拒绝后再次申请就不会出现请求对话框了，不过对于call phone权限不同，这点再看），
     * shouldShowRequestPermissionRationale也会返回false，若实在必须申请权限时可以使用方法检测，
     * 可能会引起用户的厌恶感，慎用
     * @param permissions
     * @return
     */
    protected boolean verifyShouldShowRequestPermissions(String[] permissions){
        for (String permission:permissions){
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,permission)){
                return false;
            }
        }
        return true;
    }

    /**
     * 显示提示信息,与verifyShouldShowRequestPermissions(String[] permissions)搭配使用，提醒用户需要授权，并打开应用详细设置页面
     *
     * @since 2.5.0
     *
     */
    protected void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("当前应用缺少必要权限。\\n\\n请点击\\\"设置\\\"-\\\"权限\\\"-打开所需权限。");

//        // 拒绝, 退出应用
//        builder.setNegativeButton(R.string.cancel,
//                listener
//        );

        builder.setPositiveButton(R.string.setting,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }

    /**
     *  启动应用的设置
     *
     * @since 2.5.0
     *
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
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
                    if (verifyShouldShowRequestPermissions(permissions)){
                        final int code1=code;
                        final String[] permission=permissions;
                        AlertDialog.Builder builder=new AlertDialog.Builder(this);
                        builder.setMessage("定位功能需要定位权限，请授权。")
                               .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialogInterface, int i) {
                                        ActivityCompat.requestPermissions(BaseActivity.this,permission, code1);
                                   }
                               });
                    }else {
                        boolean needCheck=needChecks.get(code);
                        if (needCheck){
                            showMissingPermissionDialog();
                            needChecks.put(code,false);
                        }
                    }
                }
            }
        }
    }
}
