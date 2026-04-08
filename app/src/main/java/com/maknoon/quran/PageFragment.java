package com.maknoon.quran;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import static com.maknoon.quran.MainActivity.EXTRA_page;
import static com.maknoon.quran.MainActivity.nightMode;
import static com.maknoon.quran.MainActivity.pagesFolder;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import coil3.ImageLoader;
import coil3.SingletonImageLoader;
import coil3.request.ImageRequest;
import coil3.svg.SvgDecoder;
import coil3.target.ImageViewTarget;

public class PageFragment extends Fragment
{
	private int page;

	static boolean svg = true; // svg will use Coil, png will use normal ImageView

	private MainActivity mainActivity;
	private final Handler hideHandler = new Handler();
	private final Runnable hideRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			mainActivity.findViewById(R.id.toolbar).setVisibility(View.INVISIBLE);
			WindowCompat.getInsetsController(mainActivity.getWindow(), mainActivity.getWindow().getDecorView()).setAppearanceLightStatusBars(!nightMode);
		}
	};

	@Override
	public void onAttach(@NonNull Context context)
	{
		super.onAttach(context);

		if (context instanceof MainActivity)
			mainActivity = (MainActivity) context;
	}

	static PageFragment newInstance(int page)
	{
		final PageFragment fragmentFirst = new PageFragment();
		final Bundle args = new Bundle();
		args.putInt(EXTRA_page, page);
		fragmentFirst.setArguments(args);
		return fragmentFirst;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		page = getArguments().getInt(EXTRA_page, 0);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.page_fragment, container, false);
		final MaterialToolbar toolbar = mainActivity.findViewById(R.id.toolbar);
		final AppCompatImageView pageView = view.findViewById(R.id.page);
		pageView.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View v)
					{
						final WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(mainActivity.getWindow(), mainActivity.getWindow().getDecorView());
						hideHandler.removeCallbacks(hideRunnable);
						if (toolbar.getVisibility() == View.VISIBLE) {
							toolbar.setVisibility(View.INVISIBLE);
                            insetsController.setAppearanceLightStatusBars(!nightMode);
						}
						else
						{
							//hideHandler.postDelayed(hideRunnable, 5000); // in ms
							toolbar.setVisibility(View.VISIBLE);
							insetsController.setAppearanceLightStatusBars(false);
						}
					}
				}
		);

		final WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(mainActivity.getWindow(), mainActivity.getWindow().getDecorView());
		if (toolbar.getVisibility() == View.VISIBLE)
			insetsController.setAppearanceLightStatusBars(false);
		else
			insetsController.setAppearanceLightStatusBars(!nightMode);

		hideHandler.postDelayed(hideRunnable, 5000);

		/*
		cannot have drawable converted from svg files since it has very lengthy paths resulting in:
		'java.lang.IllegalArgumentException: R is not a valid verb. Failure occurred at position 2 of path: STRING_TOO_LARGE'
		No solution for it using optimizers e.g. svgo. Android recommend 200dp x 200dp for performance. those files will hit the performance badly
		It is replaced with Coil lib to render the svg
		*/
		if (svg)
		{
			final File svgFile;
			if (pagesFolder.contains("warsh"))
				svgFile = new File(pagesFolder + "/warsh/" + (page + 1) + ".svgz");
			else if (pagesFolder.contains("douri"))
				svgFile = new File(pagesFolder + "/douri/" + (page + 1) + ".svgz");
			else if (pagesFolder.contains("qalon"))
				svgFile = new File(pagesFolder + "/qalon/" + (page + 1) + ".svgz");
			else //if (pagesFolder.contains("shubah"))
				svgFile = new File(pagesFolder + "/shubah/" + (page + 1) + ".svgz");

			final ImageLoader imageLoader = SingletonImageLoader.get(mainActivity);
			imageLoader.enqueue(new ImageRequest.Builder(mainActivity)
					.data(pagesFolder.contains("hafs") ? Uri.parse("file:///android_asset/hafs/" + (page + 1) + ".svgz") : svgFile)
					.decoderFactory(new SvgDecoder.Factory())
					.target(new ImageViewTarget(pageView))
					.build());
		}
		else
		{
			InputStream in = null;

			try
			{
				if (pagesFolder.contains("hafs"))
				{
					final AssetManager am = getResources().getAssets();
					in = am.open("hafs/" + (page + 1) + ".png");
				}
				else if (pagesFolder.contains("warsh"))
					in = new FileInputStream((pagesFolder + "/warsh/" + (page + 1) + ".png"));
				else if (pagesFolder.contains("douri"))
					in = new FileInputStream((pagesFolder + "/douri/" + (page + 1) + ".png"));
				else if (pagesFolder.contains("qalon"))
					in = new FileInputStream((pagesFolder + "/qalon/" + (page + 1) + ".png"));
				else //if (pagesFolder.contains("shubah"))
					in = new FileInputStream((pagesFolder + "/shubah/" + (page + 1) + ".png"));

				final Drawable pg = Drawable.createFromStream(in, null);
				pageView.setImageDrawable(pg);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (in != null)
				{
					try
					{
						in.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}

		if (nightMode)
		{
			mainActivity.getWindow().getDecorView().setBackgroundColor(Color.BLACK);

			//pg.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
			//pg.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

			float[] invertMX = {
					-1f, 0f, 0f, 0f, 255f,
					0f, -1f, 0f, 0f, 255f,
					0f, 0f, -1f, 0f, 255f,
					0f, 0f, 0f, 1f, 0f
			};

			final ColorMatrix colorMatrix = new ColorMatrix(invertMX);
			final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
			pageView.setColorFilter(filter);
		}
		else
			mainActivity.getWindow().getDecorView().setBackgroundColor(Color.rgb(255, 255, 242)); // "#FFFFF2"

		return view;
	}
}