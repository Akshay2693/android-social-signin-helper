package login.social.sample.googleAuthSignin;

import com.google.android.gms.common.api.Status;

/**
 * Created by multidots on 6/21/2016.
 */

public interface GoogleAuthResponse {

    void onGoogleAuthSignIn(GoogleAuthUser user);

    void onGoogleAuthSignInFailed();

    void onGoogleAuthSignOut(Status status);
}
