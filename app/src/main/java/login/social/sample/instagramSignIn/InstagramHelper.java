package login.social.sample.instagramSignIn;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by krunal on 03-Nov-16.
 */

public class InstagramHelper {

    private InstagramDialog mDialog;
    private InstagramUser mInstagramUser;
    private InstagramResponse mListener;

    private ProgressDialog mProgress;
    private String mAuthUrl;
    private String mTokenUrl;
    private String mAccessToken;
    private String mClientId;
    private String mClientSecret;
    private static int WHAT_ERROR = 1;
    private static int WHAT_FETCH_INFO = 2;

    /**
     * Callback url, as set in 'Manage OAuth Costumers' page
     * (https://developer.github.com/)
     */
    public static String mCallbackUrl = "";
    private static final String AUTH_URL = "https://api.instagram.com/oauth/authorize/";
    private static final String TOKEN_URL = "https://api.instagram.com/oauth/access_token";
    private static final String API_URL = "https://api.instagram.com/v1";
    private static final String TAG = "InstagramAPI";


    private Context mContext;

    public InstagramHelper(@NonNull String clientId,
                           @NonNull String clientSecret,
                           @NonNull String callbackUrl,
                           @NonNull Context context,
                           @NonNull InstagramResponse listeners) {
        if (clientId == null)
            throw new IllegalArgumentException("Instagram client id cannot be null.");
        else if (clientSecret == null)
            throw new IllegalArgumentException("Instagram client secret cannot be null.");
        else if (callbackUrl == null)
            throw new IllegalArgumentException("Instagram callback url cannot be null.");

        mContext = context;
        mListener = listeners;
        mClientId = clientId;
        mClientSecret = clientSecret;
        mCallbackUrl = callbackUrl;
        mTokenUrl = TOKEN_URL + "?client_id=" + mClientId + "&client_secret="
                + mClientSecret + "&redirect_uri=" + mCallbackUrl + "&grant_type=authorization_code";
        mAuthUrl = AUTH_URL + "?client_id=" + mClientId + "&redirect_uri="
                + mCallbackUrl + "&response_type=code&display=touch&scope=likes+comments+relationships";
        InstagramDialog.OAuthDialogListener listener = new InstagramDialog.OAuthDialogListener() {
            @Override
            public void onComplete(String code) {
                getAccessToken(code);
            }

            @Override
            public void onError(String error) {
                mListener.onInstagramSignInFail("Authorization failed");
            }
        };
        mDialog = new InstagramDialog(context, mAuthUrl, listener);
        mProgress = new ProgressDialog(context);
        mProgress.setCancelable(false);

    }

    public void performSignIn() {
        if (mAccessToken != null) {
            mAccessToken = null;
        } else {
            mDialog.show();
        }
    }

    private void getAccessToken(final String code) {
        mProgress.setMessage("Getting access token ...");
        mProgress.show();
        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Getting access token");
                int what = WHAT_FETCH_INFO;
                try {
                    URL url = new URL(TOKEN_URL);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write("client_id=" + mClientId +
                            "&client_secret=" + mClientSecret +
                            "&grant_type=authorization_code" +
                            "&redirect_uri=" + mCallbackUrl +
                            "&code=" + code);
                    writer.flush();
                    String response = streamToString(urlConnection.getInputStream());
                    Log.i(TAG, "response " + response);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();

                    mAccessToken = jsonObj.getString("access_token");
                    mInstagramUser = new InstagramUser();
                    mInstagramUser.setAccesstoken(jsonObj.getString("access_token"));
                    mInstagramUser.setUsername(jsonObj.getJSONObject("user").getString("username"));
                    mInstagramUser.setBio(jsonObj.getJSONObject("user").getString("bio"));
                    mInstagramUser.setWebsite(jsonObj.getJSONObject("user").getString("website"));
                    mInstagramUser.setProfile_picture(jsonObj.getJSONObject("user").getString("profile_picture"));
                    mInstagramUser.setFull_name(jsonObj.getJSONObject("user").getString("full_name"));
                    mInstagramUser.setId(jsonObj.getJSONObject("user").getString("id"));

                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(what, 1, 0));
            }
        }.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_ERROR) {
                mProgress.dismiss();
                if (msg.arg1 == 1) {
                    mListener.onInstagramSignInFail("Failed to get access token");
                } else if (msg.arg1 == 2) {
                    mListener.onInstagramSignInFail("Failed to get user information");
                }
            } else {
                mProgress.dismiss();
                mListener.onInstagramSignInSuccess(mInstagramUser);
            }
        }
    };

    private String streamToString(InputStream is) throws IOException {
        String str = "";
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
            } finally {
                is.close();
            }
            str = sb.toString();
        }
        return str;
    }
}
