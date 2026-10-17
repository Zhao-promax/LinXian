package com.cirida.zhao.linxian.common.constant;

/**
 * 全局常量。
 */
public final class Constants {

    private Constants() {
    }

    /** 网关透传用户身份的请求头 */
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";

    /** JWT 声明键 */
    public static final String JWT_CLAIM_USER_ID = "userId";
    public static final String JWT_CLAIM_USERNAME = "username";

    /** 订单超时未支付时长（毫秒），15 分钟 */
    public static final long ORDER_TIMEOUT_MILLIS = 15 * 60 * 1000L;
}
