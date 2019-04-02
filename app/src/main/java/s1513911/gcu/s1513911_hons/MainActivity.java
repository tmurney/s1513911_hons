package s1513911.gcu.s1513911_hons;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import android.view.View;

import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;

import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MessageDialogFragment.Listener {

    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private SpeechService mSpeechService;
    private VoiceRecorder mVoiceRecorder;

    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }
        }

    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };

    //
    private ImageView logo;
    private Button navBackground;
    private Button navBuildings;
    private Button navDepartments;
    private Button navLibrary;
    private Button navStudy;
    boolean running;
    String speechResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Resources resources = getResources();
        final Resources.Theme theme = getTheme();

        logo = (ImageView) findViewById(R.id.logo);
        logo.setImageResource(R.drawable.gcu);

        navBackground = (Button) findViewById(R.id.navBackground);
        navBuildings = (Button) findViewById(R.id.navBuildings);
        navDepartments = (Button) findViewById(R.id.navDepartments);
        navLibrary = (Button) findViewById(R.id.navLibrary);
        navStudy = (Button) findViewById(R.id.navStudy);

        navBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Function for navigating to the background; will also be used for voice commands.
                openBackground();

            }
        });






    }



    @Override
    protected void onStart() {
        super.onStart();

        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);
        // Start listening to voices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecorder();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }



    }

    @Override
    protected void onStop() {
        // Stop listening to voice
        stopVoiceRecorder();

        // Stop Cloud Speech API
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }


    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal) {
                        mVoiceRecorder.dismiss();
                    }

                    runOnUiThread(new Runnable() { //Speech service runs in the background which requires to be brought to the UI thread
                            @Override
                            public void run() {
                                if (isFinal) {

                                    speechResult = text;
                                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                                    if(speechResult.equals("voice assist")){
                                        running = true;
                                        navBackground.setTextColor(getResources().getColor(R.color.status_hearing));
                                        navBuildings.setTextColor(getResources().getColor(R.color.status_hearing));
                                        navDepartments.setTextColor(getResources().getColor(R.color.status_hearing));
                                        navLibrary.setTextColor(getResources().getColor(R.color.status_hearing));
                                        navStudy.setTextColor(getResources().getColor(R.color.status_hearing));

                                        MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.enabled);
                                        mp.start();
                                    }
                                    if(running){
                                        navigateApplication(speechResult);
                                    }

                                    if(speechResult.equals("help")){

                                        MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.help);
                                        mp.start();
                                    }
                                    if(speechResult.equals("stop") && running){
                                        running = false;
                                        navBackground.setTextColor(getResources().getColor(R.color.BigButtonText));
                                        navBuildings.setTextColor(getResources().getColor(R.color.BigButtonText));
                                        navDepartments.setTextColor(getResources().getColor(R.color.BigButtonText));
                                        navLibrary.setTextColor(getResources().getColor(R.color.BigButtonText));
                                        navStudy.setTextColor(getResources().getColor(R.color.BigButtonText));
                                    }


                                }
                            }
                        });
                    }
                };


    public void openBackground(){
        Intent intent = new Intent(this, appUniBg.class);
        startActivity(intent);

        stopVoiceRecorder();




    }

    public void openBuildings(){
        Intent intent = new Intent(this, appUniBuildings.class);
        startActivity(intent);

        stopVoiceRecorder();
    }

    public void openDepartments(){
        Intent intent = new Intent(this, appUniDepartments.class);
        startActivity(intent);

        stopVoiceRecorder();

    }

    public void openLibrary(){
        Intent intent = new Intent(this, appUniLibrary.class);
        startActivity(intent);

        stopVoiceRecorder();
    }

    public void openStudy(){
        Intent intent = new Intent(this, appUniStudy.class);
        startActivity(intent);

        stopVoiceRecorder();
    }


    public void navigateApplication(String string){

        if(string.contains("background")){
            openBackground();
        } else if (string.contains("buildings")){
            openBuildings();
        } else if (string.contains("departments")){
            openDepartments();
        } else if (string.contains("library")){
            openLibrary();
        } else if (string.contains("study areas")){
            openStudy();
        } else {
            Toast.makeText(getApplicationContext(), "Try Again", Toast.LENGTH_SHORT).show();
        }


    }
            }






