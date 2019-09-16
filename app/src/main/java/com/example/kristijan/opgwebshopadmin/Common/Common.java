package com.example.kristijan.opgwebshopadmin.Common;



import com.example.kristijan.opgwebshopadmin.Remote.APIService;
import com.example.kristijan.opgwebshopadmin.Remote.FCMRetrofitClient;

public class Common {

    private static final String fcmUrl="https://fcm.googleapis.com";

    public static APIService getFCMClient()
    {
    return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }
}
