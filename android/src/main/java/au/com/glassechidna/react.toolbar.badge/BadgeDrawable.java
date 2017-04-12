package au.com.glassechidna.react.toolbar.badge;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class BadgeDrawable extends Drawable
{
	private final Paint geometryPaint = new Paint();
	private final Paint textPaint = new Paint();
	private final int maxNumber;

	private final Rect textBounds = new Rect();

	private boolean displayed = false;
	private String text = null;

	public BadgeDrawable(final int backgroundColor, final int textColor, final float textSize, final int maxNumber)
	{
		geometryPaint.setAntiAlias(true);
		geometryPaint.setStyle(Paint.Style.FILL);
		geometryPaint.setColor(backgroundColor);

		textPaint.setAntiAlias(true);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setColor(textColor);
		textPaint.setTextSize(textSize);

		this.maxNumber = maxNumber;
	}

	@Override
	public void draw(@NonNull final Canvas canvas)
	{
		if (displayed)
		{
			final Rect bounds = getBounds();

			final int width = bounds.width();
			float radius = Math.round(0.6 * textPaint.getTextSize());
			float circleX = width - radius;
			float circleY = radius;

			canvas.drawCircle(circleX, circleY, radius, geometryPaint);

			if (text != null)
			{
				textPaint.getTextBounds(text, 0, text.length(), textBounds);
				float textY = circleY + 0.5f * (textBounds.bottom - textBounds.top);

				canvas.drawText(text, circleX, textY, textPaint);
			}
		}
	}

	@Override
	public void setAlpha(@IntRange(from = 0, to = 255) final int alpha)
	{
		geometryPaint.setAlpha(alpha);
		textPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(@Nullable final ColorFilter cf)
	{
		geometryPaint.setColorFilter(cf);
		textPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity()
	{
		return PixelFormat.TRANSLUCENT;
	}

	public void setNumber(final int number)
	{
		displayed = number != 0;

		if (displayed)
		{
			text = number > maxNumber ? "..." : (number < 0 ? null : Integer.toString(number));
		}
	}
}
