package hu.kojak.android.restservice.restapi;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Request<Progress, Result, RestInterface>
        extends AsyncTask<Void, Progress, Result> {

  private final Context mContext;
  private final Class<RestInterface> mRestClass;
  private final String mQueryID;

  private Exception mException = null;

  public Request(@NonNull Context context, @NonNull Class<RestInterface> restInterfaceClass,
                 @Nullable String queryID) {

    mContext = context;
    mRestClass = restInterfaceClass;
    mQueryID = queryID;
  }

  final Class<RestInterface> getRestClass() {
    return mRestClass;
  }

  public final String getQueryID() {
    return mQueryID;
  }

  public final Context getContext() {
    return mContext;
  }

  @TargetApi(11)
  public final void exec() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      execute();
    }
  }


  @Override
  protected Result doInBackground(Void... params) {
    try {
      return run(mContext, RestfulWebService.getService(mRestClass));
    } catch (Exception e) {
      mException = e;
      cancel(true);
    }
    return null;
  }

  @Override
  protected final void onPostExecute(Result result) {
    onPostExecute(mContext, result);
    onFinally(mContext);
  }

  @Override
  protected final void onCancelled() {
    cancelled();
  }

  @Override
  protected final void onCancelled(Result result) {
    cancelled();
  }

  private void cancelled() {
    if (mException != null) {
      onException(mContext, mException);
    } else {
      onCancelled(mContext);
    }
    onFinally(mContext);
  }

  /**
   * Runs the request with the given restService.
   */
  public abstract Result run(Context context, RestInterface restService) throws Exception;

  /**
   * Called after a request finished successfully and no error occured.
   *
   * Runs on UI thread.
   */
  public abstract void onPostExecute(Context context, Result result);


  /**
   * Called when the request was cancelled.
   * The reason could be a cancellation of the running thread.
   *
   * Called on the UI thread.
   *
   */
  public abstract void onCancelled(Context context);


  /**
   * Called if a retrofit error occured while executing request.
   * This method is called on the UI thread.
   */
  public abstract void onException(Context context, Exception error);


  /**
   * This function is called as the last function.
   * After onException, onCancelled or onPostExecute, but one thing is sure: it will be called
   * for sure and it will be the last called function in the object lifecycle.
   */
  public abstract void onFinally(Context context);



}
