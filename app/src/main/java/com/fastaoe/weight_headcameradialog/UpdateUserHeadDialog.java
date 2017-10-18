package com.fastaoe.weight_headcameradialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

class UpdateUserHeadDialog extends Dialog implements View.OnClickListener {

    private Activity mContext;
    private ImageView mImageView;

    UpdateUserHeadDialog(Context context, ImageView iv) {
        super(context, R.style.recommend_dialog);
        this.mContext = (Activity) context;
        this.mImageView = iv;
        setContentView(R.layout.dialog_account_choose_head);

        initView();
    }

    private void initView() {
        Button btn_photograph = (Button) findViewById(R.id.btn_photograph);
        Button btn_gallery = (Button) findViewById(R.id.btn_gallery);
        Button btn_cancle = (Button) findViewById(R.id.btn_cancle);

        setCanceledOnTouchOutside(false);

        btn_cancle.setOnClickListener(this);
        btn_photograph.setOnClickListener(this);
        btn_gallery.setOnClickListener(this);

        File dir = mContext.getExternalFilesDir("user_icon");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            icon_path = FileProvider.getUriForFile(mContext,
                    mContext.getApplicationContext().getPackageName() + ".provider", new File(dir, TEMP_FILE_NAME));
            camera_path = FileProvider.getUriForFile(mContext,
                    mContext.getApplicationContext().getPackageName() + ".provider", new File(dir, "camera_pic.jpg"));
        } else {
            icon_path = Uri.fromFile(new File(dir, TEMP_FILE_NAME));
            camera_path = Uri.fromFile(new File(dir, "camera_pic.jpg"));
        }
    }

    /**
     * Save the path of photo cropping is completed
     */
    private Uri icon_path;
    private Uri camera_path;
    private static final String TEMP_FILE_NAME = "temp_icon.jpg";

    private static final int CODE_GALLERY_REQUEST = 0x1;
    private static final int CODE_CAMERA_REQUEST = 0x2;
    private static final int CROP_PICTURE_REQUEST = 0x3;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_photograph:
                fromCamera(); // 从照相机获取
                break;

            case R.id.btn_gallery:
                fromGallery(); // 从相册中去获取
                break;

            case R.id.btn_cancle:
                dismiss();
                break;
        }
    }

    /**
     * Select images from a local photo album
     */
    private void fromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        Intent intentFromGallery = new Intent();
//        intentFromGallery.setType("image/*");
//        intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
        mContext.startActivityForResult(intent, CODE_GALLERY_REQUEST);
    }

    /**
     * Start the phone camera photos
     */
    private void fromCamera() {
        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, camera_path);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            intentFromCapture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        mContext.startActivityForResult(intentFromCapture, CODE_CAMERA_REQUEST);
    }


    /**
     * 给activity的onActivityResult调用
     *
     * @param resultCode  onActivityResult resultCode
     * @param requestCode onActivityResult requestCode
     * @param data        onActivityResult data
     */
    void activityResult(int resultCode, int requestCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            dismiss();
            return;
        }
        switch (requestCode) {
            case CODE_GALLERY_REQUEST:
                cropImage(data.getData(), 450, 450, CROP_PICTURE_REQUEST);
                break;
            case CODE_CAMERA_REQUEST:

                cropImage(camera_path, 450, 450, CROP_PICTURE_REQUEST);
                break;
            case CROP_PICTURE_REQUEST:
                Bitmap bitmap = decodeUriAsBitmap(icon_path);
                mImageView.setImageBitmap(bitmap);
                break;

        }
    }

    /**
     * According to the incoming a length-width ratio began to cut out pictures
     *
     * @param uri Image source
     */
    private void cropImage(Uri uri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // Set the cutting
        intent.putExtra("crop", "true");
        // aspectX , aspectY :In proportion to the width of high
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX , outputY : High cutting image width
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, icon_path);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            addurlPram(mContext, intent, camera_path, icon_path);
        }
        mContext.startActivityForResult(intent, requestCode);
    }

    private void addurlPram(Activity activity, Intent intent, Uri... uris) {
        List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            for (Uri uri : uris) {
                activity.grantUriPermission(packageName, uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
    }

    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

}
