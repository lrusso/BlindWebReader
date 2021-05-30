package ar.com.lrusso.blindwebreader;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.speech.tts.*;
import android.text.*;
import android.text.style.*;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class GlobalVars extends Application
	{
	//SYSTEM VARIABLES FOR GENERAL PURPOSES
	public static TextToSpeech 				tts;
	public static int 						TTS_MAX_INPUT_LENGTH = 4000;
	public static Context 					context;
	public static final int 				MIN_DISTANCE = 150;
	public static final int 				ACTION_SELECT = 1;
	public static final int 				ACTION_SELECT_PREVIOUS = 2;
	public static final int 				ACTION_EXECUTE = 3;
	public static int 						activityItemLocation = 0;
	public static int 						activityItemLimit = 0;
	public static float 					x1,x2,y1,y2;
	public static Toast 					mToast1 = null;
	public static Toast 					mToast2 = null;
	public static boolean 					firstToast = true;
	public static boolean 					toastMode = false; //FOR TESTING
	public static Class 					lastActivity; //IN ANDROID 2.X, AFTER A CALL ENDS, THE SYSTEM POPUPS THE CALL LOG ACTIVITY. BECAUSE OF THAT, THE APP SETS THE LAST ACTIVITY IN ORDER TO POPUP IT AFTER THE CALL. 
	public static String 					inputModeResult = null;

	//VARIABLES FOR THE WEB READER
	public static boolean 					bookmarkWasDeleted = false;
	public static int 						bookmarkToDeleteIndex = -1;
	public static boolean 					browserRequestInProgress = false;
	public static String 					browserWebTitle = null;
	public static String 					browserWebURL = null;
	public static String 					browserWebText = null;
	public static List<String> 				browserWebLinks = new ArrayList<String>();
	public static List<String> 				browserBookmarks = new ArrayList<String>();

	//GLOBAL FUNCTIONS REQUIRED BY ACTIVITIES
	public static String getLanguage()
		{
		if (Locale.getDefault().getLanguage().startsWith("es"))
			{
			return "es"; //TO WORK IN SPANISH
			}
		else if (Locale.getDefault().getLanguage().startsWith("pt"))
			{
			return "pt"; //TO WORK IN PORTUGUESE
			}
		else if (Locale.getDefault().getLanguage().startsWith("it"))
			{
			return "it"; //TO WORK IN ITALIAN
			}
		else if (Locale.getDefault().getLanguage().startsWith("fr"))
			{
			return "fr"; //TO WORK IN FRENCH
			}
		else if (Locale.getDefault().getLanguage().startsWith("de"))
			{
			return "de"; //TO WORK IN DEUTSCH
			}
		else if (Locale.getDefault().getLanguage().startsWith("ru"))
			{
			return "ru"; //TO WORK IN RUSSIAN
			}
		else if (Locale.getDefault().getLanguage().startsWith("id"))
			{
			return "id"; //TO WORK IN INDONESIAN
			}
		else if (Locale.getDefault().getLanguage().startsWith("in"))
			{
			return "in"; //TO WORK IN INDONESIAN
			}
		else
			{
			return "en"; //TO WORK IN ENGLISH AS DEFAULT
			}
		}
	
    public static void startTTS(TextToSpeech myTTS)
		{
    	try
			{
			Locale loc = new Locale(getLanguage(), "","");
			
    		if(myTTS.isLanguageAvailable(loc)>=TextToSpeech.LANG_AVAILABLE)
				{
    			myTTS.setLanguage(loc);
				}
			}
			catch(Exception e)
			{
			}
		}
	
    public static void talk(String a)
    	{
    	try
    		{
    		if (toastMode==true)
    			{
    			if (firstToast==true)
					{
    				mToast1 = Toast.makeText(context,a,Toast.LENGTH_SHORT);
    				mToast1.show();
    				try
						{
    					mToast2.cancel();
    					mToast2 = null;
						}
						catch(Exception e)
						{
						}
    				firstToast=false;
					}
					else
					{
					mToast2 = Toast.makeText(context,a,Toast.LENGTH_SHORT);
					mToast2.show();
					try
						{
						mToast1.cancel();
						mToast1 = null;
						}
						catch(Exception e)
						{
						}
					firstToast=true;
					}
    			}
				else
				{
				GlobalVars.tts.stop();
				try
					{
					AudioManager mAudioManager = (AudioManager) GlobalVars.context.getSystemService(GlobalVars.context.AUDIO_SERVICE);
					if (mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_NORMAL)
						{
						if (a.length()>TTS_MAX_INPUT_LENGTH)
							{
							a = a.substring(0, TTS_MAX_INPUT_LENGTH - 1);
							}
						HashMap<String, String> params = new HashMap<String, String>();
						params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"stringId");
						tts.speak(a, TextToSpeech.QUEUE_FLUSH, params);
						}
					}
					catch(Exception e)
					{
					}
				}
    		}
    		catch(NullPointerException e)
    		{
    		}
    		catch(Exception e)
    		{
    		}
    	}
    
	public static int detectMovement(MotionEvent event)
		{
		switch(event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
			x1 = event.getX();
			y1 = event.getY();
			break;

			case MotionEvent.ACTION_UP:
			x2 = event.getX();
			y2 = event.getY();
			float deltaX = x2 - x1;
			float deltaY = y2 - y1;

			if (-Math.abs(deltaX) < -GlobalVars.MIN_DISTANCE)
				{
				if (x2 < x1)
					{
					return ACTION_SELECT_PREVIOUS;
					}
				}
			if (Math.abs(deltaX) > GlobalVars.MIN_DISTANCE)
				{
				if (x2 > x1)
					{
					return ACTION_EXECUTE;
					}
				}
			else if (Math.abs(deltaY) > GlobalVars.MIN_DISTANCE)
				{
				if (y2 > y1)
					{
					if (activityItemLocation+1>activityItemLimit)
						{
						activityItemLocation=1;
						}
						else
						{
						activityItemLocation=activityItemLocation+1;
						}
					}
					else 
					{
					if (activityItemLocation-1<1)
						{
						activityItemLocation=activityItemLimit;
						}
						else
						{
						activityItemLocation=activityItemLocation-1;
						}
					}
				return ACTION_SELECT;
				}
			break;
			}
		return -1;
		}
		
	public static int detectKeyUp(int keyCode)
		{
		try
			{
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
				{
				if (activityItemLocation-1<1)
					{
					activityItemLocation=activityItemLimit;
					}
					else
					{
					activityItemLocation=activityItemLocation-1;
					}
				return ACTION_SELECT;
				}
			else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
				{
				if (activityItemLocation+1>activityItemLimit)
					{
					activityItemLocation=1;
					}
					else
					{
					activityItemLocation=activityItemLocation+1;
					}
				return ACTION_SELECT;
				}
			else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
				{
				return ACTION_SELECT_PREVIOUS;
				}
			else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
				{
				return ACTION_EXECUTE;
				}
			}
			catch(NullPointerException e)
			{
			}
			catch(Exception e)
			{
			}
		return -1;
		}
	
	public static boolean detectKeyDown(int keyCode)
		{
		if (keyCode == KeyEvent.KEYCODE_BACK)
			{
			return true;
			}
		return false;
		}

    public static void startActivity(Class<?> a)
		{
    	try
			{
    		Intent intent = new Intent(context, a);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		context.startActivity(intent);
			}
			catch(NullPointerException e)
			{
			}
			catch(Exception e)
			{
			}
		}

    public static void selectTextView(TextView myTextView, boolean selection)
		{
		try
			{
			if (selection==true)
				{
				myTextView.setTextColor(Color.CYAN);
				SpannableString content = new SpannableString(myTextView.getText().toString());
				content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
				myTextView.setText(content);
				}
				else
				{
				myTextView.setTextColor(Color.WHITE);
				SpannableString content1 = new SpannableString(myTextView.getText().toString());
				myTextView.setText(content1);
				}
			}
			catch(Exception e)
			{
			}
		}
    
	public static void setText(TextView myTextView, boolean style, String text)
		{
		try
			{
			myTextView.setText(text);
			if (style==true)
				{
				myTextView.setTextColor(Color.CYAN);
				SpannableString content = new SpannableString(myTextView.getText().toString());
				content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
				myTextView.setText(content);
				}
			}
			catch(Exception e)
			{
			}
		}

	public static String readFile(String file)
		{
        String result = "";
        DataInputStream in = null;
        try
			{
            in = new DataInputStream(context.openFileInput(file));
            for (;;)
				{
                result = result + in.readUTF();
				}
			}
			catch (Exception e)
			{
			}
        try
			{
            in.close();
			}
			catch(Exception e)
			{
			}
        return result;
		}

    public static void writeFile(String file, String text)
		{
        try
			{
            DataOutputStream out = new DataOutputStream(context.openFileOutput(file, Context.MODE_PRIVATE));
            out.writeUTF(text);
            out.close();
			}
			catch(Exception e)
			{
			}
		}

	public static void startInputActivity()
		{
		GlobalVars.startActivity(InputVoice.class);
		}

	public static void readBookmarksDatabase()
		{
		GlobalVars.browserBookmarks.clear();
		String result = readFile("bookmarks.dat");
		BufferedReader rdr = null;
		try
			{
			rdr = new BufferedReader(new StringReader(result));
			for (String line = rdr.readLine(); line != null; line = rdr.readLine())
				{
				if (line.length()>3)
					{
					GlobalVars.browserBookmarks.add(line);
					}
				}
			}
			catch(Exception e)
			{
			}
		try
			{
			rdr.close();
			}
			catch (Exception e)
			{
			}
		Collections.sort(GlobalVars.browserBookmarks, new Comparator<String>(){public int compare(String s1, String s2){return s1.compareToIgnoreCase(s2);}});
		}

	public static void saveBookmarksDatabase()
		{
		String toSave = "";
		for (int i=0;i<GlobalVars.browserBookmarks.size();i++)
			{
			String savedValue = GlobalVars.browserBookmarks.get(i) + "\n";
				if (toSave=="")
				{
				toSave = savedValue;
				}
				else
				{
				toSave = toSave + savedValue;
				}
			}
		GlobalVars.writeFile("bookmarks.dat",toSave);
		}
	
	public static void sortBookmarksDatabase()
		{
		Collections.sort(GlobalVars.browserBookmarks, new Comparator<String>(){public int compare(String s1, String s2){return s1.compareToIgnoreCase(s2);}});
		}

	public static boolean isValidURL(String url)
		{
		URL u = null;

		try
			{
			u = new URL(url);
			}
			catch (MalformedURLException e)
			{
			return false;
			}

		try
			{
			u.toURI();
			}
			catch (URISyntaxException e)
			{
			return false;
			}
		return true;
		}
	}