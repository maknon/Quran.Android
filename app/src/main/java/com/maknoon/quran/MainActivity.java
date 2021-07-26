package com.maknoon.quran;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.play.core.assetpacks.AssetPackLocation;
import com.google.android.play.core.assetpacks.AssetPackManager;
import com.google.android.play.core.assetpacks.AssetPackManagerFactory;
import com.google.android.play.core.assetpacks.AssetPackState;
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener;
import com.google.android.play.core.assetpacks.AssetPackStates;
import com.google.android.play.core.assetpacks.model.AssetPackStatus;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
	final static String TAG = "MainActivity";

	String language = "ar";
	SharedPreferences mPrefs;

	int page = 0;
	static String pagesFolder = "hafs"; // hafs warsh
	static boolean nightMode = false;

	PagerAdapter pagerAdapter;
	ViewPager2 mViewPager;

	static final String assetPackName = "asset_pack_warsh";

	static final String EXTRA_page = "com.maknoon.quran.page";
	static final String EXTRA_nightMode = "com.maknoon.quran.nightMode";
	static final String EXTRA_pagesFolder = "com.maknoon.quran.pagesFolder";

	AssetPackManager assetPackManager;

	MenuItem qiraatMenuItem;

	// This flag should be set to true to enable VectorDrawable support for API < 21
	static
	{
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}

	@Override
	protected void attachBaseContext(Context base)
	{
		mPrefs = base.getSharedPreferences("setting", Context.MODE_PRIVATE);
		language = mPrefs.getString("language", "ar");
		super.attachBaseContext(ContextWrapper.wrap(base, new Locale(language)));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if (savedInstanceState != null) // when recreate() this activity if language is change. savedInstanceState will be saved so we need to force the direction
		{
			// Force RTL or LTR for the appbar and FloatingActionButton since it is not working when changing the language.
			if (language.equals("ar"))
				getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
			else
				getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

			page = savedInstanceState.getInt(EXTRA_page);
			nightMode = savedInstanceState.getBoolean(EXTRA_nightMode);
		}
		else
			page = mPrefs.getInt(EXTRA_page, 0);

		pagesFolder = mPrefs.getString(EXTRA_pagesFolder, "hafs");

		final ActionBar ab = getSupportActionBar();
		if (ab != null)
		{
			ab.setTitle(null);
			ab.hide();
			ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#59000000")));
			ab.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#73000000")));
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		pagerAdapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle());
		mViewPager = findViewById(R.id.viewpager);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setCurrentItem(page, false);
		mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
		{
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{
				super.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}

			@Override
			public void onPageSelected(int position)
			{
				super.onPageSelected(position);

				page = position;

				final SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putInt(EXTRA_page, page).apply();
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{
				super.onPageScrollStateChanged(state);
			}
		});

		mViewPager.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View v)
					{
						final ActionBar ab = getSupportActionBar();
						if (ab != null)
						{
							if (ab.isShowing())
								ab.hide();
							else
								ab.show();
						}
					}
				}
		);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final int id = item.getItemId();
		switch (id)
		{
			case android.R.id.home:
				getSupportActionBar().hide();
				return true;

			case R.id.list:
				final DialogFragment goTo = new GoTo();
				goTo.show(getSupportFragmentManager(), "goTo");
				return true;

			case R.id.qiraat:
			{
				if(assetPackManager == null)
					assetPackManager = AssetPackManagerFactory.getInstance(this.getApplicationContext());

				final AssetPackLocation assetPackPath = assetPackManager.getPackLocation(assetPackName);
				if (assetPackPath != null) // Warsh is downloaded
				{
					if (pagesFolder.equals("hafs"))
						pagesFolder = assetPackPath.assetsPath();
					else
						pagesFolder = "hafs";

					final SharedPreferences.Editor mEditor = mPrefs.edit();
					mEditor.putString(EXTRA_pagesFolder, pagesFolder).apply();

					refresh();
				}
				else
				{
					assetPackManager.getPackStates(Collections.singletonList(assetPackName))
							.addOnCompleteListener(new OnCompleteListener<AssetPackStates>()
							{
								@Override
								public void onComplete(@NonNull Task<AssetPackStates> task)
								{
									AssetPackStates assetPackStates;
									try
									{
										assetPackStates = task.getResult();
										final AssetPackState assetPackState = assetPackStates.packStates().get(assetPackName);

										if(assetPackState.status() ==  AssetPackStatus.COMPLETED)
										{
											Toast.makeText(MainActivity.this, "Already Downloaded ! reset", Toast.LENGTH_SHORT).show();
											qiraatMenuItem.setVisible(true);
											updateMenu();
										}
										else
										{
											final MaterialAlertDialogBuilder ad = new MaterialAlertDialogBuilder(MainActivity.this);
											ad.setMessage(getString(R.string.download_warsh, (int) (assetPackState.totalBytesToDownload() / (1024f * 1024f))))
													.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
													{
														public void onClick(DialogInterface dialog, int id)
														{
															final List<String> ls = new ArrayList<>();
															ls.add(assetPackName);
															assetPackManager.fetch(ls);
															dialog.dismiss();
															Toast.makeText(MainActivity.this, R.string.download_warsh_inprogress, Toast.LENGTH_LONG).show();

															qiraatMenuItem.setVisible(false);
														}
													})
													.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
													{
														public void onClick(DialogInterface dialog, int id)
														{
															dialog.dismiss();
														}
													});

											final AlertDialog d = ad.create();
											d.show();
										}
									}
									catch (Exception e)
									{
										Log.e(TAG, e.getMessage());
										qiraatMenuItem.setVisible(true);
									}
								}
							});

					assetPackManager.registerListener(new AssetPackStateUpdateListener()
					{
						@Override
						public void onStateUpdate(@NonNull AssetPackState state)
						{
							switch (state.status())
							{
								case AssetPackStatus.PENDING:
									Log.i(TAG, "AssetPackState Pending");
									break;

								case AssetPackStatus.DOWNLOADING:
									long downloaded = state.bytesDownloaded();
									long totalSize = state.totalBytesToDownload();
									double percent = 100.0 * downloaded / totalSize;
									Log.i(TAG, "AssetPackState PercentDone=" + String.format("%.2f", percent));
									Toast.makeText(MainActivity.this, getString(R.string.download_warsh_percentage, String.valueOf((int)percent)), Toast.LENGTH_SHORT).show();
									break;

								case AssetPackStatus.TRANSFERRING:
									// 100% downloaded and assets are being transferred.
									// Notify user to wait until transfer is complete.
									Log.i(TAG, "AssetPackState TRANSFERRING transfer progress percentage " + state.transferProgressPercentage());
									break;

								case AssetPackStatus.COMPLETED:
									// Asset pack is ready to use. Start the game.
									Log.i(TAG, "AssetPackState COMPLETED");

									qiraatMenuItem.setVisible(true);

									final MaterialAlertDialogBuilder ad = new MaterialAlertDialogBuilder(MainActivity.this);
									ad.setMessage(R.string.download_warsh_finished)
											.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int id)
												{
													if(assetPackManager == null)
														assetPackManager = AssetPackManagerFactory.getInstance(MainActivity.this);

													final AssetPackLocation assetPackPath = assetPackManager.getPackLocation(assetPackName);
													if(assetPackPath != null)
													{
														pagesFolder = assetPackPath.assetsPath();
														final SharedPreferences.Editor mEditor = mPrefs.edit();
														mEditor.putString(EXTRA_pagesFolder, pagesFolder).apply();
														refresh();
														dialog.dismiss();
													}
												}
											})
											.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int id)
												{
													updateMenu();
													dialog.dismiss();
												}
											});

									final AlertDialog d = ad.create();
									d.show();

									break;

								case AssetPackStatus.FAILED:
									// Request failed. Notify user.
									Log.e(TAG, state.errorCode() + "");
									Toast.makeText(MainActivity.this, getString(R.string.download_warsh_error, state.errorCode()), Toast.LENGTH_LONG).show();
									qiraatMenuItem.setVisible(true);
									break;

								case AssetPackStatus.CANCELED:
									// Request canceled. Notify user.
									Toast.makeText(MainActivity.this, R.string.download_warsh_cancelled, Toast.LENGTH_LONG).show();
									qiraatMenuItem.setVisible(true);
									break;

								case AssetPackStatus.WAITING_FOR_WIFI:
									/*
									if (!waitForWifiConfirmationShown)
									{
										assetPackManager.showCellularDataConfirmation(MainActivity.this)
												.addOnSuccessListener(new OnSuccessListener()
												{
													@Override
													public void onSuccess(Integer resultCode)
													{
														if (resultCode == RESULT_OK)
														{
															Log.d(TAG, "Confirmation dialog has been accepted.");
														}
														else if (resultCode == RESULT_CANCELED)
														{
															Log.d(TAG, "Confirmation dialog has been denied by the user.");
														}
													}
												});
										waitForWifiConfirmationShown = true;
									}
									*/
									break;

								case AssetPackStatus.NOT_INSTALLED:
									// Asset pack is not downloaded yet.
									Log.e(TAG, "AssetPackStatus NOT_INSTALLED");
									break;
								case AssetPackStatus.UNKNOWN:
									Toast.makeText(MainActivity.this, getString(R.string.download_warsh_error, state.errorCode()), Toast.LENGTH_LONG).show();
									qiraatMenuItem.setVisible(true);
									break;
							}
						}
					});
				}

				return true;
			}

			case R.id.darkmode:

				if(nightMode)
				{
					nightMode = false;
					item.setIcon(R.drawable.baseline_dark_mode_24);
				}
				else
				{
					nightMode = true;
					item.setIcon(R.drawable.baseline_light_mode_24);
				}
				refresh();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	void refresh()
	{
		mViewPager.setAdapter(null);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setCurrentItem(page, false);

		updateMenu();
	}

	void updateMenu()
	{
		if(assetPackManager == null)
			assetPackManager = AssetPackManagerFactory.getInstance(this.getApplicationContext());

		final AssetPackLocation assetPackPath = assetPackManager.getPackLocation(assetPackName);
		if (assetPackPath != null) // Warsh is downloaded
		{
			if(pagesFolder.equals("hafs"))
				qiraatMenuItem.setTitle(R.string.warsh);
			else
				qiraatMenuItem.setTitle(R.string.hafs);
		}
		else
			qiraatMenuItem.setTitle(R.string.qiraat);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		final MenuItem darkmode = menu.findItem(R.id.darkmode);
		qiraatMenuItem = menu.findItem(R.id.qiraat);

		updateMenu();

		if(nightMode)
			darkmode.setIcon(R.drawable.baseline_light_mode_24);
		else
			darkmode.setIcon(R.drawable.baseline_dark_mode_24);

		return super.onCreateOptionsMenu(menu);
	}

	static class PagerAdapter extends FragmentStateAdapter
	{
		PagerAdapter(FragmentManager fm, @NonNull Lifecycle lc)
		{
			super(fm, lc);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position)
		{
			return PageFragment.newInstance(position);
		}

		@Override
		public int getItemCount()
		{
			return 604;
		}
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle savedInstanceState)
	{
		savedInstanceState.putInt(EXTRA_page, page);
		savedInstanceState.putBoolean(EXTRA_nightMode, nightMode);

		super.onSaveInstanceState(savedInstanceState);
	}
}