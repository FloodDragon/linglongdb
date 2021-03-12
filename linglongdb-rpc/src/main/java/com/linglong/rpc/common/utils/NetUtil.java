package com.linglong.rpc.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * @author Stereo on 2018/7/6.
 */
public class NetUtil {
    private static final Logger logger = LoggerFactory.getLogger(NetUtil.class);

    private static final String ANYHOST = "0.0.0.0";
    private static final String LOCALHOST = "127.0.0.1";
    public static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");
    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
    public static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    /**
     * 最小端口
     */
    private static final int MIN_PORT = 0;
    /**
     * 最大端口
     */
    private static final int MAX_PORT = 65535;

    private static volatile InetAddress LOCAL_ADDRESS = null;

    public static boolean isInvalidHost(String host) {
        String name = host;
        if (StringUtils.isBlank(host))
            return false;
        return !(isAnyHost(host)
                || isLocalHost(host)
                || isIPv4Host(name));
    }

    public static boolean isIPv4Host(String host) {
        return StringUtils.isNotBlank(host)
                && IPV4_PATTERN.matcher(host).matches();
    }

    public static boolean isLocalHost(String host) {
        return StringUtils.isNotBlank(host)
                && (LOCAL_IP_PATTERN.matcher(host).matches() || "localhost".equalsIgnoreCase(host));
    }

    public static boolean isInvalidPort(int port) {
        return port > MAX_PORT || port < MIN_PORT;
    }

    /**
     * 是否默认地址 0.0.0.0
     *
     * @param host 地址
     * @return 是否默认地址
     */
    public static boolean isAnyHost(String host) {
        return ANYHOST.equals(host);
    }

    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress())
            return false;
        String name = address.getHostAddress();
        return (name != null
                && !ANYHOST.equals(name)
                && !LOCALHOST.equals(name)
                && IP_PATTERN.matcher(name).matches());
    }

    private static InetAddress getFirstValidAddress() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable e) {
            logger.error("Failed to retriving ip address, " + e.getMessage(), e);
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable e) {
                                    logger.error("Failed to retriving ip address, " + e.getMessage(), e);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        logger.error("Failed to retriving ip address, " + e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            logger.error("Failed to retriving ip address, " + e.getMessage(), e);
        }
        logger.error("Could not get local host ip address, will use 127.0.0.1 instead.");
        return localAddress;
    }

    private static InetAddress getAddress() {
        if (LOCAL_ADDRESS != null)
            return LOCAL_ADDRESS;
        InetAddress localAddress = getFirstValidAddress();
        LOCAL_ADDRESS = localAddress;
        return localAddress;
    }

    public static String getHost() {
        InetAddress address = getAddress();
        if (address == null) {
            return null;
        }
        return address.getHostAddress();
    }

    public static String getHostPort(int port) {
        String ip = getHost();
        if (ip == null) {
            return null;
        }
        return ip.concat(":").concat(String.valueOf(port));
    }

    public static String toAddressString(InetSocketAddress address) {
        if (address == null) {
            return StringUtils.EMPTY;
        } else {
            return toIpString(address) + ":" + address.getPort();
        }
    }

    /**
     * 得到ip地址
     *
     * @param address InetSocketAddress
     * @return ip地址
     */
    public static String toIpString(InetSocketAddress address) {
        if (address == null) {
            return null;
        } else {
            InetAddress inetAddress = address.getAddress();
            return inetAddress == null ? address.getHostName() :
                    inetAddress.getHostAddress();
        }
    }
}
