package com.kongzhong.mrpc.utils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (context.charAt(0) != '/')
            sb.append('/');
        sb.append(context);
        if (path.charAt(0) != '/')
            sb.append('/');
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
        if (path.charAt(0) != '/')
            sb.append('/');
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
        if (context.charAt(0) != '/')
            sb.append('/');
        sb.append(context);
        if (path.charAt(0) != '/')
            sb.append('/');
        sb.append(path);
        return sb.toString();
    }

    public static boolean isValidAddress(String address) {
        return ADDRESS_PATTERN.matcher(address).matches();
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
        if (LOCAL_ADDRESS != null)
            return LOCAL_ADDRESS;
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

    public static String findAddressFromUrl(String url) {
        String address = "";
        Pattern ADDRESS_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}\\:\\d{1,5}");
        Matcher matcher = ADDRESS_PATTERN.matcher(url);
        if (matcher.find()) {
            matcher.reset();
            while (matcher.find()) {// 找到匹配的字符串
                address = matcher.group(0);
                break;
            }
        }
        return address;
    }

    public static String findIpFromUrl(String url) {
        String address = "";
        Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");
        Matcher matcher = IP_PATTERN.matcher(url);
        if (matcher.find()) {
            matcher.reset();
            while (matcher.find()) {// 找到匹配的字符串
                address = matcher.group(0);
                break;
            }
        }
        return address;
    }
}