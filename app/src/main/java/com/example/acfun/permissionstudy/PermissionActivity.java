package com.example.acfun.permissionstudy;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import com.example.acfun.permissionstudy.special.BaseActivity;

/**
 * Created by acfun on 2016/11/30.
 */

public class PermissionActivity extends BaseActivity implements View.OnClickListener{

    private Button mCallView;
    private Button mDownFileView;
    private Button location;
    private static int test=2333;

    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_layout);
        initView();
    }

    /**
     * 初始化UI
     */
    private void initView(){
        mCallView= (Button) findViewById(R.id.callPhone);
        mDownFileView= (Button) findViewById(R.id.downFile);
        location= (Button) findViewById(R.id.location);
        mCallView.setOnClickListener(this);
        mDownFileView.setOnClickListener(this);
        location.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.callPhone:
            {
                callPhone();
            }
                break;
            case R.id.downFile:
            {
                sdCardPermission();
            }
                break;
            case R.id.location:
            {
                locationPermission();
//                showMissingPermissionDialog();
            }
                break;
        }
    }

    /**
     * 拨打电话
     */
    private void callPhone(){
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
//            //权限申请处理
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.CALL_PHONE},
//                    1);
//        }else {
//            doCallPhone();
//        }
        hasPermissions(1, new RequestPermissionCallback() {
            @Override
            public void onRequestCallback() {
                doCallPhone();
            }
        }, Manifest.permission.CALL_PHONE);
    }

    private void sdCardPermission(){
//        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
////            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
////
////            }else {
////                showMissingPermissionDialog();
////            }
//            //权限申请处理
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    0);
//        }else {
//            doSDCard();
//        }
        hasPermissions(0, new RequestPermissionCallback() {
            @Override
            public void onRequestCallback() {
                doSDCard();
            }
        },Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void locationPermission(){
//        String[] deniedPerms=findDeniedPermissions(needPermissions);
//        if (deniedPerms.length>0){
//            ActivityCompat.requestPermissions(this,deniedPerms,3);
//        }else {
//
//        }
        hasPermissions(3, new RequestPermissionCallback() {
            @Override
            public void onRequestCallback() {

            }
        }, needPermissions);
    }

    private String[] findDeniedPermissions(String ... permissions){
        ArrayList<String > permissionList=new ArrayList<>();
        for (String perm:permissions){
            if (ContextCompat.checkSelfPermission(this,perm)!=PackageManager.PERMISSION_GRANTED){
                permissionList.add(perm);
            }
        }
        return permissionList.toArray(new String[permissionList.size()]);
    }

    private void showPermissionDialog(String message, final int requestCode, final String ...permissions){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(PermissionActivity.this,permissions,requestCode);
                    }
                }).create().show();
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     *
     */
    public void showMissingPermissionDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.notifyMsg);

        // 拒绝, 退出应用
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
                    }
                });

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

    //http://images.csdn.net/20150817/1.jpg
    private void doSDCard(){
        if (isExternalStorageWritable()){
            new Thread(){
                @Override
                public void run() {
                    try {
                        File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath());
                        if (!file.exists()){
                            file.mkdirs();
                        }
                        File newFile=new File(file.getPath(),System.currentTimeMillis()+".jpg");
//                        newFile.createNewFile();
                        URL url = new URL("http://images.csdn.net/20150817/1.jpg");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        InputStream inputStream = connection.getInputStream();
                        FileOutputStream fileOutputStream = new FileOutputStream(newFile.getAbsolutePath());
                        byte[] bytes = new byte[1024];
                        int len = 0;
                        while ((len = inputStream.read(bytes)) != -1) {
                            fileOutputStream.write(bytes);
                        }
                        inputStream.close();
                        fileOutputStream.close();
                        connection.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }.start();
        }else {
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setMessage("外部存储不可用！");
            builder.create().show();
        }
    }

    private void doCallPhone(){
        Intent intent=new Intent(Intent.ACTION_CALL);
        Uri data=Uri.parse("tel:"+"10086");
        intent.setData(data);
        startActivity(intent);
    }

    public boolean isExternalStorageWritable(){
        String state= Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        return false;
    }

    private boolean verifyPermissions(int[] grantResults){
        for (int result:grantResults){
            if (result!=PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

//    private boolean verifyShouldShowRequestPermissions(String[] permissions){
//        for (String permission:permissions){
//            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,permission)){
//                return false;
//            }
//        }
//        return true;
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode){
//            case 1:{
//                boolean isGrand=ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CALL_PHONE);
//                //拨号权限回调处理
//                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
//                    doCallPhone();
//                }else {
//                    //提示用户权限未被授予
//                    if (isGrand){
//                        showPermissionDialog("need call phone permission.",1,Manifest.permission.CALL_PHONE);
//                    }else {
//                        showMissingPermissionDialog();
//                    }
//                }
//            }
//            break;
//            case 0:{
//                boolean isGrand=ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
//                    doSDCard();
//                }else {
//                    //提示用户权限未被授予
//                    if (isGrand){
//                        showPermissionDialog("need external write permission.",1,Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                    }else {
//                        showMissingPermissionDialog();
//                    }
//                }
//            }
//            break;
//            case 3:{
//                if (verifyPermissions(grantResults)){
//
//                }else {
//                    if (verifyShouldShowRequestPermissions(permissions)){
//                        ActivityCompat.requestPermissions(this,permissions,3);
//                    }else {
//                        showMissingPermissionDialog();
//                    }
//                }
//            }
//            break;
//        }
//    }
}
