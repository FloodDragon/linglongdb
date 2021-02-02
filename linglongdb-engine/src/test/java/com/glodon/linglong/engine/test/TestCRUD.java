package com.glodon.linglong.engine.test;

import com.glodon.linglong.engine.config.DatabaseConfig;
import com.glodon.linglong.engine.config.DurabilityMode;
import com.glodon.linglong.engine.core.frame.Database;
import com.glodon.linglong.engine.core.frame.Index;
import com.glodon.linglong.engine.core.tx.Transaction;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by liuj-ai on 2021/2/2.
 */
public class TestCRUD {


    public static void main(String[] args) throws IOException {
        DatabaseConfig config = new DatabaseConfig()
                .baseFilePath("C:\\Users\\liuj-ai\\Desktop\\数据库开发\\my-db\\data")
                .minCacheSize(100_000_000)
                .durabilityMode(DurabilityMode.SYNC);

        //打开数据库
        Database db = Database.open(config);
        //开始事务
        Transaction txn = db.newTransaction();
        //构建索引
        Index userIx = db.openIndex("userIndex");
        String msg = "正在进行数据库测试";
        byte[] key;
        userIx.insert(txn, key = String.valueOf(ThreadLocalRandom.current().nextInt()).getBytes(), msg.getBytes());
        //事务提交 & 回滚
        txn.commit();//txn.reset();
        byte[] data = userIx.load(txn, key);
        System.out.println(new String(data == null ? new byte[0] : data));
        //索引关闭
        userIx.close();
        //关闭数据库
        db.close();
    }

}
