package login.social.sample.instagramSignIn;

/**
 * Created by krunal on 03-Nov-16.
 */

public interface InstagramResponse {

    public abstract void onInstagramSignInSuccess(InstagramUser user);

    public abstract void onInstagramSignInFail(String error);
}
