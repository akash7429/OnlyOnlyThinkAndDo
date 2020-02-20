package com.example.thinkanddo.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({"Content-Type:application/json",
            "Authorization:key=AAAAMMIph68:APA91bFJrgbfCwd6gELs7d0ffLALkdvST16p3u4xEpQBQ0J0hmlCdDR6u5GQCu9V1hdL8CPsL5HiyH_pD9Zua7_ZKsFPOrLG-HqAvkbWv_-UIviIAIb7U6XsmLN4iJl9Acq6eo9Px757"

    })
    @POST("fcm/send")
    Call<Response>sendNotification(@Body Sender body);
}
