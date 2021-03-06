package com.example.k.myapplication;

/*import android.content.Context;
import android.text.InputType;
import android.view.Menu;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
*/

import android.os.Bundle;
import android.app.Activity;    // Needed for extends Activity

import android.util.Log;
import android.view.KeyEvent;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.os.Environment;
import java.io.*;
import java.util.Stack;
import java.sql.Timestamp;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import www.kosoft.util.FastReverseLineInputStream;
import www.geeksforgeeks.org.StackInfix;


public class MainActivity extends Activity {
    /*  Environment.getExternalStorageDirectory() <- /storage/emulated/0
        MainActivity.this.getExternalFilesDir(null) <- /storage/emulated/0
                                                    /Android/data/com.example.k.myapplication/files
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                                                            <- /storage/emulated/0/Documents */
        /* Be sure that manifest contains:
            <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /> */
    static File rootPath = Environment.getExternalStorageDirectory();
    static File fileDir = new File(rootPath + "/Documents/AppData/");

    static int buttonCounter = 0;
    static int requiredLines = 10;
    static CircularFifoQueue<String> cgLog = new CircularFifoQueue<>(requiredLines);

 //   static Context context;
    /*LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View layout = inflater.inflate(R.layout.activity_main,(ViewGroup) findViewById(R.id.log));*/

    /*Context context;
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    TextView tvListing = inflater.inflate(R.layout.activity_main, (TextView) findViewById(R.id.log));*/
//    static LayoutInflater inflater = LayoutInflater.from(context);

    /**
     * A string constant to use in calls to the "log" methods. Its
     * value is often given by the name of the class, as this will
     * allow you to easily determine where log methods are coming
     * from when you analyze your logcat output.
     */
    private static final String TAG = "MainActivity";
    /**
     * Toggle this boolean constant's value to turn on/off logging
     * within the class.
     */
    private static final boolean VERBOSE = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        if (VERBOSE) Log.v(TAG, "+++ ON CREATE +++");

        super.onCreate(savedInstanceState);

        //activity_main: ~\00 projects\AndroidApplication1\res\layout
        setContentView(R.layout.activity_main);

        final EditText pounds = (EditText) this.findViewById(R.id.dob_aob);
        //doesn't work as intended on major phones.
        //pounds.setRawInputType(InputType.TYPE_CLASS_NUMBER);

        final TextView result = (TextView) findViewById(R.id.result);

        final TextView outputLog = (TextView) findViewById(R.id.display_log);
        //outputLog.setMaxLines(requiredLines);
        //outputLog.setMovementMethod(new ScrollingMovementMethod());

        //ViewGroup root = (ViewGroup) findViewById(R.id.log);
        final Button load = (Button) findViewById(R.id.load_log);

        load.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    if (buttonCounter % 2 == 0) {
                        cgLog.clear();
                        getTextFileData();
                        outputLog.setText("");
                        outputLog.append(printQueue(cgLog));
                        load.setText("CLEAR");
                        buttonCounter++;
                    } else { //Every 2 times button is clicked, log is cleared
                        outputLog.setText("");
                        cgLog.clear();
                        load.setText("LOAD");
                        buttonCounter++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        pounds.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    try {
                        result.setText(" Estimated Gallons: " +
                                getCalculatedGallons(pounds.getText().toString()));
                        pounds.getText().clear();

                        if(outputLog != null || !outputLog.getText().equals("")) {
                            outputLog.append("\n " + cgLog.get(cgLog.size()-1));
                        } else {
                            outputLog.append(" " + cgLog.get(cgLog.size() - 1));
                        }
                    } catch (IOException e) {
                        result.setText("No txt @ \\Documents\\AppData\\");
                    } catch (StringIndexOutOfBoundsException ex) {
                        result.setText("Must contain subtraction (-)");
                    }
                    return true;
                }
                return false;
            }
        });

        /*
        pounds.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Context context = null;
                if (v == pounds) {
                    if (hasFocus) {
                        // Open keyboard
                        ((InputMethodManager) context.getSystemService
                                (Context.INPUT_METHOD_SERVICE)).showSoftInput(pounds, InputMethodManager.SHOW_FORCED);
                    } else {
                        // Close keyboard
                        ((InputMethodManager) context.getSystemService
                                (Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(pounds.getWindowToken(), 0);
                    }
                }
            }
        });*/

        //if (VERBOSE) Log.v(TAG, "+++ ON CREATE +++");
    }

    /*public void setEditTextFocus(boolean isFocused, EditText pounds) {
        pounds.setCursorVisible(isFocused);
        pounds.setFocusable(isFocused);
        pounds.setFocusableInTouchMode(isFocused);

        if (isFocused) {
            pounds.requestFocus();
        }
    }*/

    @Override
    public void onPause() {
        super.onPause();
        if (VERBOSE) Log.v(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (VERBOSE) Log.v(TAG, "-- ON STOP --");
    }

    static public int getCalculatedGallons(String userInput) throws IOException {
        boolean isKilogram = false;
        String dob, aob;
        String time = " @ ", kgDelimiter = "#";
        double dobT, aobT;
        int calculatedGallons;
        int operatorPos = userInput.indexOf('-');

        dob = userInput.substring(0, operatorPos);
        aob = userInput.substring(operatorPos + 1);

        if (dob.contains(kgDelimiter) || aob.contains(kgDelimiter)) {
            isKilogram = true;
            dob.replace(kgDelimiter, "");
            aob.replace(kgDelimiter, "");
        }

        aob = aob.replaceAll("-", "+");

        dobT = (new StackInfix(String.valueOf(dob))).getResult();
        aobT = (new StackInfix(String.valueOf(aob))).getResult();

        if (isKilogram) {
            calculatedGallons = (int) (((dobT - aobT)*2.205) / 67 * 1000);
            isKilogram = false;
        } else {
            calculatedGallons = (int) ((dobT - aobT) / 67 * 1000);
        }

        String summary = (int) dobT + " - " + (int) aobT
                + " = " + calculatedGallons;

        String timestamp = (new Timestamp(System.currentTimeMillis())).toString();
        int colonPos = timestamp.indexOf(":");
        time += timestamp.substring((colonPos-2), (colonPos+2)+1);

        /* When queue is full, oldest/first entry is discarded in favor of new entry. */

        cgLog.add(summary + time);
        String dataExport = "\n" + summary + " @ " + timestamp;
        writeToFile(dataExport);
        return calculatedGallons;
    }

    static public void writeToFile(String lineExport) throws IOException {
        boolean success = true;
        if (!fileDir.exists()) {
            success = fileDir.mkdirs(); // due to subfolder, mkdirs not mkdir
        }
        if (success) {
            File file = new File(fileDir + File.separator +  "cgallonsfp100.txt");
            //'true' will append instead of full overwrite, if file exists
            FileWriter fw = new FileWriter(file, true);
            fw.write(lineExport);
            fw.close();
        } else {
            return;
        }
    }

    static public String printQueue(CircularFifoQueue cgLog) {
        //final RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl);
        //final RelativeLayout log = (RelativeLayout) findViewById(R.id.log);

        //LayoutInflater inflater = (LayoutInflater) R.layout.activity_main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //final TextView layout = inflater.inflate(R.layout.activity_main, (TextView) findViewById(R.id.log));

        //TextView layout = rootLayout.inflate(rootLayout.getContext(), R.layout.custom_level_coast, rootLayout.findViewById(R.id.toast_layout_root));

        //tv = new TextView[requiredLines];
        //containerLayout = (RelativeLayout) findViewById(R.id.log);

        //TextView[] tv = new TextView[requiredLines];

        //RelativeLayout containerLayout = (RelativeLayout) findViewById(R.id.log);
                                       // (ViewGroup)getLayoutInflater().inflate(R.layout.image_preview,vg);

        /*for (int i = 0; i < requiredLines; i++) {

            TextView dynaText = new TextView(this);

            dynaText.setText((i+1) + ": " + cgLog.get(i));
            dynaText.setTextSize(30);

            // Set the location of your textView.
            dynaText.setPadding(0, (i * 30), 0, 0);

            containerLayout.addView(dynaText);
        }*/

        /*TextView logDisplay = (TextView) findViewById(R.id.display_log);
        for (int i = 0; i < cgLog.size(); i++) {
            textview.setText(builder.toString());
            //System.out.println((i + 1) + ": " + cgLog.get(i));
        }*/

        String combination = "";
        for (int i = 0; i < cgLog.size(); i++) {
            if (i < cgLog.size()-1) {
                combination += " " + (cgLog.get(i) + "\n");
            } else {
                combination += " " + cgLog.get(i);
            }
        }
        return combination;
    }

    static public void getTextFileData() throws IOException {
        if (fileDir.exists()) {

            File file = new File(fileDir + File.separator +  "cgallonsfp100.txt");

            if(!file.exists())
                return;

            BufferedReader reader = new BufferedReader(new FileReader(file));

            int actualLines = 0;

            String line1 = null;
            while ((line1 = reader.readLine()) != null) {
                if (actualLines > requiredLines) {
                    break;
                }
                actualLines++;
            }
            reader.close();

            if (actualLines < requiredLines) {
                requiredLines = actualLines;
            }
            /*requiredLines = (requiredLines > actualLines) ?
                               actualLines : requiredLines;*/

            //Start reading from last line of file
            BufferedReader reverseInput = new BufferedReader
                    (new InputStreamReader
                            (new FastReverseLineInputStream(file)));

            String currentDate = (new Timestamp(System.currentTimeMillis())).
                    toString().substring(0, 10);

            Stack st = new Stack();

            //Transfer last [required lines] text entries to stack
            /*int remainingLines = requiredLines;
            while(true && remainingLines > 0)*/
            for (int i = 0; i < requiredLines; i++) {
                String line = reverseInput.readLine();  // potential logic error

                if (line == null) {
                    continue;
                }

                int timeDelimPos = line.indexOf('@');

                if ((timeDelimPos != -1)
                        && (line.substring(timeDelimPos).length() >= 21)
                        && (line.substring(0, timeDelimPos).length() >= 10)
                        && (currentDate.equals(line.substring((timeDelimPos + 2)
                        , (timeDelimPos + 2 + 10))))) {
                    String summary = line.substring(0, timeDelimPos - 1).trim();
                    String time = " @ ";

                    int colonPos = line.indexOf(":");

                    if (colonPos != -1) {
                        time += line.substring(timeDelimPos + 13,
                                timeDelimPos + 13 + 5);
                    } else {
                        continue;
                    }
                    st.push(summary + time);
                } else {
                    continue;
                }

            }

            // Transfer from stack to queue
            for (int i = 0; i < requiredLines; i++) {
                if (st.empty()) {
                    break;
                }
                cgLog.add(st.pop().toString());
            }
            // printQueue(cgLog); debugging in windows CLI
            reverseInput.close();
        } else {
            return;
        }
    }
}
