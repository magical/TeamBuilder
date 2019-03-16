package rec.games.pokemon.teambuilder.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule //hides warnings
public class GlideUtil extends AppGlideModule
{
	private static final String TAG = GlideUtil.class.getSimpleName();

	@Override
	public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder)
	{
		builder.setLogLevel(Log.ERROR); //only show major errors, not if failed to load error for sprites >= 10091
		//int diskCacheSizeBytes = 1024 * 1024 * 100; //limit to 100 MB, former limit of 250 MB
		//builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
	}
}
