package login.social.sample.instagramSignIn;

/**
 * Created by krunal on 03-Nov-16.
 */

public interface InstagramResponse {

    void onInstagramSignInSuccess(InstagramUser user);

    void onInstagramSignInFail(String error);
}
