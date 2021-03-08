package com.linglong.engine.test;

import com.linglong.engine.config.DatabaseConfig;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.frame.Database;
import com.linglong.engine.core.frame.Scanner;
import com.linglong.engine.core.frame.Sorter;

import java.io.*;
import java.util.Random;

/**
 * 字典排序
 * <p>
 * Created by liuj-ai on 2021/3/8.
 */
public class TestSorter {

    private static final String tableName = "data";
    private static final String basePath = "C:\\Users\\liuj-ai\\Desktop\\数据库开发\\linglongdb\\";

    public static void main(String[] args) throws IOException {
        DatabaseConfig config = new DatabaseConfig()
                .baseFilePath(basePath + tableName)
                .minCacheSize(100_000_000)
                .durabilityMode(DurabilityMode.NO_FLUSH);
        Database db = Database.open(config);
        try {
            Sorter s = db.newSorter(null);
            Random random = new Random();
            for (int i = 0; i < 1000000; i++) {
                byte buf[] = new byte[8];
                long num = random.nextInt(1000000);
                buf[0] = (byte) (num >>> 56);
                buf[1] = (byte) (num >>> 48);
                buf[2] = (byte) (num >>> 40);
                buf[3] = (byte) (num >>> 32);
                buf[4] = (byte) (num >>> 24);
                buf[5] = (byte) (num >>> 16);
                buf[6] = (byte) (num >>> 8);
                buf[7] = (byte) (num >>> 0);
                s.add(buf, "欢迎使用玲珑数据库".getBytes());
            }
            // Index ix = s.finish();
            //System.out.println(ix.getNameString());
            Scanner scanner = s.finishScan();// ? s.finishScanReverse() : s.finishScan();
            scanner.scanAll((buf, v) -> {
                long k = (((long) buf[0] << 56) +
                        ((long) (buf[1] & 255) << 48) +
                        ((long) (buf[2] & 255) << 40) +
                        ((long) (buf[3] & 255) << 32) +
                        ((long) (buf[4] & 255) << 24) +
                        ((buf[5] & 255) << 16) +
                        ((buf[6] & 255) << 8) +
                        ((buf[7] & 255) << 0));
                System.out.println("k=" + k + " v=" + new String(v));
            });
            s.reset();
        } finally {
            db.close();
        }
    }
}
