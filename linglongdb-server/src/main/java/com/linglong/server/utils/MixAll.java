package com.linglong.server.utils;

import com.linglong.base.common.LocalHost;
import com.linglong.base.utils.UUID;

import java.io.*;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class MixAll {

    private static String UUID_CODE;

    static {
        try {
            UUID_CODE = LocalHost.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            UUID_CODE = null;
        }
    }

    public static String getUUID() {
        return new UUID(UUID_CODE).toString();
    }

    public static String parseThrowable(Throwable t) {
        StringBuilder builder = new StringBuilder();
        Writer writer = null;
        PrintWriter printWriter = null;
        try {
            writer = new StringWriter();
            printWriter = new PrintWriter(writer);
            t.printStackTrace(printWriter);
            printWriter.flush();
            builder.append(writer.toString());
            printWriter.close();
            writer.close();
        } catch (Exception e) {
            return null;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }
            if (printWriter != null) {
                try {
                    printWriter.close();
                } catch (Exception e) {
                }
            }
        }
        return builder.toString();
    }

    public static boolean checkPowerOfTwo(long num) {
        if (num >= 2) {
            while (num != 1) {
                if (num % 2 == 0) {
                    num = num / 2;
                } else {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean createIfNotExistsDir(File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    public static String dateFormater(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(time);
    }

    public static byte[] uncompress(final byte[] src) throws IOException {
        byte[] result = src;
        byte[] uncompressData = new byte[src.length];
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(src);
        InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(src.length);

        try {
            while (true) {
                int len = inflaterInputStream.read(uncompressData, 0, uncompressData.length);
                if (len <= 0) {
                    break;
                }
                byteArrayOutputStream.write(uncompressData, 0, len);
            }
            byteArrayOutputStream.flush();
            result = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                byteArrayInputStream.close();
            } catch (IOException e) {
            }
            try {
                inflaterInputStream.close();
            } catch (IOException e) {
            }
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
            }
        }
        return result;
    }

    public static byte[] compress(final byte[] src, final int level) throws IOException {
        byte[] result = src;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(src.length);
        Deflater deflater = new Deflater(level);
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
        try {
            deflaterOutputStream.write(src);
            deflaterOutputStream.flush();
            deflaterOutputStream.close();
            result = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            deflater.end();
            throw e;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException ignored) {
            }
            deflater.end();
        }
        return result;
    }

    public static void properties2Pojo(Properties properties, Object obj) {
        Method[] methods = obj.getClass().getMethods();
        if (methods != null) {
            for (Method method : methods) {
                String methodName = method.getName();
                if (methodName.startsWith("set")) {
                    try {
                        String tmp = methodName.substring(4);
                        String firstChar = methodName.substring(3, 4);
                        String key = firstChar.toLowerCase() + tmp;
                        String value = properties.getProperty(key);
                        if (value != null) {
                            Class<?>[] types = method.getParameterTypes();
                            if (types != null && types.length > 0) {
                                String type = types[0].getSimpleName();
                                Object arg = null;
                                if (type.equals("int") || type.equals("Integer")) {
                                    arg = Integer.parseInt(value);
                                } else if (type.equals("float") || type.equals("Float")) {
                                    arg = Float.parseFloat(value);
                                } else if (type.equals("double") || type.equals("Double")) {
                                    arg = Double.parseDouble(value);
                                } else if (type.equals("long") || type.equals("Long")) {
                                    arg = Long.parseLong(value);
                                } else if (type.equals("boolean") || type.equals("Boolean")) {
                                    arg = Boolean.parseBoolean(value);
                                } else if (type.equals("String")) {
                                    arg = value;
                                } else {
                                    continue;
                                }
                                method.invoke(obj, arg);
                            }
                        }

                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

    public static String getSHAString(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return toHexString(md.digest(input.getBytes(StandardCharsets.UTF_8)));
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }

    public static class Counter {
        private final long countResetTimePeriodMs;
        private long count = 0L;
        private long timestamp = now();

        private Counter(long countResetTimePeriodMs) {
            this.countResetTimePeriodMs = countResetTimePeriodMs;
        }

        public synchronized long getCount() {
            return count;
        }

        public synchronized long increment() {
            final long now = now();
            if (now - timestamp > countResetTimePeriodMs) {
                count = 0;
            }
            timestamp = now;
            return ++count;
        }

        private static long now() {
            return System.nanoTime() / 1000000;
        }
    }

    public static Counter getCounter(long countResetTimePeriodMs) {
        return new Counter(countResetTimePeriodMs);
    }

    public static Counter getCounter() {
        return new Counter(Long.MAX_VALUE);
    }
}
