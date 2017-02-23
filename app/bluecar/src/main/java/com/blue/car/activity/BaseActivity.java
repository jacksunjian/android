package com.blue.car.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.blue.car.R;
import com.blue.car.utils.ToastUtils;

import java.util.List;

import butterknife.ButterKnife;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public abstract class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int NOTHING_PERMISSIONS_REQUEST = -1000;

    protected abstract Integer getLayoutId();

    protected abstract void initConfig();

    protected abstract void initView();

    protected abstract void initData();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);

        initConfig();
        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        if (getPermissionRequestCode() == NOTHING_PERMISSIONS_REQUEST) {
            ToastUtils.showToast(this, "Permission RequestCode must be override", Toast.LENGTH_SHORT);
            return;
        }
        if (EasyPermissions.somePermissionPermanentlyDenied(this, list)) {
            new AppSettingsDialog.Builder(this, getPermissionRationale())
                    .setTitle(getString(R.string.goto_permission_setting))
                    .setPositiveButton(getString(R.string.go_to))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setRequestCode(getPermissionRequestCode())
                    .build()
                    .show();
        } else {
            processTemporaryPermissionsDenied(requestCode, list);
        }
    }

    protected void processTemporaryPermissionsDenied(int requestCode, List<String> list) {
        ToastUtils.showToast(this, getString(R.string.warning_of_permissions_denied), Toast.LENGTH_SHORT);
    }

    protected String getPermissionRationale() {
        return getString(R.string.tip_of_permissions_request);
    }

    //if development need request permission, must override this method
    protected int getPermissionRequestCode() {
        return NOTHING_PERMISSIONS_REQUEST;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }
}