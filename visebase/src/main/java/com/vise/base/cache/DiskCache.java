package com.vise.base.cache;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.vise.base.ViseConfig;
import com.vise.base.kit.Codec;
import com.vise.base.kit.Kits;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-19 15:10
 */
public class DiskCache implements ICache {
    public static final String TAG_CACHE = "=====createTime{createTime_v}expireMills{expireMills_v}";
    public static final String REGEX = "=====createTime\\{(\\d{1,})\\}expireMills\\{(\\d{1,})\\}";
    public static final long NO_CACHE = -1L;

    private DiskLruCache cache;
    private Pattern compile;

    private static DiskCache instance;

    private DiskCache(Context context) {
        compile = Pattern.compile(REGEX);
        try {
            File cacheDir = getDiskCacheDir(context, getCacheDir());
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            cache = DiskLruCache.open(cacheDir, Kits.Package.getVersionCode(context), 1, 10 * 1024 * 1024);        //10M
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DiskCache getInstance(Context context) {
        if (instance == null) {
            synchronized (DiskCache.class) {
                if (instance == null) {
                    instance = new DiskCache(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void put(String key, String value) {
        put(key, value, NO_CACHE);
    }

    public void put(String key, String value, long expireMills) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) return;

        String name = getMd5Key(key);
        try {
            if (!TextUtils.isEmpty(get(name))) {
                cache.remove(name);
            }

            DiskLruCache.Editor editor = cache.edit(name);
            StringBuilder content = new StringBuilder(value);
            content.append(TAG_CACHE.replace("createTime_v", "" + Calendar.getInstance().getTimeInMillis()).replace("expireMills_v", "" + expireMills));
            editor.set(0, content.toString());
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void put(String key, Object value) {
        put(key, value != null ? value.toString() : null, NO_CACHE);
    }


    public String get(String key) {
        try {
            String md5Key = getMd5Key(key);
            DiskLruCache.Snapshot snapshot = cache.get(md5Key);
            if (snapshot != null) {
                String content = snapshot.getString(0);

                if (!TextUtils.isEmpty(content)) {
                    Matcher matcher = compile.matcher(content);
                    long createTime = 0;
                    long expireMills = 0;
                    while (matcher.find()) {
                        createTime = Long.parseLong(matcher.group(1));
                        expireMills = Long.parseLong(matcher.group(2));
                    }
                    int index = content.indexOf("=====createTime");

                    if ((createTime + expireMills > Calendar.getInstance().getTimeInMillis())
                            || expireMills == NO_CACHE) {
                        return content.substring(0, index);
                    } else {
                        cache.remove(md5Key);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void remove(String key) {
        try {
            cache.remove(getMd5Key(key));
        } catch (Exception e) {
        }
    }

    public boolean contains(String key) {
        try {
            DiskLruCache.Snapshot snapshot = cache.get(getMd5Key(key));
            return snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void clear() {
        try {
            cache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getMd5Key(String key) {
        return Codec.MD5.getMessageDigest(key.getBytes());
    }

    private static File getDiskCacheDir(Context context, String dirName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + dirName);
    }

    private String getCacheDir() {
        return ViseConfig.CACHE_DISK_DIR;
    }
}
