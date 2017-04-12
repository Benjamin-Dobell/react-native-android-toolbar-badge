package au.com.glassechidna.react.toolbar.badge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.*;

import au.com.glassechidna.react.drawables.DrawableStore;

public class ToolbarBadgeAndroidModule extends ReactContextBaseJavaModule
{
	public static final String REACT_CLASS = "RNToolbarBadgeAndroidModule";

	public static DrawableStore getDrawableStore()
	{
		return drawableStore;
	}

	@Override
	public String getName()
	{
		return REACT_CLASS;
	}

	public ToolbarBadgeAndroidModule(final ReactApplicationContext reactContext)
	{
		super(reactContext);
	}

	@ReactMethod
	public void obtainBadgeDrawableName(final ReadableMap icon, final Integer width, final Integer height, final Integer backgroundColor,
		final Integer textColor, final Integer textSize, final Integer maxNumber, final Integer number, final Callback callback)
	{
		final String imageUri = icon.getString(PROP_ICON_URI);
		final int badgeNumber = number < 0 ? -1 : (number > maxNumber ? Integer.MAX_VALUE : number);

		final String badgeDrawableIdentifier = backgroundColor + "_" + textColor + "_" + textSize + "_" + badgeNumber;
		final String badgeUri = BADGE_URI_PREFIX + imageUri.replace("://", "_") + "?" + width + "_" + height + "_" + badgeDrawableIdentifier;

		if (drawableStore.getIdentifier(badgeUri) != 0)
		{
			callback.invoke(null, badgeUri);
		}
		else
		{
			final BadgeDrawable badgeDrawable;

			if (badgeDrawableMap.containsKey(badgeDrawableIdentifier))
			{
				badgeDrawable = badgeDrawableMap.get(badgeDrawableIdentifier);
			}
			else
			{
				badgeDrawable = new BadgeDrawable(backgroundColor, textColor, textSize, maxNumber);
				badgeDrawable.setNumber(badgeNumber);

				badgeDrawableMap.put(badgeDrawableIdentifier, badgeDrawable);
			}

			if (imageUri.startsWith("http://") || imageUri.startsWith("https://") || imageUri.startsWith("file://"))
			{
				createBadgeFromBitmap(imageUri, badgeUri, width, height, badgeDrawable, callback);
			}
			else
			{
				createBadgeFromDrawable(imageUri, badgeUri, width, height, badgeDrawable, callback);
			}
		}
	}

	@ReactMethod
	public void cleanUp()
	{
		badgeDrawableMap.clear();
		drawableStore.clear();

		for (int i = 0; i < imageReferences.size(); i++)
		{
			imageReferences.get(i).close();
		}

		imageReferences.clear();
	}

	private Drawable buildWrapperDrawable(final BadgeDrawable badgeDrawable, final int badgeWidth, final int badgeHeight, final Drawable drawable, final int drawableWidth, final int drawableHeight)
	{
		final int width = drawableWidth <= 0 ? badgeWidth : Math.min(drawableWidth, badgeWidth);
		final int height = drawableHeight <= 0 ? badgeHeight : Math.min(drawableHeight, badgeHeight);

		final int left = (badgeWidth - width) / 2;
		final int top = (badgeHeight - height) / 2;
		final int right = badgeWidth - left - width;
		final int bottom = badgeHeight - top - height;

		final BadgeWrapperDrawable wrapperDrawable = new BadgeWrapperDrawable(drawable, badgeDrawable, badgeWidth, badgeHeight);
		wrapperDrawable.setLayerInset(0, left, top, right, bottom);
		return wrapperDrawable;
	}

	private void createBadgeFromBitmap(final String imageUri, final String badgeUri, final int badgeWidth, final int badgeHeight, final BadgeDrawable badgeDrawable, final Callback callback)
	{
		final ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imageUri))
			.setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
			.setProgressiveRenderingEnabled(false)
			.build();

		final ImagePipeline imagePipeline = Fresco.getImagePipeline();
		final DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(request, getReactApplicationContext());

		final DataSubscriber<CloseableReference<CloseableImage>> dataSubscriber = new BaseDataSubscriber<CloseableReference<CloseableImage>>() {
			@Override
			protected void onNewResultImpl(final DataSource<CloseableReference<CloseableImage>> dataSource)
			{
				if (dataSource.isFinished())
				{
					final CloseableReference<CloseableImage> imageReference = dataSource.getResult();
					final CloseableImage image = imageReference.get();

					if (image instanceof CloseableBitmap)
					{
						imageReferences.add(imageReference);

						final Bitmap bitmap = ((CloseableBitmap) image).getUnderlyingBitmap();
						final BitmapDrawable bitmapDrawable = new BitmapDrawable(getReactApplicationContext().getResources(), bitmap);

						final Drawable wrapperDrawable = buildWrapperDrawable(badgeDrawable, badgeWidth, badgeHeight, bitmapDrawable, bitmap.getWidth(), bitmap.getHeight());

						drawableStore.setDrawable(badgeUri, wrapperDrawable);
						callback.invoke(null, badgeUri);
					}
					else
					{
						imageReference.close();
						callback.invoke(imageUri + " is not decodable as a regular bitmap");
					}
				}
			}

			@Override
			protected void onFailureImpl(final DataSource<CloseableReference<CloseableImage>> dataSource)
			{
				callback.invoke(dataSource.getFailureCause().getMessage());
			}
		};

		dataSource.subscribe(dataSubscriber, CallerThreadExecutor.getInstance());
	}

	private void createBadgeFromDrawable(final String drawableName, final String badgeUri, final int badgeWidth, final int badgeHeight, final BadgeDrawable badgeDrawable, final Callback callback)
	{
		final Resources res = getReactApplicationContext().getResources();
		final int drawableIdentifier = res.getIdentifier(drawableName, "drawable", getReactApplicationContext().getPackageName());

		if (drawableIdentifier == 0)
		{
			callback.invoke(drawableName + " is not a valid URL or the name of a drawable");
		}
		else
		{
			final Drawable drawable = res.getDrawable(drawableIdentifier);
			final Drawable wrapperDrawable = buildWrapperDrawable(badgeDrawable, badgeWidth, badgeHeight, drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

			drawableStore.setDrawable(badgeUri, wrapperDrawable);
			callback.invoke(null, badgeUri);
		}
	}


	private static final String BADGE_URI_PREFIX = "toolbar-badge://";
	private static final String PROP_ICON_URI = "uri";

	private static final HashMap<String, BadgeDrawable> badgeDrawableMap = new HashMap<String, BadgeDrawable>();
	private static final DrawableStore drawableStore = new DrawableStore();
	private static final List<CloseableReference<CloseableImage>> imageReferences = new ArrayList<CloseableReference<CloseableImage>>();
}
