package ar.com.lrusso.blindwebreader;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.*;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class WebReader extends Activity implements TextToSpeech.OnInitListener
	{
	public static int PERMISSION_REQUEST_CODE = 123;

	private TextView browsergoogle;
	private TextView bookmarks;
	private TextView privacypolicy;
	
    @Override protected void onCreate(Bundle savedInstanceState)
    	{
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.browser);
		GlobalVars.lastActivity = WebReader.class;
		browsergoogle = (TextView) findViewById(R.id.browsergoogle);
		bookmarks = (TextView) findViewById(R.id.bookmarks);
		privacypolicy = (TextView) findViewById(R.id.privacypolicy);
		GlobalVars.activityItemLocation=0;
		GlobalVars.activityItemLimit=3;

		GlobalVars.context = this;
		GlobalVars.startTTS(GlobalVars.tts);
		GlobalVars.tts = new TextToSpeech(this,this);
		GlobalVars.tts.setPitch((float) 1.0);

		//READ WEB BOOKMARKS DATABASE
		GlobalVars.readBookmarksDatabase();

		//CHECKS IF MARSHMALLOW TO ASK THE USER FOR PERMISSIONS
		if (Build.VERSION.SDK_INT>=23){try{marshmallowPermissions();}catch(Exception e){}}
		}
    
	@Override public void onResume()
		{
		super.onResume();
		GlobalVars.lastActivity = WebReader.class;
		GlobalVars.activityItemLocation=0;
		GlobalVars.activityItemLimit=3;
		GlobalVars.selectTextView(browsergoogle,false);
		GlobalVars.selectTextView(bookmarks,false);
		GlobalVars.selectTextView(privacypolicy,false);
		if (GlobalVars.inputModeResult!=null)
			{
			if (GlobalVars.browserRequestInProgress==false)
				{
				GlobalVars.browserRequestInProgress=true;

				if (GlobalVars.inputModeResult.toLowerCase().startsWith("www.") && GlobalVars.inputModeResult.length()>=9)
					{
					new WebReaderThreadGoTo().execute("https://" + GlobalVars.inputModeResult);
					}
					else
					{
					new WebReaderThreadGoTo().execute("https://www.google.com/custom?q=" + GlobalVars.inputModeResult);
					}
				}
				else
				{
				GlobalVars.talk(getResources().getString(R.string.layoutBrowserErrorPendingRequest));
				}
			GlobalVars.inputModeResult = null;
			}
			else
			{
			GlobalVars.talk(getResources().getString(R.string.layoutBrowserOnResume));
			}
		}

	public void onInit(int status)
		{
		if (status == TextToSpeech.SUCCESS)
			{
			GlobalVars.talk(getResources().getString(R.string.app_welcome));
			}
			else
			{
			ContextThemeWrapper themedContext = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
			new AlertDialog.Builder(themedContext).setTitle(getResources().getString(R.string.TTSRequiredTitle)).setMessage(getResources().getString(R.string.TTSRequiredMessage)).setPositiveButton(getResources().getString(R.string.TTSRequiredButton),new DialogInterface.OnClickListener()
				{
				public void onClick(DialogInterface dialog, int which)
					{
					try
						{
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.tts")));
						}
						catch (ActivityNotFoundException e)
						{
						try
							{
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts")));
							}
							catch (ActivityNotFoundException e2)
							{
							}
						}
					}
				}).show();
			}
		}

		public void select()
		{
		switch (GlobalVars.activityItemLocation)
			{
			case 1: //SEARCH IN GOOGLE
			GlobalVars.selectTextView(browsergoogle,true);
			GlobalVars.selectTextView(bookmarks,false);
			GlobalVars.selectTextView(privacypolicy,false);
			if (GlobalVars.browserRequestInProgress==true)
				{
				GlobalVars.talk(getResources().getString(R.string.layoutBrowserSearchInGoogle) + 
								getResources().getString(R.string.layoutBrowserAWebPageItsBeenLoading));
				}
				else
				{
				GlobalVars.talk(getResources().getString(R.string.layoutBrowserSearchInGoogle));
				}
			break;

			case 2: //LIST BOOKMARKS
			GlobalVars.selectTextView(bookmarks, true);
			GlobalVars.selectTextView(browsergoogle,false);
			GlobalVars.selectTextView(privacypolicy,false);
			GlobalVars.talk(getResources().getString(R.string.layoutBrowserListBookmarks));
			break;

			case 3: //GO BACK TO THE MAIN MENU
			GlobalVars.selectTextView(privacypolicy,true);
			GlobalVars.selectTextView(bookmarks,false);
			GlobalVars.selectTextView(browsergoogle,false);
			GlobalVars.talk(getResources().getString(R.string.layoutBrowserPrivacyPolicy));
			break;
			}
		}

	public void execute()
		{
		switch (GlobalVars.activityItemLocation)
			{
			case 1: //SEARCH IN GOOGLE
			if (GlobalVars.browserRequestInProgress==false)
				{
				GlobalVars.startInputActivity();
				}
				else
				{
				GlobalVars.talk(getResources().getString(R.string.layoutBrowserErrorPendingRequest));
				}
			break;

			case 2: //LIST BOOKMARKS
			GlobalVars.startActivity(WebReaderBookmarksList.class);
			break;

			case 3: //PRIVACY POLICY
			GlobalVars.talk(getResources().getString(R.string.layoutBrowserPrivacyPolicyText));
			break;
			}
		}
	
	@Override public boolean onTouchEvent(MotionEvent event)
		{
		int result = GlobalVars.detectMovement(event);
		switch (result)
			{
			case GlobalVars.ACTION_SELECT:
			select();
			break;

			case GlobalVars.ACTION_EXECUTE:
			execute();
			break;
			}
		return super.onTouchEvent(event);
		}

	public boolean onKeyUp(int keyCode, KeyEvent event)
		{
		int result = GlobalVars.detectKeyUp(keyCode);
		switch (result)
			{
			case GlobalVars.ACTION_SELECT:
			select();
			break;

			case GlobalVars.ACTION_EXECUTE:
			execute();
			break;
			}
		return super.onKeyUp(keyCode, event);
		}

	@TargetApi(Build.VERSION_CODES.M)
	public void marshmallowPermissions()
		{
		List<String> listPermissionsNeeded = new ArrayList<String>();

		if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
			{
			listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
			}

		if (!listPermissionsNeeded.isEmpty())
			{
			requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),PERMISSION_REQUEST_CODE);
			}
		}

	@TargetApi(Build.VERSION_CODES.M)
	@Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
		{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PERMISSION_REQUEST_CODE)
			{
			try
				{
				if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)
					{
					GlobalVars.talk(getResources().getString(R.string.app_welcome));
					}
				}
				catch(Exception e)
				{
				}
			}
		}
	}