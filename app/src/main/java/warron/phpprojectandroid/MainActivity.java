package warron.phpprojectandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mingle.entity.MenuEntity;
import com.mingle.sweetpick.BlurEffect;
import com.mingle.sweetpick.CustomDelegate;
import com.mingle.sweetpick.RecyclerViewDelegate;
import com.mingle.sweetpick.SweetSheet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPreview;
import warron.phpprojectandroid.Base.BaseActivity;
import warron.phpprojectandroid.LoginMudule.ForgetPassAC;
import warron.phpprojectandroid.Tools.CommonListActivity;
import warron.phpprojectandroid.Tools.EmptyListActivity;
import warron.phpprojectandroid.Tools.FooterListActivity;
import warron.phpprojectandroid.Tools.GridListActivity;
import warron.phpprojectandroid.Tools.NetRequestMudule.AsyncHttpNet;
import warron.phpprojectandroid.Tools.NetRequestMudule.DataModel;

import static java.util.Locale.*;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private SweetSheet mSweetSheet;

    @Override
    public int getContentView() {
        return R.layout.activity_main;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.btn_common_list).setOnClickListener(this);
        findViewById(R.id.btn_empty_list).setOnClickListener(this);
        findViewById(R.id.btn_footer_list).setOnClickListener(this);
        findViewById(R.id.btn_grid_list).setOnClickListener(this);
        findViewById(R.id.btn_Upload_image).setOnClickListener(this);
        setupSheepView();
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.app_name);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.btn_common_list:
                intent = new Intent(this, CommonListActivity.class);
                break;
            case R.id.btn_empty_list:
                intent = new Intent(this, EmptyListActivity.class);
                break;
            case R.id.btn_footer_list:
                intent = new Intent(this, FooterListActivity.class);
                break;
            case R.id.btn_grid_list:
                intent = new Intent(this, GridListActivity.class);
                break;
            case R.id.btn_Upload_image: {
                if (mSweetSheet.isShow())
                    mSweetSheet.dismiss();
                else
                    mSweetSheet.show();
            }
            break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    private void setupSheepView() {
        final RelativeLayout rl = (RelativeLayout) findViewById(R.id.rlayout);
        // SweetSheet 控件,根据 rl 确认位置
        if (mSweetSheet == null) {

            mSweetSheet = new SweetSheet(rl);
        }
        MenuEntity album = new MenuEntity();
        album.iconId = R.mipmap.ic_xiangche;
        album.titleColor = 0xffb3b3b3;
        album.title = "相册";
        final MenuEntity takePhoto = new MenuEntity();
        takePhoto.iconId = R.mipmap.ic_xiangji;
        takePhoto.titleColor = 0xffb3b3b3;
        takePhoto.title = "拍照";
        MenuEntity dismiss = new MenuEntity();
        dismiss.titleColor = 0xffb3b3b3;
        dismiss.title = "取消";

        //设置数据源 (数据源支持设置 list 数组,也支持从菜单中获取)
        final ArrayList<MenuEntity> list = new ArrayList<MenuEntity>();
        list.add(album);
        list.add(takePhoto);
        list.add(dismiss);
        mSweetSheet.setMenuList(list);
        //根据设置不同的 Delegate 来显示不同的风格.
        mSweetSheet.setDelegate(new RecyclerViewDelegate(true));
        //根据设置不同Effect 来显示背景效果BlurEffect:模糊效果.DimEffect 变暗效果
        mSweetSheet.setBackgroundEffect(new BlurEffect(8));
        //设置点击事件
        mSweetSheet.setOnMenuItemClickListener(new SweetSheet.OnMenuItemClickListener() {
            @Override
            public boolean onItemClick(int position, MenuEntity menuEntity1) {
                //即时改变当前项的颜色
//                list.get(position).titleColor = 0xff5823ff;
                ((RecyclerViewDelegate) mSweetSheet.getDelegate()).notifyDataSetChanged();
                File directory = new File(Environment.getExternalStorageDirectory(), "warron/warron.jpg");
                try {
                    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(directory.getPath()));
                    ((ImageView) findViewById(R.id.imageViewT)).setImageBitmap(bitmap1);// 将图片显示在ImageView里
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (position == 0) {
                    selectPhotoInAlbum();
                } else if (position == 1) {
                    takePhotoAction();
                } else {
                    mSweetSheet.dismiss();
                }
                return true; //true 会自动关闭.
            }

        });
    }

    private void selectPhotoInAlbum() {
        PhotoPicker.builder()
                .setPhotoCount(9)//设置上传照片最大数
                .start(MainActivity.this);
    }

    private void takePhotoAction() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }

    @SuppressLint({"WrongConstant", "ResourceType"})
    @Override //选择图片后的回调
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;
        ArrayList<File> list = new ArrayList<File>();
        if (resultCode == RESULT_OK &&
                (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {

            List<String> photos = null;
            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                bitmap = BitmapFactory.decodeFile(photos.get(0), null);
            }
            for (int i = 0; i < photos.size(); i++) {
                if (photos.get(i) instanceof String) {//如果是字符串类型
                    File file = new File(photos.get(i));
                    list.add(file);
                }
            }
        } else if (resultCode == Activity.RESULT_OK) {//
            String sdStatus = Environment.getExternalStorageState();
            if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
                Log.i("warron","SD卡不可用 请检查权限");
                return;
            }
            Bundle bundle = data.getExtras();
            bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式

            //创建本地文件夹 写入外置内存卡
            File directory = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
            if (!directory.exists())
                directory.mkdirs();//这里用这个好一些
            //创建文件
            SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",
                    Locale.getDefault());
            File file = new File(directory, dataFormat.format(new Date()) + "_meme.jpg");
            if (file.exists())
                file.delete();//删除文件
            //设置图片格式
            Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    OutputStream stream = new FileOutputStream(file);
                    if (bitmap != null) {
                        bitmap.compress(format, 100, stream);
                    }
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
             list.add(file);
        }
        if (bitmap != null) {
            ((ImageView) findViewById(R.id.imageViewT)).setImageBitmap(bitmap);// 将图片显示在ImageView里
        }
        uploadImg(list);
    }

    private void uploadImg(ArrayList<File> imgFileList) {//可以上传多张

        AsyncHttpNet net = new AsyncHttpNet(this) {
            @Override
            public void fail(int statusCode, Map resultMap) {

            }

            @Override
            public void onSuccess(Map resultMap) {
                super.onSuccess(resultMap);
                Toast.makeText(MainActivity.this, resultMap.get("msg").toString(), Toast.LENGTH_SHORT).show();
            }
        };
        DataModel model = new DataModel();
        model.keyId = "18408246301";
        model.files = new ArrayList<File>();
        model.files = imgFileList;
        net.uploadImg(model, "uploadimgs.php");
    }
}