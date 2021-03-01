package com.linglong.base.common;

import java.net.*;
import java.util.Enumeration;

/**
 * @author Stereo
 */
public class LocalHost {
    public static void main(String[] args) throws Exception {
        System.out.println(getLocalHost());
    }

    private static volatile InetAddress cLocalAddress;

    public static InetAddress getLocalHost() throws UnknownHostException {
        InetAddress local = cLocalAddress;
        if (local == null) synchronized (LocalHost.class) {
            local = cLocalAddress;
            if (local == null) {
                cLocalAddress = local = doGetLocalHost();
            }
        }
        return local;
    }

    private static InetAddress doGetLocalHost() throws UnknownHostException {
        final InetAddress local = InetAddress.getLocalHost();

        if (!local.isLoopbackAddress()) {
            return local;
        }

        NetworkInterface ni = null;

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface n = en.nextElement();
                if (!n.isLoopback()) {
                    ni = n;
                    break;
                }
            }
        } catch (SocketException e) {
            if (ni == null) {
                UnknownHostException u = new UnknownHostException(e.getMessage());
                u.initCause(e);
                throw u;
            }
        }

        if (ni == null) {
            return local;
        }

        Inet4Address v4 = null;
        Inet6Address v6 = null;

        Enumeration<InetAddress> en = ni.getInetAddresses();

        while (en.hasMoreElements()) {
            InetAddress a = en.nextElement();
            if (a instanceof Inet4Address && v4 == null) {
                v4 = (Inet4Address) a;
            } else if (a instanceof Inet6Address && v6 == null) {
                v6 = (Inet6Address) a;
            }
        }

        InetAddress actual;

        if (v4 == null) {
            if (v6 == null) {
                return local;
            }
            actual = v6;
        } else if (v6 == null || Boolean.getBoolean("java.net.preferIPv4Stack")) {
            actual = v4;
        } else {
            actual = v6;
        }

        String name = actual.getHostName();

        if (name.equals(actual.getHostAddress())) {
            name = local.getHostName();
            if (name.equals(local.getHostAddress())) {
                name = null;
            }
        }

        return InetAddress.getByAddress(name, actual.getAddress());
    }
}
