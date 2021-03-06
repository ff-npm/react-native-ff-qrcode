package com.ffomall.rns;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ffomall.rns.almns.RealPathFromUriUtils;
import com.ffomall.rns.qrcode.R;
import com.ffomall.rns.qrcodes.QRCodeView;
import com.ffomall.rns.zxings.ZingView;
import com.yanzhenjie.permission.AndPermission;

/**
 * Name:
 * <p>
 * 2019/7/12 by StoneWay
 * <p>
 * Outline:
 */
public class SacnActivity extends Activity implements QRCodeView.Delegate {

    static ZingView zxingview;
    ImageView mGoBack;
    ImageView mAlbum;
    static ImageView mLight;
    static boolean isOpenLighted = false;

    public final static int CHOOSE_REQUEST = 188;
    private static final int CODE_GALLERY_REQUEST = 0xa0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sa);
        Log.e("##扫描界面oncreat",System.currentTimeMillis()+"");
        zxingview = (ZingView) findViewById(R.id.zing);
        zxingview.setDelegate(this);
        zxingview.getScanBoxView().setOnlyDecodeScanBoxArea(true); // 仅识别扫描框中的码

        mGoBack = (ImageView) findViewById(R.id.toolbar_go_back);
        mGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zxingview.closeFlashlight();
                finish();
            }
        });
        mAlbum= (ImageView) findViewById(R.id.toolbar_menu_img);
        mAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
                AndPermission.with(SacnActivity.this).runtime().permission(perms)
                        .onGranted(permissions ->  scanAlbumQr())
                        .onDenied(permissions ->  {
                            scanResultCallBack.onScanSuccess(201, "NO_ALBUM", "");

                        })
                        .start();
            }
        });
        mLight= (ImageView) findViewById(R.id.light);
        mLight.setOnClickListener(v -> {
            if (isOpenLighted) {
                zxingview.closeFlashlight();
                mLight.setImageResource(R.mipmap.icon_light_white);
            } else {
                zxingview.openFlashlight();
                mLight.setImageResource(R.mipmap.icon_light_red);
            }
            isOpenLighted = !isOpenLighted;
        });
    }

    private void scanAlbumQr() {
        Intent intentFromGallery = new Intent(Intent.ACTION_PICK, null);
        intentFromGallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intentFromGallery, CODE_GALLERY_REQUEST);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.e("##扫描界面onStart",System.currentTimeMillis()+"");
        zxingview.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
        zxingview.startSpotAndShowRect(); // 显示扫描框，并开始识别
    }

    @Override
    protected void onStop() {
        zxingview.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
           zxingview.closeFlashlight();
           finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        Log.e("##isOpenLighted",isOpenLighted+"");
        zxingview.closeFlashlight();
        zxingview.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
        zxingview.startSpot();
        if (result == null) {
            result = "";
        }
        Log.e("###", "扫描结果" + result);
        scanResultCallBack.onScanSuccess(200, "SUCCESS", result);
        finish();
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        String tipText = zxingview.getScanBoxView().getTipText();
        String ambientBrightnessTip = "\n环境过暗，请打开闪光灯";
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                zxingview.getScanBoxView().setTipText(tipText + ambientBrightnessTip);
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip));
                zxingview.getScanBoxView().setTipText(tipText);
            }
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Toast.makeText(SacnActivity.this, "打开相机失败", Toast.LENGTH_LONG).show();
        Log.e("##", "打开相机失败");
        finish();
    }


    public static ScanResultCallBack scanResultCallBack;

    public static void getScanResult(ScanResultCallBack scanResultCallBack) {
        SacnActivity.scanResultCallBack = scanResultCallBack;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        zxingview.startSpotAndShowRect(); // 显示扫描框，并开始识别
        if (resultCode == Activity.RESULT_OK && requestCode == CODE_GALLERY_REQUEST) {
            String imgpathstr = RealPathFromUriUtils.getRealPathFromUri(this, data.getData());
            Log.e("###imgpathstr",imgpathstr);
            zxingview.decodeQRCode(imgpathstr);
        }
    }



    public static void swithLightStatue(int lightOn) {
        if (lightOn==0){
            isOpenLighted=false;
        }else {
            isOpenLighted=true;
        }
        if (isOpenLighted) {
            zxingview.closeFlashlight();
            mLight.setImageResource(R.mipmap.icon_light_white);
        } else {
            zxingview.openFlashlight();
            mLight.setImageResource(R.mipmap.icon_light_red);
        }
        isOpenLighted = !isOpenLighted;
    }
}