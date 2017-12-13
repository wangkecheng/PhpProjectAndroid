package warron.phpprojectandroid.Tools.NetRequestMudule;
//http://blog.csdn.net/zknxx/article/details/52281220
//https://www.cnblogs.com/r-decade/p/5819916.html

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.protocol.HTTP;
import warron.phpprojectandroid.LoginMudule.RegisterActivity;

//http://loopj.com/android-async-http/ AsyncHttpClient 官网
//contentType:http://tool.oschina.net/commons/
public abstract class AsyncHttpNet implements AsyncHttpNetInterface {

    public static final int TypeImage = 100;
    public static final int TypeMp4 = 101;
    public static final int TypeMp3 = 102;
    public static final int TypeOther = 103;
    private int TypeDownload;//定义下载文件的类型
    private File pathDirectory;//文件存储路径
    private String fileName;//文件名
    private String HOST_URL = "http://192.168.7.37/";

    AsyncHttpNetInterface netInterface;//创建监听器
    Context context;

    public AsyncHttpNet() {

        netInterface = (AsyncHttpNetInterface) this;//初始化监听器
    }

    public AsyncHttpNet(Context context) {
        this.context = context;
        this.netInterface = (AsyncHttpNetInterface) this;//初始化监听器
    }

    //实现接口方法， 这里不做的话再调用的时候 我成为block的方法中要将所有的方法都实现，也就是说block中的方法会重载对应方法
    public void onStart() {
    }

    public void onSuccess(Map map) {
    }

    public void onSuccessImg(Bitmap bitmap) {
    }

    public void onSuccessMp3(File response) {
    }

    public void onSuccessVideo(File response) {
    }

    public void onSuccessOtherFile() {
    }

    public void onProgress(long bytesWritten, long totalSize) {
    }

    public void onRetry() {
    }

    public void fail(int statusCode, Header[] headers, byte[] errorResponse, Throwable e, File file) {
    }

    public void get(DataModel model, String url) {
        post(model, url);
    }

    public void post(DataModel model, String url) {

        String json = JSON.toJSONString(model);
        Map map = JSON.parseObject(json, Map.class);//将对象中的属性和值转换成字典
        RequestParams params = new RequestParams(map);
        HOST_URL = HOST_URL + url;
        AsyncHttpClient client = new AsyncHttpClient();//这里创建请求网络的对象
        client.post(HOST_URL, params, new AsyncHttpResponseHandler() {
            public void onStart() {
                super.onStart();
                if (netInterface != null)
                    netInterface.onStart();
            }

            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                if (netInterface != null)
                    netInterface.onProgress(bytesWritten, totalSize);
            }

            public void onRetry(int retryNo) {
                super.onRetry(retryNo);
                if (netInterface != null)
                    netInterface.onRetry();
            }

            public void onSuccess(int i, Header[] headers, byte[] bytes) {

                resolveRequestSuccess(bytes);
            }

            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                try {
                    String resultStr = new String(errorResponse, "UTF-8");
                    if (netInterface != null)
                        netInterface.fail(403, JSON.parseObject(resultStr, Map.class));
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    //处理一般请求数据成功的操作
    public void resolveRequestSuccess(byte[] bytes) {
        try {
            String stringResult = new String(bytes, "UTF-8");
            Map mapObj = JSON.parseObject(stringResult, Map.class);
            if (mapObj == null || mapObj.get("code") == null) {//只要没有返回code都不运行表面的代码
                Toast.makeText(context, "无返回值或无请求状态 'code'返回", Toast.LENGTH_SHORT).show();
                return;
            }
            int code = Integer.parseInt(mapObj.get("code").toString());
            if (code != 200) {
                Toast.makeText(context, mapObj.get("msg").toString(), Toast.LENGTH_SHORT).show();

                if (netInterface != null)
                    netInterface.fail(Integer.parseInt(mapObj.get("code").toString()), JSON.parseObject(stringResult, Map.class));
            } else if (netInterface != null && code == 200)
                netInterface.onSuccess(mapObj);
            else {
                Toast.makeText(context, "未知错误", Toast.LENGTH_SHORT).show();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //下载二进制文件
    public void downloadImage(DataModel model, String url) {
        download(model, url, TypeImage, null);
    }

    public void downloadMp3(DataModel model, String url) {//不带目录
        downloadMp3(model, url, null);
    }

    public void downloadMp3(DataModel model, String url, File directory) {
        download(model, url, TypeMp3, directory);//二者的请求机制是一样的，但用更加安全的post方式
    }

    public void downloadVideo(DataModel model, String url) {
        downloadVideo(model, url, null);
    }

    public void downloadVideo(DataModel model, String url, File directory) {
        download(model, url, TypeMp4, null);
    }

    public void download(DataModel model, final String url, final int typeDonload, File directory) {
        this.TypeDownload = typeDonload;//初始化下载类型
        pathDirectory = directory;//初始化路径,若路径为空，则后面会初始化一个默认路径
        fileName = url.substring(url.lastIndexOf("/") + 1, url.length());//获取下载链接后面的文件名
        if (fileName == null) {
            fileName = "temp" + System.currentTimeMillis() + ".mp3";//为空时，先初始化为mp3
            if (typeDonload == TypeMp4)
                fileName = "temp" + System.currentTimeMillis() + ".mp4";//满足条件则为mp4的后缀
        }
        String json = JSON.toJSONString(model);
        Map map = JSON.parseObject(json, Map.class);//将对象中的属性和值转换成字典
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader(HTTP.CONTENT_TYPE, "audio/mpeg");
        client.addHeader(HTTP.CONTENT_TYPE, "video/mpeg4");//添加请求类型
        final RequestParams finalParams = new RequestParams(map);
        RequestHandle requestHandle = client.post(url, finalParams, new FileAsyncHttpResponseHandler(context) {

            public void onStart() {
                super.onStart();
            }

            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                if (netInterface != null)
                    netInterface.onProgress(bytesWritten, totalSize);
            }

            public void onRetry(int retryNo) {
                netInterface.onRetry();
            }

            public void onSuccess(int statusCode, Header[] headers, File response) {

                resolveDownloadSuccess(statusCode, headers, response);
            }

            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                if (netInterface != null)
                    netInterface.fail(i, null);
            }
        });
    }

    //如果请求失败换做get请求一下
    public void requestByGetAgain(String url, RequestParams params) {

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader(HTTP.CONTENT_TYPE, "audio/mpeg");
        client.addHeader(HTTP.CONTENT_TYPE, "video/mpeg4");//添加请求类型
        HOST_URL = HOST_URL + url;
        RequestHandle requestHandle = client.get(HOST_URL, params, new FileAsyncHttpResponseHandler(context) {

            public void onStart() {
                super.onStart();
            }

            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                if (netInterface != null)
                    netInterface.onProgress(bytesWritten, totalSize);
            }

            public void onRetry(int retryNo) {
                if (netInterface != null)
                    netInterface.onRetry();
            }

            public void onSuccess(int statusCode, Header[] headers, File response) {
                resolveDownloadSuccess(statusCode, headers, response);
            }

            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                if (netInterface != null)
                    netInterface.fail(i, null);
            }
        });
    }

    public void uploadImg(DataModel model, String url) {

        RequestParams params = new RequestParams();
        try {
            for (int i = 0; i < model.files.size(); i++) {
                params.put("files" + i, model.files.get(i));
            }

            params.put("keyId", model.keyId);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        HOST_URL = HOST_URL + url;
        AsyncHttpClient client = new AsyncHttpClient();//这里创建请求网络的对象
        //设置请求头  注意这里token是根据后台设置的配置，value是根据不同用户的token值设置
//        client.addHeader("token", "MTUwOTY3NDk3Noyqn6G6rc2Jm358bIqYdHU");
        client.post(HOST_URL, params, new AsyncHttpResponseHandler() {

            public void onStart() {
                super.onStart();
                if (netInterface != null)
                    netInterface.onStart();
            }

            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                if (netInterface != null)
                    netInterface.onProgress(bytesWritten, totalSize);
            }

            public void onRetry(int retryNo) {
                super.onRetry(retryNo);
                if (netInterface != null)
                    netInterface.onRetry();
            }

            public void onSuccess(int i, Header[] headers, byte[] bytes) {

                resolveRequestSuccess(bytes);
            }

            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                try {
                    String resultStr = new String(errorResponse, "UTF-8");
                    if (netInterface != null)
                        netInterface.fail(statusCode, JSON.parseObject(resultStr, Map.class));
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void resolveDownloadSuccess(int statusCode, Header[] headers, File response) {
        if (TypeDownload == TypeImage) {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(response.getPath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                try {
                    inputStream.close();//关闭流
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (netInterface != null)
                    netInterface.onSuccessImg(bitmap);
                return;
            }
        } else if (TypeDownload == TypeMp3 || TypeDownload == TypeMp4) {

            FileInputStream inputStream = null;//将文件转为输入流
            try {
                inputStream = new FileInputStream(response.getPath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (pathDirectory == null)//若为传入directory 初始化directory
                pathDirectory = new File(Environment.getExternalStorageDirectory(), "warron");

            if (!pathDirectory.exists()) {
                pathDirectory.mkdirs();//不存在的时候就创建
            }

            if (inputStream != null) {
                File file = null;

                try {
                    file = new File(pathDirectory, fileName);
                    if (!file.exists()) {
                        file.createNewFile();//创建文件
                    }
                    OutputStream output = new FileOutputStream(file);
                    byte[] buffer = new byte[4 * 1024];
                    while (inputStream.read(buffer) != -1) {
                        output.write(buffer);
                        output.flush();//将输出流写入文件中
                    }
                    inputStream.close();//关闭流
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (TypeDownload == TypeMp3) {//返回文件，可以调用实现播放
                    if (netInterface != null)
                        netInterface.onSuccessMp3(file);
                } else {
                    if (netInterface != null)
                        netInterface.onSuccessVideo(file);
                }
                return;
            }
        }
        //如果能够走到这里说明上边的return没有生效，表示失败了
        if (netInterface != null)

            netInterface.fail(403, null);
    }
}

//这个接口可以单独写一个类文件 但为了方便拷贝，直接放在这里，下面操作对象的方法ObjectUtils同理
interface AsyncHttpNetInterface {

    //网络请求的回调接口，相当于ios中的block
    void onStart();//开始请求

    void onSuccess(Map resultMap);//请求成功

    void onSuccessImg(Bitmap bitmap);//请求图片成功

    void onSuccessMp3(File response);//请求Mp3成功

    void onSuccessVideo(File response);//Video请求成功

    void onSuccessOtherFile();//请求其他文件成功

    void onProgress(long bytesWritten, long totalSize);//进度

    void onRetry();//重试

    void fail(int statusCode, Map resultMap);//失败
}


//将对象转换为字典
class ObjectUtils {

    private static final String JAVAP = "java.";
    private static final String JAVADATESTR = "java.util.Date";

    /**
     * 获取利用反射获取类里面的值和名称
     *
     * @param obj
     * @return
     * @throws IllegalAccessException
     */

    public static Map<String, Object> objectToMap(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = field.get(obj);
            if (value == null || fieldName.equals("serialVersionUID"))
                continue;
            map.put(fieldName, value);
        }

        return map;
    }

    /**
     * 利用递归调用将Object中的值全部进行获取
     *
     * @param timeFormatStr 格式化时间字符串默认<strong>2017-03-10 10:21</strong>
     * @param obj           对象
     * @param excludeFields 排除的属性
     * @return
     * @throws IllegalAccessException
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Map<String, String> objectToMapString(String timeFormatStr, Object obj, String... excludeFields) throws IllegalAccessException {
        Map<String, String> map = new HashMap<>();

        if (excludeFields.length != 0) {
            List<String> list = Arrays.asList(excludeFields);
            objectTransfer(timeFormatStr, obj, map, list);
        } else {
            objectTransfer(timeFormatStr, obj, map, null);
        }
        return map;
    }


    /**
     * 递归调用函数
     *
     * @param obj           对象
     * @param map           map
     * @param excludeFields 对应参数
     * @return
     * @throws IllegalAccessException
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private static Map<String, String> objectTransfer(String timeFormatStr, Object obj, Map<String, String> map, List<String> excludeFields) throws IllegalAccessException {
        boolean isExclude = false;
        //默认字符串
        String formatStr = "YYYY-MM-dd HH:mm:ss";
        //设置格式化字符串
        if (timeFormatStr != null && !timeFormatStr.isEmpty()) {
            formatStr = timeFormatStr;
        }
        if (excludeFields != null) {
            isExclude = true;
        }
        Class<?> clazz = obj.getClass();
        //获取值
        for (Field field : clazz.getDeclaredFields()) {
            String fieldName = clazz.getSimpleName() + "." + field.getName();
            //判断是不是需要跳过某个属性
            if (isExclude && excludeFields.contains(fieldName)) {
                continue;
            }
            //设置属性可以被访问
            field.setAccessible(true);
            Object value = field.get(obj);
            Class<?> valueClass = value.getClass();
            if (valueClass.isPrimitive()) {
                map.put(fieldName, value.toString());

            } else if (valueClass.getName().contains(JAVAP)) {//判断是不是基本类型
                if (valueClass.getName().equals(JAVADATESTR)) {
                    //格式化Date类型
                    SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
                    Date date = (Date) value;
                    String dataStr = sdf.format(date);
                    map.put(fieldName, dataStr);
                } else {
                    map.put(fieldName, value.toString());
                }
            } else {
                objectTransfer(timeFormatStr, value, map, excludeFields);
            }
        }
        return map;
    }

}