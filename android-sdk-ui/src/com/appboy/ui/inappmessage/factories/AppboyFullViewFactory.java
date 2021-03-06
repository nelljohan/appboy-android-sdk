package com.appboy.ui.inappmessage.factories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.widget.RelativeLayout;

import com.appboy.Appboy;
import com.appboy.IAppboyImageLoader;
import com.appboy.enums.AppboyViewBounds;
import com.appboy.enums.inappmessage.ImageStyle;
import com.appboy.enums.inappmessage.Orientation;
import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageFull;
import com.appboy.support.StringUtils;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageFullView;
import com.appboy.ui.support.FrescoLibraryUtils;
import com.appboy.ui.support.ViewUtils;

public class AppboyFullViewFactory implements IInAppMessageViewFactory {

  @Override
  public AppboyInAppMessageFullView createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
    Context applicationContext = activity.getApplicationContext();
    InAppMessageFull inAppMessageFull = (InAppMessageFull) inAppMessage;
    boolean isGraphic = inAppMessageFull.getImageStyle().equals(ImageStyle.GRAPHIC);
    AppboyInAppMessageFullView view = getAppropriateFullView(activity, isGraphic);
    view.inflateStubViews(activity, inAppMessageFull);

    if (FrescoLibraryUtils.canUseFresco(applicationContext)) {
      view.setMessageSimpleDrawee(inAppMessageFull);
    } else {
      // Since this image is the width of the screen, the view bounds are uncapped
      String imageUrl = view.getAppropriateImageUrl(inAppMessage);
      if (!StringUtils.isNullOrEmpty(imageUrl)) {
        IAppboyImageLoader appboyImageLoader = Appboy.getInstance(applicationContext).getAppboyImageLoader();
        appboyImageLoader.renderUrlIntoView(applicationContext, imageUrl, view.getMessageImageView(), AppboyViewBounds.NO_BOUNDS);
      }
    }

    // modal frame should not be clickable.
    view.getFrameView().setOnClickListener(null);
    view.setMessageBackgroundColor(inAppMessageFull.getBackgroundColor());
    view.setFrameColor(inAppMessageFull.getFrameColor());
    view.setMessageButtons(inAppMessageFull.getMessageButtons());
    view.setMessageCloseButtonColor(inAppMessageFull.getCloseButtonColor());
    if (!isGraphic) {
      view.setMessage(inAppMessageFull.getMessage());
      view.setMessageTextColor(inAppMessageFull.getMessageTextColor());
      view.setMessageHeaderText(inAppMessageFull.getHeader());
      view.setMessageHeaderTextColor(inAppMessageFull.getHeaderTextColor());
      view.setMessageHeaderTextAlignment(inAppMessageFull.getHeaderTextAlign());
      view.setMessageTextAlign(inAppMessageFull.getMessageTextAlign());
      view.resetMessageMargins(inAppMessageFull.getImageDownloadSuccessful());
    }
    resetLayoutParamsIfAppropriate(activity, inAppMessageFull, view);

    return view;
  }

  /**
   * For in-app messages that have a preferred orientation and are being displayed on tablet,
   * ensure the in-app message appears in the style of the preferred orientation regardless of
   * actual screen orientation.
   *
   * @param activity
   * @param inAppMessage
   * @param view
   * @return true if params were reset
   */
  boolean resetLayoutParamsIfAppropriate(Activity activity, IInAppMessage inAppMessage, AppboyInAppMessageFullView view) {
    if (!ViewUtils.isRunningOnTablet(activity)) {
      return false;
    }
    if (inAppMessage.getOrientation() == null || inAppMessage.getOrientation() == Orientation.ANY) {
      return false;
    }
    int longEdge = view.getLongEdge();
    int shortEdge = view.getShortEdge();
    if (longEdge > 0 && shortEdge > 0) {
      RelativeLayout.LayoutParams layoutParams;
      if (inAppMessage.getOrientation() == Orientation.LANDSCAPE) {
        layoutParams = new RelativeLayout.LayoutParams(longEdge, shortEdge);
      } else {
        layoutParams = new RelativeLayout.LayoutParams(shortEdge, longEdge);
      }
      layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
      view.getMessageBackgroundObject().setLayoutParams(layoutParams);
      return true;
    }
    return false;
  }

  @SuppressLint("InflateParams")
  AppboyInAppMessageFullView getAppropriateFullView(Activity activity, boolean isGraphic) {
    if (isGraphic) {
      return (AppboyInAppMessageFullView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_full_graphic, null);
    } else {
      return (AppboyInAppMessageFullView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_full, null);
    }
  }
}
