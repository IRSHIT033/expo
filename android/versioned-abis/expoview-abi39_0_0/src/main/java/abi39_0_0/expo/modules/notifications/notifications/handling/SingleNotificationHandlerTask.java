package abi39_0_0.expo.modules.notifications.notifications.handling;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import abi39_0_0.org.unimodules.core.ModuleRegistry;
import abi39_0_0.org.unimodules.core.Promise;
import abi39_0_0.org.unimodules.core.interfaces.services.EventEmitter;

import abi39_0_0.expo.modules.notifications.notifications.NotificationSerializer;
import expo.modules.notifications.notifications.model.Notification;
import expo.modules.notifications.notifications.model.NotificationBehavior;
import expo.modules.notifications.notifications.service.NotificationsHelper;

/**
 * A "task" responsible for managing response to a single notification.
 */
/* package */ class SingleNotificationHandlerTask {
  /**
   * Name of the event asking the delegate for behavior.
   */
  private final static String HANDLE_NOTIFICATION_EVENT_NAME = "onHandleNotification";
  /**
   * Name of the event emitted if the delegate doesn't respond in time.
   */
  private final static String HANDLE_NOTIFICATION_TIMEOUT_EVENT_NAME = "onHandleNotificationTimeout";

  /**
   * Seconds since sending the {@link #HANDLE_NOTIFICATION_EVENT_NAME} until the task
   * is considered timed out.
   */
  private final static int SECONDS_TO_TIMEOUT = 3;

  private Handler mHandler;
  private EventEmitter mEventEmitter;
  private Notification mNotification;
  private NotificationBehavior mBehavior;
  private NotificationsHelper mNotificationsHelper;
  private NotificationsHandler mDelegate;

  private Runnable mTimeoutRunnable = SingleNotificationHandlerTask.this::handleTimeout;

  /* package */ SingleNotificationHandlerTask(Handler handler, ModuleRegistry moduleRegistry, Notification notification, NotificationsHelper notificationsHelper, NotificationsHandler delegate) {
    mHandler = handler;
    mEventEmitter = moduleRegistry.getModule(EventEmitter.class);
    mNotification = notification;
    mNotificationsHelper = notificationsHelper;
    mDelegate = delegate;
  }

  /**
   * @return Identifier of the task.
   */
  /* package */ String getIdentifier() {
    return mNotification.getNotificationRequest().getIdentifier();
  }

  /**
   * Starts the task, i.e. sends an event to the app's delegate and starts a timeout
   * after which the task finishes itself.
   */
  /* package */ void start() {
    Bundle eventBody = new Bundle();
    eventBody.putString("id", getIdentifier());
    eventBody.putBundle("notification", NotificationSerializer.toBundle(mNotification));
    mEventEmitter.emit(HANDLE_NOTIFICATION_EVENT_NAME, eventBody);

    mHandler.postDelayed(mTimeoutRunnable, SECONDS_TO_TIMEOUT * 1000);
  }

  /**
   * Stops the task abruptly (in case the app is being destroyed and there is no reason
   * to wait for the response anymore).
   */
  /* package */ void stop() {
    finish();
  }

  /**
   * Informs the task of a response - behavior requested by the app.
   *
   * @param behavior Behavior requested by the app
   * @param promise  Promise to fulfill once the behavior is applied to the notification.
   */
  /* package */ void handleResponse(NotificationBehavior behavior, final Promise promise) {
    mBehavior = behavior;
    if (!behavior.shouldShowAlert()) {
      promise.resolve(null);
      finish();
      return;
    }

    mHandler.post(new Runnable() {
      @Override
      public void run() {
        mNotificationsHelper.presentNotification(mNotification, mBehavior, new ResultReceiver(mHandler) {
          @Override
          protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == NotificationsHelper.SUCCESS_CODE) {
              promise.resolve(null);
            } else {
              Exception e = (Exception) resultData.getSerializable(NotificationsHelper.EXCEPTION_KEY);
              promise.reject("ERR_NOTIFICATION_PRESENTATION_FAILED", "Notification presentation failed.", e);
            }
          }
        });
        finish();
      }
    });
  }

  /**
   * Callback called by {@link #mTimeoutRunnable} after timeout time elapses.
   * <p>
   * Sends a timeout event to the app.
   */
  private void handleTimeout() {
    Bundle eventBody = new Bundle();
    eventBody.putString("id", getIdentifier());
    eventBody.putBundle("notification", NotificationSerializer.toBundle(mNotification));
    mEventEmitter.emit(HANDLE_NOTIFICATION_TIMEOUT_EVENT_NAME, eventBody);

    finish();
  }

  /**
   * Callback called when the task fulfills its responsibility. Clears up {@link #mHandler}
   * and informs {@link #mDelegate} of the task's state.
   */
  private void finish() {
    mHandler.removeCallbacks(mTimeoutRunnable);
    mDelegate.onTaskFinished(this);
  }
}
