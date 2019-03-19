package ir.nimcode.dolphin.api;

import java.util.Map;

import ir.nimcode.dolphin.model.CheckUpdate;
import ir.nimcode.dolphin.model.Document;
import ir.nimcode.dolphin.model.FormsUpdate;
import ir.nimcode.dolphin.model.User;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by saeed on 11/15/17.
 */

public interface APIBaseAdapter {

    String HEADERS = "Accept: application/json";

    @FormUrlEncoded
    @Headers(HEADERS)
    @POST("checkUpdate")
    Call<ResponseBase<CheckUpdate>> checkUpdate(@Field("version_code") int version_code);

    @FormUrlEncoded
    @Headers(HEADERS)
    @POST("updatePlayId")
    Call<ResponseBody> updatePlayId(@Header("Authorization") String authorization,
                                    @Field("play_id") String play_id);

    @FormUrlEncoded
    @Headers(HEADERS)
    @POST("login")
    Call<ResponseBase<User>> login(@Field("play_id") String play_id,
                                   @Field("username") String first_name,
                                   @Field("password") String last_name);


    @FormUrlEncoded
    @Headers(HEADERS)
    @POST("forgetPassword")
    Call<ResponseBody> forgetPassword(@Field("token") String token,
                                      @Field("mobile") String mobile);

    @FormUrlEncoded
    @Headers(HEADERS)
    @POST("changePassword")
    Call<ResponseBody> changePassword(@Header("Authorization") String authorization,
                                      @Field("old_password") String old_password,
                                      @Field("new_password") String new_password);

    @FormUrlEncoded
    @Headers(HEADERS)
    @POST("changeProfileInfo")
    Call<ResponseBody> changeProfileInfo(@Header("Authorization") String authorization,
                                         @Field("fullname") String fullname,
                                         @Field("mobile") String mobile,
                                         @Field("email") String email);

    @Multipart
    @Headers(HEADERS)
    @POST("upload")
    Call<ResponseBody> upload(@Header("Authorization") String authorization,
                              @Part MultipartBody.Part image);

    @Headers(HEADERS)
    @POST("saveDocument")
    Call<ResponseBody> saveDocument(@Header("Authorization") String authorization,
                                    @Body Map<String, Object> document);

    @Headers(HEADERS)
    @POST("saveDocumentSeries")
    Call<ResponseBody> saveDocumentSeries(@Header("Authorization") String authorization,
                                          @Body Map<String, Object> documentSeries);

    @FormUrlEncoded
    @Headers(HEADERS)
    @POST("getFormsUpdate")
    Call<ResponseBase<FormsUpdate>> getFormsUpdate(@Header("Authorization") String authorization,
                                                   @Field("last_forms_updated_time") long last_forms_updated_time);

    @FormUrlEncoded
    @Headers(HEADERS)
    @POST("getDocumentsUpdate")
    Call<ResponseBase<Pagination<Document>>> getDocumentsUpdate(@Header("Authorization") String authorization,
                                                                @Field("last_documents_updated_time") long last_documents_updated_time,
                                                                @Field("page") int page);

    @FormUrlEncoded
    @Headers(HEADERS)
    @POST("getKmlsUpdate")
    Call<ResponseBody> getKmlsUpdate(@Header("Authorization") String authorization,
                                     @Field("last_kmls_updated_time") long last_kmls_updated_time);


}