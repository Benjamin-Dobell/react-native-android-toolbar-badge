package au.com.glassechidna.react.toolbar.badge;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

public class BadgeWrapperDrawable extends LayerDrawable
{
	private final int width;
	private final int height;

	public BadgeWrapperDrawable(final Drawable iconDrawable, final BadgeDrawable badgeDrawable, final int width, final int height)
	{
		super(new Drawable[] { iconDrawable, badgeDrawable });

		this.width = width;
		this.height = height;
	}

	@Override
	public int getIntrinsicWidth()
	{
		return width;
	}

	@Override
	public int getIntrinsicHeight()
	{
		return height;
	}
}
