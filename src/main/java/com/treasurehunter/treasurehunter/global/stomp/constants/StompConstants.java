package com.treasurehunter.treasurehunter.global.stomp.constants;

public class StompConstants {

    private StompConstants() {}

    public static final String AUTH_HEADER = "Authorization";
    public static final String PREFIX_BEARER = "Bearer ";

    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_AUTHORITIES = "authorities";
    public static final String ATTR_EXP = "exp";

    public static final String DEST_CHAT_ROOM_PREFIX = "/queue/chat.room.";
    public static final String DEST_ERROR_MESSAGE = "/user/queue/error";
    public static final int ROOM_ID_LEN = 36;
}
