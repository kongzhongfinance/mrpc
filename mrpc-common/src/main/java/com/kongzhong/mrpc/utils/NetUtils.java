package com.kongzhong.mrpc.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网络相关工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NetUtils {

    public static final String LOCALHOST = "127.0.0.1";
    public static final String ANYHOST = "0.0.0.0";
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private static final int RND_PORT_START = 30000;
    private static final int RND_PORT_RANGE = 10000;
    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;
    private static volatile InetAddress LOCAL_ADDRESS = null;
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}\\:\\d{1,5}$");
    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    /**
     * @Title: getRandomPort @Description: 获取随机端口 @param @return int @throws
     */
    public static int getRandomPort() {
        return RND_PORT_START + RANDOM.nextInt(RND_PORT_RANGE);
    }

    /**
     * @Title: getAvailablePort @Description: 获取可用端口 @param @return int @throws
     */
    public static int getAvailablePort() {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.bind(null);
            return ss.getLocalPort();
        } catch (IOException e) {
            return getRandomPort();
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * @Title: getAvailablePort @Description: 获取可用端口，如果获取不到 递加 @param @return
     * int @throws
     */
    public static int getAvailablePort(int port) {
        if (port <= 0) {
            return getAvailablePort();
        }
        for (int i = port; i < MAX_PORT; i++) {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(i);
                return i;
            } catch (IOException e) {
                // continue
            } finally {
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return port;
    }

    /**
     * @param
     * @return boolean
     * @throws
     * @Title: checkPortConfig
     * @Description: 检查端口是否可用
     */
    public static boolean checkPortConfig(int listenPort) {
        if (listenPort < 0 || listenPort > 65536) {
            throw new IllegalArgumentException("无效的端口: " + listenPort);
        }
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(listenPort);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(listenPort);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    // should not be thrown, just detect port available.
                }
            }
        }
        return false;
    }

    /**
     * @param
     * @return String URL
     * @throws
     * @Title: toURL
     * @Description: 构造URL
     */
    public static String toURL(String protocol, String host, String context, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://");
        sb.append(host);
        if (context.charAt(0) != '/') {
            sb.append('/');
        }
        sb.append(context);
        if (path.charAt(0) != '/') {
            sb.append('/');
        }
        sb.append(path);
        return sb.toString();
    }

    /**
     * @param
     * @return String URL
     * @throws
     * @Title: toURL
     * @Description: 构造URL
     */
    public static String toURL(String protocol, String host, int port, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://");
        sb.append(host).append(':').append(port);
        if (path.charAt(0) != '/') {
            sb.append('/');
        }
        sb.append(path);
        return sb.toString();
    }

    /**
     * @param
     * @return String URL
     * @throws
     * @Title: toURL
     * @Description: 构造URL
     */
    public static String toURL(String protocol, String host, int port, String context, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://");
        sb.append(host).append(':').append(port);
        if (context.charAt(0) != '/') {
            sb.append('/');
        }
        sb.append(context);
        if (path.charAt(0) != '/') {
            sb.append('/');
        }
        sb.append(path);
        return sb.toString();
    }

    public static boolean isValidAddress(String address) {
        return ADDRESS_PATTERN.matcher(address).matches();
    }

    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }
        String name = address.getHostAddress();
        return (name != null
                && !ANYHOST.equals(name)
                && !LOCALHOST.equals(name)
                && IP_PATTERN.matcher(name).matches());
    }

    /**
     * @param
     * @return String
     * @throws
     * @Title: getLocalHost
     * @Description: 获取本机地址
     */
    public static String getLocalHost() {
        InetAddress address = getLocalAddress();
        return address == null ? LOCALHOST : address.getHostAddress();
    }

    /**
     * 遍历本地网卡，返回第一个合理的IP。
     *
     * @return 本地网卡IP
     */
    public static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }
        InetAddress localAddress = getLocalAddress0();
        LOCAL_ADDRESS = localAddress;
        return localAddress;
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Exception e) {
//            logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        if (network.isLoopback() || network.isVirtual() || !network.isUp()) {
                            continue;
                        }
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        while (addresses.hasMoreElements()) {
                            try {
                                InetAddress address = addresses.nextElement();
                                if (isValidAddress(address)) {
                                    return address;
                                }
                            } catch (Exception e) {
//                                logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
                            }
                        }
                    } catch (Exception e) {
//                        logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
//            logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
        }
//        logger.error("Could not get local host ip address, will use 127.0.0.1 instead.");
        return localAddress;
    }

    /**
     * 获取主机名
     * @return 主机名
     */
    public static String getHostName() {

        String name = null;
        try {
            Enumeration<NetworkInterface> infs = NetworkInterface.getNetworkInterfaces();
            while (infs.hasMoreElements() && (name == null)) {
                NetworkInterface net = infs.nextElement();
                if (net.isLoopback()) {
                    continue;
                }
                Enumeration<InetAddress> addr = net.getInetAddresses();
                while (addr.hasMoreElements()) {

                    InetAddress inet = addr.nextElement();

                    if (inet.isSiteLocalAddress()) {
                        name = inet.getHostAddress();
                    }

                    if (!inet.getCanonicalHostName().equalsIgnoreCase(inet.getHostAddress())) {
                        name = inet.getCanonicalHostName();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            name = "localhost";
        }
        return name;
    }

    /**
     * 获取内网IP
     * @return 内网IP
     */
    public static String getSiteIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }

    /**
     * 把IP按点号分4段，每段一整型就一个字节来表示，通过左移位来实现。
     * 第一段放到最高的8位，需要左移24位，依此类推即可
     *
     * @param ipStr ip地址
     * @return 整形
     */
    public static Integer ip2Num(String ipStr) {
        if (ipStr == null || "".equals(ipStr)) {
            return -1;
        }

        if (ipStr.contains(":")) {
            //ipv6的地址，不解析，返回127.0.0.1
            ipStr = "127.0.0.1";
        }

        String[] ips = ipStr.split("\\.");

        return (Integer.parseInt(ips[0]) << 24) + (Integer.parseInt(ips[1]) << 16) + (Integer.parseInt(ips[2]) << 8) + Integer.parseInt(ips[3]);
    }

    /**
     * 把整数分为4个字节，通过右移位得到IP地址中4个点分段的值
     *
     * @param ipNum ip int value
     * @return ip str
     */
    public static String num2Ip(int ipNum) {
        return ((ipNum >> 24) & 0xFF) + "." + ((ipNum >> 16) & 0xFF) + "." + ((ipNum >> 8) & 0xFF) + "." + (ipNum & 0xFF);
    }

}