package com.apollo.apk1installer;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private Intent newIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requiresWriteExternalStoragePermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        newIntent = intent;
        requiresWriteExternalStoragePermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1111) {
            requiresWriteExternalStoragePermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void requiresWriteExternalStoragePermission() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            installApk();
        } else {
            EasyPermissions.requestPermissions(this, "请允许存储权限",
                    0, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == 0) {
            installApk();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            if (requestCode == 0) {
                new AppSettingsDialog
                        .Builder(this)
                        .setTitle("需要存储权限")
                        .setRationale("请到设置中打开应用存储权限")
                        .setRequestCode(1111)
                        .build()
                        .show();
            }
        }
    }

    private void installApk() {
        Uri data = null;
        if (getIntent() != null && getIntent().getData() != null) {
            data = getIntent().getData();
        } else if (newIntent != null && newIntent.getData() != null) {
            data = newIntent.getData();
        }
        if (data == null) return;
        String dataStr = data.toString();
        try {
            dataStr = dataStr.replace("tencent/MicroMsg/Download/", "$");
            String fileName = dataStr.substring(dataStr.indexOf("$") + 1);
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tencent/MicroMsg/Download/");
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files == null || files.length == 0) return;
                for (File value : files) {
                    if (value.getName().equals(fileName)) {
                        Uri fileUri = FileProvider.getUriForFile(this, getApplicationInfo().packageName + ".fileProvider", value);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "敬请期待新版本", Toast.LENGTH_LONG).show();
        }
    }
}
